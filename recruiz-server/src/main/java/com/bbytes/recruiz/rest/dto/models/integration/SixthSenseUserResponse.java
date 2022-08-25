
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SixthSenseUserResponse implements Serializable {

	private final static long serialVersionUID = 2048592560889704603L;

	private SixthSenseMessageObject messageObject;

	@JsonProperty("users")
	private List<String> users = null;

	@JsonProperty("success")
	private List<String> successUserList = new ArrayList<String>();

	@JsonProperty("failure")
	private List<SixthSenseFailedUserDTO> failedUserList = null;

}
