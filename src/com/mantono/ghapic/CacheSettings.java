package com.mantono.ghapic;

public class CacheSettings
{
	private final CachePolicy resource, search;
	private final int resourceThreshold, searchThreshold;

	public CacheSettings(final CachePolicy resource, final int resourceThreshold, final CachePolicy search, final int searchThreshold)
	{
		this.resource = resource;
		this.resourceThreshold = resourceThreshold;

		this.search = search;
		this.searchThreshold = searchThreshold;
	}

	public CacheSettings(final CachePolicy common, final int resourceThreshold, final int searchThreshold)
	{
		this.resource = this.search = common;
		this.resourceThreshold = resourceThreshold;
		this.searchThreshold = searchThreshold;
	}
	
	public CacheSettings(final CachePolicy common)
	{
		this.resource = this.search = common;
		this.resourceThreshold = WorkManager.HOURLY_RATE / 2;
		this.searchThreshold = WorkManager.MINUTE_SEARCH_RATE / 2;
	}

	public CacheSettings(final int resourceThreshold, final int searchThreshold)
	{
		this.resource = this.search = CachePolicy.THRESHOLD;
		this.resourceThreshold = resourceThreshold;
		this.searchThreshold = searchThreshold;
	}
	
	public CacheSettings()
	{
		this.resource = this.search = CachePolicy.THRESHOLD;
		this.resourceThreshold = WorkManager.HOURLY_RATE / 2;
		this.searchThreshold = WorkManager.MINUTE_SEARCH_RATE / 2;
	}

	public CachePolicy resourcePolicy()
	{
		return resource;
	}

	public CachePolicy searchPolicy()
	{
		return search;
	}

	public int resourceThreshold()
	{
		return resourceThreshold;
	}

	public int searchThreshold()
	{
		return searchThreshold;
	}
}
