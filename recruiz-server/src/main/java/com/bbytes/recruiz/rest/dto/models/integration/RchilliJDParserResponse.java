package com.bbytes.recruiz.rest.dto.models.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RchilliJDParserResponse {

	@JsonProperty("JDParsedData")
	public RchilliJDParserData jDParsedData;

}
