package org.aaron.sms.broker;

/*
 * #%L
 * Simple Message Service Broker
 * %%
 * Copyright (C) 2013 Aaron Riekenberg
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTopicContainer {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTopicContainer.class);

	private final ConcurrentHashMap<String, SMSTopic> topicNameToInfo = new ConcurrentHashMap<>();

	public SMSTopicContainer() {

	}

	public void destroy() {
		log.info("destroy");
	}

	public SMSTopic getTopic(String topicName, EventLoopGroup eventLoopGroup) {
		checkNotNull(topicName, "topicName is null");
		checkArgument(topicName.length() > 0, "topicName is empty");
		checkNotNull(eventLoopGroup, "eventLoopGroup is null");

		SMSTopic topic = topicNameToInfo.get(topicName);

		if (topic == null) {
			final SMSTopic newTopic = new SMSTopic(eventLoopGroup.next());
			topic = topicNameToInfo.putIfAbsent(topicName, newTopic);
			if (topic == null) {
				log.info("created topic '" + topicName + "'");
				topic = newTopic;
			}
		}

		return topic;
	}
}
