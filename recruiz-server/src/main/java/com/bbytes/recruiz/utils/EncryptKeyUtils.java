package com.bbytes.recruiz.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptKeyUtils {

	private final static String key = "rEcru2AesEnc0ing";

	static byte[] encryptedEm;

	public static String getEncryptedKey(String value) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			encryptedEm = encrypted;
		} catch (Exception ex) {
		}
		return Base64.encodeBase64URLSafeString(encryptedEm);
	}

	public static String getDecryptedKey(String encryptedValue) {
		String decryptedEmail="";
		try {
			Cipher cipher = Cipher.getInstance("AES");
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			byte[] bb = Base64.decodeBase64(encryptedValue.getBytes());
			decryptedEmail = new String(cipher.doFinal(bb));
		} catch (Exception ex) {
		}

		return decryptedEmail;
	}
}
