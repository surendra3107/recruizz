package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class OrgannizationPermissionDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String duplicateCheck;
	
	private String mandatoryDocs;
	
	private String documentsCheck;

	private String checkRolloutOfferletter;
	
}
