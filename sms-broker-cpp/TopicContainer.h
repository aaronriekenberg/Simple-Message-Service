#ifndef TOPIC_CONTAINER_H
#define TOPIC_CONTAINER_H

#include <string>
#include <unordered_map>
#include "Topic.h"

namespace smsbroker
{

class TopicContainer
{
public:
	TopicContainer() = default;

	~TopicContainer() = default;

	Topic& getTopic(const std::string& topicName);

private:
	TopicContainer(const TopicContainer& rhs) = delete;

	TopicContainer& operator=(const TopicContainer& rhs) = delete;

	std::unordered_map<std::string, Topic> m_topicNameToTopic;

};

}

#endif
