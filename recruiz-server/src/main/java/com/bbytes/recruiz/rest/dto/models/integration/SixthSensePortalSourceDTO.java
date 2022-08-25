
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;

/**
 * Sixth Sense portal sources response DTO for UI
 * 
 * @author akshay
 *
 */
@Data
public class SixthSensePortalSourceDTO implements Serializable {

	private static final long serialVersionUID = -1160215755936607507L;

	private String sourceUserId;

	private String password;

	private String otpEmailID;

	private String otpEmailPwd;

	private String source;

}
