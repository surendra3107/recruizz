package com.bbytes.recruiz.enums;

public enum TemplateCategory {

	apply("Apply"),email("Email"), forward("Forward Profile"), interview("Interview Schedule"), interview_cancel(
			"Interview Cancel"), interview_schedule_hr(
					"Interview Schedule For HR"), interview_cancel_interviewer("Interview Cancel For Interviewer");

	String displayName;

	private TemplateCategory(String displayName) {
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
