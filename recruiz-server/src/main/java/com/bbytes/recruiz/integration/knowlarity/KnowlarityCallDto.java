package com.bbytes.recruiz.integration.knowlarity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class KnowlarityCallDto implements Serializable {

	private static final long serialVersionUID = -6116364643249538856L;
	
	@JsonProperty("candidateName")
	private String candidateName;
	
	@JsonProperty("candidateEmail")
	private String candidateEmail;
	
	@JsonProperty("candidateMobile")
	private String candidateMobile;
	
	@JsonProperty("positionId")
	private String positionId;
	
	@JsonProperty("prospectId")
	private String prospectId;

}
