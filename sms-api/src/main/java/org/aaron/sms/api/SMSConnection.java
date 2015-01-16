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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.aaron.sms.protocol.SMSProtocolChannelInitializer;
import org.aaron.sms.protocol.protobuf.SMSProtocol;
import org.aaron.sms.protocol.protobuf.SMSProtocol.ClientToBrokerMessage.ClientToBrokerMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

/**
 * SMSConnection represents a single client connection to an SMS Broker (a TCP
 * socket connection).
 * 
 * SMSConnection asynchronously attempts to connect to the SMS Broker when
 * start() is called.
 * 
 * If the connection to the SMS Broker is lost, SMSConnection automatically
 * attempts to reconnect. When the connection is reestablished to the SMS
 * Broker, subscriptions to all topics are reestablished automatically.
 * 
 * While there is no active connection to the SMS Broker, all calls to
 * writeToTopic will silently discard messages. It is the user's responsibility
 * to manage this if necessary.
 * 
 * This class is safe for use by multiple concurrent threads.
 */
public class SMSConnection {

	private static final Logger log = LoggerFactory
			.getLogger(SMSConnection.class);

	private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

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

	private final Object destroyLock = new Object();

	private final String brokerAddress;

	private final int brokerPort;

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
			synchronized (destroyLock) {
				if (connectionState.get() == ConnectionState.DESTROYED) {
					ctx.channel().close();
				} else {
					allChannels.add(ctx.channel());
				}
			}
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.debug("channelActive {}", ctx.channel());
			connectedChannels.add(ctx.channel());
			resubscribeToTopics();
			fireConnectionStateListenerCallback(SMSConnectionStateListener::handleConnectionOpen);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.debug("channelInactive {}", ctx.channel());
			fireConnectionStateListenerCallback(SMSConnectionStateListener::handleConnectionClosed);
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx)
				throws Exception {
			log.debug("channelUnregistered {}", ctx.channel());
			reconnectAsync(reconnectDelay, reconnectDelayUnit);
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

	/**
	 * Constructor method
	 * 
	 * @param serverAddress
	 *            Broker address
	 * @param serverPort
	 */
	public SMSConnection(String brokerAddress, int brokerPort) {
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
	public SMSConnection(String brokerAddress, int brokerPort,
			long reconnectDelay, TimeUnit reconnectDelayUnit) {
		this.brokerAddress = checkNotNull(brokerAddress,
				"brokerAddress is null");

		checkArgument(brokerPort > 0, "brokerPort must be positive");
		this.brokerPort = brokerPort;

		checkArgument(reconnectDelay > 0, "reconnectDelay must be positive");
		this.reconnectDelay = reconnectDelay;

		this.reconnectDelayUnit = checkNotNull(reconnectDelayUnit,
				"reconnectDelayUnit is null");
	}

	private void assertState(ConnectionState expectedState) {
		final ConnectionState localState = connectionState.get();
		checkState(localState == expectedState,
				"Expected current state = %s, actual current state = %s",
				expectedState, localState);
	}

	/**
	 * Register an SMSConnectionStateListener for this connection.
	 * 
	 * @param listener
	 */
	public void registerConnectionStateListener(
			SMSConnectionStateListener listener) {
		checkNotNull(listener, "listener is null");

		connectionStateListeners.add(listener);
	}

	/**
	 * Unregister an SMSConnectionStateListener for this connection.
	 * 
	 * @param listener
	 */
	public void unregisterConnectionStateListener(
			SMSConnectionStateListener listener) {
		checkNotNull(listener, "listener is null");

		connectionStateListeners.remove(listener);
	}

	/**
	 * Start the SMSConnection. Initiates a connection attempt to the SMS
	 * Broker.
	 */
	public void start() {
		checkState(connectionState.compareAndSet(ConnectionState.NOT_STARTED,
				ConnectionState.RUNNING), "Invalid state for start");

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		reconnectAsync(0, TimeUnit.SECONDS);
	}

	/**
	 * Is the SMSConnection started?
	 * 
	 * @return true if started, false otherwise
	 */
	public boolean isStarted() {
		return (connectionState.get() == ConnectionState.RUNNING);
	}

	private void reconnectAsync(long delay, TimeUnit delayUnit) {
		if (!isStarted()) {
			return;
		}

		eventLoopGroup
				.schedule(
						() -> {
							if (isStarted()) {
								new Bootstrap()
										.group(eventLoopGroup)
										.channel(NioSocketChannel.class)
										.handler(
												new SMSProtocolChannelInitializer(
														ClientHandler::new,
														SMSProtocol.BrokerToClientMessage
																.getDefaultInstance()))
										.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
												1000)
										.connect(brokerAddress, brokerPort);
							}
						}, delay, delayUnit);
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

	/**
	 * Subscribe to a topic to begin receiving messages from it.
	 * 
	 * @param topicName
	 *            topic name
	 * @param messageListener
	 *            message listener
	 */
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

	/**
	 * Unsubscribe from a topic to stop receiving messages from it
	 * 
	 * @param topicName
	 *            topic name
	 */
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

	/**
	 * Write a message to a topic asynchronously.
	 * 
	 * This method makes a copy of the message, so it is safe for the client to
	 * to reuse it.
	 * 
	 * If this SMSConnection is not currently connected to an SMS Broker, the
	 * message will be silently dropped.
	 * 
	 * @param topicName
	 *            topic name
	 * @param message
	 *            message payload
	 */
	public void writeToTopic(String topicName, ByteString message) {
		checkNotNull(topicName, "topicName is null");
		checkArgument(topicName.length() > 0, "topicName is empty");
		checkNotNull(message, "message is null");
		assertState(ConnectionState.RUNNING);

		connectedChannels.writeAndFlush(SMSProtocol.ClientToBrokerMessage
				.newBuilder()
				.setMessageType(
						ClientToBrokerMessageType.CLIENT_SEND_MESSAGE_TO_TOPIC)
				.setTopicName(topicName).setMessagePayload(message));
	}

	/**
	 * Destroy this SMSConnection. Close the connection to the SMS Broker and
	 * destroy all resources.
	 * 
	 * This SMSConnection must not be used after destroy is called.
	 * 
	 * It is the user's responsibility to call destroy on all SMSConnections
	 * created.
	 */
	public void destroy() {
		synchronized (destroyLock) {
			if (!connectionState.compareAndSet(ConnectionState.RUNNING,
					ConnectionState.DESTROYED)) {
				return;
			}

			connectionStateListeners.clear();

			subscribedTopicToListener.clear();

			allChannels.close();
		}
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
			fireMessageListenerCallback(listener,
					l -> l.handleIncomingMessage(message.getMessagePayload()));
		}
	}

	private void fireConnectionStateListenerCallback(
			Consumer<SMSConnectionStateListener> callback) {
		connectionStateListeners.forEach(listener -> {
			try {
				callback.accept(listener);
			} catch (Exception e) {
				log.warn("fireConnectionStateListenerCallback", e);
			}
		});
	}

	private void fireMessageListenerCallback(SMSMessageListener listener,
			Consumer<SMSMessageListener> callback) {
		try {
			callback.accept(listener);
		} catch (Exception e) {
			log.warn("fireMessageListenerCallback", e);
		}
	}
}
