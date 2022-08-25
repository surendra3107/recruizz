package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum EmployeeActivityType {

	OnBoardingTaskStatusChange("Task Status Changed");
    
	String displayName;

	private static Map<Object, Object> map = new HashMap<Object, Object>();

	static {
		for (EmployeeActivityType status : EmployeeActivityType.values()) {
			map.put(status.displayName, status);
		}
	}

	public static EmployeeActivityType getValueByDisplayName(String displayName) {
		Object obj = map.get(displayName);
		if (obj != null)
			return (EmployeeActivityType) map.get(displayName);
		return null;
	}

	private EmployeeActivityType(String displayName) {
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
