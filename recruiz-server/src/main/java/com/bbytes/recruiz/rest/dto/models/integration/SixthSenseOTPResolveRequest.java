package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * Input otp resolve request object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseOTPResolveRequest implements Serializable {

	private static final long serialVersionUID = 1747412434010161738L;

	private String source;

	Map<String, Map<String, String>> resolveOtpMap = new HashMap<String, Map<String, String>>();

}
