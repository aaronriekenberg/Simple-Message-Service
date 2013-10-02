#include "UUIDGenerator.h"

namespace smsbroker {

boost::uuids::random_generator UUIDGenerator::uuidGenerator;

std::mutex UUIDGenerator::uuidGeneratorMutex;

std::string UUIDGenerator::getUUID() {
	std::lock_guard<std::mutex> lock(uuidGeneratorMutex);
	return boost::uuids::to_string(uuidGenerator());
}

}
