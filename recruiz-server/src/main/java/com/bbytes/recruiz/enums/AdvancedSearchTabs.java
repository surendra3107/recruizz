package com.bbytes.recruiz.enums;

public enum AdvancedSearchTabs {

	BASIC("Basic Search"), KEYWORD("Keyword Search"), BOOLEAN("Boolean Search");

	String displayName;

	private AdvancedSearchTabs(String displayName) {
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
