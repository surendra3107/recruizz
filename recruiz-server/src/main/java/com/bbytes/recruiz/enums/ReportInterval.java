package com.bbytes.recruiz.enums;

public enum ReportInterval {

	Last_Month("1 MONTH", "Last Month"),Last_3_Months("3 MONTH", "Last 3 months"), Last_6_Months("6 MONTH", "Last 6 months"), Last_12_Months("12 MONTH",
			"Last 12 months"), From_Start("fromStart", "From the start");

	String intervalValue;

	String displayName;

	ReportInterval(String intervalValue, String displayName) {
		this.intervalValue = intervalValue;
		this.displayName = displayName;
	}

	/**
	 * @return the intervalValue
	 */
	public String getIntervalValue() {
		return intervalValue;
	}

	/**
	 * @param intervalValue
	 *            the intervalValue to set
	 */
	public void setIntervalValue(String intervalValue) {
		this.intervalValue = intervalValue;
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
