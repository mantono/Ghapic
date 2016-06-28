package com.mantono.ghapic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RequestCache
{
	private final Map<Request, Response> cachedResponses;
	private final Map<Request, Long> cacheTimestamps;
	private final Semaphore permission = new Semaphore(1);

	public RequestCache()
	{
		this.cachedResponses = new HashMap<Request, Response>(100);
		this.cacheTimestamps = new HashMap<Request, Long>(100);
	}

	public long save(final Request request, final Response response)
	{
		try
		{
			permission.acquire();
			cachedResponses.put(request, response);
			return cacheTimestamps.put(request, System.currentTimeMillis());
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			permission.release();
		}
		
		return -1;
	}

	public Response getResponse(final Request request)
	{
		return cachedResponses.get(request);
	}

	public boolean cacheExists(final Request request)
	{
		return cachedResponses.containsKey(request);
	}

	public boolean cacheExists(final Request request, final long maxAge, final TimeUnit time)
	{
		if(!cacheExists(request))
			return false;

		final long now = System.currentTimeMillis();
		final long cacheTimestamp = cacheTimestamps.get(request);
		final long diff = now - cacheTimestamp;
		final long maxAgeMillis = time.toMillis(maxAge);
		return diff < maxAgeMillis;
	}

	public int clear()
	{
		try
		{
			permission.acquire();
			final int size = size();
			cachedResponses.clear();
			cacheTimestamps.clear();
			return size;
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			permission.release();
		}

		return -1;
	}
	
	public int clear(final long maxAge, final TimeUnit time)
	{
		try
		{
			permission.acquire();
			final long maxAgeMillis = time.toMillis(maxAge);
			final int sizeBefore = size();
			Set<Request> oldRequests = findOldRequests(maxAgeMillis);
			cachedResponses.entrySet().removeAll(oldRequests);
			cacheTimestamps.entrySet().removeAll(oldRequests);
			final int sizeAfter = size();
			return sizeBefore - sizeAfter;
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			permission.release();
		}

		return -1;
	}

	private Set<Request> findOldRequests(long maxAgeMillis)
	{
		final Set<Request> toRemove = new HashSet<Request>(cachedResponses.size());
		Iterator<Entry<Request, Long>> iter  = cacheTimestamps.entrySet().iterator();
		
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
