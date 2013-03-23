package org.aaron.sms.testapp;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.aaron.sms.api.SMSConnection;
import org.aaron.sms.api.SMSConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSTestReceiver implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(SMSTestReceiver.class);

	private static final Timer timer = new Timer(true);

	private final AtomicInteger messagesReceived = new AtomicInteger(0);

	private final String topicName;

	public SMSTestReceiver(String topicName) {
		this.topicName = topicName;
	}

	@Override
	public void run() {
		try {
			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
					10001);

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					log.info(topicName + " messages received last second = "
							+ messagesReceived.getAndSet(0));
				}
			}, 1000, 1000);

			smsConnection.setListener(new SMSConnectionListener() {

				@Override
				public void handleIncomingMessage(String topicName,
						byte[] message) {
					log.debug("handleIncomingMessage topic {} length {}",
							topicName, message.length);
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

			while (true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			log.warn("main", e);
		}
	}

	public static void main(String[] args) {
		final ArrayList<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < 50; ++i) {
			final Thread t = new Thread(new SMSTestReceiver("test.topic." + i));
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
