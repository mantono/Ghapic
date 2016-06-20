package com.mantono.ghapic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Client
{
	private final ThreadPoolExecutor threadPool;
	private final String accessToken;
	private final Limit limit;
	
	public Client(final String accessToken)
	{
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(50);
		this.threadPool = new ThreadPoolExecutor(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
		this.limit = new Limit(accessToken, workQueue);
		this.accessToken = accessToken;
	}
	
	public Future<String> submitRequest(final Verb method, final String resource) throws MalformedURLException, IOException
	{
		return submitRequest(new Request(method, resource));
	}
	
	public Future<String> submitRequest(final Request request) throws IOException
	{
		final RequestConsumer consumer = new RequestConsumer(limit, accessToken, request);
		return threadPool.submit(consumer);
	}
}
