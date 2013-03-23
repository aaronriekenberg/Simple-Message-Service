package org.aaron.sms.broker;

import org.aaron.sms.entities.protobuf.SMSProtocol;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.DefaultChannelGroup;

class SMSTopic {

	private final DefaultChannelGroup channelGroup = new DefaultChannelGroup();

	public SMSTopic() {

	}

	public void addSubscription(Channel channel) {
		channelGroup.add(channel);
	}

	public void removeSubscription(Channel channel) {
		channelGroup.remove(channel);
	}

	public void write(SMSProtocol.ServerToClientMessage message) {
		channelGroup.write(ChannelBuffers.wrappedBuffer(message.toByteArray()));
	}
}