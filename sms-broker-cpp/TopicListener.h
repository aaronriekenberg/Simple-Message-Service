#ifndef TOPIC_LISTENER_H
#define TOPIC_LISTENER_H

#include <memory>
#include <string>
#include <vector>
#include "Buffer.h"

namespace smsbroker {

class TopicListener {
public:

	virtual ~TopicListener() = default;

	virtual const std::string& getTopicListenerID() const = 0;

	virtual void writeSerializedBrokerToClientMessage(
			BufferSharedPtr pSerializedBuffer) = 0;

};

}
;

#endif
