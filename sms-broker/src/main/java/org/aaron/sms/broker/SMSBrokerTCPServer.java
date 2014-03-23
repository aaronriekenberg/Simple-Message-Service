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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aaron.sms.protocol.SMSProtocolChannelInitializer;
import org.aaron.sms.protocol.protobuf.SMSProtocol;
import org.aaron.sms.protocol.protobuf.SMSProtocol.BrokerToClientMessage.BrokerToClientMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSBrokerTCPServer {

	private static final Logger log = LoggerFactory
			.getLogger(SMSBrokerTCPServer.class);

	private final ChannelGroup allChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	private final CountDownLatch destroyedLatch = new CountDownLatch(1);

	private final EventLoopGroup bossGroup = new NioEventLoopGroup();

	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	private final SMSTopicContainer topicContainer;

	private class ServerHandler extends
			SimpleChannelInboundHandler<SMSProtocol.ClientToBrokerMessage> {

		@Override
		public void channelRegistered(ChannelHandlerContext ctx)
				throws Exception {
			log.debug("channelRegistered {}", ctx.channel());
			allChannels.add(ctx.channel());
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("channelActive {}", ctx.channel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			log.debug("exceptionCaught {}", ctx.channel(), cause);
			ctx.channel().close();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
			log.info("channelInactive {}", ctx.channel());
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx,
				SMSProtocol.ClientToBrokerMessage message) {
			try {
				log.debug("channelRead0 from {} message = '{}'", ctx.channel(),
						message);
				processIncomingMessage(ctx.channel(), message);
			} catch (Exception e) {
				log.warn("channelRead0", e);
				ctx.channel().close();
			}
		}
	}

	public SMSBrokerTCPServer(SMSTopicContainer topicContainer,
			String listenAddress, int listenPort) throws InterruptedException {
		this.topicContainer = topicContainer;

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		final ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(
						new SMSProtocolChannelInitializer(ServerHandler::new,
								SMSProtocol.ClientToBrokerMessage
										.getDefaultInstance()))
				.option(ChannelOption.SO_REUSEADDR, true);

		final Channel serverChannel = b.bind(listenAddress, listenPort).sync()
				.channel();
		allChannels.add(serverChannel);

		log.info("listening on " + serverChannel.localAddress());

	}

	public void destroy() {
		log.info("destroy");

		allChannels.close();

		bossGroup.shutdownGracefully();

		workerGroup.shutdownGracefully();

		destroyed.set(true);

		destroyedLatch.countDown();
	}

	public void awaitDestroyed() throws InterruptedException {
		destroyedLatch.await();
	}

	public boolean isDestroyed() {
		return destroyed.get();
	}

	private void processIncomingMessage(Channel channel,
			SMSProtocol.ClientToBrokerMessage message) {
		switch (message.getMessageType()) {
		case CLIENT_SEND_MESSAGE_TO_TOPIC: {
			final String topicName = message.getTopicName();
			final SMSTopic topic = topicContainer.getTopic(topicName);
			topic.write(SMSProtocol.BrokerToClientMessage
					.newBuilder()
					.setMessageType(
							BrokerToClientMessageType.BROKER_TOPIC_MESSAGE_PUBLISH)
					.setTopicName(topicName)
					.setMessagePayload(message.getMessagePayload()).build());
			break;
		}

		case CLIENT_SUBSCRIBE_TO_TOPIC: {
			final String topicName = message.getTopicName();
			final SMSTopic topic = topicContainer.getTopic(topicName);
			topic.addSubscription(channel);
			break;
		}

		case CLIENT_UNSUBSCRIBE_FROM_TOPIC: {
			final String topicName = message.getTopicName();
			final SMSTopic topic = topicContainer.getTopic(topicName);
			topic.removeSubscription(channel);
			break;
		}

		}
	}

}
