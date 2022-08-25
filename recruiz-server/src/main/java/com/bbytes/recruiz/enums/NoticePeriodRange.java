package com.bbytes.recruiz.enums;

public enum NoticePeriodRange {

	CurrentlyServing("Currently Serving", 0, 0), 
	Zero_To_TenDays("0-10 Days", 0, 10), 
	Eleven_To_TwentyDays("11-20 Days", 11, 20), 
	TwentyOne_To_ThirtyDays("21-30 Days", 21, 30), 
	ThirtyOne_To_SixtyDays("31-60 Days", 31, 60), 
	SixtyOne_To_NinteyDays("61-90 Days", 61, 90), 
	Above90Days("90+ Days", 90, 365);

	private String displayName;

	private Integer daysFrom;

	private Integer daysTo;

	private NoticePeriodRange(String displayName, Integer daysFrom, Integer daysTo) {
		this.displayName = displayName;
		this.setDaysFrom(daysFrom);
		this.setDaysTo(daysTo);

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

	/**
	 * @return the daysFrom
	 */
	public Integer getDaysFrom() {
		return daysFrom;
	}

	/**
	 * @param daysFrom the daysFrom to set
	 */
	public void setDaysFrom(Integer daysFrom) {
		this.daysFrom = daysFrom;
	}

	/**
	 * @return the daysTo
	 */
	public Integer getDaysTo() {
		return daysTo;
	}

	/**
	 * @param daysTo the daysTo to set
	 */
	public void setDaysTo(Integer daysTo) {
		this.daysTo = daysTo;
	}

	
}
