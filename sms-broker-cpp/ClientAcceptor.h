#ifndef CLIENT_ACCEPTOR_H
#define CLIENT_ACCEPTOR_H

#include <boost/asio.hpp>
#include <memory>
#include "ClientSession.h"

namespace smsbroker {

class ClientAcceptor: public std::enable_shared_from_this<ClientAcceptor> {
public:
	typedef std::shared_ptr<ClientAcceptor> SharedPtr;

	static SharedPtr create(boost::asio::io_service& ioService,
			const boost::asio::ip::tcp::endpoint& localEndpoint);

	~ClientAcceptor() = default;

	void start();

private:
	ClientAcceptor(const ClientAcceptor& rhs) = delete;

	ClientAcceptor& operator=(const ClientAcceptor& rhs) = delete;

	ClientAcceptor(boost::asio::io_service& ioService,
			const boost::asio::ip::tcp::endpoint& localEndpoint);

	void registerForAccept();

	void handleAccept(ClientSession::SharedPtr pSession,
			const boost::system::error_code& error);

	boost::asio::ip::tcp::acceptor m_acceptor;

};

}

#endif
