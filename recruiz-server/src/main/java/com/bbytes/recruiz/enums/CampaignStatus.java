package com.bbytes.recruiz.enums;

public enum CampaignStatus {

	Completed("Completed"),Scheduled("Scheduled"),Stopped("Stopped"),NotScheduled("Not Scheduled");
	
	String displayName;

	private CampaignStatus(String displayName) {
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
