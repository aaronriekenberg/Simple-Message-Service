#include "Topic.h"

namespace smsbroker {

void Topic::subscribe(std::shared_ptr<TopicListener> pTopicListener) {
	std::lock_guard<std::mutex> lock(m_mutex);
	m_idToListener[pTopicListener->getTopicListenerID()] = pTopicListener;
	rebuidListenersVector();
}

void Topic::unsubscribe(const TopicListener& topicListener) {
	std::lock_guard<std::mutex> lock(m_mutex);
	m_idToListener.erase(topicListener.getTopicListenerID());
	rebuidListenersVector();
}

bool Topic::hasSubscribers() const {
	std::lock_guard<std::mutex> lock(m_mutex);
	if (m_pListenersVector) {
		return true;
	}
	return false;
}

void Topic::rebuidListenersVector() {
	if (m_idToListener.empty()) {
		m_pListenersVector.reset();
	} else {
		std::shared_ptr<ListenersVector> pNewListenersVector(
				new ListenersVector);
		pNewListenersVector->reserve(m_idToListener.size());
		for (const auto& entry : m_idToListener) {
			pNewListenersVector->push_back(entry.second);
		}
		m_pListenersVector = pNewListenersVector;
	}
}

void Topic::publishSerializedBrokerToClientMessage(
		ConstBufferSharedPtr pBuffer) const {
	std::shared_ptr<ConstListenersVector> pListenersVector;

	{
		std::lock_guard<std::mutex> lock(m_mutex);
		pListenersVector = m_pListenersVector;
	}

	if (pListenersVector) {
		for (auto pListener : (*pListenersVector)) {
			pListener->writeSerializedBrokerToClientMessage(pBuffer);
		}
	}
}

}
