#include "Broker.h"
#include "ClientAcceptor.h"
#include "TcpResolver.h"

namespace smsbroker {

Broker::SharedPtr Broker::create(
		const std::tuple<std::string, std::string>& listenAddressAndPort) {
	return SharedPtr(new Broker(listenAddressAndPort));
}

void Broker::run() {
	createAcceptors();
	m_ioService.run();
}

Broker::Broker(const std::tuple<std::string, std::string>& listenAddressAndPort) :
		m_listenAddressAndPort(listenAddressAndPort), m_ioService(1) {

}

void Broker::createAcceptors() {
	TcpResolver resolver(m_ioService);
	boost::asio::ip::tcp::endpoint listenEndpoint = resolver.resolve(
			m_listenAddressAndPort);
	ClientAcceptor::create(m_topicContainer, m_bufferPool, m_ioService,
			listenEndpoint)->start();
}

}
