package com.bbytes.recruiz.enums.integration;

public enum SixthSensePPGDegreeType {

	FullTime(1, "Full Time"), PartTime(2, "Part Time"), Correspondence(3, "Correspondence");

	private final int code;
	private final String displayName;

	SixthSensePPGDegreeType(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}