package com.mantono.ghapic

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Serializable

const val ENV_VARIABLE = "GITHUB_API_TOKEN"
private val TOKEN_REGEX = Regex("[\\da-f]{40}")

data class AccessToken(val token: String = findAccessToken()): Serializable
{
	init
	{
		if (!isValidFormat(token))
			throw IllegalArgumentException("Bad token format, expected hexadecimal token of length 40, but got: "
					+ token + " (length " + token.length + ")")
	}
}

fun isValidFormat(accessToken: String): Boolean = accessToken.matches(TOKEN_REGEX)
private fun findAccessToken(): String = System.getenv(ENV_VARIABLE) ?: readTokenFromFile()
private fun readTokenFromFile(): String = readTokenFromFile(File(".token"))

private fun readTokenFromFile(file: File): String
{
	return try
	{
		FileReader(file).use { fileReader ->
			val bfReader = BufferedReader(fileReader)
			val line = bfReader.readLine()
			isValidFormat(line)

			line
		}
	}
	catch (e: IOException)
	{
		e.printStackTrace()
		System.exit(1)
		"BadToken"
	}
}