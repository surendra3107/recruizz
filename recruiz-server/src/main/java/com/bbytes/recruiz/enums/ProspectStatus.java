package com.bbytes.recruiz.enums;

public enum ProspectStatus {

	New("New"), Contacted("Contacted"), Interested("Interested"), Proposal("Proposal"), Negotiation(
			"Negotiation"), Onhold("On Hold"), Lost("Lost"), Won("Won");

	String displayName;

	private ProspectStatus(String displayName) {
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
