
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SixthSenseIdDTO implements Serializable
{

	private static final long serialVersionUID = -3956790479267744943L;

	@JsonProperty("strings")
    public List<String> strings = null;
   
    @JsonProperty("values")
    public List<Integer> values = null;

}
