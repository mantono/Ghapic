package com.mantono.ghapic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Client
{
	private final WorkManager threadPool;
	private final String accessToken;
	private final RequestCache resourceCache, searchCache;
	private CacheSettings cache;

	public Client()
	{
		this(findAccessToken());
	}

	public Client(final File tokenFile)
	{
		this(readTokenFromFile(tokenFile));
	}

	public Client(final String accessToken)
	{
		checkAccessTokenFormat(accessToken);
		this.accessToken = accessToken;
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
		this.resourceCache = new RequestCache();
		this.searchCache = new RequestCache();
		this.cache = new CacheSettings();
	}

	public Client(final CacheSettings cache)
	{
		this();
		this.cache = cache;
	}

	private static String findAccessToken()
	{
		final String envToken = System.getenv("GITHUB_API_TOKEN");

		if(envToken != null)
			return envToken;
		else
			return readTokenFromFile();
	}

	private static String readTokenFromFile()
	{
		return readTokenFromFile(new File(".token"));
	}

	private static String readTokenFromFile(final File file)
	{
		try(final FileReader fileReader = new FileReader(file))
		{
			final BufferedReader bfReader = new BufferedReader(fileReader);
			final String line = bfReader.readLine();
			checkAccessTokenFormat(line);

			return line;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		return "BadToken";
	}

	private static void checkAccessTokenFormat(String accessToken)
	{
		if(!accessToken.matches("[\\da-f]{40}"))
		{
			throw new IllegalArgumentException("Bad token format, expected hexadecimal token of length 40, but got: "
					+ accessToken + " (length " + accessToken.length() + ")");
		}
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

		final RequestConsumer consumer = new RequestConsumer(accessToken, resource);
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

		final RequestConsumer consumer = new RequestConsumer(accessToken, query);
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
