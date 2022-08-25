package com.bbytes.recruiz.enums;

public enum TaskState {

	YET_TO_START("Yet to start"), IN_PROGRESS("In Progress"), COMPLETED("Completed");

	String displayName;

	TaskState(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public TaskState getEnum(String displayName) {
		for (TaskState taskState : values()) {
			if (taskState.getDisplayName().equals(displayName))
				return taskState;
		}
		return TaskState.IN_PROGRESS;
	}

}
