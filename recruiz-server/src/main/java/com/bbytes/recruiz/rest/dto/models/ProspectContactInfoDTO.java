package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProspectContactInfoDTO implements Serializable{

	private static final long serialVersionUID = -6567721003055030661L;
	
    private long id;
	
	private String name;
	
	private String email;
	
	private String mobile;
	
	private String designation;

}
