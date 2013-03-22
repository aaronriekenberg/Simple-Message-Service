package org.aaron.sms.broker;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTopicContainer {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTopicContainer.class);

	private final ConcurrentHashMap<String, SMSTopic> topicNameToInfo = new ConcurrentHashMap<String, SMSTopic>();

	public void init() {
		log.info("init");
	}

	public void destroy() {
		log.info("destroy");
	}

	public SMSTopic getTopic(String topicName) {
		if (topicName == null) {
			throw new NullPointerException("topicName is null");
		}
		if (topicName.isEmpty()) {
			throw new IllegalArgumentException("topicName is empty");
		}

		SMSTopic topic = topicNameToInfo.get(topicName);

		if (topic == null) {
			final SMSTopic newTopic = new SMSTopic();
			topic = topicNameToInfo.putIfAbsent(topicName, newTopic);
			if (topic == null) {
				log.info("created topic '" + topicName + "'");
				topic = newTopic;
			}
		}

		return topic;
	}
}
