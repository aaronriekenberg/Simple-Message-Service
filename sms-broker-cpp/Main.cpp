#include "Broker.h"
#include "Log.h"
#include "ThreadName.h"
#include <thread>

int main(int argc, char** argv) {
	try {
		smsbroker::ThreadName::set("main");

		auto pBroker = smsbroker::Broker::create(
				std::make_tuple("0.0.0.0", "10001"),
				std::thread::hardware_concurrency() * 2);
		pBroker->run();
	} catch (const std::exception& e) {
		smsbroker::Log::getInfoInstance() << "main caught exception: "
				<< e.what();
		return 1;
	} catch (...) {
		smsbroker::Log::getInfoInstance() << "main caught unknown exception";
		return 1;
	}
	return 0;
}
