package com.bbytes.recruiz.enums;

public enum Currency {

	Rupee("INR"),Dollar("USD"),Dirham("AED");
	//Rupee("\u20B9"),Dollar("\u0024"),Dirham("\u062f\u002e\u0625");

	String unicode;

	private Currency(String unicode) {
		this.unicode = unicode;
	}

	/**
	 * @return the unicode
	 */
	public String getUnicode() {
		return unicode;
	}

	/**
	 * @param unicode
	 *            the unicode to set
	 */
	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

}
