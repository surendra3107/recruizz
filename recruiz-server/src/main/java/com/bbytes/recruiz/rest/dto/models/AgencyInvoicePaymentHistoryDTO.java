package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class AgencyInvoicePaymentHistoryDTO implements Serializable{

	private static final long serialVersionUID = -2236036985743484644L;
	
    private long historyId;
	
	private double totalAmount;
	
	private double recivedAmount;
	
	private float pendingAmount;
	
	private Date paymentDate;
	
	private String currency;
	
	private Date creationDate;
	
	private Date modificationDate;
	
	private String clientName;
	
	public void setPendingAmount(double pendingAmount){
		this.pendingAmount = (float) pendingAmount;
	}
	
	// it has added because receipt needs this info.
	
	List<CandidateInvoiceDTO> candidateInvoices;
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getPaymentDate() {
		return paymentDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

}
