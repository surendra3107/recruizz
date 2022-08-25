package com.bbytes.recruiz.enums.integration;

import java.util.HashMap;
import java.util.Map;

public enum SixthSenseJobStatus {

	Any(1, "Any"), FullTime(2, "Full Time"), PartTime(3, "Part Time"), Permanent(4, "Permanent"), Temporary(5, "Temporary"), Contract(6, "Contract");

	private final int code;
	private final String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	SixthSenseJobStatus(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	static {
		for (SixthSenseJobStatus status : SixthSenseJobStatus.values()) {
			map.put(status.code, status);
		}
	}

	public static SixthSenseJobStatus valueOf(int code) {
		return (SixthSenseJobStatus) map.get(code);
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}