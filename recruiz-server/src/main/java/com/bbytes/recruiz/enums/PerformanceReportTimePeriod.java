package com.bbytes.recruiz.enums;

public enum PerformanceReportTimePeriod {

	Last_Hour(-1, 1, "Last 1 Hour","1 HOUR"), Last_12Hours(-1, 12, "Last 12 Hours","12 HOUR"), Last_24Hours(-1, 24,
			"Last 24 Hours","24 HOUR"), Last_Week(7, -1, "Last week","7 DAY"), Last_Month(30, -1, "Last month","1 MONTH"), Last_3_Months(90, -1,
					"Last 3 months","3 MONTH"), Last_6_Months(180, -1, "Last 6 months","6 MONTH"), Last_12_Months(365, -1, "Last 12 months","12 MONTH"),Custom(-1, -1, "Custom","1 MONTH");
	
    
	Integer days;

	Integer hours;

	String displayName;
	
	String interval;



	PerformanceReportTimePeriod(int days, int hours, String displayName,String interval) {
		this.days = days;
		this.hours = hours;
		this.displayName = displayName;
		this.interval =interval;
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
	 * @param hours
	 *            the hours to set
	 */
	public void setHours(Integer hours) {
		this.hours = hours;
	}

	/**
	 * @return the hours
	 */
	public Integer getHours() {
		return hours;
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
	
	public String getInterval() {
		return interval;
	}

}
