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
public class SixthSensePortalCredentialDTO implements Serializable {

	private static final long serialVersionUID = 6857754235631568554L;

	// sixth sense login id
	@JsonProperty("sourceUserId")
	private String sourceUserId;

	// sixth sense password
	@JsonProperty("password")
	private String password;

	// email address which opt comes
	@JsonProperty("otpEmailID")
	private String otpEmailID;

	// password of email address which opt comes
	@JsonProperty("otpEmailPwd")
	private String otpEmailPwd;
	
	@JsonProperty("message")
	private String message;

}
