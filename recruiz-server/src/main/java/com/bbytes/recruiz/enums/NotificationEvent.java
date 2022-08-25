package com.bbytes.recruiz.enums;

public enum NotificationEvent {

	HR_ADDED_TO_POSITION("HR added to position"), HR_REMOVED_FROM_POSITION(
			"HR removed from position"), POSITION_MODIFIED("Position Modified"), POSITION_STATUS_CHANEGD(
					"Position Status Changed"), CLIENT_MODIFIED("Client Modified"), CLIENT_STATUS_CHANGED(
							"Client Status Changed"), USER_ROLE_CHANGED("User Role Changed"), POSITION_CREATED(
									"Position Created"), CLIENT_DELETED("Client Deleted"), CANDIDATE_STATUS_CHANGED(
											"Candidate Status Changed"), CANDIDATE_DETAILS_UPDATED(
													"Candidate Details Changed"), CANDIDATE_DELETED(
															"Candidate Deleted"), CLIENT_ADDED("Client Created"), CLIENT_INTERVIEWER_REMOVED("Interviewer Removed"), CLIENT_DECISIONMAKER_REMOVED("Decision Maker Removed"), CLIENT_DECISIONMAKER_ADDED("Decision Maker added"), CLIENT_INTERVIEWER_ADDED("Interviewer Added");

	String displayName;

	private NotificationEvent(String displayName) {
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
