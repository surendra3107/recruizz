package com.bbytes.recruiz.rest.dto.models.integration;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortalCandidateMassEmail {

	public String source;

	public String emailKeys;

}