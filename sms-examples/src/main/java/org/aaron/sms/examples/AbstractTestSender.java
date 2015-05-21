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

import org.aaron.sms.api.SMSConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public abstract class AbstractTestSender implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractTestSender.class);

	private final String topicName;

	private final int messageSizeBytes;

	private final long sleepBetweenSendsMS;

	public AbstractTestSender(String topicName, int messageSizeBytes,
			long sleepBetweenSendsMS) {
		this.topicName = checkNotNull(topicName);
		this.messageSizeBytes = messageSizeBytes;
		this.sleepBetweenSendsMS = sleepBetweenSendsMS;
	}

	@Override
	public void run() {
		try {
			final SMSConnection smsConnection = createConnection();

			smsConnection.registerConnectionStateListener(newState -> log.info(
					"connection state changed {}", newState));

			smsConnection.start();

			final ByteString buffer = ByteString
					.copyFrom(new byte[messageSizeBytes]);
			while (true) {
				smsConnection.writeToTopic(topicName, buffer);
				Thread.sleep(sleepBetweenSendsMS);
			}
		} catch (Exception e) {
			log.warn("run", e);
		}
	}

	protected abstract SMSConnection createConnection();

}
