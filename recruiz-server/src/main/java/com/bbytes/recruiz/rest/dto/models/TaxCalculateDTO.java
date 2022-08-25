package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaxCalculateDTO implements Serializable{
	
	private static final long serialVersionUID = -6984510064214880671L;

	// it will keep information of tax percent of subTotal = value
	
	private String taxName;
	
	private Double taxPercent;
	
	private Double value;
	
	private Double percent;
}
