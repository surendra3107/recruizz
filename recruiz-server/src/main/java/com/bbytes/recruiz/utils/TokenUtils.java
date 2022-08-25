package com.bbytes.recruiz.utils;

import org.springframework.security.crypto.codec.Base64;

public final class TokenUtils {
	private TokenUtils() {
	}

	public static String encode(String token) {
		return new String(Base64.encode(token.getBytes()));
	}

	public static String decode(String token) {
		return new String(Base64.decode(token.getBytes()));
	}
}
