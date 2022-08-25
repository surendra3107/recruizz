package com.bbytes.recruiz.enums;

/**
 * DASHBOARD means recruiz app user , STANDALONE is for external user 
 * @author BBytes
 *
 */
public enum WebMode {

	DASHBOARD("Dashboard"), STANDALONE("Standalone"),EXTERNAL_APP("ExternalApp");

	String displayName;

	private WebMode(String displayName) {
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
