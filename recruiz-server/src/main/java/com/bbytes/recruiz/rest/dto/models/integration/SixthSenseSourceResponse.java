
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SixthSenseSourceResponse implements Serializable {

	private final static long serialVersionUID = 2048592560889704603L;

	private SixthSenseMessageObject messageObject;

	@JsonProperty("sources")
	private List<String> sources = null;

}
