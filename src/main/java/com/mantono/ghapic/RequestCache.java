package com.mantono.ghapic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RequestCache
{
	private final ConcurrentHashMap<Request, Future<Response>> cachedResponses;
	private final ConcurrentHashMap<Request, Long> cacheTimestamps;

	public RequestCache()
	{
		this.cachedResponses = new ConcurrentHashMap<Request, Future<Response>>(100);
		this.cacheTimestamps = new ConcurrentHashMap<Request, Long>(100);
	}

	public long save(final Request request, final Future<Response> response)
	{
		cachedResponses.put(request, response);
		return cacheTimestamps.put(request, System.currentTimeMillis());
	}

	public Future<Response> getResponse(final Request request)
	{
		return cachedResponses.get(request);
	}

	public boolean isCached(final Request request)
	{
		return cachedResponses.containsKey(request);
	}

	public boolean isCached(final Request request, final long maxAge, final TimeUnit time)
	{
		if(!isCached(request))
			return false;

		final long now = System.currentTimeMillis();
		final long cacheTimestamp = cacheTimestamps.get(request);
		final long diff = now - cacheTimestamp;
		final long maxAgeMillis = time.toMillis(maxAge);
		return diff < maxAgeMillis;
	}

	public int clear()
	{
		final int size = size();
		cachedResponses.clear();
		cacheTimestamps.clear();
		return size;
	}

	public int clear(final long maxAge, final TimeUnit time)
	{
		final long maxAgeMillis = time.toMillis(maxAge);
		final int sizeBefore = size();
		Set<Request> oldRequests = findOldRequests(maxAgeMillis);
		cachedResponses.entrySet().removeAll(oldRequests);
		cacheTimestamps.entrySet().removeAll(oldRequests);
		final int sizeAfter = size();
		return sizeBefore - sizeAfter;
	}

	private Set<Request> findOldRequests(long maxAgeMillis)
	{
		final Set<Request> toRemove = new HashSet<Request>(cachedResponses.size());
		Iterator<Entry<Request, Long>> iter = cacheTimestamps.entrySet().iterator();

		while(iter.hasNext())
		{
			Entry<Request, Long> entry = iter.next();
			final long diff = System.currentTimeMillis() - entry.getValue();
			if(diff > maxAgeMillis)
				toRemove.add(entry.getKey());
		}

		return toRemove;
	}

	private int size()
	{
		return cachedResponses.size();
	}
}
