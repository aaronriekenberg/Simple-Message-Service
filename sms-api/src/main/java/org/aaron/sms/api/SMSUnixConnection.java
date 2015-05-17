package org.aaron.sms.api;

/*
 * #%L
 * Simple Message Service API
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

import static com.google.common.base.Preconditions.checkNotNull;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * TCP version of SMSConnection.
 * 
 * This version uses Unix Domain sockets and works on Linux only. It supports
 * local connections to the broker only.
 */
public class SMSUnixConnection extends AbstractSMSConnection {

	private static final EpollEventLoopGroup EPOLL_EVENT_LOOP_GROUP = new EpollEventLoopGroup();

	private final Path brokerSocketPath;

	/**
	 * Constructor method
	 * 
	 * @param brokerSocketPath
	 *            Broker socket path
	 */
	public SMSUnixConnection(Path brokerSocketPath) {
		this(brokerSocketPath, 1, TimeUnit.SECONDS);
	}

	/**
	 * Constructor method
	 * 
	 * @param brokerSocketPath
	 *            Broker socket path
	 * @param reconnect
	 *            delay reconnect delay time
	 * @param reconnect
	 *            delay unit reconnect delay time unit
	 */
	public SMSUnixConnection(Path brokerSocketPath, long reconnectDelay,
			TimeUnit reconnectDelayUnit) {
		super(reconnectDelay, reconnectDelayUnit);

		this.brokerSocketPath = checkNotNull(brokerSocketPath,
				"brokerSocketPath is null");
	}

	@Override
	protected ChannelFuture doBootstrapConnection(
			ChannelInitializer<Channel> channelInitializer) {
		return new Bootstrap().group(EPOLL_EVENT_LOOP_GROUP)
				.channel(EpollDomainSocketChannel.class)
				.handler(channelInitializer)
				.connect(new DomainSocketAddress(brokerSocketPath.toFile()));
	}

	@Override
	protected EventLoopGroup getEventLoopGroup() {
		return EPOLL_EVENT_LOOP_GROUP;
	}

}
