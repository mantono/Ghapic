package com.mantono.ghapic

import java.net.URL
import java.util.HashMap

private const val API_URL = "https://api.github.com/search"

data class Search(val type: SearchType, val _query: String): Request
{
	val query: String
	get()
	{
		return when(_query.contains(API_URL))
		{
			true -> _query.replace(API_URL.toRegex(), "")
			false -> "/" + _query
		}
	}

	override val method: Verb = Verb.GET
	override val resource: URL
		get() = URL(API_URL + type.name.toLowerCase() + "?q=" + query + parseParameters() + sort())

	var order: SortOrder = SortOrder.ASC
	var sortBy: String? = null
	private val parameters = HashMap<String, CharSequence>()

	private fun sort(): String
	{
		return if (sortBy == null) "" else "&sort=" + sortBy + "&order=" + order.name.toLowerCase()
	}

	private fun parseParameters(): String
	{
		val para = StringBuilder("+")
		for (pair in parameters.entries)
			para.append(pair.key + ":" + pair.value + "+")

		para.deleteCharAt(para.length - 1)

		return para.toString()
	}

	fun setParameter(key: String, value: CharSequence)
	{
		parameters.put(key, value)
	}

	fun clearParameters()
	{
		parameters.clear()
	}
}