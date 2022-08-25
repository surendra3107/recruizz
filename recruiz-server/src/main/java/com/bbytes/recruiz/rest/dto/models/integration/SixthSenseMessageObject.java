
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Sixth Sense Message Object
 * @author akshay
 *
 */
@Data
public class SixthSenseMessageObject implements Serializable{

	private static final long serialVersionUID = -2678067021554212198L;
	
	@JsonProperty("code")
    public Integer code;
    
	@JsonProperty("message")
    public String message;

}
