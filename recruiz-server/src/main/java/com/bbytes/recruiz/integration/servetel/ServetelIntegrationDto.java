package com.bbytes.recruiz.integration.servetel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ServetelIntegrationDto implements Serializable  {
	
	private static final long serialVersionUID = -6116364643249538856L;
	
	@JsonProperty("loginId")
	private String loginId;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("productId")
	private String productId;

}
