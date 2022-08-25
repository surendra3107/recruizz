package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class InvoiceIdOrgDTO implements Serializable{

	private static final long serialVersionUID = -3780875154525931199L;

	private Long id;
	
	private String OrgLogoUrl;
	
}
