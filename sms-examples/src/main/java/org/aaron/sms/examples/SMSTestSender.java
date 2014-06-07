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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTestSender implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTestSender.class);

	private final String topicName;

	public SMSTestSender(String topicName) {
		this.topicName = checkNotNull(topicName);
	}

	@Override
	public void run() {
		try {
			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
					10001);

			smsConnection.setListener(new SMSConnectionListener() {

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

			final byte[] buffer = new byte[MESSAGE_SIZE_BYTES];
			while (true) {
				smsConnection.writeToTopic(topicName, buffer);
				Thread.sleep(SLEEP_BETWEEN_SENDS_MS);
			}
		} catch (Exception e) {
			log.warn("main", e);
		}
	}

	private static final int NUM_SENDERS = 50;

	private static final int MESSAGE_SIZE_BYTES = 50;

	private static final long SLEEP_BETWEEN_SENDS_MS = 10;

	public static void main(String[] args) {
		log.info("NUM_SENDERS = {}", NUM_SENDERS);
		log.info("MESSAGE_SIZE_BYTES = {}", MESSAGE_SIZE_BYTES);
		log.info("SLEEP_BETWEEN_SENDS_MS = {}", SLEEP_BETWEEN_SENDS_MS);
		final List<Thread> threadList = IntStream
				.range(0, NUM_SENDERS)
				.mapToObj(
						i -> {
							final Thread t = new Thread(new SMSTestSender(
									"test.topic." + i));
							t.start();
							return t;
						}).collect(Collectors.toList());

		threadList.forEach(t -> {
			try {
				t.join();
			} catch (Exception e) {
				log.warn("join", e);
			}
		});

	}
}
