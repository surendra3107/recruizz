package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;


@Data
public class CandidateInvoiceDTO implements Serializable{

	
	private static final long serialVersionUID = 1350142025849782945L;

	private long candidateInvoiceId;
	
	private String candidateName;
	
	private String candidateEmail;
	
	private String postionName;
	
	private String positionCode;
	
	//private double offeredSalary;
	
	private Long clientId;

    //private double percentage;
	
	//private double value;
	
	//private Date offeredDate;

	private Date joiningDate;
	
	// here clientName is adding to dto on for generate Api
	
	private String clientName;

	public double amount;
	
	public CandidateInvoiceDTO(){}
	
	public CandidateInvoiceDTO(long candidateInvoiceId, String candidateName, String candidateEmail, String postionName,
			String positionCode, Date joiningDate) {
		super();
		this.candidateInvoiceId = candidateInvoiceId;
		this.candidateName = candidateName;
		this.candidateEmail = candidateEmail;
		this.postionName = postionName;
		this.positionCode = positionCode;
		//this.offeredSalary = offeredSalary;
		//this.offeredDate = offeredDate;
		this.joiningDate = joiningDate;
	}
	
	/*@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getOfferedDate() {
		return offeredDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setOfferedDate(Date offeredDate) {
		this.offeredDate = offeredDate;
	}*/

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getJoiningDate() {
		return joiningDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setJoiningDate(Date joiningDate) {
		this.joiningDate = joiningDate;
	}
}
