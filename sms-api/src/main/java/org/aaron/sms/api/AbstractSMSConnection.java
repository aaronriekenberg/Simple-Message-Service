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
import static com.google.common.base.Preconditions.checkState;
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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.aaron.sms.common.util.FunctionalReentrantReadWriteLock;
import org.aaron.sms.protocol.SMSProtocolChannelInitializer;
import org.aaron.sms.protocol.protobuf.SMSProtocol;
import org.aaron.sms.protocol.protobuf.SMSProtocol.ClientToBrokerMessage.ClientToBrokerMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public abstract class AbstractSMSConnection implements SMSConnection {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractSMSConnection.class);

	private final DefaultChannelGroup allChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final DefaultChannelGroup connectedChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final ConcurrentHashMap<String, SMSMessageListener> subscribedTopicToListener = new ConcurrentHashMap<>();

	private enum ConnectionState {
		NOT_STARTED,

		RUNNING,

		DESTROYED
	}

	private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(
			ConnectionState.NOT_STARTED);

	private final Set<SMSConnectionStateListener> connectionStateListeners = Collections
			.newSetFromMap(new ConcurrentHashMap<>());

	private final FunctionalReentrantReadWriteLock destroyLock = new FunctionalReentrantReadWriteLock();

	private final long reconnectDelay;

	private final TimeUnit reconnectDelayUnit;

	private class ClientHandler extends
			SimpleChannelInboundHandler<SMSProtocol.BrokerToClientMessage> {

		public ClientHandler() {

		}

		@Override
		public void channelRegistered(ChannelHandlerContext ctx)
				throws Exception {
			log.debug("channelRegistered {}", ctx.channel());

			/*
			 * Need to synchronize on destroyLock to avoid another thread
			 * calling destroy() between connectionState.get() and
			 * allChannels.add() below.
			 */
			destroyLock.doInReadLock(() -> {
				if (connectionState.get() == ConnectionState.DESTROYED) {
					ctx.channel().close();
				} else {
					allChannels.add(ctx.channel());
				}
			});
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.debug("channelActive {}", ctx.channel());
			connectedChannels.add(ctx.channel());
			resubscribeToTopics();
			fireConnectionStateListenerCallback(SMSConnectionState.CONNECTED_TO_BROKER);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.debug("channelInactive {}", ctx.channel());
			fireConnectionStateListenerCallback(SMSConnectionState.NOT_CONNECTED_TO_BROKER);
			reconnectAsync();
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
				SMSProtocol.BrokerToClientMessage message) {
			try {
				log.debug("channelRead0 from {} message = '{}'", ctx.channel(),
						message);
				switch (message.getMessageType()) {
				case BROKER_TOPIC_MESSAGE_PUBLISH:
					handleBrokerTopicMessagePublish(message);
					break;
				}
			} catch (Exception e) {
				log.warn("channelRead0", e);
				ctx.channel().close();
			}
		}
	}

	public AbstractSMSConnection(long reconnectDelay,
			TimeUnit reconnectDelayUnit) {
		checkArgument(reconnectDelay > 0, "reconnectDelay must be positive");
		this.reconnectDelay = reconnectDelay;

		this.reconnectDelayUnit = checkNotNull(reconnectDelayUnit,
				"reconnectDelayUnit is null");
	}

	@Override
	public void registerConnectionStateListener(
			SMSConnectionStateListener listener) {
		checkNotNull(listener, "listener is null");

		connectionStateListeners.add(listener);
	}

	@Override
	public void unregisterConnectionStateListener(
			SMSConnectionStateListener listener) {
		checkNotNull(listener, "listener is null");

		connectionStateListeners.remove(listener);
	}

	@Override
	public void start() {
		checkState(connectionState.compareAndSet(ConnectionState.NOT_STARTED,
				ConnectionState.RUNNING), "Invalid state for start");

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		reconnectAsync(0, TimeUnit.SECONDS);
	}

	@Override
	public boolean isStarted() {
		return (connectionState.get() == ConnectionState.RUNNING);
	}

	protected abstract ChannelFuture doBootstrapConnection(
			ChannelInitializer<Channel> channelInitializer);

	private void bootstrapConnection() {
		if (!isStarted()) {
			return;
		}

		final ChannelInitializer<Channel> channelInitializer = new SMSProtocolChannelInitializer(
				ClientHandler::new,
				SMSProtocol.BrokerToClientMessage.getDefaultInstance());
		final ChannelFuture future = doBootstrapConnection(channelInitializer);
		future.addListener(f -> {
			final boolean success = f.isSuccess();
			log.debug("connect success {}", success);
			if (!success) {
				reconnectAsync();
			}
		});
	}

	protected abstract EventLoopGroup getEventLoopGroup();

	private void reconnectAsync() {
		reconnectAsync(reconnectDelay, reconnectDelayUnit);
	}

	private void reconnectAsync(long delay, TimeUnit delayUnit) {
		if (!isStarted()) {
			return;
		}

		getEventLoopGroup().schedule(() -> bootstrapConnection(), delay,
				delayUnit);
	}

	private void resubscribeToTopics() {
		if (!isStarted()) {
			return;
		}

		log.debug("resubscribeToTopics {}", subscribedTopicToListener);
		subscribedTopicToListener
				.keySet()
				.forEach(
						topicName -> connectedChannels
								.write(SMSProtocol.ClientToBrokerMessage
										.newBuilder()
										.setMessageType(
												ClientToBrokerMessageType.CLIENT_SUBSCRIBE_TO_TOPIC)
										.setTopicName(topicName)));
		connectedChannels.flush();
	}

	@Override
	public void subscribeToTopic(String topicName,
			SMSMessageListener messageListener) {
		checkNotNull(topicName, "topicName is null");
		checkArgument(topicName.length() > 0, "topicName is empty");
		checkNotNull(messageListener, "messageListener is null");

		if (subscribedTopicToListener.put(topicName, messageListener) == null) {
			connectedChannels
					.writeAndFlush(SMSProtocol.ClientToBrokerMessage
							.newBuilder()
							.setMessageType(
									ClientToBrokerMessageType.CLIENT_SUBSCRIBE_TO_TOPIC)
							.setTopicName(topicName));
		}
	}

	@Override
	public void unsubscribeFromTopic(String topicName) {
		checkNotNull(topicName, "topicName is null");
		checkArgument(topicName.length() > 0, "topicName is empty");

		if (subscribedTopicToListener.remove(topicName) != null) {
			connectedChannels
					.writeAndFlush(SMSProtocol.ClientToBrokerMessage
							.newBuilder()
							.setMessageType(
									ClientToBrokerMessageType.CLIENT_UNSUBSCRIBE_FROM_TOPIC)
							.setTopicName(topicName));
		}
	}

	@Override
	public void writeToTopic(String topicName, ByteString message) {
		checkNotNull(topicName, "topicName is null");
		checkArgument(topicName.length() > 0, "topicName is empty");
		checkNotNull(message, "message is null");

		connectedChannels.writeAndFlush(SMSProtocol.ClientToBrokerMessage
				.newBuilder()
				.setMessageType(
						ClientToBrokerMessageType.CLIENT_SEND_MESSAGE_TO_TOPIC)
				.setTopicName(topicName).setMessagePayload(message));
	}

	@Override
	public void destroy() {
		destroyLock.doInWriteLock(() -> {
			if (connectionState.compareAndSet(ConnectionState.RUNNING,
					ConnectionState.DESTROYED)) {

				connectionStateListeners.clear();

				subscribedTopicToListener.clear();

				allChannels.close();

			}
		});
	}

	private void handleBrokerTopicMessagePublish(
			SMSProtocol.BrokerToClientMessage message) {
		checkNotNull(message, "message is null");
		checkNotNull(message.getTopicName(), "topic name is null");
		checkArgument(message.getTopicName().length() > 0,
				"topic name is emtpy");
		checkNotNull(message.getMessagePayload(), "message payload is null");

		final SMSMessageListener listener = subscribedTopicToListener
				.get(message.getTopicName());
		if (listener != null) {
			fireMessageListenerCallback(listener, message.getMessagePayload());
		}
	}

	private void fireConnectionStateListenerCallback(SMSConnectionState newState) {
		connectionStateListeners.forEach(listener -> {
			try {
				listener.connectionStateChanged(newState);
			} catch (Exception e) {
				log.warn("fireConnectionStateListenerCallback", e);
			}
		});
	}

	private void fireMessageListenerCallback(SMSMessageListener listener,
			ByteString message) {
		try {
			listener.handleIncomingMessage(message);
		} catch (Exception e) {
			log.warn("fireMessageListenerCallback", e);
		}
	}
}
