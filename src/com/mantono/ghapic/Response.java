package com.mantono.ghapic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Response
{
	private final Map<String, List<String>> headerFields;
	private final List<String> body;
	
	public Response(final Map<String, List<String>> header, final List<String> body)
	{
		this.headerFields = header;
		this.body = body;
	}
	
	public Map<String, List<String>> getHeader()
	{
		return Collections.unmodifiableMap(headerFields);
	}
	
	public String getHeaderField(final String field)
	{
		return headerFields.get(field).get(0);
	}
	
	public List<String> getBody()
	{
		return body;
	}
}
