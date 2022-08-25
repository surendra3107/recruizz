package com.bbytes.recruiz.enums;

public enum CandidateActivityType {

	InterviewScheduled("Interview Scheduled"),InterviewReScheduled("Interview Rescheduled"), Email("Email"), Rejected("Rejected"), StatusChanged("Status Changed"), SourcedToBoard(
			"Sourced for Position"), Added("Added"), DetailsUpdated("Details Updated"), Forwarded(
					"Profile Forwarded"), RemovedFromBoard(
									"Removed From Pepeline"), InterviewCancelled(
											"Interview Cancelled"), MovedInBoard("Moved in Pipeline"), FeedbackRecieved("Feedback Recieved"), Deleted("Deleted"),PipelineStatusChanged("Status Changed in Pipeline"), SEND_PROFILE_FOR_APPROVAL("send profile for approval"), ACCEPTED_PROFILE("accepted profile"),REJECTED_PROFILE("rejected profile"),CALL_LOG("Call Log"),OWNERSHIP_CHANGED("Owner ship Changed");

	String displayName;

	private CandidateActivityType(String displayName) {
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
