#include "Broker.h"
#include "ClientAcceptor.h"
#include "Log.h"
#include "TcpResolver.h"
#include "ThreadName.h"
#include <thread>

namespace smsbroker {

Broker::SharedPtr Broker::create(
		const std::tuple<std::string, std::string>& listenAddressAndPort,
		int numThreads) {
	return SharedPtr(new Broker(listenAddressAndPort, numThreads));
}

void Broker::run() {
	createAcceptors();

	std::vector<std::thread> threadVector;
	for (int i = 0; i < m_numThreads; ++i) {
		threadVector.emplace_back(
				[=] () {
					ThreadName::set(std::string("io-") + std::to_string(i));
					Log::getInfoInstance() << "started io thread " << std::to_string(i);

					try {
						m_ioService.run();
					} catch (const std::exception& e) {
						Log::getInfoInstance() << "io thread caught exception: " << e.what();
					} catch (...) {
						Log::getInfoInstance() << "io thread caught unknown exception";
					}
				});
	}

	for (auto& thread : threadVector) {
		thread.join();
	}
}

Broker::Broker(const std::tuple<std::string, std::string>& listenAddressAndPort,
		int numThreads) :
		m_listenAddressAndPort(listenAddressAndPort), m_numThreads(numThreads), m_ioService(
				numThreads) {

}

void Broker::createAcceptors() {
	TcpResolver resolver(m_ioService);
	boost::asio::ip::tcp::endpoint listenEndpoint = resolver.resolve(
			m_listenAddressAndPort);
	ClientAcceptor::create(m_ioService, listenEndpoint)->start();
}

}
