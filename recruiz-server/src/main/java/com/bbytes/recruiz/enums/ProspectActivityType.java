package com.bbytes.recruiz.enums;

public enum ProspectActivityType {

	Added("Added"), Rejected("Rejected"), Converted("Converted"), Deleted("Deleted"), DetailsUpdated("Details Updated") , AddedPositionToProspect("Added Position To Prospect");

	String displayName;

	private ProspectActivityType(String displayName) {
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
