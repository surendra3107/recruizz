package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class InvoiceSettingsDTO implements Serializable {

	private static final long serialVersionUID = 137375334374772539L;

	private long id;

	// registered agency details

	//private String gstin;

	//private String pan;

	private String organizationName;

	private String organization_address_1;

	private String organization_address_2;

	private String organizationCity;

	private String organizationState;

	private String organizationCountry;

	private String organizationPin;

	private String organizationPhone;

	// organization account details

	private String chequePayable;

	private String organizationAccountName;

	private String organizationAccountNumber;

	private String organizationBankName;

	private String organizationBankBranchName;

	private String organizationBankIfsc;

	private String note;

	// Billing person information

	private String billClientName;

	private String billContactName;

	private String bill_address_1;

	private String bill_address_2;

	private String billCity;

	private String billState;

	private String billCountry;

	private String billPin;

	private String billPhone;
	
	private String orgLogoUrl; 

	Map<String, Double> taxDetails = new HashMap<String, Double>();
	
	List<TaxRelatedDetailsDTO> taxRelatedDetailsDTOs;
}
