#ifndef THREAD_NAME_H
#define THREAD_NAME_H

#include <string>

namespace smsbroker {

class ThreadName {
public:

	static void set(const std::string& name);

	static const std::string& get();

private:

	static thread_local std::string m_threadName;

	ThreadName() = delete;

	~ThreadName() = delete;

};

}

#endif
