package com.bbytes.recruiz.enums;

public enum AdvancedSearchType {

	advanceSearch("Advance Search"), basicSearch("Basic Search");

	private final String displayName;

	AdvancedSearchType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}