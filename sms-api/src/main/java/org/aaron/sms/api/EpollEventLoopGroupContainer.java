package org.aaron.sms.api;

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

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;

import java.util.Optional;

class EpollEventLoopGroupContainer {

	private static final Optional<EpollEventLoopGroup> EVENT_LOOP_GROUP;

	static {
		if (Epoll.isAvailable()) {
			EVENT_LOOP_GROUP = Optional.of(new EpollEventLoopGroup());
		} else {
			EVENT_LOOP_GROUP = Optional.empty();
		}
	}

	private EpollEventLoopGroupContainer() {

	}

	public static boolean isPresent() {
		return EVENT_LOOP_GROUP.isPresent();
	}

	public static EpollEventLoopGroup get() {
		return EVENT_LOOP_GROUP.get();
	}

}
