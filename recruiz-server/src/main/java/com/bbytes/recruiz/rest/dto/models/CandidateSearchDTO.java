package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class CandidateSearchDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String[] expectedCTC;

	private String[] totalExp;

	private String[] noticePeriod;

	private String[] empTypeList;

	private String[] statusList;

	private String[] currentLocationList;

	private String[] preferredLocationList;

	private String[] skills;
	
	private String[] actualSourceList;

	private String isServingNotice;

}
