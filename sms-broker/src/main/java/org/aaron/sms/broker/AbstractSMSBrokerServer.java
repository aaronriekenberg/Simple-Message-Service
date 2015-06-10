package org.aaron.sms.broker;

/*
 * #%L
 * Simple Message Service Broker
 * %%
 * Copyright (C) 2013 - 2015 Aaron Riekenberg
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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.aaron.sms.common.util.FunctionalReentrantReadWriteLock;
import org.aaron.sms.protocol.SMSProtocolChannelInitializer;
import org.aaron.sms.protocol.protobuf.SMSProtocol;
import org.aaron.sms.protocol.protobuf.SMSProtocol.BrokerToClientMessage.BrokerToClientMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

abstract class AbstractSMSBrokerServer {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractSMSBrokerServer.class);

	private final DefaultChannelGroup allChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	private final CountDownLatch destroyedLatch = new CountDownLatch(1);

	private final FunctionalReentrantReadWriteLock destroyLock = new FunctionalReentrantReadWriteLock();

	private final SMSTopicContainer topicContainer;

	private class ServerHandler extends
			SimpleChannelInboundHandler<SMSProtocol.ClientToBrokerMessage> {

		@Override
		public void channelRegistered(ChannelHandlerContext ctx)
				throws Exception {
			log.debug("channelRegistered {}", ctx.channel());

			/*
			 * Need to synchronize on destroyLock to avoid another thread
			 * calling destroy() between destroyed.get() and allChannels.add()
			 * below.
			 */
			destroyLock.doInReadLock(() -> {
				if (destroyed.get()) {
					ctx.channel().close();
				} else {
					allChannels.add(ctx.channel());
				}
			});
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("channelActive {}", ctx.channel());
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
			log.info("channelInactive {}", ctx.channel());
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx)
				throws Exception {
			log.debug("channelUnregistered {}", ctx.channel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			log.debug("exceptionCaught {}", ctx.channel(), cause);
			ctx.channel().close();
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

	public AbstractSMSBrokerServer(SMSTopicContainer topicContainer) {
		this.topicContainer = checkNotNull(topicContainer,
				"topicContainer is null");
	}

	protected abstract EventLoopGroup getEventLoopGroup();

	protected abstract ChannelFuture doBootstrap(
			ChannelInitializer<Channel> childHandler);

	@PostConstruct
	public void init() {
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		final ChannelInitializer<Channel> childHandler = new SMSProtocolChannelInitializer(
				ServerHandler::new,
				SMSProtocol.ClientToBrokerMessage.getDefaultInstance());

		final ChannelFuture channelFuture = doBootstrap(childHandler);

		final Channel serverChannel = channelFuture.syncUninterruptibly()
				.channel();
		allChannels.add(serverChannel);

		log.info("listening on {} ({})", serverChannel.localAddress(),
				getEventLoopGroup());
	}

	protected abstract void doDestroy();

	@PreDestroy
	public void destroy() {
		log.info("destroy");

		destroyLock.doInWriteLock(() -> {
			if (destroyed.compareAndSet(false, true)) {

				allChannels.close();

				doDestroy();

				destroyedLatch.countDown();

			}
		});
	}

	public boolean isDestroyed() {
		return destroyed.get();
	}

	public void awaitDestroyed() throws InterruptedException {
		destroyedLatch.await();
	}

	public void awaitDestroyedUninterruptible() {
		while (!isDestroyed()) {
			Uninterruptibles.awaitUninterruptibly(destroyedLatch);
		}
	}

	private void processIncomingMessage(Channel channel,
			SMSProtocol.ClientToBrokerMessage message) {
		final String topicName = message.getTopicName();
		final SMSTopic topic = topicContainer.getTopic(topicName);

		switch (message.getMessageType()) {
		case CLIENT_SEND_MESSAGE_TO_TOPIC:
			topic.write(SMSProtocol.BrokerToClientMessage
					.newBuilder()
					.setMessageType(
							BrokerToClientMessageType.BROKER_TOPIC_MESSAGE_PUBLISH)
					.setTopicName(topicName)
					.setMessagePayload(message.getMessagePayload()).build());
			break;

		case CLIENT_SUBSCRIBE_TO_TOPIC:
			topic.addSubscription(channel);
			break;

		case CLIENT_UNSUBSCRIBE_FROM_TOPIC:
			topic.removeSubscription(channel);
			break;

		}
	}

}
