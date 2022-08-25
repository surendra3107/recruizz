package com.bbytes.recruiz.enums;

public enum AppUsageStatRange {

	OneHour(60, "1 hour"),Last12Hour(12*60, "12 hour"),OneDay(24*60, "24 Hours"), OneWeek(7*24*60, "One week"), OneMonth(30*24*60, "One month"), Last3Months(90*24*60,
			"3 months"), Last6Months(180*24*60, "6 months"), Custom(-1, "Custom");

	Integer intervalMinutes;

	String displayName;

	AppUsageStatRange(int intervalMinutes, String displayName) {
		this.intervalMinutes = intervalMinutes;
		this.displayName = displayName;
	}

	/**
	 * @return the days
	 */
	public Integer getIntervalMinutes() {
		return intervalMinutes;
	}

	/**
	 * @param intervalInMinutes
	 *            the days to set
	 */
	public void setIntervalMinutes(Integer intervalInMinutes) {
		this.intervalMinutes = intervalInMinutes;
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
