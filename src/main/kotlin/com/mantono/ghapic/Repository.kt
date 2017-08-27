package com.mantono.ghapic

data class Repository(val owner: String, val name: String, val id: Int = -1)
{
	val path: String = "$owner/$name"
	override fun toString(): String = path
}
