package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ProspectDTO implements Serializable {

	private static final long serialVersionUID = 2152545220965293461L;

	private long prospectId;

	private String companyName; // Mandatory

	private String name; // Mandatory

	private String mobile; // Mandatory

	private String email; // Mandatory

	private String owner; // by default it will be the logged in user

	private String designation;

	private String location;

	private String address;

	private String source;

	private String website;

	private String status = ProspectStatus.New.toString();
	
	private String industry; // Mandatory
	
	private String category; // Mandatory
	
	private int prospectRating; // Mandatory
	
	private double dealSize; 
	
	private double percentage;
		
	private double value;
	
	private String currency = Currency.Rupee.toString();
	
	private Date modificationDate;
	
	private Date creationDate;

	// if the prospectStatus is Lost then Reason is Mandatory .
	private String reason = "N/A";
	
	private String mode = GlobalConstants.PROSPECT_MODE;

	private Set<ProspectContactInfoDTO> prospectContactInfo = new HashSet<ProspectContactInfoDTO>();
	
	private Set<ProspectPostionDTO> prospectPostionDTOs = new HashSet<ProspectPostionDTO>();
	
	private Map<String, String> customField = new HashMap<>();
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getModificationDate() {
		return modificationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}
