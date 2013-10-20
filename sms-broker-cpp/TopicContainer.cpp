#include "Log.h"
#include "TopicContainer.h"

namespace smsbroker {

TopicContainer::TopicNameToPointerMap TopicContainer::m_sharedTopicNameToTopic;

std::mutex TopicContainer::m_mutex;

thread_local TopicContainer::TopicNameToPointerMap TopicContainer::m_threadLocalTopicNameToTopic;

Topic& TopicContainer::getTopic(const std::string& topicName) {
	Topic* pTopic = nullptr;
	auto i = m_threadLocalTopicNameToTopic.find(topicName);
	if (i != m_threadLocalTopicNameToTopic.end()) {
		pTopic = i->second;
	} else {
		pTopic = &getTopicFromSharedStorage(topicName);
		m_threadLocalTopicNameToTopic[topicName] = pTopic;
	}
	return (*pTopic);
}

Topic& TopicContainer::getTopicFromSharedStorage(const std::string& topicName) {
	std::lock_guard<std::mutex> lock(m_mutex);
	Topic* pTopic = nullptr;
	auto i = m_sharedTopicNameToTopic.find(topicName);
	if (i != m_sharedTopicNameToTopic.end()) {
		pTopic = i->second;
	} else {
		Log::getInfoInstance() << "creating topic " << topicName;
		pTopic = new Topic;
		m_sharedTopicNameToTopic[topicName] = pTopic;
	}
	return (*pTopic);
}

}
