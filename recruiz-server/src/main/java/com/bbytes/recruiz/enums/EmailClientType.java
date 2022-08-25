package com.bbytes.recruiz.enums;

public enum EmailClientType {

	Gmail("Gmail"),Yahoo("Yahoo"),Outlook("outlook.com"),Custom("Custom");

	String displayName;

	private EmailClientType(String displayName) {
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
