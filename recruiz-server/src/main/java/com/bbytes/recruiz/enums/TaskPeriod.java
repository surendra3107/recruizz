package com.bbytes.recruiz.enums;

public enum TaskPeriod {

	Today("Today"), Tomorrow("Tomorrow"), ThisWeek("This Week"), ThisMonth("This Month");

	String displayName;

	TaskPeriod(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


}
