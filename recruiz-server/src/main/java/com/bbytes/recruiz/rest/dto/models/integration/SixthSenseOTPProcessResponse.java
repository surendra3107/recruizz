package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Input otp process response request object
 * 
 * @author akshay
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SixthSenseOTPProcessResponse extends SixthSenseAbstractResolve implements Serializable {

	private static final long serialVersionUID = 19410010994720095L;

	private SixthSenseMessageObject messageObject;

	// otp html
	@JsonProperty("OTPHTML")
	private String otpHtml;
	
	private boolean selected;
	
	private String sourceUserId;
	
	private String solveAttempt;

}
