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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.aaron.sms.entities.SMSProtocolConstants;
import org.aaron.sms.entities.protobuf.SMSProtocol;
import org.aaron.sms.entities.protobuf.SMSProtocol.ClientToServerMessage.ClientToServerMessageType;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
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

	private final ChannelGroup allChannels = new DefaultChannelGroup();

	private final ChannelGroup connectedChannels = new DefaultChannelGroup();

	private final ExecutorService cachedThreadPool = Executors
			.newCachedThreadPool();

	private final ClientSocketChannelFactory clientSocketChannelFactory = new NioClientSocketChannelFactory(
			cachedThreadPool, cachedThreadPool, 1);

	private final ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(1);

	private final Set<String> subscribedTopics = Collections
			.synchronizedSet(new HashSet<String>());

	private final AtomicBoolean started = new AtomicBoolean(false);

	private final AtomicReference<SMSConnectionListener> listener = new AtomicReference<SMSConnectionListener>();

	private final String brokerAddress;

	private final int brokerPort;

	private class ClientHandler extends SimpleChannelUpstreamHandler {

		private final AtomicBoolean haveBeenConnected = new AtomicBoolean(false);

		public ClientHandler() {

		}

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			log.debug("channelOpen {}", e.getChannel());
			allChannels.add(e.getChannel());
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			log.debug("channelConnected {}", e.getChannel());
			connectedChannels.add(e.getChannel());
			haveBeenConnected.set(true);
			resubscribeToTopics();
			fireConnectionOpen();
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
			log.debug("channelClosed {}", e.getChannel());
			if (haveBeenConnected.get()) {
				fireConnectionClosed();
			}

			scheduledExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						reconnect();
					} catch (Exception e) {
						log.warn("run", e);
					}
				}
			}, 1, TimeUnit.SECONDS);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			if (log.isDebugEnabled()) {
				log.debug("exceptionCaught " + e.getChannel(), e.getCause());
			}
			e.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx,
				MessageEvent event) {
			try {
				log.debug("messageReceived from {} message = '{}'",
						event.getChannel(), event.getMessage());
				final SMSProtocol.ServerToClientMessage message = (SMSProtocol.ServerToClientMessage) event
						.getMessage();
				switch (message.getMessageType()) {
				case SERVER_TOPIC_MESSAGE_PUBLISH:
					fireMessageReceived(message.getTopicName(), message
							.getMessagePayload().toByteArray());
					break;
				}
			} catch (Exception e) {
				log.warn("messageReceived", e);
				event.getChannel().close();
			}
		}
	}

	private class ClientPipelineFactory implements ChannelPipelineFactory {

		public ClientPipelineFactory() {

		}

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			return Channels.pipeline(

			new LoggingHandler(InternalLogLevel.DEBUG),

			new LengthFieldPrepender(
					SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES),

			new LengthFieldBasedFrameDecoder(
					SMSProtocolConstants.MAX_MESSAGE_LENGTH_BYTES, 0,
					SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES, 0,
					SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES),

			new ProtobufEncoder(), new ProtobufDecoder(
					SMSProtocol.ServerToClientMessage.getDefaultInstance()),

			new ClientHandler());
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
		this.brokerAddress = brokerAddress;
		this.brokerPort = brokerPort;
	}

	/**
	 * Set an SMSConnectionListener for this connection. Only one
	 * SMSConnectionListener may be registered at a time.
	 * 
	 * @param listener
	 * @throws SMSException
	 *             if listener is null
	 */
	public void setListener(SMSConnectionListener listener) throws SMSException {
		if (listener == null) {
			throw new SMSException("listener is null");
		}
		this.listener.set(listener);
	}

	/**
	 * Start the SMSConnection. Initiates a connection attempt to the SMS
	 * Broker.
	 * 
	 * @throws SMSException
	 *             if the SMSConnection is already started
	 */
	public void start() throws SMSException {
		if (!started.compareAndSet(false, true)) {
			throw new SMSException("already started");
		}

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		reconnect();
	}

	private void reconnect() {
		if (!started.get()) {
			return;
		}

		final ClientBootstrap clientBootstrap = new ClientBootstrap(
				clientSocketChannelFactory);
		clientBootstrap.setPipelineFactory(new ClientPipelineFactory());
		clientBootstrap.setOption("remoteAddress", new InetSocketAddress(
				brokerAddress, brokerPort));
		clientBootstrap.setOption("connectTimeoutMillis", 1000);
		clientBootstrap.connect();
	}

	private void resubscribeToTopics() {
		if (!started.get()) {
			return;
		}

		synchronized (subscribedTopics) {
			log.debug("resubscribeToTopics {}", subscribedTopics);
			for (String topicName : subscribedTopics) {
				connectedChannels
						.write(SMSProtocol.ClientToServerMessage
								.newBuilder()
								.setMessageType(
										ClientToServerMessageType.CLIENT_SUBSCRIBE_TO_TOPIC)
								.setTopicName(topicName));
			}
		}
	}

	/**
	 * Subscribe to a topic to begin receiving messages from it.
	 * 
	 * @param topicName
	 *            topic name
	 * @throws SMSException
	 *             if topicName is null or empty, or if this SMSConnection has
	 *             not been started
	 */
	public void subscribeToTopic(String topicName) throws SMSException {
		if (topicName == null) {
			throw new NullPointerException("topicName is null");
		}
		if (topicName.isEmpty()) {
			throw new IllegalArgumentException("topicName is empty");
		}
		if (!started.get()) {
			throw new SMSException("not started");
		}

		subscribedTopics.add(topicName);
		connectedChannels.write(SMSProtocol.ClientToServerMessage
				.newBuilder()
				.setMessageType(
						ClientToServerMessageType.CLIENT_SUBSCRIBE_TO_TOPIC)
				.setTopicName(topicName));
	}

	/**
	 * Unsubscribe from a topic to stop receiving messages from it
	 * 
	 * @param topicName
	 *            topic name
	 * @throws SMSException
	 *             if topicName is null or empty, or if this SMSConnection has
	 *             not been started
	 */
	public void unsubscribeFromTopic(String topicName) throws SMSException {
		if (topicName == null) {
			throw new NullPointerException("topicName is null");
		}
		if (topicName.isEmpty()) {
			throw new IllegalArgumentException("topicName is empty");
		}
		if (!started.get()) {
			throw new SMSException("not started");
		}

		subscribedTopics.remove(topicName);
		connectedChannels
				.write(SMSProtocol.ClientToServerMessage
						.newBuilder()
						.setMessageType(
								ClientToServerMessageType.CLIENT_UNSUBSCRIBE_FROM_TOPIC)
						.setTopicName(topicName));
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
	 * @throws SMSException
	 *             if topicName is null or empty, if message is null, or if this
	 *             SMSConnection has not been started
	 */
	public void writeToTopic(String topicName, byte[] message)
			throws SMSException {
		if (topicName == null) {
			throw new NullPointerException("topicName is null");
		}
		if (topicName.isEmpty()) {
			throw new IllegalArgumentException("topicName is empty");
		}
		if (message == null) {
			throw new NullPointerException("message is null");
		}
		if (!started.get()) {
			throw new SMSException("not started");
		}

		connectedChannels.write(SMSProtocol.ClientToServerMessage
				.newBuilder()
				.setMessageType(
						ClientToServerMessageType.CLIENT_SEND_MESSAGE_TO_TOPIC)
				.setTopicName(topicName)
				.setMessagePayload(ByteString.copyFrom(message)));
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
		if (!started.compareAndSet(true, false)) {
			return;
		}

		scheduledExecutorService.shutdown();
		allChannels.close();
		clientSocketChannelFactory.releaseExternalResources();
		cachedThreadPool.shutdown();
	}

	private void fireConnectionOpen() {
		try {
			final SMSConnectionListener localListener = listener.get();
			if (localListener != null) {
				localListener.handleConnectionOpen();
			}
		} catch (Exception e) {
			log.warn("fireConnectionOpen", e);
		}
	}

	private void fireConnectionClosed() {
		try {
			final SMSConnectionListener localListener = listener.get();
			if (localListener != null) {
				localListener.handleConnectionClosed();
			}
		} catch (Exception e) {
			log.warn("fireConnectionClosed", e);
		}
	}

	private void fireMessageReceived(String topicName, byte[] message) {
		try {
			final SMSConnectionListener localListener = listener.get();
			if (localListener != null) {
				localListener.handleIncomingMessage(topicName, message);
			}
		} catch (Exception e) {
			log.warn("fireMessageReceived", e);
		}
	}
}
