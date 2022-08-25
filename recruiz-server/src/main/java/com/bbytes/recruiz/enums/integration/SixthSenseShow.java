package com.bbytes.recruiz.enums.integration;

public enum SixthSenseShow {

	All_Candidates(1, "All Candidates"), New_Resumes_Only(2, "New Resumes Only"), Modified_Candidates(3,
			"Modified Candidates");

	private final int code;
	private final String displayName;

	SixthSenseShow(int code, String displayName) {
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