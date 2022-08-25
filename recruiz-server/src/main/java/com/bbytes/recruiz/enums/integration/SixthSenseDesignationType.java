package com.bbytes.recruiz.enums.integration;

public enum SixthSenseDesignationType {

	Current(1, "Current"), Previous(2, "Previous"), Current_Previous(3, "Current / Previous");

	private final int code;
	private final String displayName;

	SixthSenseDesignationType(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public int getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}