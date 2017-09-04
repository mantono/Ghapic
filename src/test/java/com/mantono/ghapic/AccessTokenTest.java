package com.mantono.ghapic;

import static com.mantono.ghapic.AccessTokenKt.isValidFormat;
import static com.mantono.ghapic.AccessTokenKt.readTokenFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

public class AccessTokenTest
{
	private final static String VALID_TOKEN = "abcdef01234567489abcabcdef01234567489abc"; 
	
	@Test
	public void testCheckAccessTokenFormatWithValidToken()
	{
		assertEquals(40, VALID_TOKEN.length());
		assertTrue(isValidFormat(VALID_TOKEN));
	}
	
	@Test
	public void testCheckAccessTokenFormatTooShortToken()
	{
		final String shortToken = "123456789abc";
		assertFalse(shortToken.length() == 40);
		assertFalse(isValidFormat(shortToken));
	}
	
	@Test
	public void testCheckAccessTokenFormatNotHexadecimal()
	{
		final String noHex = "qwerty01234567489abcabcdef01234567489abc";
		assertEquals(40, noHex.length());
		assertFalse(isValidFormat(noHex));
	}
	
	@Test
	public void testGetAccessTokenFromFile()
	{
		final String token = readTokenFromFile(new File("src/test/validToken"));
		assertEquals(VALID_TOKEN, token);
	}
}
