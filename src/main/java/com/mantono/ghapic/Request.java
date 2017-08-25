package com.mantono.ghapic;

import java.io.Serializable;
import java.net.URL;

public interface Request extends Serializable
{
	Verb getMethod();
	URL getUrl();
}
