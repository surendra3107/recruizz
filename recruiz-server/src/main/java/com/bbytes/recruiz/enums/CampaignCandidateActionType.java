package com.bbytes.recruiz.enums;

public enum CampaignCandidateActionType {

	Delivered("Delivered"), Opened("Opened"), Clicked("Clicked"), PageOpened("Page Opened"), InterestedClick(
			"Interested Click"), NotInterestedClick("Not Interested Click"), ProfileUpdated("Profile Updated");

	String displayName;

	private CampaignCandidateActionType(String displayName) {
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
