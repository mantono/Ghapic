package com.mantono.ghapic

import java.net.URL

private const val API_URL: String = "https://api.github.com"

class Resource(override val method: Verb, resource: String): Request
{
	override val resource: URL

	init
	{
		val strRes: String = when(resource.contains(API_URL))
		{
			true -> resource.replace(API_URL.toRegex(), "")
			false -> "/" + resource
		}
		this.resource = URL(API_URL + strRes)
	}
}