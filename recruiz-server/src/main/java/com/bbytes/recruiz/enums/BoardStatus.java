package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum BoardStatus {

	YetToProcess("Yet To Process"), InProgress("In Progress"), Approved("Approved"), Selected("Selected"), Rejected("Rejected"), OnHold(
			"On Hold"), Duplicate("Duplicate"), DroppedOut("Dropped Out"), NotInterested("Not Interested"), Offered(
					"Offered"), OfferAccepted("Offer Accepted"), OfferDeclined("Offer Declined"), Joined(
							"Joined"), Waitlisted("Waitlisted"), Employee("Employee"), MovedOut("Moved Out"), NoShow("No Show");

	String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (BoardStatus status : BoardStatus.values()) {
			map.put(status.displayName, status);
		}
	}

	public static BoardStatus getValueByDisplayName(String displayName) {
		Object obj = map.get(displayName);
		if (obj != null)
			return (BoardStatus) map.get(displayName);
		return null;
	}

	private BoardStatus(String displayName) {
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
