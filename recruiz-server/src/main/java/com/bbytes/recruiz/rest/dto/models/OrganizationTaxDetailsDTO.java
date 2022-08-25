package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationTaxDetailsDTO implements Serializable{

	private static final long serialVersionUID = -3732178781587019447L;
	
	private long id;
	
	private String taxName;
	
	private String taxValue;

	public OrganizationTaxDetailsDTO(String taxName, String taxValue) {
		this.taxName = taxName;
		this.taxValue = taxValue;
	}
	
}
