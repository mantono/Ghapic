package com.mantono.ghapic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Client
{
	private final WorkManager threadPool;
	private final AccessToken token;
	private final RequestCache resourceCache, searchCache;
	private CacheSettings cache;



	public Client(final AccessToken token)
	{
		this.token = token;
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
		this.resourceCache = new RequestCache();
		this.searchCache = new RequestCache();
		this.cache = new CacheSettings();
	}

	public Client(final CacheSettings cache)
	{
		this(new AccessToken());
		this.cache = cache;
	}
	
	public Client()
	{
		this(new CacheSettings());
	}

	public boolean acceptsRequests()
	{
		return !threadPool.limitIsReached();
	}

	public Future<Response> submitRequest(final String resource) throws MalformedURLException, IOException
	{
		if(resource.matches("^/?search/"))
			throw new IllegalArgumentException("A search query was done, but only regular resouce requests are allowed.");
		return submitRequest(new Resource(Verb.GET, resource));
	}

	public Future<Response> submitRequest(final Verb method, final String resource) throws MalformedURLException, IOException
	{
		if(resource.matches("^/?search/"))
			throw new IllegalArgumentException("A search query was done, but only regular resouce requests are allowed.");
		return submitRequest(new Resource(method, resource));
	}
	
	public Future<Response> submitRequest(final SearchType type, final String query) throws MalformedURLException, IOException
	{
		return submitRequest(new Search(type, query));
	}

	public Future<Response> submitRequest(final Resource resource) throws IOException
	{
		if(useResourceCache())
			if(resourceCache.isCached(resource))
				return resourceCache.getResponse(resource);

		final RequestConsumer consumer = new RequestConsumer(token, resource);
		Future<Response> response = threadPool.submit(consumer);

		final boolean saveToCache = cache.resourcePolicy() != CachePolicy.NEVER;

		if(saveToCache)
			resourceCache.save(resource, response);

		return response;

	}

	public Future<Response> submitRequest(final Search query) throws IOException
	{
		if(useSearchCache())
			if(searchCache.isCached(query))
				return searchCache.getResponse(query);

		final RequestConsumer consumer = new RequestConsumer(token, query);
		Future<Response> response = threadPool.submit(consumer);

		final boolean saveToCache = cache.searchPolicy() != CachePolicy.NEVER;

		if(saveToCache)
			searchCache.save(query, response);

		return response;
	}

	private boolean useResourceCache()
	{
		final boolean alwaysCache = cache.resourcePolicy() == CachePolicy.ALWAYS;
		final boolean cacheThresholdReached = cache.resourcePolicy() == CachePolicy.THRESHOLD
				&& threadPool.remainingResourceRequests() < cache.resourceThreshold();
		return alwaysCache || cacheThresholdReached;
	}

	private boolean useSearchCache()
	{
		final boolean alwaysCache = cache.searchPolicy() == CachePolicy.ALWAYS;
		final boolean cacheThresholdReached = cache.searchPolicy() == CachePolicy.THRESHOLD
				&& threadPool.remainingSearchRequests() < cache.searchThreshold();
		return alwaysCache || cacheThresholdReached;
	}
}
