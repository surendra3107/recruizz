package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class OfferLetterApprovalDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;
	
	private long id;
	
	private String positionId;
	
	private String candidateId;
	
	private String clientName;
	
    private String positionName;
	
	private String candidateName;

	private String email;
	
	private String deliveryLead;
	
	private String recruitersName;
	
	private String accountManager;
	
	private String client;
	
    private String clientPoC;
	
	private String clientPhone;
	
	private String clientEmail;
	
	private String designation;
	
    private Date dateOfOffer;
	
	private String experience;
	
	private Date doj;
	
	private Date billingDate;
	
    private String compensationAnnual = "0";
	
	private String projectDurationMonths;
	
	private String billingHours;
	
	private String billRatePerHour;
	
    private String subConCompensation = "0";
	
	private String serviceTax = "0";
	
	private String relocationCost = "0";
	
	private String joiningBonusCost = "0";
	
	private String noticePeriodCost = "0";
	
	private String oneTimeCost = "0";
	
	private String headHunting;
	
    private String otherAssociatedCost = "0";
	
	private String monthlyCost = "0";
	
	private String costProjectDuration = "0";
	
	private String billingMonthly;
	
	private String billingProjectDuration;
	
	private String monthlyDirectCost = "0";
	
	private String annualDirectCost = "0";
	
    private String montlyGrossMargin = "0";
	
	private String annualGorssMargin = "0";
	
	private String grossMarginPercentage = "0";
	
	private Date approvedDate;
	
    private String approvedBy;
	
	private String remarks;
	
	private String offerStatus;
	
	private Date iSJoinedOn;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getiSJoinedOn() {
		return iSJoinedOn;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public void setiSJoinedOn(Date iSJoinedOn) {
		this.iSJoinedOn = iSJoinedOn;
	}
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDateOfOffer() {
		return dateOfOffer;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public void setDateOfOffer(Date dateOfOffer) {
		this.dateOfOffer = dateOfOffer;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDoj() {
		return doj;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public void setDoj(Date doj) {
		this.doj = doj;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getBillingDate() {
		return billingDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getApprovedDate() {
		return approvedDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}
	
}
