package com.bbytes.recruiz.enums;

public enum OnBoardingCategory {

	BeforeJoining("Before Joining"),AfterJoining("After Joining");
	
	String displayName;

	private OnBoardingCategory(String displayName) {
		this.displayName = displayName;
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

}
