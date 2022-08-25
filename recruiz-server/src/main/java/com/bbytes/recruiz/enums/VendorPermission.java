package com.bbytes.recruiz.enums;

public enum VendorPermission {

	PositionDetails("Position Details"), CandidateDetails("Candidate Details"), ViewBoard("View Board");

	String displayName;

	private VendorPermission(String displayName) {
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
