#ifndef SHARED_TOPIC_CONTAINER_H
#define SHARED_TOPIC_CONTAINER_H

#include <mutex>
#include <string>
#include <unordered_map>
#include "Topic.h"

namespace smsbroker {

class SharedTopicContainer {
public:
	SharedTopicContainer() = default;

	~SharedTopicContainer() = default;

	Topic& getTopic(const std::string& topicName);

private:
	SharedTopicContainer(const SharedTopicContainer& rhs) = delete;

	SharedTopicContainer& operator=(const SharedTopicContainer& rhs) = delete;

	typedef std::unordered_map<std::string, Topic*> TopicNameToPointerMap;

	TopicNameToPointerMap m_topicNameToTopic;

	std::mutex m_mutex;

};

}

#endif
