package com.mantono.ghapic

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.*

const val HOURLY_RATE: Int = 5000
const val MINUTE_SEARCH_RATE: Int = 30

class WorkManager(corePoolSize: Int = 4,
                  maximumPoolSize: Int = 2 * corePoolSize,
                  keepAliveTime: Long = 5000L,
                  unit: TimeUnit = TimeUnit.MILLISECONDS,
                  private val workQueue: BlockingQueue<Runnable>) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue)
{
	private var resetTime: Instant = Instant.now()
	private var searchResetTime: Instant = Instant.now()
	private var remainingRequests: Int = HOURLY_RATE
		get
	private var remainingSearchRequests: Int = MINUTE_SEARCH_RATE
		get

	override fun beforeExecute(t: Thread, r: Runnable)
	{
		super.beforeExecute(t, r)
		sleep()
		while(limitIsReached() || searchLimitIsReached())
			Thread.yield()
	}

	override fun afterExecute(r: Runnable, t: Throwable)
	{
		super.afterExecute(r, t)

		if (r !is Future<*>)
			return

		val future = r as Future<*>?
		val response: Response = future!!.get() as? Response ?: return

		val rateLimit = Integer.parseInt(response["X-RateLimit-Limit"])

		val remaining = Integer.parseInt(response["X-RateLimit-Remaining"])
		val resetTime = response["X-RateLimit-Reset"]
		val time = java.lang.Long.parseLong(resetTime)

		if (rateLimit == HOURLY_RATE)
		{
			this.remainingRequests = remaining
			setResetTime(time)
		}
		else if (rateLimit == MINUTE_SEARCH_RATE)
		{
			this.remainingSearchRequests = remaining
			setSearchResetTime(time)
		}


	}

	private fun sleepTime(): Long = (5300 / (remainingRequests - pendingRequests())).toLong()

	private fun sleep(): Long
	{
		sleepTime().run {
			if (this > 0)
				Thread.sleep(this)
			return this
		}
	}

	fun consumedRequests(): Int = HOURLY_RATE - remainingRequests
	fun consumedSearchRequests(): Int = MINUTE_SEARCH_RATE - remainingSearchRequests
	fun pendingRequests(): Int = workQueue.size
	fun limitIsReached(): Boolean = remainingRequests - pendingRequests() < 20
	fun searchLimitIsReached(): Boolean = remainingSearchRequests < 1

	/**
	 *
	 * @return the amount of seconds until the rate limit resets.
	 */
	fun timeUntilReset(): Long = Instant.now().until(resetTime, ChronoUnit.SECONDS as TemporalUnit)

	/**
	 *
	 * @return the amount of seconds until the rate limit for searches resets.
	 */
	fun timeUntilSearchReset(): Long = Instant.now().until(searchResetTime, ChronoUnit.SECONDS as TemporalUnit)

	private fun setResetTime(time: Long)
	{
		this.resetTime = Instant.ofEpochSecond(time)
	}

	private fun setSearchResetTime(time: Long)
	{
		this.searchResetTime = Instant.ofEpochSecond(time)
	}
}