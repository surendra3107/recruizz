package com.bbytes.recruiz.enums;

public enum CandidateModificationRange {

    AlwaysUpdate(0, "Always Update"),Undefined(-1, "Never Update"),THIRTY_DAYS(30, "30 Days"), SIXTY_DAYS(60, "60 Days"), NINTY_DAYS(90, "90 Days");

    int rangeValue;
    String displayName;

    private CandidateModificationRange(int rangeValue, String displayName) {
	this.rangeValue = rangeValue;
	this.displayName = displayName;
    }

    public int getRangeValue() {
	return rangeValue;
    }

    public void setRangeValue(int rangeValue) {
	this.rangeValue = rangeValue;
    }

    public String getDisplayName() {
	return displayName;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

}
