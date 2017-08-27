package com.mantono.ghapic;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Response
{
	private final Map<String, List<String>> headerFields;
	private final JsonNode body;
	
	public Response(final Map<String, List<String>> header, final JsonNode body)
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
	
	public JsonNode getBody()
	{
		return body;
	}
}
