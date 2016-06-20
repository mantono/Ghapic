package com.mantono.ghapic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.BlockingQueue;

public class Limit
{
	private Instant limitResetTime = Instant.now();
	private int remainingRequests = -1;
	private final BlockingQueue<Runnable> workQueue;

	public Limit(String accessToken, BlockingQueue<Runnable> workQueue)
	{
		this.workQueue = workQueue;
	}
	
	public long sleepTime()
	{
		return 5300/(remainingRequests() - pendingRequests());
	}
	
	public long sleep()
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
		return 5000 - remainingRequests;
	}
	
	public int remainingRequests()
	{
		return remainingRequests;
	}
	
	public int pendingRequests()
	{
		return workQueue.size();
	}
	
	public boolean limitIsReached()
	{
		return remainingRequests() - pendingRequests() < 20;
	}

	public synchronized void setRemaining(int remaining)
	{
		this.remainingRequests = remaining;
	}
	
	/**
	 * 
	 * @return the amount of seconds until the rate limit resets.
	 */
	public long timeUntilReset()
	{
		return Instant.now().until(limitResetTime, (TemporalUnit) ChronoUnit.SECONDS);
	}
	
	public void setResetTime(long time)
	{
		this.limitResetTime = Instant.ofEpochSecond(time);
	}

}
