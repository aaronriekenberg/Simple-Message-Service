package org.aaron.sms.broker;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aaron.sms.entities.SMSProtocolConstants;
import org.aaron.sms.entities.protobuf.SMSProtocol;
import org.aaron.sms.entities.protobuf.SMSProtocol.ServerToClientMessage.ServerToClientMessageType;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
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
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
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

public class SMSBrokerTCPServer {

	private static final Logger log = LoggerFactory
			.getLogger(SMSBrokerTCPServer.class);

	private final ChannelGroup allChannels = new DefaultChannelGroup();

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private ServerSocketChannelFactory serverSocketChannelFactory = null;

	private SMSTopicContainer topicContainer = null;

	private String listenAddress = "0.0.0.0";

	private int listenPort = 10001;

	private class ClientHandler extends SimpleChannelUpstreamHandler {

		public ClientHandler() {

		}

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			allChannels.add(e.getChannel());
			log.info("channelOpen {}", e.getChannel());
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			log.debug("channelConnected {}", e.getChannel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			if (log.isDebugEnabled()) {
				log.debug("exceptionCaught " + e.getChannel(), e.getCause());
			}
			e.getChannel().close();
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctopicContainertx,
				ChannelStateEvent e) {
			log.info("channelClosed {}", e.getChannel());
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx,
				MessageEvent event) {
			try {
				log.debug(
						"messageReceived from {} message = '{}'"
								+ event.getChannel(), event.getMessage());
				processIncomingMessage(event.getChannel(),
						(SMSProtocol.ClientToServerMessage) event.getMessage());
			} catch (Exception e) {
				log.warn("messageReceived", e);
				event.getChannel().close();
			}
		}
	}

	private class ServerPipelineFactory implements ChannelPipelineFactory {

		public ServerPipelineFactory() {

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
					SMSProtocol.ClientToServerMessage.getDefaultInstance()),

			new ClientHandler());
		}
	}

	public void init() {
		log.info("init");

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		serverSocketChannelFactory = new NioServerSocketChannelFactory(
				executor, executor);
		final ServerBootstrap serverBootstrap = new ServerBootstrap(
				serverSocketChannelFactory);
		serverBootstrap.setPipelineFactory(new ServerPipelineFactory());
		serverBootstrap.setOption("reuseAddress", Boolean.TRUE);
		final Channel serverChannel = serverBootstrap
				.bind(new InetSocketAddress(listenAddress, listenPort));
		allChannels.add(serverChannel);
		log.info("listening on " + serverChannel.getLocalAddress());
	}

	public void destroy() {
		log.info("destroy");

		allChannels.close();

		serverSocketChannelFactory.releaseExternalResources();

		executor.shutdown();
	}

	private void processIncomingMessage(Channel channel,
			SMSProtocol.ClientToServerMessage message) {
		switch (message.getMessageType()) {
		case CLIENT_SEND_MESSAGE_TO_TOPIC: {
			final String topicName = message.getTopicName();
			final SMSTopic topic = topicContainer.getTopic(topicName);
			topic.write(SMSProtocol.ServerToClientMessage
					.newBuilder()
					.setMessageType(
							ServerToClientMessageType.SERVER_TOPIC_MESSAGE_PUBLISH)
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

	public void setListenAddress(String listenAddress) {
		this.listenAddress = listenAddress;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public void setTopicContainer(SMSTopicContainer topicContainer) {
		this.topicContainer = topicContainer;
	}

}
