package com.bbytes.recruiz.utils;

import org.apache.commons.lang3.math.NumberUtils;

public class MathUtils {

	public static double round(double valueToRound, int numberOfDecimalPlaces) {
		double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
		double interestedInZeroDPs = valueToRound * multipicationFactor;
		return Math.round(interestedInZeroDPs) / multipicationFactor;
	}

	public static Double percentage(double valueToRound) {
		double multipicationFactor = Math.pow(10, 2);
		double interestedInZeroDPs = valueToRound * multipicationFactor;
		return Math.round(interestedInZeroDPs) / multipicationFactor;
	}

	public static Float percentage(Float valueToRound) {
		double multipicationFactor = Math.pow(10, 2);
		double interestedInZeroDPs = valueToRound * multipicationFactor;
		return (float) (Math.round(interestedInZeroDPs) / multipicationFactor);
	}

	public static Double currencySymbolConvertor(String str) {
		try {
			if (str != null) {
				str = str.trim();
				if (str.endsWith("K") || str.endsWith("k")) {
					str = str.replace("K", "").replace("k", "");
					if (NumberUtils.isNumber(str)) {
						Double result = NumberUtils.toDouble(str) * 1000;
						return result;
					}
				} else if (str.endsWith("L") || str.endsWith("l")) {
					str = str.replace("L", "").replace("l", "");
					if (NumberUtils.isNumber(str)) {
						Double result = NumberUtils.toDouble(str) * 100000;
						return result;
					}
				} else if (str.endsWith("Cr") || str.endsWith("CR") || str.endsWith("cr")) {
					str = str.replace("Cr", "").replace("CR", "").replace("cr", "");
					if (NumberUtils.isNumber(str)) {
						Double result = NumberUtils.toDouble(str) * 10000000;
						return result;
					}
				}

			}
		} catch (Exception e) {
			// do nothing
		}

		return -1D;
	}

	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}