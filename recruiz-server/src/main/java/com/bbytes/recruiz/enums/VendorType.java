package com.bbytes.recruiz.enums;

public enum VendorType {

	RecruitmentFirm("Recruitment Firm");

	String displayName;

	private VendorType(String displayName) {
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
