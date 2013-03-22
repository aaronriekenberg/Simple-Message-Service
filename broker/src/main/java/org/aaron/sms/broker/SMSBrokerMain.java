package org.aaron.sms.broker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SMSBrokerMain {

	public static void main(String[] args) {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"META-INF/spring/sms-broker-spring.xml");
		final SMSBrokerTCPServer brokerTCPServer = context
				.getBean(SMSBrokerTCPServer.class);
		while (true) {
			try {
				brokerTCPServer.awaitBrokerDestroyed();
				break;
			} catch (InterruptedException e) {
			}
		}
	}

}
