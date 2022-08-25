package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class EmployeeDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long ID;
	private String firstName;
	private String middleName;
	private String lastName;
	private String officialEmail;
	private String employmentStatus;
	private String employmentType;
	private String jobLocation;
	private String reportingManager;
	private String jobTitle;
	private Date doj;
	private String role;
	private String empID;
	private String team;
	private String hrContact;
	private String street;
	private String city;
	private String state;
	private String country;
	private String postalCode;
	private Date dob;
	private String gender;
	private String personalEmail;
	private String primaryContact;
	private String alternateEmail;
	private String placedAt;

	private Long inProgressOnBoardingCount;
	private Long completedOnBoardingCount;

	private String clientName;
	private String positionName;
	
	private Map<String, String> customField = new HashMap<>();

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDob() {
		return dob;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDob(Date dob) {
		this.dob = dob;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDoj() {
		return doj;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDoj(Date doj) {
		this.doj = doj;
	}

}
