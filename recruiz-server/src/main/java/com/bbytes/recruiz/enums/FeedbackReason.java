package com.bbytes.recruiz.enums;

public enum FeedbackReason {

	COMMUNICATION_ISSUE("Communication Issue"), POOR_PERFORMANCE("Poor Performance"), NOT_RELEVANT(
			"Not Relevant"), OTHERS("Others");

	String displayName;

	private FeedbackReason(String displayName) {
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
