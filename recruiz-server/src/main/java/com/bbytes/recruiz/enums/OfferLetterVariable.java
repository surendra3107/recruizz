package com.bbytes.recruiz.enums;

public enum OfferLetterVariable {

	
	text("text"), component("component"), deduction("deduction"), calculation("calculation"),
	monthlyGross("monthlyGross"), annuallyGross("annuallyGross"), annuallyDeductions("annuallyDeductions"), monthlyDeductions("monthlyDeductions"),
	annuallyCtc("annuallyCtc"), monthlyCtc("monthlyCtc"),finalCostMonthlyCtc("finalCostMonthlyCtc"),finalCostAnnuallyCtc("finalCostAnnuallyCtc");

	String displayName;

	private OfferLetterVariable(String displayName) {
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
