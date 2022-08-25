package com.bbytes.recruiz.enums;

public enum Status {

	Active("Active"), OnHold("On Hold"), Closed("Closed"), StopSourcing("Stop Sourcing");

	String displayName;

	private Status(String displayName) {
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
