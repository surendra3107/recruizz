package com.bbytes.recruiz.rest.dto.models;

import com.bbytes.recruiz.domain.Position;

import lombok.Data;

@Data
public class OfferLetterStatusWiseReport {

	long approval_id;
	
	String approvalStatus;
	
	CandidateDuplicateDTO candidate;
	
	Position position;
	
}
