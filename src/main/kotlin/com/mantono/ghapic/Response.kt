package com.mantono.ghapic

import com.fasterxml.jackson.databind.JsonNode

class Response(val header: Map<String, List<String>>, val body: JsonNode)
{
	operator fun get(field: String): String? = header[field]?.get(0)
}
