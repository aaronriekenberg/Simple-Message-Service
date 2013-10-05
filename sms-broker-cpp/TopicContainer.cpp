#include "Log.h"
#include "TopicContainer.h"

namespace smsbroker {

std::unordered_map<std::string, Topic*> TopicContainer::m_sharedTopicNameToTopic;

std::mutex TopicContainer::m_mutex;

__thread std::unordered_map<std::string, Topic*>* TopicContainer::m_pThreadLocalTopicNameToTopic =
		nullptr;

Topic& TopicContainer::getTopic(const std::string& topicName) {
	if (!m_pThreadLocalTopicNameToTopic) {
		m_pThreadLocalTopicNameToTopic = new std::unordered_map<std::string,
				Topic*>;
	}
	auto& m_threadLocalMap = *m_pThreadLocalTopicNameToTopic;

	Topic* pTopic = nullptr;
	auto i = m_threadLocalMap.find(topicName);
	if (i != m_threadLocalMap.end()) {
		pTopic = i->second;
	} else {
		pTopic = &getTopicFromSharedStorage(topicName);
		m_threadLocalMap[topicName] = pTopic;
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
