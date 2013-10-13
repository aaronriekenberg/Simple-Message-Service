#ifndef TCP_RESOLVER_H
#define TCP_RESOLVER_H

#include <boost/asio.hpp>
#include <string>
#include <tuple>

namespace smsbroker {

class TcpResolver {
public:

	TcpResolver(boost::asio::io_service& ioService);

	virtual ~TcpResolver() = default;

	boost::asio::ip::tcp::endpoint
	resolve(const std::tuple<std::string, std::string>& addressPortPair);

private:

	TcpResolver(const TcpResolver& rhs) = delete;

	TcpResolver& operator=(const TcpResolver& rhs) = delete;

	boost::asio::ip::tcp::resolver m_resolver;

};

}

#endif
