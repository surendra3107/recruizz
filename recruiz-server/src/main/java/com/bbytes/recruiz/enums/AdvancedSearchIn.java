package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum AdvancedSearchIn {

	Entire_Resume(1, "Entire Resume"), Resume_Title_Key_Skills(2, "Resume Title & Key Skills"), Resume_Title(3,
			"Resume Title"), All(-1, "All"), Candidate_Id(-2, "candidateId");

	private final int code;
	private final String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (AdvancedSearchIn searchIn : AdvancedSearchIn.values()) {
			map.put(searchIn.code, searchIn);
		}
	}

	public static AdvancedSearchIn valueOf(int code) {
		return (AdvancedSearchIn) map.get(code);
	}

	AdvancedSearchIn(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}
	
	public String toString() {
		return this.displayName;
	}

}