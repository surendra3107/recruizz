package com.bbytes.recruiz.enums.integration;

// Year of passing 
public enum SixthSenseYOPType {

	UG(1, "UG"), PG(2, "PG"), PPG(3, "PPG"),;

	private final int code;
	private final String displayName;

	SixthSenseYOPType(int code, String displayName) {
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