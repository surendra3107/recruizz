package com.bbytes.recruiz.rest.dto.models.integration;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SixthSenseCandidateDTO  {

	private String fullName;

	private String currentCompany = "N/A";

	private String currentTitle = "N/A";

	private String currentLocation = "N/A";

	private String highestQual = "N/A";

	private String totalExp;

	private String currentCtc;

	private String expectedCtc;

	private String preferredLocation = "N/A";

	private String source = "N/A";
	
	private Map<String, String> candidateInfo = new HashMap<String, String>();


}