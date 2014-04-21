/*
 * SimpleCache
 * Copyright (C) 2008 Christian Schenk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.christianschenk.simplecache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a very simple implementation of an object cache.
 * 
 * @author Christian Schenk
 */
public class SimpleCache<T> {

	/** Objects are stored here */
	private final Map<String, T> objects;
	/** Holds custom expiration dates */
	private final Map<String, Long> expire;
	/** The default expiration date */
	private final long defaultExpire;
	/** Is used to speed up some operations */
	private final ExecutorService threads;

	/**
	 * Constructs the cache with a default expiration time for the objects of
	 * 100 seconds.
	 */
	public SimpleCache() {
		this(100);
	}

	/**
	 * Construct a cache with a custom expiration date for the objects.
	 * 
	 * @param defaultExpire
	 *            default expiration time in seconds
	 */
	public SimpleCache(final long defaultExpire) {
		this.objects = Collections.synchronizedMap(new HashMap<String, T>());
		this.expire = Collections.synchronizedMap(new HashMap<String, Long>());

		this.defaultExpire = defaultExpire;

		this.threads = Executors.newFixedThreadPool(256);
		Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(this.removeExpired(), this.defaultExpire / 2, this.defaultExpire, TimeUnit.SECONDS);
	}

	/**
	 * This Runnable removes expired objects.
	 */
	private final Runnable removeExpired() {
		return new Runnable() {
			public void run() {
				for (final String name : expire.keySet()) {
					if (System.currentTimeMillis() > expire.get(name)) {
						threads.execute(createRemoveRunnable(name));
					}
				}
			}
		};
	}

	/**
	 * Returns a runnable that removes a specific object from the cache.
	 * 
	 * @param name
	 *            the name of the object
	 */
	private final Runnable createRemoveRunnable(final String name) {
		return new Runnable() {
			public void run() {
				objects.remove(name);
				expire.remove(name);
			}
		};
	}

	/**
	 * Returns the default expiration time for the objects in the cache.
	 * 
	 * @return default expiration time in seconds
	 */
	public long getExpire() {
		return this.defaultExpire;
	}

	/**
	 * Put an object into the cache.
	 * 
	 * @param name
	 *            the object will be referenced with this name in the cache
	 * @param obj
	 *            the object
	 */
	public void put(final String name, final T obj) {
		this.put(name, obj, this.defaultExpire);
	}

	/**
	 * Put an object into the cache with a custom expiration date.
	 * 
	 * @param name
	 *            the object will be referenced with this name in the cache
	 * @param obj
	 *            the object
	 * @param expire
	 *            custom expiration time in seconds
	 */
	public void put(final String name, final T obj, final long expireTime) {
		this.objects.put(name, obj);
		this.expire.put(name, System.currentTimeMillis() + expireTime * 1000);
	}

	/**
	 * Returns an object from the cache.
	 * 
	 * @param name
	 *            the name of the object you'd like to get
	 * @param type
	 *            the type of the object you'd like to get
	 * @return the object for the given name and type
	 */
	public T get(final String name) {
		final Long expireTime = this.expire.get(name);
		if (expireTime == null) return null;
		if (System.currentTimeMillis() > expireTime) {
			this.threads.execute(this.createRemoveRunnable(name));
			return null;
		}
		return this.objects.get(name);
	}

	/**
	 * Convenience method.
	 */
	@SuppressWarnings("unchecked")
	public <R extends T> R get(final String name, final Class<R> type) {
		return (R) this.get(name);
	}
}