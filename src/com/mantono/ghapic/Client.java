package com.mantono.ghapic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client
{
	private final WorkManager threadPool;
	private final String accessToken;
	
	public Client(final String accessToken)
	{
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
		this.accessToken = accessToken;
	}
	
	public boolean acceptsRequests()
	{
		return !threadPool.limitIsReached();
	}
	
	public Future<Response> submitRequest(final String resource) throws MalformedURLException, IOException
	{
		return submitRequest(new Request(Verb.GET, resource));
	}
	
	public Future<Response> submitRequest(final Verb method, final String resource) throws MalformedURLException, IOException
	{
		return submitRequest(new Request(method, resource));
	}
	
	public Future<Response> submitRequest(final Request request) throws IOException
	{
		final RequestConsumer consumer = new RequestConsumer(accessToken, request);
		return threadPool.submit(consumer);
	}
}
