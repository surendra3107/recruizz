package com.bbytes.recruiz.enums.integration;

import java.util.HashMap;
import java.util.Map;

public enum SixthSenseJobType {

	Any(1, "Any"), Permanent(2, "Permanent"), Temporary(3, "Temporary"), Contract(4, "Contract");

	private final int code;
	private final String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	SixthSenseJobType(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	static {
		for (SixthSenseJobType status : SixthSenseJobType.values()) {
			map.put(status.code, status);
		}
	}

	public static SixthSenseJobType valueOf(int code) {
		return (SixthSenseJobType) map.get(code);
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}