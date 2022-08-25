package com.bbytes.recruiz.enums;

public enum PositionRequestStatus {

	Pending("Pending"), InProcess("In Process"), Rejected("Reject"), Closed("Closed"), OnHold("On Hold"), Removed(
			"Removed");

	String displayName;

	private PositionRequestStatus(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}
}
