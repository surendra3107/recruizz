package com.bbytes.recruiz.rest.dto.models;

import java.util.List;

import lombok.Data;

@Data
public class OfferLetterReportDTO {

	int totalRolloutCount;
	int totalPositionCount;
	int totalRejectOfferLetter;
	int totalAcceptOfferLetter;
	int totalPendingOfferLetter;
	int totalOfferLetterGeneration;
	List<OfferLetterPositionDTO>  offerLetterPositionReport;
}
