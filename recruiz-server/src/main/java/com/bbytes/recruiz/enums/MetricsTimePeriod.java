package com.bbytes.recruiz.enums;

public enum MetricsTimePeriod {

	Daily(0, "day"), Weekly(7, "day"), BiWeekly(15, "day"), Monthly(365, "month");

	Integer days;

	String aggrType;

	/**
	 * @return the aggrType
	 */
	public String getAggrType() {
		return aggrType;
	}

	MetricsTimePeriod(int days, String aggrType) {
		this.days = days;
		this.aggrType = aggrType;
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

}
