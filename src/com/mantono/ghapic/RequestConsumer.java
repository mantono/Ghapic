package com.mantono.ghapic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class RequestConsumer implements Callable<String>
{
	private final Request request;
	private final String accessToken;
	private final Limit limit;

	public RequestConsumer(final Limit limit, final String token, final Request request)
	{
		this.limit = limit;
		this.accessToken = token;
		this.request = request;
	}

	@Override
	public String call() throws Exception
	{
		limit.sleep();
		HttpsURLConnection connection = null;
		try
		{
			connection = (HttpsURLConnection) request.getUrl().openConnection();
			connection.setRequestMethod(request.getMethod().name());
			connection.setRequestProperty("Authorization", "token " + accessToken);
			connection.setRequestProperty("User-Agent", "ghapic");
			connection.setRequestProperty("Accept", "application/vnd.github.v3.text+json");
			connection.setDoOutput(true);
			
			connection.connect();
			
			final String rateLimitRemaining = connection.getHeaderField("X-RateLimit-Remaining");
			final int remaining = Integer.parseInt(rateLimitRemaining);
			limit.setRemaining(remaining);
			
			final String resetTime = connection.getHeaderField("X-RateLimit-Reset");
			final int time = Integer.parseInt(resetTime);
			limit.setResetTime(time);
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		}
		finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
	}
}
