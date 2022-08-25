package com.bbytes.recruiz.utils;

import java.util.Formatter;

import javax.crypto.Mac;

import org.apache.commons.codec.digest.HmacUtils;

public class HMACUtil {

	public static boolean verifySignature(String signature, String token, String timestamp, String secret) {
		Mac mac = HmacUtils.getHmacSha256(secret.getBytes());
		byte[] signed = mac.doFinal((timestamp + token).getBytes());
		String signedAsString = toHexString(signed);
		return signedAsString.equals(signature);
	}

	private static String toHexString(byte[] bytes) {
		Formatter formatter = new Formatter();
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}
		String finalString = formatter.toString();
		formatter.close();
		return finalString;
	}

}