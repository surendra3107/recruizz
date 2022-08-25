package com.bbytes.recruiz.enums;

public enum ExpectedCTCRange {

	Below_1_LPA("Below 100,000", 0, 100000), 
	One_To_Two_LPA("100,000-200,000", 100000, 200000), 
	Two_To_Three_LPA("200,000-300,000", 200000, 300000), 
	Three_To_Four_LPA("300,000-400,000", 300000, 400000), 
	Four_To_Five_LPA("400,000-500,000", 400000, 500000), 
	Five_To_Eight_LPA("500,000-800,000", 500000, 800000), 
	Eight_To_Ten_LPA("800,000-100,0000", 800000, 1000000), 
	Ten_To_Fifteen_LPA("10,00,000-15,00,000", 1000000, 1500000), 
	Above_Fifteen_LPA("Above 15,00,000", 1500000, 10000000);

	String displayName;

	private double fromCTC;

	private double toCTC;

	private ExpectedCTCRange(String displayName, double fromCTC, double toCTC) {
		this.displayName = displayName;
		this.setFromCTC(fromCTC);
		this.setToCTC(toCTC);
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the fromCTC
	 */
	public double getFromCTC() {
		return fromCTC;
	}

	/**
	 * @param fromCTC
	 *            the fromCTC to set
	 */
	public void setFromCTC(double fromCTC) {
		this.fromCTC = fromCTC;
	}

	/**
	 * @return the toCTC
	 */
	public double getToCTC() {
		return toCTC;
	}

	/**
	 * @param toCTC
	 *            the toCTC to set
	 */
	public void setToCTC(double toCTC) {
		this.toCTC = toCTC;
	}

}
