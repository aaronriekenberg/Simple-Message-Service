#ifndef TOPIC_H
#define TOPIC_H

#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>
#include "TopicListener.h"

namespace smsbroker {

class Topic {
public:
	Topic() = default;

	~Topic() = default;

	Topic(const Topic& rhs) = delete;

	Topic& operator=(const Topic& rhs) = delete;

	void subscribe(std::shared_ptr<TopicListener> pTopicListener);

	void unsubscribe(std::shared_ptr<TopicListener> pTopicListener);

	typedef std::shared_ptr<std::vector<unsigned char>> BufferSharedPtr;

	void publishSerializedBrokerToClientMessage(BufferSharedPtr pBuffer,
			size_t bufferSize);

private:
	std::unordered_map<std::string, std::weak_ptr<TopicListener>> m_idToWeakListener;

	std::vector<std::string> m_idsToRemove;

	std::mutex m_mutex;

};

}

#endif
