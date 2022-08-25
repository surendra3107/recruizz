package com.bbytes.recruiz.enums;

public enum AssesmentStatus {

	YetToAttempt("Not Taken"),Submited("Submited"),Evaluated("Evaluated");
	
	String displayName;

	private AssesmentStatus(String displayName) {
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
