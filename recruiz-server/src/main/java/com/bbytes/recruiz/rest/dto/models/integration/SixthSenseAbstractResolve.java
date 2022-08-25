package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * This is abstract class for resolve related attributes
 * 
 * @author akshay
 *
 */
@Data
public class SixthSenseAbstractResolve implements Serializable {

	private static final long serialVersionUID = -413849101252441525L;

	// this attribute true means otp/captcha already solved, else need to
	// resolve
	private boolean resolved = true;

	private String resolveType;

	private String resolveSource;

	// this is for image captcha
	private byte[] resolveHTML;
	
	// this is for v2 captcha
	private String resolveHTMLRaw;
	
	private int responseCode;
	
	private String newKeywords;
	
	private String message;

	private Map<String, String> hiddenParameterMap = new HashMap<String, String>();
}
