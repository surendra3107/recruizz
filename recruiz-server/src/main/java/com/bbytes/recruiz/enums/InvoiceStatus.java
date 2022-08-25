package com.bbytes.recruiz.enums;

public enum InvoiceStatus {

	Pending("Pending"), Paid("Paid"), PartialPayment("Partial Payment"), OverDue("Over Due"), Archive("Archive");

	String displayName;

	private InvoiceStatus(String displayName) {
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
