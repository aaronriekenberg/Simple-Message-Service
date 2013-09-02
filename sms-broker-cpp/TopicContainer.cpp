#include "Log.h"
#include "TopicContainer.h"

namespace smsbroker {

Topic& TopicContainer::getTopic(const std::string& topicName) {
	std::unordered_map<std::string, Topic>::iterator i =
			m_topicNameToTopic.find(topicName);
	if (i != m_topicNameToTopic.end()) {
		return i->second;
	} else {
		Log::getInfoInstance() << "creating topic " << topicName;
		return m_topicNameToTopic[topicName];
	}
}

}
