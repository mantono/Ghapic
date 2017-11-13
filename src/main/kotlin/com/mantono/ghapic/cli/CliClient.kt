package com.mantono.ghapic.cli

import com.mantono.ghapic.AccessToken
import com.mantono.ghapic.Client
import com.mantono.ghapic.Resource
import com.mantono.ghapic.Verb

suspend fun main(args: Array<String>)
{
    val token: AccessToken = AccessToken()
    val client: Client = Client(token)

    repl(client, readLine())
    System.exit(0)
}

suspend tailrec fun repl(client: Client, line: String?)
{
    if(line.isNullOrBlank() || line == "exit")
        return
    val resource = Resource(Verb.GET, line!!)
    client.submitRequest(resource).get().apply {
        println(header)
        println(body)
    }
    repl(client, line)
}
