package com.mantono.ghapic;

import java.net.MalformedURLException;
import java.net.URL;

public class Resource implements Request
{
	public final static String API_URL = "https://api.github.com";
	private final Verb method;
	private final URL url;
	
	public Resource(Verb method, String resource) throws MalformedURLException
	{
		this.method = method;
		if(resource.contains(API_URL))
			resource = resource.replaceAll(API_URL, "");
		else if(resource.charAt(0) != '/')
			resource = "/" + resource;
		this.url = new URL(API_URL + resource);
	}

	@Override
	public Verb getMethod()
	{
		return method;
	}
	
	@Override
	public URL getUrl()
	{
		return url;
	}
}
