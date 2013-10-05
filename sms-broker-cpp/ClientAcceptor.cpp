#include "BoostSystemUtil.h"
#include "ClientAcceptor.h"
#include "Log.h"

namespace smsbroker {

ClientAcceptor::SharedPtr ClientAcceptor::create(
		boost::asio::io_service& ioService,
		const boost::asio::ip::tcp::endpoint& localEndpoint) {
	return SharedPtr(new ClientAcceptor(ioService, localEndpoint));
}

void ClientAcceptor::start() {
	Log::getInfoInstance() << "listening on " << m_acceptor.local_endpoint();
	registerForAccept();
}

ClientAcceptor::ClientAcceptor(boost::asio::io_service& ioService,
		const boost::asio::ip::tcp::endpoint& localEndpoint) :
		m_acceptor(ioService, localEndpoint) {

}

void ClientAcceptor::registerForAccept() {
	auto pSession = ClientSession::create(m_acceptor.get_io_service());
	auto sharedThis = shared_from_this();
	m_acceptor.async_accept(pSession->getClientSocket(),
			[=] (const boost::system::error_code& error)
			{
				sharedThis->handleAccept(pSession, error);
			});
}

void ClientAcceptor::handleAccept(ClientSession::SharedPtr pSession,
		const boost::system::error_code& error) {
	if (error) {
		if (Log::isDebugEnabled()) {
			Log::getDebugInstance() << "ClientAcceptor::handleAccept error = "
					<< BoostSystemUtil::buildErrorCodeString(error);
		}
	} else {
		pSession->handleClientSocketAccepted();
	}

	registerForAccept();
}

}
