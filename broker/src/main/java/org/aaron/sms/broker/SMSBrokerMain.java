package org.aaron.sms.broker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SMSBrokerMain {

	public static void main(String[] args) {
		new ClassPathXmlApplicationContext(
				"META-INF/spring/sms-broker-spring.xml");

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
	}

}
