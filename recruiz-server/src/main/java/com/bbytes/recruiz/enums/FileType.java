package com.bbytes.recruiz.enums;

public enum FileType {

	Salary_Slip("Salary Slip"), Exit_Letter("Exit Letter"), Appointment_Letter("Appointment Letter"), Offer_In_Hand(
			"Offer In Hand"), Other_Docs("Other Docs"), New_Appointment_Letter(
					"New Appointment Letter"), Original_Resume("Original Resume"), Original_Converted_Resume(
							"Original Converted Resume"), Masked_Resume_Original(
									"Masked Resume Original"), Masked_Resume_Converted("Masked Resume Converted"), DP(
											"dp"), EMAIL_ATTACHMENT(
													"Email Attachement"), COVER_LETTER("Cover Letter"), JD("jd"),
														PAN_CARD("Pan card"),AADHAR_CARD("Aadhar card"),UPDATED_RESUME("Updated resume"),TENTH_EDU_DOC("Educational documents- 10th"),
														TWELETH_EDU_DOC("Educational documents- 12th"),DEGREE_DOC("Educational documents- Degree"),APPOINTMENT_LETTER("Employment documents- Appointment letter"),SALARY_SLIPS("Salary Slips"),RELIEVING_LETTER("Relieving letter"),
														      PASSPORT_PHOTOGRAPH("Scanned copy of passport size photograph"),STATEMENT_CHEQUE("Bank statement or cancelled Cheque"),ADDRESS_PROOF("Local and permanent address proof");

	String displayName;

	private FileType(String displayName) {
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
