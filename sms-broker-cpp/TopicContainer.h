#ifndef TOPIC_CONTAINER_H
#define TOPIC_CONTAINER_H

#include <mutex>
#include <string>
#include <unordered_map>
#include "Topic.h"

namespace smsbroker {

class TopicContainer {
public:
	TopicContainer() = delete;

	~TopicContainer() = delete;

	static Topic& getTopic(const std::string& topicName);

private:
	static Topic& getTopicFromSharedStorage(const std::string& topicName);

	typedef std::unordered_map<std::string, Topic*> TopicNameToPointerMap;

	static TopicNameToPointerMap m_sharedTopicNameToTopic;

	static std::mutex m_mutex;

	static thread_local TopicNameToPointerMap m_threadLocalTopicNameToTopic;

};

}

#endif
