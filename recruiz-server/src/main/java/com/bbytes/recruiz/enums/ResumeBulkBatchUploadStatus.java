package com.bbytes.recruiz.enums;

public enum ResumeBulkBatchUploadStatus {
	UPLOADING("Uploading"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), STOPPED("Stopped"), SCHEDULED("Scheduled");

	String displayName;

	private ResumeBulkBatchUploadStatus(String displayName) {
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
