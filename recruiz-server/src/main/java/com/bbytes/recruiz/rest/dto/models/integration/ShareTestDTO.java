package com.bbytes.recruiz.rest.dto.models.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ShareTestDTO {

	private String testId;

	private String qnLink;

	private Boolean random = true;

	private String questionSetId;

	private String recruizTenant;

	private String positionCode;

	private List<String> recruizCandidateMailIds = new ArrayList<>();

	private List<String> recruizNotifyMailIds = new ArrayList<>();

	Map<String, String> recruizCandidateProfileMap = new HashMap<>();

}