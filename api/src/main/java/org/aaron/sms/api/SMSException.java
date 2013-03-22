package org.aaron.sms.api;

/**
 * Exception thrown by SMSConnection.
 */
public class SMSException extends Exception {

	private static final long serialVersionUID = 1L;

	public SMSException(String message) {
		super(message);
	}

	public SMSException(Throwable cause) {
		super(cause);
	}

	public SMSException(String message, Throwable cause) {
		super(message, cause);
	}

}
