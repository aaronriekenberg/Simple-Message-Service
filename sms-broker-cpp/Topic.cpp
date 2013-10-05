#include "Topic.h"

namespace smsbroker {

void Topic::subscribe(std::shared_ptr<TopicListener> pTopicListener) {
	std::lock_guard<std::mutex> lock(m_mutex);
	m_idToWeakListener[pTopicListener->getTopicListenerID()] = pTopicListener;
}

void Topic::unsubscribe(std::shared_ptr<TopicListener> pTopicListener) {
	std::lock_guard<std::mutex> lock(m_mutex);
	m_idToWeakListener.erase(pTopicListener->getTopicListenerID());
}

void Topic::publishSerializedBrokerToClientMessage(
		ConstBufferSharedPtr pBuffer) {
	std::vector<std::shared_ptr<TopicListener>> listenersToNotify;

	{
		std::lock_guard<std::mutex> lock(m_mutex);
		for (const auto& entry : m_idToWeakListener) {
			if (auto pListener = entry.second.lock()) {
				listenersToNotify.push_back(pListener);
			} else {
				m_idsToRemove.push_back(entry.first);
			}
		}

		for (const auto& id : m_idsToRemove) {
			m_idToWeakListener.erase(id);
		}

		m_idsToRemove.clear();
	}

	for (auto pListener : listenersToNotify) {
		pListener->writeSerializedBrokerToClientMessage(pBuffer);
	}
}

}
