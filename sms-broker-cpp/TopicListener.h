#ifndef TOPIC_LISTENER_H
#define TOPIC_LISTENER_H

#include <memory>
#include <string>
#include <vector>
#include "Topic.h"

namespace smsbroker {

class TopicListener {
public:

	virtual ~TopicListener() = default;

	virtual const std::string& getTopicListenerID() const = 0;

	typedef std::shared_ptr<std::vector<unsigned char>> BufferSharedPtr;

	virtual void writeSerializedBrokerToClientMessage(
			BufferSharedPtr pSerializedBuffer, size_t bufferSize) = 0;

};

}
;

#endif
