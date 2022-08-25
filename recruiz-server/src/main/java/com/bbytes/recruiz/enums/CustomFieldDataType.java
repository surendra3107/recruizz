package com.bbytes.recruiz.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CustomFieldDataType {

	@JsonProperty("text")
	text, 
	@JsonProperty("dropDown")
	dropDown;
}
