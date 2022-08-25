package com.bbytes.recruiz.enums;

public enum Source {
	// as per RCZ-784 removing ImportedByParser from enum
	Facebook("Facebook", true), Twitter("Twitter", true), Google("Google", true), LinkedIn("LinkedIn", true), Naukri("Naukri",
			true), Monster("Monster", true), Indeed("Indeed", true), Timesjobs("Timesjobs", true), Angel_co("Angel.co",
					true), Babajobs("Babajobs", true), iimjob("iimjob", true), CareersPortal("careersite",
							false), ExternalJobPortal("External Job Portal", false), Internal("Internal", false), DataImport("Data Import",
									false), EmployeeReferred("Employee Referred", false), EmailShare("Email Share", true), EmailSync(
											"Email Sync", true), ExistingDataSilo("Existing Data Silo", false), Others("Others", false);

	String displayName;

	Boolean isSocial;

	private Source(String displayName) {
		this.displayName = displayName;
	}

	private Source(String displayName, Boolean isSocial) {
		this.displayName = displayName;
		this.isSocial = isSocial;
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

	/**
	 * @return isSocial
	 */
	public Boolean getIsSocial() {
		return isSocial;
	}

	/**
	 * @param isSocial
	 *            Whether social share is allowed
	 */
	public void setIsSocial(Boolean isSocial) {
		this.isSocial = isSocial;
	}

	// get display name of given value
	public static String getDisplayNameOfStatus(String value) {
		for (Source source : Source.values()) {
			if (source.name().equalsIgnoreCase(value)) {
				return source.getDisplayName();
			}
		}
		return value;
	}

}
