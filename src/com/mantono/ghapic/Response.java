package com.mantono.ghapic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Response
{
	private final Map<String, String> headerFields;
	private final List<String> body;
	
	public Response(final Map<String, String> header, final List<String> body)
	{
		this.headerFields = Collections.unmodifiableMap(header);
		this.body = body;
	}
	
	public Map<String, String> getHeader()
	{
		return headerFields;
	}
	
	public String getHeaderField(final String field)
	{
		return headerFields.get(field);
	}
	
	public List<String> getBody()
	{
		return body;
	}
}
