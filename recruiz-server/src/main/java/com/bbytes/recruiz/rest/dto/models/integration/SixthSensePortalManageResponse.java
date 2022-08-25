
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Sixth Sense portal manage response object
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSensePortalManageResponse implements Serializable {

	private static final long serialVersionUID = 2803891105558006925L;

	@JsonProperty("sources")
	public String sources;

	@JsonProperty("monster")
	private List<SixthSensePortalCredentialDTO> monster = new ArrayList<SixthSensePortalCredentialDTO>();

	@JsonProperty("naukri")
	private List<SixthSensePortalCredentialDTO> naukri = new ArrayList<SixthSensePortalCredentialDTO>();

	@JsonProperty("times")
	private List<SixthSensePortalCredentialDTO> times = new ArrayList<SixthSensePortalCredentialDTO>();

	@JsonProperty("shine")
	private List<SixthSensePortalCredentialDTO> shine = new ArrayList<SixthSensePortalCredentialDTO>();

	public SixthSenseMessageObject messageObject;

}
