package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input candidate profile request object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseCandidateProfileDTO implements Serializable {

	private static final long serialVersionUID = -7894743322455989874L;

	// sixth sense source name
	@JsonProperty("source")
	private String source;

	// sixth sense candidate profile URL
	@JsonProperty("profileURL")
	private String candidateProfileURL;

	// sixth sense candidate document URL
	@JsonProperty("documentURL")
	private String documentURL;

	// sixth sense candidate uiData URL
	@JsonProperty("uiDataURL")
	private String uiDataURL;

}
