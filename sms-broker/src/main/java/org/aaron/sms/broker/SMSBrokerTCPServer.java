package org.aaron.sms.broker;

/*
 * #%L
 * Simple Message Service Broker
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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import org.aaron.sms.common.eventloop.TCPEventLoopGroupContainer;
import org.springframework.beans.factory.annotation.Value;

public class SMSBrokerTCPServer extends AbstractSMSBrokerServer {

	@Value("${smsbroker.tcp.listen_address}")
	private String listenAddress;

	@Value("${smsbroker.tcp.listen_port}")
	private int listenPort;

	public SMSBrokerTCPServer(SMSTopicContainer topicContainer) {
		super(topicContainer);
	}

	@Override
	protected EventLoopGroup getEventLoopGroup() {
		return TCPEventLoopGroupContainer.getEventLoopGroup();
	}

	@Override
	protected ChannelFuture doBootstrap(ChannelInitializer<Channel> childHandler) {
		final ServerBootstrap b = new ServerBootstrap();
		b.group(getEventLoopGroup())
				.channel(TCPEventLoopGroupContainer.getServerChannelClass())
				.childHandler(childHandler)
				.option(ChannelOption.SO_REUSEADDR, true);
		return b.bind(listenAddress, listenPort);
	}

	@Override
	protected void doDestroy() {

	}

}
