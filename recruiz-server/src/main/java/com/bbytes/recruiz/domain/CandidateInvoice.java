package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "candidate_invoice")
@EntityListeners({ AbstractEntityListener.class })
@Data
@EqualsAndHashCode(callSuper = false,exclude = {"agencyInvoice"})
@ToString(exclude = {"agencyInvoice"})
@NoArgsConstructor
public class CandidateInvoice extends  AbstractEntity {
	
	private static final long serialVersionUID = -2980194712499443971L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name ="id")
	private long id;
	
	@Column(name ="candidate_name")
	private String candidateName;
	
	@Column(name = "candidate_email")
	private String candidateEmail;
	
	@Column(name = "position_name")
	private String postionName;
	
	@Column(name = "position_code")
	private String positionCode;
	
	@Column(name = "client_name")
	private String clientName;
	
	@Column(name = "client_id")
	private Long clientId;
	
	//@Column(name = "offered_salary")
	//private double offeredSalary;
	
	//@Column(name = "offered_date")
	//private Date offeredDate;

	@Column(name = "joining_date")
	private Date joiningDate;
	
	//@Column(name = "percentage")
	//private double percentage;
	
	@Column(name = "amount")
	private double amount;
	
	//@Column(name = "value")
	//private double value;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private AgencyInvoice agencyInvoice;
	
	public CandidateInvoice(String candidateName, String candidateEmail, String postionName, String positionCode,Date joiningDate) {
		super();
		this.candidateName = candidateName;
		this.candidateEmail = candidateEmail;
		this.postionName = postionName;
		this.positionCode = positionCode;
		//this.offeredSalary = offeredSalary;
		//this.offeredDate = offeredDate;
		this.joiningDate = joiningDate;
	}
	
	
	//@JsonSerialize(using = DatePickerDateSerializer.class)
	//public Date getOfferedDate() {
		//return offeredDate;
	//}

	//@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	//public void setOfferedDate(Date offeredDate) {
		//this.offeredDate = offeredDate;
	//}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getJoiningDate() {
		return joiningDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setJoiningDate(Date joiningDate) {
		this.joiningDate = joiningDate;
	}

}
