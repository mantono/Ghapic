package com.mantono.ghapic;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class AccessTokenTest
{
	private final static String VALID_TOKEN = "abcdef01234567489abcabcdef01234567489abc"; 
	
	@Test
	public void testCheckAccessTokenFormatWithValidToken()
	{
		assertEquals(40, VALID_TOKEN.length());
		assertTrue(AccessToken.isValidFormat(VALID_TOKEN));
	}
	
	@Test
	public void testCheckAccessTokenFormatTooShortToken()
	{
		final String shortToken = "123456789abc";
		assertFalse(shortToken.length() == 40);
		assertFalse(AccessToken.isValidFormat(shortToken));
	}
	
	@Test
	public void testCheckAccessTokenFormatNotHexadecimal()
	{
		final String noHex = "qwerty01234567489abcabcdef01234567489abc";
		assertEquals(40, noHex.length());
		assertFalse(AccessToken.isValidFormat(noHex));
	}
	
	@Test
	public void testGetAccessTokenFromFile()
	{
		final AccessToken token = new AccessToken(new File("test/validToken"));
		assertEquals(VALID_TOKEN, token.getToken());
	}
}
