package org.aaron.sms.common;

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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockUtils {

	private LockUtils() {

	}

	public static void doInReadLock(ReentrantReadWriteLock lock, Runnable r) {
		checkNotNull(lock, "lock is null");
		checkNotNull(r, "r is null");

		lock.readLock().lock();
		try {
			r.run();
		} finally {
			lock.readLock().unlock();
		}
	}

	public static void doInWriteLock(ReentrantReadWriteLock lock, Runnable r) {
		checkNotNull(lock, "lock is null");
		checkNotNull(r, "r is null");

		lock.writeLock().lock();
		try {
			r.run();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
