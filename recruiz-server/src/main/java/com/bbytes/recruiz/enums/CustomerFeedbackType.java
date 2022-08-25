package com.bbytes.recruiz.enums;

public enum CustomerFeedbackType {
	Bugs("Bugs"), Enhancement("Enhancement"), Comments("Comments"),Others("Others");

	String type;

	private CustomerFeedbackType(String type) {
		this.type = type;
	}

	/**
	 * @return the unicode
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param unicode
	 *            the unicode to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
