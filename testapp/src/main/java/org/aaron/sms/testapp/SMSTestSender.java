package org.aaron.sms.testapp;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTestSender implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTestSender.class);

	private final AtomicBoolean smsConnectionClosed = new AtomicBoolean(false);

	private final String topicName;

	public SMSTestSender(String topicName) {
		this.topicName = topicName;
	}

	@Override
	public void run() {
		try {
			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
					10001);

			smsConnection.setListener(new SMSConnectionListener() {

				@Override
				public void handleIncomingMessage(String topicName,
						byte[] message) {
					log.debug("handleIncomingMessage topic {} length {}",
							topicName, message.length);
				}

				@Override
				public void handleConnectionOpen() {
					log.info("handleConnectionOpen");
				}

				@Override
				public void handleConnectionClosed() {
					log.info("handleConnectionClosed");
					// smsConnectionClosed.set(true);
				}
			});

			smsConnection.start();

			final byte[] buffer = new byte[5000];
			while (true) {
				if (smsConnectionClosed.get()) {
					smsConnection.destroy();
					break;
				}

				smsConnection.writeToTopic(topicName, buffer);
				Thread.sleep(1);
			}
		} catch (Exception e) {
			log.warn("main", e);
		}
	}

	public static void main(String[] args) {
		final ArrayList<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < 30; ++i) {
			final Thread t = new Thread(new SMSTestSender("test.topic." + i));
			t.start();
			threadList.add(t);
		}
		for (Thread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				log.warn("main", e);
			}
		}
	}
}
