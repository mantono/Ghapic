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
	
	public Client(final String accessToken)
	{
		checkAccessTokenFormat(accessToken);
		this.accessToken = accessToken;
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
	}
	
	public Client(final File tokenFile)
	{
		final String fileToken = readTokenFromFile(tokenFile);
		this.accessToken = fileToken;
		checkAccessTokenFormat(accessToken);
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
		
	}

	public Client()
	{
		final String envToken = System.getenv("GITHUB_API_TOKEN");

		if(envToken != null)
		{
			this.accessToken = envToken;
		}
		else
		{
			final String fileToken = readTokenFromFile();
			this.accessToken = fileToken;
		}
		
		checkAccessTokenFormat(accessToken);
		
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
		this.threadPool = new WorkManager(2, 12, 2500, TimeUnit.MILLISECONDS, workQueue);
	}

	private String readTokenFromFile()
	{
		return readTokenFromFile(new File(".token"));
	}

	private String readTokenFromFile(final File file)
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
	
	private void checkAccessTokenFormat(String accessToken)
	{
		if(!accessToken.matches("[\\da-f]{40}"))
		{
			throw new IllegalArgumentException(
					"Bad token format, expected hexadecimal token of length 40, but got: "
							+ accessToken + " (length " + accessToken.length() + ")");
		}
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
