#include "Log.h"
#include "TopicContainer.h"

namespace smsbroker {

Topic& TopicContainer::getTopic(const std::string& topicName) {
	std::lock_guard<std::mutex> lock(m_mutex);
	Topic* pTopic = nullptr;
	std::unordered_map<std::string, Topic*>::iterator i =
			m_topicNameToTopic.find(topicName);
	if (i != m_topicNameToTopic.end()) {
		pTopic = i->second;
	} else {
		Log::getInfoInstance() << "creating topic " << topicName;
		pTopic = new Topic;
		m_topicNameToTopic[topicName] = pTopic;
	}
	return (*pTopic);
}

}
