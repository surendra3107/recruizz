package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Failure user data object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseFailedUserDTO implements Serializable {

	private static final long serialVersionUID = -8359247009554678440L;

	@JsonProperty("user")
	private String user;

	@JsonProperty("error")
	private String error;

}
