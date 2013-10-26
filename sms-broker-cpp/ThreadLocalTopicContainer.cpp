#include "ThreadLocalTopicContainer.h"
#include "SharedTopicContainer.h"

namespace smsbroker {

thread_local std::unique_ptr<ThreadLocalTopicContainer> ThreadLocalTopicContainer::m_pThreadLocalInstance =
		nullptr;

void ThreadLocalTopicContainer::createInstance(
		SharedTopicContainer& sharedTopicContainer) {
	m_pThreadLocalInstance.reset(
			new ThreadLocalTopicContainer(sharedTopicContainer));
}

ThreadLocalTopicContainer& ThreadLocalTopicContainer::getInstance() {
	return *m_pThreadLocalInstance;
}

ThreadLocalTopicContainer::ThreadLocalTopicContainer(
		SharedTopicContainer& sharedTopicContainer) :
		m_sharedTopicContainer(sharedTopicContainer) {

}

Topic& ThreadLocalTopicContainer::getTopic(const std::string& topicName) {
	Topic* pTopic = nullptr;
	auto i = m_topicNameToTopic.find(topicName);
	if (i != m_topicNameToTopic.end()) {
		pTopic = i->second;
	} else {
		pTopic = &(m_sharedTopicContainer.getTopic(topicName));
		m_topicNameToTopic[topicName] = pTopic;
	}
	return (*pTopic);
}

}
