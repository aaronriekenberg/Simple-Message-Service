package org.aaron.sms.common.eventloop;

/*
 * #%L
 * Simple Message Service Common
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

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;

public class UnixEventLoopGroupContainer {

	private static final EventLoopGroup EVENT_LOOP_GROUP;

	private static final Class<? extends Channel> CLIENT_CHANNEL_CLASS;

	private static final Class<? extends ServerChannel> SERVER_CHANNEL_CLASS;

	static {
		if (Epoll.isAvailable()) {
			EVENT_LOOP_GROUP = EpollEventLoopGroupContainer.EVENT_LOOP_GROUP;
			CLIENT_CHANNEL_CLASS = EpollDomainSocketChannel.class;
			SERVER_CHANNEL_CLASS = EpollServerDomainSocketChannel.class;
		} else {
			throw new IllegalStateException("Epoll is not available",
					Epoll.unavailabilityCause());
		}
	}

	private UnixEventLoopGroupContainer() {

	}

	public static EventLoopGroup getEventLoopGroup() {
		return EVENT_LOOP_GROUP;
	}

	public static Class<? extends Channel> getClientChannelClass() {
		return CLIENT_CHANNEL_CLASS;
	}

	public static Class<? extends ServerChannel> getServerChannelClass() {
		return SERVER_CHANNEL_CLASS;
	}

}
