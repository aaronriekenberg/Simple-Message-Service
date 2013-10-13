#ifndef BROKER_H
#define BROKER_H

#include <boost/asio.hpp>
#include <memory>
#include <string>
#include "TopicContainer.h"

namespace smsbroker {

class Broker {
public:
	Broker() = delete;

	~Broker() = delete;

	static void run(
			const std::tuple<std::string, std::string>& listenAddressAndPort,
			int numThreads);

private:
	static void createAcceptor(
			const std::tuple<std::string, std::string>& listenAddressAndPort,
			boost::asio::io_service& ioService);

};

}

#endif
