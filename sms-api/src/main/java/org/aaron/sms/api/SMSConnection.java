package org.aaron.sms.api;

/*
 * #%L
 * Simple Message Service API
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

import com.google.protobuf.ByteString;

/**
 * SMSConnection represents a single client connection to an SMS Broker.
 * 
 * SMSConnection asynchronously attempts to connect to the SMS Broker when
 * start() is called.
 * 
 * If the connection to the SMS Broker is lost, SMSConnection automatically
 * attempts to reconnect. When the connection is reestablished to the SMS
 * Broker, subscriptions to all topics are reestablished automatically.
 * 
 * While there is no active connection to the SMS Broker, all calls to
 * writeToTopic will silently discard messages. It is the user's responsibility
 * to manage this if necessary.
 * 
 * SMSConnection is safe for use by multiple concurrent threads.
 */
public interface SMSConnection {

	/**
	 * Start the SMSConnection. Initiates a connection attempt to the SMS
	 * Broker.
	 */
	public void start();

	/**
	 * Is the SMSConnection started?
	 * 
	 * @return true if started, false otherwise
	 */
	public boolean isStarted();

	/**
	 * Subscribe to a topic to begin receiving messages from it.
	 * 
	 * @param topicName
	 *            topic name
	 * @param messageListener
	 *            message listener
	 */
	public void subscribeToTopic(String topicName,
			SMSMessageListener messageListener);

	/**
	 * Unsubscribe from a topic to stop receiving messages from it
	 * 
	 * @param topicName
	 *            topic name
	 */
	public void unsubscribeFromTopic(String topicName);

	/**
	 * Write a message to a topic asynchronously.
	 * 
	 * If this SMSConnection is not currently connected to an SMS Broker, the
	 * message will be silently dropped.
	 * 
	 * @param topicName
	 *            topic name
	 * @param message
	 *            message payload
	 */
	public void writeToTopic(String topicName, ByteString message);

	/**
	 * Destroy this SMSConnection. Close the connection to the SMS Broker and
	 * destroy all resources.
	 * 
	 * This SMSConnection must not be used after destroy is called.
	 * 
	 * It is the user's responsibility to call destroy on all SMSConnections
	 * created.
	 */
	public void destroy();

}
