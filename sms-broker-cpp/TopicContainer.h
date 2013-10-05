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

	static std::unordered_map<std::string, Topic*> m_sharedTopicNameToTopic;

	static std::mutex m_mutex;

	static __thread std::unordered_map<std::string, Topic*>* m_pThreadLocalTopicNameToTopic;

};

}

#endif
