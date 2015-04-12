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

import java.util.stream.IntStream;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTCPTestReceiver extends AbstractTestReceiver {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTCPTestReceiver.class);

	public SMSTCPTestReceiver(String topicName) {
		super(topicName);
	}

	@Override
	protected SMSConnection createConnection() {
		return new SMSTCPConnection("127.0.0.1", 10001);
	}

	private static final int NUM_RECEIVERS = 50;

	public static void main(String[] args) {
		log.info("NUM_RECEIVERS = {}", NUM_RECEIVERS);

		IntStream.range(0, NUM_RECEIVERS).mapToObj(i -> "test.topic." + i)
				.map(SMSTCPTestReceiver::new)
				.forEach(SMSTCPTestReceiver::start);

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				log.warn("main", e);
			}
		}
	}

}
