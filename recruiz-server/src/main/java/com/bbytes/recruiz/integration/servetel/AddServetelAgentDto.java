package com.bbytes.recruiz.integration.servetel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AddServetelAgentDto implements Serializable  {
	
private static final long serialVersionUID = -6116364643249538856L;
	
	@JsonProperty("agentId")
	private String agentId;

	@JsonProperty("agentName")
	private String agentName;
	
	@JsonProperty("mobile")
	private String mobile;
	
	@JsonProperty("userId")
	private String userId;
	
}
