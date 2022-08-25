package com.bbytes.recruiz.enums;

public enum EmploymentType {

	FullTime("Full Time"), PartTime("Part Time"), Temporary("Temporary"), Contract("Contract"),Internship("Internship");

	String displayName;

	private EmploymentType(String displayName) {
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
