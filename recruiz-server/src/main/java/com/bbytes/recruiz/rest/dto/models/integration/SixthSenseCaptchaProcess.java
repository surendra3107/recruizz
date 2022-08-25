package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input captcha image process request object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseCaptchaProcess implements Serializable {

	private static final long serialVersionUID = 2060610645006245210L;

	// sixth sense source name
	@JsonProperty("source")
	private String source;

	// sixth sense captcha image URL
	@JsonProperty("captchaImageURL")
	private String captchaImageURL;

}
