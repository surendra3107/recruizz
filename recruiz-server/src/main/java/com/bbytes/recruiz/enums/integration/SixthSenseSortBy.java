package com.bbytes.recruiz.enums.integration;

public enum SixthSenseSortBy {

	Relevance(1, "Relevance"), Resume_Freshness(2, "Resume Freshness"), Last_Active_Date(3,
			"Last Active Date"), Experience(4,
					"Experience"), Most_Viewed(5, "Most Viewed"), Least_Viewed(6, "Least Viewed");

	private final int code;
	private final String displayName;

	SixthSenseSortBy(int code, String displayName) {
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