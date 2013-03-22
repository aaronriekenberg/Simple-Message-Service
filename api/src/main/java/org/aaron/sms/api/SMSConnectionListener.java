package org.aaron.sms.api;

/**
 * A listener for events from an SMSConnection.
 */
public interface SMSConnectionListener {

	/**
	 * Notification that the SMSConnection is now connected to the SMS Broker.
	 */
	public void handleConnectionOpen();

	/**
	 * Notification that the SMSConnection is now disconnected from the SMS
	 * Broker.
	 */
	public void handleConnectionClosed();

	/**
	 * Handle an incoming message for a topic subscription.
	 * 
	 * @param topicName
	 *            topic name
	 * @param message
	 *            message payload
	 */
	public void handleIncomingMessage(String topicName, byte[] message);

}
