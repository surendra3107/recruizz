package com.bbytes.recruiz.enums;

public enum CampaignType {

	EmailReachOut("Email Reach out"), OnlineAssessment("Online Assessment"), IVRAssessment(
			"IVR Assessment"), PositionCampaign("Position Campaign"), ProfileEnrichment("Profile Enrichment");

	String displayName;

	private CampaignType(String displayName) {
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

	public static final String EmailReachOutConstant = "Email Reach out";
	public static final String OnlineAssessmentConstant = "Online Assessment";
	public static final String IVRAssessmentConstant = "IVR Assessment";
	public static final String PositionCampaignConstant = "Position Campaign";
	public static final String ProfileEnrichmentConstant = "Profile Enrichment";

}
