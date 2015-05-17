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

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSUnixConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSUnixTestSender extends AbstractTestSender {

	private static final Logger log = LoggerFactory
			.getLogger(SMSUnixTestSender.class);

	public SMSUnixTestSender(String topicName) {
		super(topicName, MESSAGE_SIZE_BYTES, SLEEP_BETWEEN_SENDS_MS);
	}

	@Override
	protected SMSConnection createConnection() {
		return new SMSUnixConnection(Paths.get("/tmp", "sms-unix-socket"));
	}

	private static final int NUM_SENDERS = 50;

	private static final int MESSAGE_SIZE_BYTES = 5_000;

	private static final long SLEEP_BETWEEN_SENDS_MS = 1;

	public static void main(String[] args) {
		log.info("NUM_SENDERS = {}", NUM_SENDERS);
		log.info("MESSAGE_SIZE_BYTES = {}", MESSAGE_SIZE_BYTES);
		log.info("SLEEP_BETWEEN_SENDS_MS = {}", SLEEP_BETWEEN_SENDS_MS);

		final List<Thread> threadList = IntStream.range(0, NUM_SENDERS)
				.mapToObj(i -> "test.topic." + i).map(SMSUnixTestSender::new)
				.map(Thread::new).collect(Collectors.toList());

		threadList.forEach(Thread::start);

		threadList.forEach(t -> {
			try {
				t.join();
			} catch (Exception e) {
				log.warn("join", e);
			}
		});

	}
}
