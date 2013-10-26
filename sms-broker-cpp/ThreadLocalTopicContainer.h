#ifndef THREAD_LOCAL_TOPIC_CONTAINER_H
#define THREAD_LOCAL_TOPIC_CONTAINER_H

#include <mutex>
#include <string>
#include <unordered_map>
#include "Topic.h"

namespace smsbroker {

class SharedTopicContainer;

class ThreadLocalTopicContainer {
public:
	static void createThreadLocalInstance(
			SharedTopicContainer& sharedTopicContainer);

	static ThreadLocalTopicContainer& getThreadLocalInstance();

	~ThreadLocalTopicContainer() = default;

	Topic& getTopic(const std::string& topicName);

private:
	ThreadLocalTopicContainer(SharedTopicContainer& sharedTopicContainer);

	ThreadLocalTopicContainer(const ThreadLocalTopicContainer& rhs) = delete;

	ThreadLocalTopicContainer& operator=(const ThreadLocalTopicContainer& rhs) = delete;

	typedef std::unordered_map<std::string, Topic*> TopicNameToPointerMap;

	TopicNameToPointerMap m_topicNameToTopic;

	SharedTopicContainer& m_sharedTopicContainer;

	static thread_local std::unique_ptr<ThreadLocalTopicContainer> m_pThreadLocalInstance;

};

}

#endif
