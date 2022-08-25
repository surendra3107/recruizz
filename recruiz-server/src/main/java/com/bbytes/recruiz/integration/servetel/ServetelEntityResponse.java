package com.bbytes.recruiz.integration.servetel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ServetelEntityResponse implements Serializable{
	
	private static final long serialVersionUID = 2803891105558006925L;
	
	@JsonProperty("success")
	public Boolean success;
	
	@JsonProperty("message")
	public String message;
	
	@JsonProperty("agent_id")
	public String agent_id;

}
