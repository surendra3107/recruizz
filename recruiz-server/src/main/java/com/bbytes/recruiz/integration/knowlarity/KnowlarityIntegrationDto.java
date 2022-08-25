package com.bbytes.recruiz.integration.knowlarity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class KnowlarityIntegrationDto implements Serializable  {

	private static final long serialVersionUID = -6116364643249538856L;
	
	@JsonProperty("srNumber")
	private String srNumber;
	
	@JsonProperty("callerId")
	private String callerId;
	
	@JsonProperty("authorizationKey")
	private String authorizationKey;
	
	@JsonProperty("xApikey")
	private String xApikey;
	
}
