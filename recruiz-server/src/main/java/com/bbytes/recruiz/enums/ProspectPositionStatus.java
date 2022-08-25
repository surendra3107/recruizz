package com.bbytes.recruiz.enums;

public enum ProspectPositionStatus {

	Pending("pending"), Processed("processed");

	String displayName;

	private ProspectPositionStatus(String displayName) {
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
