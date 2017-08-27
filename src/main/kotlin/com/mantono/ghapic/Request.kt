package com.mantono.ghapic;

import java.io.Serializable;
import java.net.URL;

interface Request: Serializable
{
	val method: Verb
	val resource: URL
}
