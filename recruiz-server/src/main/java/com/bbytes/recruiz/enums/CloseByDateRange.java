package com.bbytes.recruiz.enums;

public enum CloseByDateRange {

	Today("Today", -1, 0), Next_7_Days("Next 7 Days", 0, 7), Next_14_Days("Next 14 Days", 0,
			14), Next_30_Days("Next 30 Days", 0, 30), More_Than_30_Days("More than 30 Days", 30, 365);

	String displayName;

	private Integer closeDaysFrom;

	private Integer closeDaysTo;

	private CloseByDateRange(String displayName, Integer closeDaysFrom, Integer closeDaysTo) {
		this.displayName = displayName;
		this.closeDaysFrom = closeDaysFrom;
		this.closeDaysTo = closeDaysTo;
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
	 * @return the closeDaysFrom
	 */
	public Integer getCloseDaysFrom() {
		return closeDaysFrom;
	}

	/**
	 * @param closeDaysFrom
	 *            the closeDaysFrom to set
	 */
	public void setCloseDaysFrom(Integer closeDaysFrom) {
		this.closeDaysFrom = closeDaysFrom;
	}

	/**
	 * @return the closeDaysTo
	 */
	public Integer getCloseDaysTo() {
		return closeDaysTo;
	}

	/**
	 * @param closeDaysTo
	 *            the closeDaysTo to set
	 */
	public void setCloseDaysTo(Integer closeDaysTo) {
		this.closeDaysTo = closeDaysTo;
	}

}
