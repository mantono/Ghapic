package com.mantono.ghapic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.Callable
import javax.net.ssl.HttpsURLConnection

const val VERSION: String = "0.1" // TODO read this from build.gradle defined version instead
private val mapper: ObjectMapper = ObjectMapper()

class RequestConsumer(val token: AccessToken, val request: Request): Callable<Response>
{
	override fun call(): Response
	{
		val connection: HttpsURLConnection =  request.resource.openConnection() as HttpsURLConnection
		return connection.run {
			requestMethod = request.method.name
			setRequestProperty("Authorization", "token " + token.token)
			setRequestProperty("User-Agent", "Ghapic/v$VERSION")
			setRequestProperty("Accept", "application/vnd.github.v3.text+json")
			doOutput = true

			connect()
			val body: JsonNode = mapper.readTree(inputStream)
			Response(headerFields, body)
		}
	}
}