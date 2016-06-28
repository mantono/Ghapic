package com.mantono.ghapic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Search implements Request
{
	public final static String API_URL = "https://api.github.com/search";
	private final Verb method = Verb.GET;
	private final SearchType type;
	private SortOrder order = SortOrder.ASC;
	private String sortBy;
	private String query;
	private Map<String, CharSequence> parameters = new HashMap<String, CharSequence>();

	public Search(final SearchType type, String query) throws MalformedURLException
	{
		if(query.contains(API_URL))
			query = query.replaceAll(API_URL, "");
		else if(query.charAt(0) != '/')
			query = "/" + query;
		this.query = query;
		this.type = type;
	}

	@Override
	public Verb getMethod()
	{
		return method;
	}

	@Override
	public URL getUrl()
	{
		try
		{
			final String parameterData = parseParameters();
			return new URL(API_URL + type.name().toLowerCase() + "?q=" + query + parameterData + sort());
		}
		catch(MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getQuery()
	{
		return query;
	}
	
	public void setQuery(String query)
	{
		this.query = query;
	}
	
	private String sort()
	{
		if(sortBy == null)
			return "";
		return "&sort=" + sortBy + "&order=" + order.name().toLowerCase();
	}
	
	public String getSortBy()
	{
		return sortBy;
	}
	
	public void setSortBy(final String field)
	{
		this.sortBy = field;
	}
	
	public SortOrder getSortingOrder()
	{
		return order;
	}
	
	public void setSortingOrder(final SortOrder order)
	{
		this.order = order;
	}

	private String parseParameters()
	{
		final StringBuilder para = new StringBuilder("+");
		for(Entry<String, CharSequence> pair : parameters.entrySet())
			para.append(pair.getKey() + ":" + pair.getValue() + "+");
		
		para.deleteCharAt(para.length()-1);
		
		return para.toString();
	}

	public void setParameter(final String key, final CharSequence value)
	{
		parameters.put(key, value);
	}
	
	public void clearParameters()
	{
		parameters.clear();
	}
	
}
