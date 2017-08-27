package com.mantono.ghapic

import java.io.IOException
import java.net.MalformedURLException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class Client(private val token: AccessToken, val cache: CacheSettings = CacheSettings())
{
	private val threadPool: WorkManager
	private val resourceCache: RequestCache = RequestCache()
	private	val searchCache: RequestCache = RequestCache()

	init
	{
		val workQueue = ArrayBlockingQueue<Runnable>(100)
		this.threadPool = WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue)
	}

	fun acceptsRequests(): Boolean = !threadPool.limitIsReached()

	fun submitRequest(resource: String): Future<Response>
	{
		if (resource.matches("^/?search/".toRegex()))
			throw IllegalArgumentException("A searchPolicy query was done, but only regular resouce requests are allowed.")
		return submitRequest(Resource(Verb.GET, resource))
	}

	@Throws(MalformedURLException::class, IOException::class)
	fun submitRequest(method: Verb, resource: String): Future<Response>
	{
		if (resource.matches("^/?search/".toRegex()))
			throw IllegalArgumentException("A searchPolicy query was done, but only regular resource requests are allowed.")
		return submitRequest(Resource(method, resource))
	}

	@Throws(MalformedURLException::class, IOException::class)
	fun submitRequest(type: SearchType, query: String): Future<Response>
	{
		return submitRequest(Search(type, query))
	}

	fun submitRequest(resource: Resource): Future<Response>
	{
		if (useResourceCache())
			if (resourceCache.isCached(resource))
				return resourceCache.cachedResponse(resource)

		val consumer = RequestConsumer(token, resource)
		val response = threadPool.submit(consumer)

		val saveToCache = cache.resourcePolicy != CachePolicy.NEVER

		if (saveToCache)
			resourceCache.save(resource, response)

		return response

	}

	fun submitRequest(query: Search): Future<Response>
	{
		if(useSearchCache())
			if(searchCache.isCached(query))
				return searchCache.cachedResponse(query)

		val consumer = RequestConsumer(token, query)
		val response = threadPool.submit(consumer)

		val saveToCache = cache.searchPolicy != CachePolicy.NEVER

		if (saveToCache)
			searchCache.save(query, response)

		return response
	}

	private fun useResourceCache(): Boolean
	{
		val alwaysCache = cache.resourcePolicy == CachePolicy.ALWAYS
		val cacheThresholdReached = cache.resourcePolicy == CachePolicy.THRESHOLD && threadPool.remainingResourceRequests() < cache.resourceThreshold
		return alwaysCache || cacheThresholdReached
	}

	private fun useSearchCache(): Boolean
	{
		val alwaysCache = cache.searchPolicy == CachePolicy.ALWAYS
		val cacheThresholdReached = cache.searchPolicy == CachePolicy.THRESHOLD && threadPool.remainingSearchRequests() < cache.searchThreshold
		return alwaysCache || cacheThresholdReached
	}
}