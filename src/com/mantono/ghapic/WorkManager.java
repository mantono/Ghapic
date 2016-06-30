package com.mantono.ghapic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkManager extends ThreadPoolExecutor
{
	public static final int HOURLY_RATE = 5000;
	public static final int MINUTE_SEARCH_RATE = 30;
	private final BlockingQueue<Runnable> workQueue;
	private Instant limitResetTime = Instant.now();
	private Instant limitSearchResetTime = Instant.now();
	private int remainingRequests = HOURLY_RATE;
	private int remainingSearchRequests = MINUTE_SEARCH_RATE;

	public WorkManager(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, BlockingQueue<Runnable> workQueue)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.workQueue = workQueue;
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		sleep();
		while(limitIsReached() || searchLimitIsReached())
			Thread.yield();
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);
		try
		{
			if(!(r instanceof Future<?>))
				return;
			
			Future<?> future = (Future<?>) r;
			Object obj = future.get();
			
			if(!(obj instanceof Response))
				return;
			
			Response response = (Response) obj;
			
			final int rateLimit = Integer.parseInt(response.getHeaderField("X-RateLimit-Limit"));
			
			final int remaining = Integer.parseInt(response.getHeaderField("X-RateLimit-Remaining"));
			final String resetTime = response.getHeaderField("X-RateLimit-Reset");
			final long time = Long.parseLong(resetTime);
			
			if(rateLimit == HOURLY_RATE)
			{
				this.remainingRequests = remaining;
				setResetTime(time);
			}
			else if(rateLimit == MINUTE_SEARCH_RATE)
			{
				this.remainingSearchRequests = remaining;
				setSearchResetTime(time);
			}
		
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			e.printStackTrace();
		}
	}

	private long sleepTime()
	{
		return 5300/(remainingResourceRequests() - pendingRequests());
	}
	
	private long sleep()
	{
		final long sleep = sleepTime();
		try
		{
			if(sleep > 0)
				Thread.sleep(sleep);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return sleep;
	}
	
	public int consumedRequests()
	{
		return HOURLY_RATE - remainingRequests;
	}
	
	public int consumedSearchRequests()
	{
		return MINUTE_SEARCH_RATE - remainingSearchRequests;
	}
	
	public int remainingResourceRequests()
	{
		return remainingRequests;
	}
	
	public int remainingSearchRequests()
	{
		return remainingSearchRequests;
	}
	
	public int pendingRequests()
	{
		return workQueue.size();
	}
	
	public boolean limitIsReached()
	{
		return remainingResourceRequests() - pendingRequests() < 20;
	}
	
	public boolean searchLimitIsReached()
	{
		return remainingSearchRequests() < 1;
	}
	
	/**
	 * 
	 * @return the amount of seconds until the rate limit resets.
	 */
	public long timeUntilReset()
	{
		return Instant.now().until(limitResetTime, (TemporalUnit) ChronoUnit.SECONDS);
	}
	
	/**
	 * 
	 * @return the amount of seconds until the rate limit for searches resets.
	 */
	public long timeUntilSearchReset()
	{
		return Instant.now().until(limitSearchResetTime, (TemporalUnit) ChronoUnit.SECONDS);
	}
	
	private void setResetTime(long time)
	{
		this.limitResetTime = Instant.ofEpochSecond(time);
	}
	
	private void setSearchResetTime(long time)
	{
		this.limitSearchResetTime = Instant.ofEpochSecond(time);
	}
}
