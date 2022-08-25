package com.bbytes.recruiz.enums.integration;

import java.util.HashMap;
import java.util.Map;

public enum AdvancedSearchNoticePeriod {

	Any(0, "Any", 0, 1000), Currently_Serving_Notice_Period(-20,
			"Currently Serving Notice Period", 0, 0), Fiftin_Days_or_less(15,
					"15 Days or less", 0, 15), Thirty_Days(30, "30 Days", 0, 30),
					Sixty_Days(60, "60 Days", 0, 60), Ninty_Days(90, "90 Days", 0, 90), More_than_3_Months(180, "More than 3 Months", 91, 180);
	
	//Old code - Has even monster related dropdowns
//	Any(0, "Any(N)", 0, 1000), Immediate(-10, "Immediate(M)", 0, 0), Currently_Serving_Notice_Period(-20,
//			"Currently Serving Notice Period(N)", 0, 0), Seven_Days(7, "7 Days(M)", 0, 7), Fiftin_Days_or_less(15,
//					"15 Days or less", 0, 15), Thirty_Days(30, "30 Days", 0, 30), FourtyFive_Days(45, "45 Days(M)", 0,
//							45), Sixty_Days(60, "60 Days", 0, 60), Ninty_Days(90, "90 Days", 0, 90), More_than_3_Months(
//									180, "More than 3 Months(N)", 91, 180), OneHundred_Eighty_Days(180, "180 Days(M)",
//											0, 180), ThreeHundred_SixtyFive_Days(365, "365 Days(M)", 0, 365);

	private final int code;
	private final String displayName;
	private Integer daysFrom;
	private Integer daysTo;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	AdvancedSearchNoticePeriod(int code, String displayName, Integer daysFrom, Integer daysTo) {
		this.code = code;
		this.displayName = displayName;
		this.setDaysFrom(daysFrom);
		this.setDaysTo(daysTo);
	}

	static {
		for (AdvancedSearchNoticePeriod noticePeriod : AdvancedSearchNoticePeriod.values()) {
			map.put(noticePeriod.code, noticePeriod);
		}
	}

	public static AdvancedSearchNoticePeriod valueOf(int code) {
		return (AdvancedSearchNoticePeriod) map.get(code);
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public Integer getDaysFrom() {
		return daysFrom;
	}

	public void setDaysFrom(Integer daysFrom) {
		this.daysFrom = daysFrom;
	}

	public Integer getDaysTo() {
		return daysTo;
	}

	public void setDaysTo(Integer daysTo) {
		this.daysTo = daysTo;
	}

}