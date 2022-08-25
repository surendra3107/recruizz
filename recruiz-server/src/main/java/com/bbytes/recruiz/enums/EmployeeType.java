package com.bbytes.recruiz.enums;

public enum EmployeeType {

	FullTime("Full Time"),PartTime("Part Time"), Temporary("Temporary"), Contract("Contract"),Internship("Internship");

	String displayName;

	private EmployeeType(String displayName) {
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
