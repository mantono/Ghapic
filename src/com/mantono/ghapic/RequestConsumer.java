package com.mantono.ghapic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class RequestConsumer implements Callable<Response>
{
	private final Request request;
	private final String accessToken;

	public RequestConsumer(final String token, final Request request)
	{
		this.accessToken = token;
		this.request = request;
	}

	@Override
	public Response call() throws Exception
	{
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
			
			final Map<String, List<String>> header = extractHeader(connection);
			final List<String> body = parseBody(connection);
			return new Response(header, body);
		}
		finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
	}

	private List<String> parseBody(HttpsURLConnection connection)
	{
		
		try(InputStream is = connection.getInputStream();)
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			List<String> body = new ArrayList<String>(); 
			String line;
			while((line = rd.readLine()) != null)
				body.add(line);

			return body;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, List<String>> extractHeader(HttpsURLConnection connection)
	{
		return connection.getHeaderFields();
	}
}