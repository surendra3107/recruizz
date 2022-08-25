package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum EmployeeStatus {

	Active("Active"), MovedOut("Moved Out"),Sabbatical("Sabbatical");
	
	String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (EmployeeStatus status : EmployeeStatus.values()) {
			map.put(status.displayName, status);
		}
	}

	public static EmployeeStatus getValueByDisplayName(String displayName) {
		Object obj = map.get(displayName);
		if (obj != null)
			return (EmployeeStatus) map.get(displayName);
		return null;
	}

	private EmployeeStatus(String displayName) {
		this.displayName = displayName;
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
