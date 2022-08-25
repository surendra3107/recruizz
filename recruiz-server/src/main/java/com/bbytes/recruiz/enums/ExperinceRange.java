package com.bbytes.recruiz.enums;

public enum ExperinceRange {

	Zero_To_OneYear("0-1 Year", 0.0, 1.0), 
	One_To_TwoYears("1-2 Years", 1.0, 2.0), 
	Two_To_ThreeYears("2-3 Years", 2.0, 3.0), 
	Three_To_FourYears("3-4 Years", 3.0, 4.0), 
	Four_To_FiveYears("4-5 Years", 4.0, 5.0), 
	Five_To_EightYears("5-8 Years", 5.0, 8.0), 
	Eight_To_TenYears("8-10 Years", 8.0, 10.0), 
	Above10Years("10+ Years", 10.0, 100.0);

	private String displayName;

	private Double fromExp;

	private Double toExp;

	private ExperinceRange(String displayName, Double fromExp, Double toExp) {
		this.displayName = displayName;
		this.fromExp = fromExp;
		this.toExp = toExp;

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
	 * @return the fromExp
	 */
	public Double getFromExp() {
		return fromExp;
	}

	/**
	 * @param fromExp
	 *            the fromExp to set
	 */
	public void setFromExp(Double fromExp) {
		this.fromExp = fromExp;
	}

	/**
	 * @return the toExp
	 */
	public Double getToExp() {
		return toExp;
	}

	/**
	 * @param toExp
	 *            the toExp to set
	 */
	public void setToExp(Double toExp) {
		this.toExp = toExp;
	}

}
