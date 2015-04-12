package org.aaron.sms.examples;

/*
 * #%L
 * Simple Message Service Examples
 * %%
 * Copyright (C) 2013 - 2015 Aaron Riekenberg
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.aaron.sms.api.SMSConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestReceiver {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractTestReceiver.class);

	private static final ScheduledExecutorService executor = Executors
			.newScheduledThreadPool(1);

	private final AtomicInteger messagesReceived = new AtomicInteger(0);

	private final String topicName;

	public AbstractTestReceiver(String topicName) {
		this.topicName = checkNotNull(topicName);
	}

	public void start() {
		try {
			final SMSConnection smsConnection = createConnection();

			executor.scheduleAtFixedRate(
					() -> log.info(topicName
							+ " messages received last second = "
							+ messagesReceived.getAndSet(0)), 1, 1,
					TimeUnit.SECONDS);

			smsConnection.registerConnectionStateListener(newState -> log.info(
					"connection state changed {}", newState));

			smsConnection.subscribeToTopic(topicName, message -> {
				log.debug("handleIncomingMessage topic {} length {}",
						topicName, message.size());
				messagesReceived.getAndIncrement();
			});

			smsConnection.start();

		} catch (Exception e) {
			log.warn("start", e);
		}
	}

	protected abstract SMSConnection createConnection();

}
