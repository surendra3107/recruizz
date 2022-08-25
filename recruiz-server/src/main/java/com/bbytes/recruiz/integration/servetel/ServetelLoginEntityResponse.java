package com.bbytes.recruiz.integration.servetel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ServetelLoginEntityResponse implements Serializable{

	private static final long serialVersionUID = 2803891105558006925L;
	
	@JsonProperty("success")
	public Boolean success;
	
	@JsonProperty("access_token")
	public String access_token;
	
	@JsonProperty("token_type")
	public String token_type;
	
	@JsonProperty("expires_in")
	public String expires_in;
	
	public String message;
}
