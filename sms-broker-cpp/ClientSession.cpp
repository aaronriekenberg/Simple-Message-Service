#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/random_generator.hpp>
#include <sstream>
#include "ClientSession.h"
#include "Log.h"
#include "SMSProtocol.pb.h"

namespace {

std::string getUUID() {
	boost::uuids::random_generator generator;
	return boost::uuids::to_string(generator());
}

}

namespace smsbroker {

ClientSession::SharedPtr ClientSession::create(TopicContainer& topicContainer,
		boost::asio::io_service& ioService) {
	return SharedPtr(new ClientSession(topicContainer, ioService));
}

ClientSession::ClientSession(TopicContainer& topicContainer,
		boost::asio::io_service& ioService) :
		m_id(getUUID()), m_topicContainer(topicContainer), m_clientSocket(
				ioService), m_strand(ioService) {

}

ClientSession::~ClientSession() noexcept {
	terminate();
}

const std::string& ClientSession::getTopicListenerID() const {
	return m_id;
}

boost::asio::ip::tcp::socket& ClientSession::getClientSocket() {
	return m_clientSocket;
}

void ClientSession::handleClientSocketAccepted() {
	auto sharedThis = shared_from_this();
	m_strand.dispatch([=] {
		sharedThis->handleClientSocketAcceptedInStrand();
	});
}

void ClientSession::writeSerializedBrokerToClientMessage(
		BufferSharedPtr pSerializedBuffer, size_t bufferSize) {
	auto sharedThis = shared_from_this();
	m_strand.dispatch(
			[=] {
				sharedThis->writeSerializedBrokerToClientMessageInStrand(pSerializedBuffer,bufferSize);
			});
}

void ClientSession::terminate() {
	if (!m_clientSocketClosed) {
		m_clientSocket.close();
		m_clientSocketClosed = true;
		if (!m_connectionString.empty()) {
			Log::getInfoInstance() << "disconnect client to broker "
					<< m_connectionString << " id " << m_id;
		}
	}
}

void ClientSession::handleClientSocketAcceptedInStrand() {
	std::stringstream connectionStringSS;
	connectionStringSS << m_clientSocket.remote_endpoint() << " -> "
			<< m_clientSocket.local_endpoint();
	m_connectionString = connectionStringSS.str();

	Log::getInfoInstance() << "connected client to broker "
			<< m_connectionString << " id " << m_id;

	readHeader();
}

void ClientSession::writeSerializedBrokerToClientMessageInStrand(
		BufferSharedPtr pSerializedBuffer, size_t bufferSize) {
	if (m_clientSocketClosed) {
		return;
	}

	const bool writeInProgress = (!m_writeQueue.empty());
	m_writeQueue.push_back(std::make_tuple(pSerializedBuffer, bufferSize));
	if (!writeInProgress) {
		writeNextBufferInQueueIfNecessary();
	}
}

void ClientSession::writeNextBufferInQueueIfNecessary() {
	if (!m_writeQueue.empty()) {
		const auto& bufferAndSize = m_writeQueue.front();
		const size_t bufferSize = std::get<1>(bufferAndSize);
		m_writeHeader[0] = (bufferSize >> 24);
		m_writeHeader[1] = (bufferSize >> 16);
		m_writeHeader[2] = (bufferSize >> 8);
		m_writeHeader[3] = (bufferSize);
		std::array<boost::asio::const_buffer, 2> writeBufferArray = { {
				boost::asio::buffer(m_writeHeader), boost::asio::buffer(
						*std::get<0>(bufferAndSize), bufferSize) } };
		auto sharedThis = shared_from_this();
		boost::asio::async_write(m_clientSocket, writeBufferArray,
				m_strand.wrap([=] (const boost::system::error_code& error,
						size_t bytesWritten)
				{
					sharedThis->writeComplete(error);
				}));
	}
}

void ClientSession::writeComplete(const boost::system::error_code& error) {
	if (m_clientSocketClosed) {
		terminate();
	} else if (error) {
		terminate();
	}

	m_writeQueue.pop_front();
	writeNextBufferInQueueIfNecessary();
}

void ClientSession::readHeader() {
	if (m_readBuffer.size() < 4) {
		m_readBuffer.resize(4, 0);
	}
	auto sharedThis = shared_from_this();
	boost::asio::async_read(m_clientSocket,
			boost::asio::buffer(m_readBuffer, 4),
			m_strand.wrap([=] (const boost::system::error_code& error,
					size_t bytesTransferred)
			{
				sharedThis->readHeaderComplete(error, bytesTransferred);
			}));
}

void ClientSession::readHeaderComplete(const boost::system::error_code& error,
		size_t bytesTransferred) {
	if (m_clientSocketClosed) {
		terminate();
	} else if (error) {
		terminate();
	} else {
		size_t payloadSize = 0;
		payloadSize |= (m_readBuffer[0] << 24);
		payloadSize |= (m_readBuffer[1] << 16);
		payloadSize |= (m_readBuffer[2] << 8);
		payloadSize |= (m_readBuffer[3]);
		if (payloadSize == 0) {
			terminate();
		} else {
			readPayload(payloadSize);
		}
	}
}

void ClientSession::readPayload(size_t payloadSize) {
	if (m_readBuffer.size() < payloadSize) {
		m_readBuffer.resize(payloadSize, 0);
	}
	auto sharedThis = shared_from_this();
	boost::asio::async_read(m_clientSocket,
			boost::asio::buffer(m_readBuffer, payloadSize),
			m_strand.wrap([=] (const boost::system::error_code& error,
					size_t bytesTransferred)
			{
				sharedThis->readPayloadComplete(error, bytesTransferred);
			}));
}

void ClientSession::readPayloadComplete(const boost::system::error_code& error,
		size_t bytesTransferred) {
	if (m_clientSocketClosed) {
		terminate();
	} else if (error) {
		terminate();
	} else {
		if (!m_clientToBrokerMessage.ParseFromArray(&(m_readBuffer[0]),
				m_readBuffer.size())) {
			terminate();
		} else {
			switch (m_clientToBrokerMessage.messagetype()) {
			case sms::protocol::protobuf::ClientToBrokerMessage_ClientToBrokerMessageType_CLIENT_SUBSCRIBE_TO_TOPIC: {
				Topic& topic = m_topicContainer.getTopic(
						m_clientToBrokerMessage.topicname());
				topic.subscribe(shared_from_this());
				break;
			}
			case sms::protocol::protobuf::ClientToBrokerMessage_ClientToBrokerMessageType_CLIENT_UNSUBSCRIBE_FROM_TOPIC: {
				Topic& topic = m_topicContainer.getTopic(
						m_clientToBrokerMessage.topicname());
				topic.unsubscribe(shared_from_this());
				break;
			}
			case sms::protocol::protobuf::ClientToBrokerMessage_ClientToBrokerMessageType_CLIENT_SEND_MESSAGE_TO_TOPIC: {
				Topic& topic = m_topicContainer.getTopic(
						m_clientToBrokerMessage.topicname());

				m_brokerToClientMessage.set_messagetype(
						sms::protocol::protobuf::BrokerToClientMessage_BrokerToClientMessageType_BROKER_TOPIC_MESSAGE_PUBLISH);
				m_brokerToClientMessage.set_messagepayload(
						m_clientToBrokerMessage.messagepayload());
				m_brokerToClientMessage.set_topicname(
						m_clientToBrokerMessage.topicname());

				const size_t brokerToClientMessageSize =
						m_brokerToClientMessage.ByteSize();

				BufferSharedPtr pBuffer(
						new std::vector<unsigned char>(
								brokerToClientMessageSize, 0));

				m_brokerToClientMessage.SerializeToArray(&((*pBuffer)[0]),
						pBuffer->size());

				topic.publishSerializedBrokerToClientMessage(pBuffer,
						brokerToClientMessageSize);
				break;
			}
			}

			readHeader();
		}
	}
}

}
