#include "Broker.h"
#include "ClientAcceptor.h"
#include "Log.h"
#include "TcpResolver.h"
#include "ThreadLocalTopicContainer.h"
#include "ThreadName.h"
#include <thread>

namespace smsbroker {

Broker::SharedPtr Broker::create(
		const std::tuple<std::string, std::string>& listenAddressAndPort,
		int numThreads) {
	return SharedPtr(new Broker(listenAddressAndPort, numThreads));
}

Broker::Broker(const std::tuple<std::string, std::string>& listenAddressAndPort,
		int numThreads) :
		m_ioService(numThreads), m_listenAddressAndPort(listenAddressAndPort), m_numThreads(
				numThreads) {

}

void Broker::run() {
	createAcceptor();

	std::vector<std::thread> threadVector;
	threadVector.reserve(m_numThreads);
	for (int i = 0; i < m_numThreads; ++i) {
		threadVector.emplace_back(
				[=] () {
					try {
						ThreadName::set(std::string("io-") + std::to_string(i));
						Log::getInfoInstance() << "started io thread " << i;

						ThreadLocalTopicContainer::createThreadLocalInstance(m_sharedTopicContainer);
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

void Broker::createAcceptor() {
	TcpResolver resolver(m_ioService);
	auto listenEndpoint = resolver.resolve(m_listenAddressAndPort);
	ClientAcceptor::create(m_ioService, listenEndpoint)->start();
}

}
