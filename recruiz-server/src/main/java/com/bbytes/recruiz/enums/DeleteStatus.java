package com.bbytes.recruiz.enums;

public enum DeleteStatus {

	Not_Applicable("N/A"), Delete_InProgress("Delete InProgress"), Deleted("Deleted");

	String displayName;

	private DeleteStatus(String displayName) {
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
