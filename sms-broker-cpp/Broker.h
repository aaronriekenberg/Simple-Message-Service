#ifndef BROKER_H
#define BROKER_H

#include <boost/asio.hpp>
#include <memory>
#include <string>
#include "TopicContainer.h"

namespace smsbroker {

class Broker {
public:
	typedef std::shared_ptr<Broker> SharedPtr;

	static SharedPtr create(
			const std::tuple<std::string, std::string>& listenAddressAndPort,
			int numThreads);

	~Broker() = default;

	void run();

private:
	Broker(const std::tuple<std::string, std::string>& listenAddressAndPort,
			int numThreads);

	Broker(const Broker& rhs) = delete;

	Broker& operator=(const Broker& rhs) = delete;

	void createAcceptors();

	const std::tuple<std::string, std::string> m_listenAddressAndPort;

	const int m_numThreads;

	TopicContainer m_topicContainer;

	boost::asio::io_service m_ioService;

};

}

#endif
