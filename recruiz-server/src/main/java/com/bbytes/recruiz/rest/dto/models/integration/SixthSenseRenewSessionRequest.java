package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Renew session request object to re-active the session
 * 
 * @author akshay
 *
 */
@Data
public class SixthSenseRenewSessionRequest implements Serializable {

	private static final long serialVersionUID = -1198066797417609531L;

	// sixth sense user name
	@JsonProperty("userName")
	private String userName;

	// sixth sense old session Id
	@JsonProperty("sessionToken")
	private String oldSessionToken;

}
