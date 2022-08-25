package com.bbytes.recruiz.rest.dto.models.integration;

import lombok.Data;

@Data
public class ShareTestResultDTO {

	private String testId;

	private String candidateEmail;

	private String expectedScore;

	private String obtainedScore;

	private String positionCode;

	private String orgId;
	
	private String status;
}