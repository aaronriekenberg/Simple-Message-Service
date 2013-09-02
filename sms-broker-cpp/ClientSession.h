#ifndef CLIENT_SESSION_H
#define CLIENT_SESSION_H

#include <boost/asio.hpp>
#include <array>
#include <deque>
#include <memory>
#include <string>
#include <vector>
#include "BufferPool.h"
#include "SMSProtocol.pb.h"
#include "TopicContainer.h"
#include "TopicListener.h"

namespace smsbroker {

class ClientSession: public std::enable_shared_from_this<ClientSession>,
		public TopicListener {
public:
	typedef std::shared_ptr<ClientSession> SharedPtr;

	static SharedPtr create(TopicContainer& topicContainer,
			BufferPool& bufferPool, boost::asio::io_service& ioService);

	virtual ~ClientSession() noexcept;

	virtual const std::string& getTopicListenerID() const override;

	boost::asio::ip::tcp::socket& getClientSocket();

	void handleClientSocketAccepted();

	virtual void writeSerializedBrokerToClientMessage(
			BufferSharedPtr pSerializedBuffer, size_t bufferSize) override;

private:
	ClientSession(TopicContainer& topicContainer, BufferPool& bufferPool,
			boost::asio::io_service& ioService);

	ClientSession(const ClientSession& rhs) = delete;

	ClientSession& operator=(const ClientSession& rhs) = delete;

	void terminate();

	void writeNextBufferInQueueIfNecessary();

	void writeComplete(const boost::system::error_code& error);

	void readHeader();

	void readHeaderComplete(const boost::system::error_code& error,
			size_t bytesTransferred);

	void readPayload(size_t payloadSize);

	void readPayloadComplete(const boost::system::error_code& error,
			size_t bytesTransferred);

	const std::string m_id;

	TopicContainer& m_topicContainer;

	BufferPool& m_bufferPool;

	boost::asio::ip::tcp::socket m_clientSocket;

	std::string m_connectionString;

	bool m_clientSocketClosed = false;

	std::vector<unsigned char> m_readBuffer;

	std::deque<std::tuple<BufferSharedPtr, size_t>> m_writeQueue;

	std::array<unsigned char, 4> m_writeHeader;

	sms::protocol::protobuf::ClientToBrokerMessage m_clientToBrokerMessage;

	sms::protocol::protobuf::BrokerToClientMessage m_brokerToClientMessage;

};

}

#endif
