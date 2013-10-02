#ifndef UUID_GENERATOR_H
#define UUID_GENERATOR_H

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/random_generator.hpp>
#include <mutex>
#include <string>

namespace smsbroker {

class UUIDGenerator {
public:

	static std::string getUUID();

private:
	UUIDGenerator() = delete;

	~UUIDGenerator() = delete;

	static boost::uuids::random_generator uuidGenerator;

	static std::mutex uuidGeneratorMutex;

};

}

#endif
