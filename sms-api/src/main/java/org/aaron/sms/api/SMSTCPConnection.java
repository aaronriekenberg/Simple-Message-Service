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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * TCP version of SMSConnection.
 * 
 * This version uses NIO and works on all platforms. It supports remote network
 * connections to the broker.
 */
public class SMSTCPConnection extends AbstractSMSConnection {

	private static final Integer CONNECT_TIMEOUT_MS = 1_000;

	private static final NioEventLoopGroup NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup();

	private final String brokerAddress;

	private final int brokerPort;

	/**
	 * Constructor method
	 * 
	 * @param serverAddress
	 *            Broker address
	 * @param serverPort
	 */
	public SMSTCPConnection(String brokerAddress, int brokerPort) {
		this(brokerAddress, brokerPort, 1, TimeUnit.SECONDS);
	}

	/**
	 * Constructor method
	 * 
	 * @param serverAddress
	 *            Broker address
	 * @param serverPort
	 * @param reconnect
	 *            delay reconnect delay time
	 * @param reconnect
	 *            delay unit reconnect delay time unit
	 */
	public SMSTCPConnection(String brokerAddress, int brokerPort,
			long reconnectDelay, TimeUnit reconnectDelayUnit) {
		super(reconnectDelay, reconnectDelayUnit);

		this.brokerAddress = checkNotNull(brokerAddress,
				"brokerAddress is null");

		checkArgument(brokerPort > 0, "brokerPort must be positive");
		this.brokerPort = brokerPort;
	}

	@Override
	protected ChannelFuture doBootstrapConnection(
			ChannelInitializer<Channel> channelInitializer) {
		return new Bootstrap()
				.group(NIO_EVENT_LOOP_GROUP)
				.channel(NioSocketChannel.class)
				.handler(channelInitializer)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						CONNECT_TIMEOUT_MS).connect(brokerAddress, brokerPort);
	}

	@Override
	protected EventLoopGroup getEventLoopGroup() {
		return NIO_EVENT_LOOP_GROUP;
	}

}
