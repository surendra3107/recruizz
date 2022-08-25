package com.bbytes.recruiz.rest.dto.models;

import java.util.List;

import lombok.Data;

@Data
public class OfferLetterPositionDTO {

	long positionId;
	String PositionName;
	int pendingCount;
	int approvedCount;
	int rejectedCount;
	int offerLetterGenerationCount;
	List<OfferCandidateRepotDTO> candidateList;
}
