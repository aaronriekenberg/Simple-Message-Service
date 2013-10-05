#include "ThreadName.h"

namespace {

const std::string UNKNOWN_THREAD_NAME = "unknown";

}

namespace smsbroker {

__thread std::string* ThreadName::m_pThreadName = nullptr;

void ThreadName::set(const std::string& name) {
	delete m_pThreadName;
	m_pThreadName = new std::string(name);
}

const std::string& ThreadName::get() {
	if (m_pThreadName) {
		return (*m_pThreadName);
	}
	return UNKNOWN_THREAD_NAME;
}

}
