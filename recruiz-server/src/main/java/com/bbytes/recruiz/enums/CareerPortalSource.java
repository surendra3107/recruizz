package com.bbytes.recruiz.enums;

public enum CareerPortalSource {

	Naukri("Naukri"), Monster("Monster"), Hirist("Hirist"), Shine("Shine"), TimesJob("Times Job");

	String displayName;

	private CareerPortalSource(String displayName) {
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
