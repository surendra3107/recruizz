package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class OfferLetterApprovalDataDTO implements Serializable {
	
	private static final long serialVersionUID = 909228259372068381L;
	
	private String clientName;
	
	private String positionName;
	
	private String candidateName;
	
	private String email;
	
	private double currentCTC;
	
	private String percentHike;
	
	private double ctcOffered;
	
	private String profitMargin;
	
	
}
