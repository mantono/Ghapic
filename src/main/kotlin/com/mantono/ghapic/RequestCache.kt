package com.mantono.ghapic

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class RequestCache
{
	private val cachedResponses: ConcurrentHashMap<Request, Future<Response>> = ConcurrentHashMap(100)
	private val cacheTimestamps: ConcurrentHashMap<Request, Long> = ConcurrentHashMap(100)
	val size: Int = cachedResponses.size

	fun save(request: Request, response: Future<Response>): Long
	{
		cachedResponses.put(request, response)
		return cacheTimestamps.put(request, System.currentTimeMillis()) ?: -1L
	}

	fun cachedResponse(request: Request): Future<Response>? = cachedResponses[request]

	fun isCached(request: Request): Boolean = cachedResponses.containsKey(request)

	fun isCached(request: Request, maxAge: Long, time: TimeUnit): Boolean
	{
		if (!isCached(request))
			return false

		val now = System.currentTimeMillis()
		val cacheTimestamp = cacheTimestamps[request] ?: 0
		val diff = now - cacheTimestamp
		val maxAgeMillis = time.toMillis(maxAge)
		return diff < maxAgeMillis
	}

	fun clear(): Int
	{
		val size = size()
		cachedResponses.clear()
		cacheTimestamps.clear()
		return size
	}

	fun clear(maxAge: Long, time: TimeUnit): Int
	{
		val sizeBefore = size()

		cacheTimestamps.asSequence()
				.filter { isOld(it.value, maxAge, time) }
				.map { it.key }
				.forEach {
					cachedResponses.remove(it)
					cacheTimestamps.remove(it)
				}

		val sizeAfter = size()
		return sizeBefore - sizeAfter
	}

	private fun isOld(creationTime: Long, maxAge: Long, time: TimeUnit): Boolean
	{
		val threshold = time.toMillis(maxAge)
		val diff = System.currentTimeMillis() - creationTime
		return diff > threshold
	}

	fun size(): Int = cachedResponses.size
}