package com.bbytes.recruiz.rest.dto.models.integration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MassMailSendRequest {

	public String subject;

	public String senderEmailId;

	public String jobDescription;

	public List<PortalCandidateMassEmail> portalCandidateEmails = new ArrayList<>();

}