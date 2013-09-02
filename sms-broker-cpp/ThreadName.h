#ifndef THREAD_NAME_H
#define THREAD_NAME_H

#include <string>

namespace smsbroker {

class ThreadName {
public:

	static void set(const std::string& name);

	static const std::string& get();

private:

	static __thread std::string* m_pThreadName;

	ThreadName() = delete;

	~ThreadName() = delete;

};

}

#endif
