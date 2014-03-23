package org.aaron.sms.protocol;

/*
 * #%L
 * Simple Message Service Protocol
 * %%
 * Copyright (C) 2013 - 2014 Aaron Riekenberg
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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Objects;
import java.util.function.Supplier;

import com.google.protobuf.MessageLite;

public class SMSProtocolChannelInitializer extends ChannelInitializer<Channel> {

	private final Supplier<? extends ChannelHandler> handlerSupplier;

	private final MessageLite messagePrototype;

	public SMSProtocolChannelInitializer(
			Supplier<? extends ChannelHandler> handlerSupplier,
			MessageLite messagePrototype) {
		this.handlerSupplier = Objects.requireNonNull(handlerSupplier,
				"handlerSupplier is null");
		this.messagePrototype = Objects.requireNonNull(messagePrototype,
				"messagePrototype is null");
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		final ChannelPipeline p = ch.pipeline();
		p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));

		p.addLast("frameEncoder", new LengthFieldPrepender(
				SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES));

		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
				SMSProtocolConstants.MAX_MESSAGE_LENGTH_BYTES, 0,
				SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES, 0,
				SMSProtocolConstants.MESSAGE_HEADER_LENGTH_BYTES));

		p.addLast("protobufEncoder", new ProtobufEncoder());

		p.addLast("protobufDecoder", new ProtobufDecoder(messagePrototype));

		p.addLast("customHandler", handlerSupplier.get());
	}
}
