package com.bbytes.recruiz.enums;

public enum RoundType {

	Source("Source"), Shortlist("Shortlist"), Internal("Internal"), General("General"), Online("Online"), Telephonic(
			"Telephonic"), FaceToFace("Face to Face"), HR("HR");

	String displayName;

	private RoundType(String displayName) {
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
