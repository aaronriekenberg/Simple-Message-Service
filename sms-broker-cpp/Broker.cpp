#include "Broker.h"
#include "ClientAcceptor.h"
#include "Log.h"
#include "TcpResolver.h"
#include "ThreadName.h"
#include <thread>

namespace smsbroker {

void Broker::run(
		const std::tuple<std::string, std::string>& listenAddressAndPort,
		int numThreads) {
	auto pIOService = std::make_shared<boost::asio::io_service>(numThreads);

	createAcceptor(listenAddressAndPort, *pIOService);

	std::vector<std::thread> threadVector;
	threadVector.reserve(numThreads);
	for (int i = 0; i < numThreads; ++i) {
		threadVector.emplace_back(
				[=] () {
					ThreadName::set(std::string("io-") + std::to_string(i));
					Log::getInfoInstance() << "started io thread " << i;

					try {
						pIOService->run();
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

void Broker::createAcceptor(
		const std::tuple<std::string, std::string>& listenAddressAndPort,
		boost::asio::io_service& ioService) {
	TcpResolver resolver(ioService);
	boost::asio::ip::tcp::endpoint listenEndpoint = resolver.resolve(
			listenAddressAndPort);
	ClientAcceptor::create(ioService, listenEndpoint)->start();
}

}
