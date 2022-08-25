package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ProspectPostionDTO implements Serializable {

	private static final long serialVersionUID = -8199340724184123483L;

	private long positionId;

	private String positionName;

	private double percentage;

	private double value;

	private Date closureDate;
	
	private Date creationDate;

	private Integer numberOfOpenings;

	private Double minExperience;

	private Double maxExperience;

	private Set<String> keySkills = new HashSet<String>();
	
	private String location;
	
	private String type; // Mandatory
	
	private boolean remoteWork = false; // Mandatory
	
	private double maxSal; // Mandatory
	
	private double minSal; //  Mandatory
	
	private String industry; // Mandatory
	
	private String functionalArea; // Mandatory
	
	private String clientName; 
	    
	private String status;
	
	private String currency;
	
	@JsonIgnore
	private Set<String> locationSet = new HashSet<String>();
	
	private Set<String> educationQualification = new HashSet<String>();
	
	private Map<String, String> customField = new HashMap<>();
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getClosureDate() {
		return closureDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setClosureDate(Date closureDate) {
		this.closureDate = closureDate;
	}
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Set<String> getLocationSet() {
		Set<String> locations = new HashSet<String>();
		if (this.location != null) {
			Collections.addAll(locations, this.location.split("\\|"));
			return this.locationSet = locations;
		}
		return new HashSet<String>();
	}
}
