package com.bbytes.recruiz.enums;

public enum UserType {

	Vendor("vendor"),APP("app"), DepartmentHead("department_head");

	String displayName;

	private UserType(String displayName) {
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
