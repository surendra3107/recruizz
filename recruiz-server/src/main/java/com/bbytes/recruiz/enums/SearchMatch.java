package com.bbytes.recruiz.enums;

public enum SearchMatch {

	Excellent_fit("Excellent fit"), Good_fit("Good fit"), Moderate_fit("Moderate fit");

	String displayText;

	private SearchMatch(String displayText) {
		this.displayText = displayText;
	}

	/**
	 * @return the gender
	 */
	public String getDisplayText() {
		return displayText;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(String displayText) {
		this.displayText = displayText;
	}

}
