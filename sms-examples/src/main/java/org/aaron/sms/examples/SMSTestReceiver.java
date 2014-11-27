package org.aaron.sms.examples;

/*
 * #%L
 * Simple Message Service Examples
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public class SMSTestReceiver {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTestReceiver.class);

	private static final ScheduledExecutorService executor = Executors
			.newScheduledThreadPool(1);

	private final AtomicInteger messagesReceived = new AtomicInteger(0);

	private final String topicName;

	public SMSTestReceiver(String topicName) {
		this.topicName = checkNotNull(topicName);
	}

	public void start() {
		try {
			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
					10001);

			executor.scheduleAtFixedRate(
					() -> log.info(topicName
							+ " messages received last second = "
							+ messagesReceived.getAndSet(0)), 1, 1,
					TimeUnit.SECONDS);

			smsConnection.setListener(new SMSConnectionListener() {

				@Override
				public void handleIncomingMessage(String topicName,
						ByteString message) {
					log.debug("handleIncomingMessage topic {} length {}",
							topicName, message.size());
					if (!SMSTestReceiver.this.topicName.equals(topicName)) {
						throw new IllegalStateException("received topic name '"
								+ topicName + "' expected '"
								+ SMSTestReceiver.this.topicName + "'");
					}
					messagesReceived.getAndIncrement();
				}

				@Override
				public void handleConnectionOpen() {
					log.info("handleConnectionOpen");
				}

				@Override
				public void handleConnectionClosed() {
					log.info("handleConnectionClosed");
				}
			});

			smsConnection.start();

			smsConnection.subscribeToTopic(topicName);

		} catch (Exception e) {
			log.warn("start", e);
		}
	}

	private static final int NUM_RECEIVERS = 50;

	public static void main(String[] args) {
		log.info("NUM_RECEIVERS = {}", NUM_RECEIVERS);
		IntStream.range(0, NUM_RECEIVERS).forEach(
				i -> new SMSTestReceiver("test.topic." + i).start());
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				log.warn("main", e);
			}
		}
	}
}
