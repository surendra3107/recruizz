package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input for ATS API Secret Key object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseATSAPISecretKey implements Serializable {

	private static final long serialVersionUID = -2557929712089085567L;

	// client id refers as tenant id
	@JsonProperty("clientId")
	private String clientId;

	@JsonProperty("clientSecret")
	private String clientSecret;

}
