package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaxRelatedDetailsDTO implements Serializable{

	private static final long serialVersionUID = -5368650919879481097L;

	private String name;
	
	private String value;
}
