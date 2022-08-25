package com.bbytes.recruiz.enums;

public enum ViewUsageType {

	PER_DAY_VIEW("Views allowed per day", 1), PER_WEEK_VIEW("Views allowed per week", 7), PER_MONTH_VIEW("Views allowed per month",
			30), UNLIMITED_VIEW("Unlimited Views", -1);

	private final String displayName;

	private final int numberOfDays;

	ViewUsageType(String displayName, int numberOfDays) {
		this.displayName = displayName;
		this.numberOfDays = numberOfDays;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public int getNumberOfDays() {
		return this.numberOfDays;
	}

}