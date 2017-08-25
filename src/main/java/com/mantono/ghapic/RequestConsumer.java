package com.mantono.ghapic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestConsumer implements Callable<Response>
{
	private final Request request;
	private final AccessToken accessToken;

	public RequestConsumer(final AccessToken token, final Request request)
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
			connection.setRequestProperty("Authorization", "token " + accessToken.getToken());
			connection.setRequestProperty("User-Agent", "ghapic");
			connection.setRequestProperty("Accept", "application/vnd.github.v3.text+json");
			connection.setDoOutput(true);

			connection.connect();

			final Map<String, List<String>> header = extractHeader(connection);
			final JsonNode body = parseBody(connection);
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

	private JsonNode parseBody(HttpsURLConnection connection) throws IOException
	{

		InputStream input = connection.getInputStream();
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.readTree(input);
		return node;
	}

	private Map<String, List<String>> extractHeader(HttpsURLConnection connection)
	{
		return connection.getHeaderFields();
	}
}
