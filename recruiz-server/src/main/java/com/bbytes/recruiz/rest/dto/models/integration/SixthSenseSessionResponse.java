
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Sixth Sense Session response object
 * 
 * @author akshay
 *
 */
@Data
public class SixthSenseSessionResponse implements Serializable {

	private static final long serialVersionUID = 6319171244004107049L;

	@JsonProperty("sessionToken")
	public String sessionToken;

	public SixthSenseMessageObject messageObject;

}
