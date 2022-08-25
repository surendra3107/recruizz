package com.bbytes.recruiz.rest.dto.models;

import lombok.Data;

@Data
public class OfferCandidateRepotDTO {

	long candidateId;
	String candidateName;
	String candidateEmail;
	String rejectionReason;
	String offerStatus;
	String isOfferLetterSent;
	//String annuallyCTC;
	
}
