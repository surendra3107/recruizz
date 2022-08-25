package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProfileDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String email;
	
	private String mobile;

	private String userName;

	private String timeZone;
	
	private String designation;
	
	private String profileSignature;

}


