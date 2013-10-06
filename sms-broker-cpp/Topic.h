#ifndef TOPIC_H
#define TOPIC_H

#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>
#include "Buffer.h"
#include "TopicListener.h"

namespace smsbroker {

class Topic {
public:
	Topic() = default;

	~Topic() = default;

	void subscribe(std::shared_ptr<TopicListener> pTopicListener);

	void unsubscribe(const TopicListener& topicListener);

	bool hasSubscribers() const;

	void publishSerializedBrokerToClientMessage(
			ConstBufferSharedPtr pBuffer) const;

private:
	Topic(const Topic& rhs) = delete;

	Topic& operator=(const Topic& rhs) = delete;

	void rebuidListenersVector();

	std::unordered_map<std::string, std::shared_ptr<TopicListener>> m_idToListener;

	typedef std::vector<TopicListener*> ListenersVector;

	typedef const std::vector<TopicListener*> ConstListenersVector;

	std::shared_ptr<ConstListenersVector> m_pListenersVector;

	mutable std::mutex m_mutex;

};

}

#endif
