package com.bbytes.recruiz.enums.integration;

import java.util.HashMap;
import java.util.Map;

public enum SixthSenseSource {

	monster("Monster"), naukri("Naukri"), times("Times"), shine("Shine");

	private final String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (SixthSenseSource source : SixthSenseSource.values()) {
			map.put(source.name(), source);
		}
	}

	public static SixthSenseSource getSourceValue(String source) {
		return (SixthSenseSource) map.get(source);
	}

	SixthSenseSource(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}