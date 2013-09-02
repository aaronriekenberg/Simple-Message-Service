#include "Topic.h"

namespace smsbroker {

void Topic::subscribe(std::shared_ptr<TopicListener> pTopicListener) {
	m_idToWeakListener[pTopicListener->getTopicListenerID()] = pTopicListener;
}

void Topic::unsubscribe(std::shared_ptr<TopicListener> pTopicListener) {
	m_idToWeakListener.erase(pTopicListener->getTopicListenerID());
}

void Topic::publishSerializedBrokerToClientMessage(BufferSharedPtr pBuffer,
		size_t bufferSize) {
	for (const auto& entry : m_idToWeakListener) {
		if (auto pListener = entry.second.lock()) {
			pListener->writeSerializedBrokerToClientMessage(pBuffer,
					bufferSize);
		} else {
			m_idsToRemove.push_back(entry.first);
		}
	}

	for (const auto& id : m_idsToRemove) {
		m_idToWeakListener.erase(id);
	}

	m_idsToRemove.clear();
}

}
