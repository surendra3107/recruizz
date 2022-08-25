package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input session request object to active the session
 * 
 * @author akshay
 *
 */
@Data
public class SixthSenseUserCredential implements Serializable {

	private static final long serialVersionUID = -8266466646242518019L;

	// sixth sense user name
	@JsonProperty("userName")
	private String userName;

	// sixth sense password
	@JsonProperty("password")
	private String password;

}
