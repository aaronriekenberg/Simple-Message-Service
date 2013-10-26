#include "Log.h"
#include "SharedTopicContainer.h"

namespace smsbroker {

Topic& SharedTopicContainer::getTopic(const std::string& topicName) {
	std::lock_guard<std::mutex> lock(m_mutex);
	Topic* pTopic = nullptr;
	auto i = m_topicNameToTopic.find(topicName);
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
