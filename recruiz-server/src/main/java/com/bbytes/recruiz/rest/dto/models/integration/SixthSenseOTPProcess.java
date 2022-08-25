package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input otp process request object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseOTPProcess implements Serializable {

	private static final long serialVersionUID = -7606563364208080326L;

	// job portal source name
	@JsonProperty("source")
	private String source;

	// source user id
	@JsonProperty("sourceUserId")
	private String sourceUserId;

}
