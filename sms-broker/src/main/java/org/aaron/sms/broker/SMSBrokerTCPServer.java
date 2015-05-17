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
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.google.common.base.Preconditions;

public class SMSBrokerTCPServer extends AbstractSMSBrokerServer {

	private final String listenAddress;

	private final int listenPort;

	private final EventLoopGroup eventLoopGroup;

	private final Class<? extends ServerChannel> serverChannelClass;

	public SMSBrokerTCPServer(SMSTopicContainer topicContainer,
			String listenAddress, int listenPort) {
		super(topicContainer);

		this.listenAddress = Preconditions.checkNotNull(listenAddress,
				"listenAddress is null");

		Preconditions.checkArgument(listenPort > 0, "listenPort must be > 0");
		this.listenPort = listenPort;

		if (Epoll.isAvailable()) {
			eventLoopGroup = new EpollEventLoopGroup();
			serverChannelClass = EpollServerSocketChannel.class;
		} else {
			eventLoopGroup = new NioEventLoopGroup();
			serverChannelClass = NioServerSocketChannel.class;
		}
	}

	@Override
	protected EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	@Override
	protected ChannelFuture doBootstrap(ChannelInitializer<Channel> childHandler) {
		final ServerBootstrap b = new ServerBootstrap();
		b.group(eventLoopGroup).channel(serverChannelClass)
				.childHandler(childHandler)
				.option(ChannelOption.SO_REUSEADDR, true);
		return b.bind(listenAddress, listenPort);
	}

	@Override
	protected void doDestroy() {
		eventLoopGroup.shutdownGracefully();
	}

}
