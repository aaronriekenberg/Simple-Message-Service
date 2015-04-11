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

import static com.google.common.base.Preconditions.checkNotNull;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.atomic.AtomicLong;

import org.aaron.sms.protocol.protobuf.SMSProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SMSTopic {

	private static final Logger log = LoggerFactory.getLogger(SMSTopic.class);

	private final DefaultChannelGroup channelGroup = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final AtomicLong messagesSent = new AtomicLong(0);

	public SMSTopic(String topicName) {
		checkNotNull(topicName, "topicName is null");
		log.info("create SMSTopic '{}'", topicName);
	}

	public void addSubscription(Channel channel) {
		checkNotNull(channel, "channel is null");

		channelGroup.add(channel);
	}

	public void removeSubscription(Channel channel) {
		checkNotNull(channel, "channel is null");

		channelGroup.remove(channel);
	}

	public void write(SMSProtocol.BrokerToClientMessage message) {
		checkNotNull(message, "message is null");

		messagesSent.getAndIncrement();

		channelGroup.writeAndFlush(message);
	}
}