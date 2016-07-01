package com.mantono.ghapic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

public class AccessToken implements Serializable
{
	public static final String ENV_VARIABLE = "GITHUB_API_TOKEN";
	private static final String TOKEN_REGEX = "[\\da-f]{40}";
	private final String token;

	public AccessToken()
	{
		this(findAccessToken());
	}

	public AccessToken(final String token)
	{
		if(!isValidFormat(token))
			throw new IllegalArgumentException("Bad token format, expected hexadecimal token of length 40, but got: "
					+ token + " (length " + token.length() + ")");
		this.token = token;
	}

	public AccessToken(final File tokenFile)
	{
		this(readTokenFromFile(tokenFile));
	}
	
	private static String findAccessToken()
	{
		final String envToken = System.getenv(ENV_VARIABLE);

		if(envToken != null)
			return envToken;
		else
			return readTokenFromFile();
	}

	private static String readTokenFromFile()
	{
		return readTokenFromFile(new File(".token"));
	}

	private static String readTokenFromFile(final File file)
	{
		try(final FileReader fileReader = new FileReader(file))
		{
			final BufferedReader bfReader = new BufferedReader(fileReader);
			final String line = bfReader.readLine();
			isValidFormat(line);

			return line;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		return "BadToken";
	}

	public static boolean isValidFormat(String accessToken)
	{
		return accessToken.matches(TOKEN_REGEX);
	}

	public String getToken()
	{
		return token;
	}
	
	@Override
	public String toString()
	{
		return getToken();
	}

}
