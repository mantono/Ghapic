package com.mantono.ghapic

enum class CachePolicy
{
	/**
	 * Always cache requests and their responses, no matter of how many
	 * remaining requests that are available.
	 */
	ALWAYS,

	/**
	 * Never cache. No cache will be saved and no cache will be looked up.
	 */
	NEVER,

	/**
	 * Write to cache all the time, but do not make look ups in the cache record
	 * until a certain threshold for the API rate limit request is reached.
	 */
	THRESHOLD
}
