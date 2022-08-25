package com.bbytes.recruiz.enums;

public enum IcsFileType {

	Interviewer("interviewer"), Candidate("candidate"), Task("task");

	String displayName;

	private IcsFileType(String displayName) {
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
