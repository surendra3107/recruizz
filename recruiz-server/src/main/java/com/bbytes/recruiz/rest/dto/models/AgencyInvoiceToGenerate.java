package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class AgencyInvoiceToGenerate implements Serializable{

	private static final long serialVersionUID = 7968544679107984627L;
	
	private String clientName;
	
    private String candidateName;
	
	private String candidateEmail;
	
	private String postionName;
	
	private String positionCode;
	
	private double offeredSalary;
	
	private Date offeredDate;

	private Date joiningDate;      
}
