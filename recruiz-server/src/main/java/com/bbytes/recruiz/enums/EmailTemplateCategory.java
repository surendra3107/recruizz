package com.bbytes.recruiz.enums;

public enum EmailTemplateCategory {

	interview("Candidate Interview"), email("Candidate Email"), interviewer("Interviewer Templates"), apply(
			"Application Responses"), forward("Forward Profile"), interview_schedule_hr(
					"HR Templates"), campaign("Campaign Templates"), email_candidate("Bulk Email"), prospect("prospect");

	String displayName;

	private EmailTemplateCategory(String displayName) {
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
