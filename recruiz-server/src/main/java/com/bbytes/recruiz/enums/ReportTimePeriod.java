package com.bbytes.recruiz.enums;

public enum ReportTimePeriod {

	Today(0, "Today"), Last_Week(7, "Last week"), Last_Month(30, "Last month"), Last_3_Months(90,
			"Last 3 months"), Last_6_Months(180, "Last 6 months"), Custom(-1, "Custom");

	Integer days;

	String displayName;

	ReportTimePeriod(int days, String displayName) {
		this.days = days;
		this.displayName = displayName;
	}

	/**
	 * @return the days
	 */
	public Integer getDays() {
		return days;
	}

	/**
	 * @param days
	 *            the days to set
	 */
	public void setDays(Integer days) {
		this.days = days;
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
