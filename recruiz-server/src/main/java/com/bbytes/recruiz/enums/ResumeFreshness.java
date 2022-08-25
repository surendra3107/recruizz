package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum ResumeFreshness {

	Three("3", "3", 0, 3), Seven("7", "7", 0, 7), Fifteen("15", "15", 0, 15), Thirty("30", "30", 0, 30), Ninety("90",
			"90", 0, 90), One_Hundred_Eighty("180", "180", 0, 180), Three_Hundred_SixtyFive("365", "365", 0,
					365), All("all", "All", 0, 1000), Four_to_Seven("4-7", "4-7", 4, 7), Eight_to_fifteen("8-15",
							"8-15", 8, 15), Sixteen_to_Thirty("16-30", "16-30", 16, 30), Thirty_One_to_Ninety("31-90",
									"31-90", 31, 90), NinetyOne_to_OneEighty("91-180", "91-180", 91,
											180), OneEightyOne_to_ThreeSixtyFive("181-365", "181-365", 181,
													365), ThreeSixtySix_to_FiveFourty("366-540", "366-540", 366, 540);

	private final String code;
	private final String displayName;
	private Integer daysFrom;
	private Integer daysTo;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (ResumeFreshness freshness : ResumeFreshness.values()) {
			map.put(freshness.code, freshness);
		}
	}

	public static ResumeFreshness getValueof(String code) {
		return (ResumeFreshness) map.get(code);
	}

	private ResumeFreshness(String code, String displayName, Integer daysFrom, Integer daysTo) {
		this.code = code;
		this.displayName = displayName;
		this.daysFrom = daysFrom;
		this.daysTo = daysTo;
	}

	public String getCode() {
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