package com.mantono.ghapic;

import java.net.MalformedURLException;
import java.net.URL;

public class Request
{
	public final static String API_URL = "https://api.github.com/";
	private final Verb method;
	private final URL url;
	
	public Request(Verb method, String resource) throws MalformedURLException
	{
		this.method = method;
		if(resource.contains(API_URL))
			resource = resource.replaceAll(API_URL, "");
		this.url = new URL(API_URL + resource);
	}

	public Verb getMethod()
	{
		return method;
	}
	
	public URL getUrl()
	{
		return url;
	}
}
