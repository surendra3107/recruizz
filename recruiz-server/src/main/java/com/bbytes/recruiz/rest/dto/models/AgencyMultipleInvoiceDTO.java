package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class AgencyMultipleInvoiceDTO implements Serializable {

	private static final long serialVersionUID = -5404314256074044093L;

	private long invoiceId;
	
	// it is for UI shown
	private long id;
	
	// it is unique
	private String invoiceNumber;

	private String clientName;

	private long clientId;

	private String invoiceStatus;

	private Date dueDate = new Date();

	private String currency;

	private double subTotal;

	private Date invoiceGeneratedDate;

	private double discount;
	
	private double discountValue;

	private double totalAmount;
	
	private Double totalAmountAfterDiscount;

	//private String totalAmountInWords;

	private double paymentReceived;

	private Date paymentReceivedDate;

	private float pendingAmount;
	
	public void setPendingAmount(double pendingAmount){
		this.pendingAmount = (float) pendingAmount;
	}

	private int delayDay;

	private String orgLogoUrl;

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
	
	private String gstin;
	
	private String pan;

	private Map<String, Double> taxDetails = new HashMap<String, Double>();
	
	// while get agency invoice in this calculating tax percent value
	private List<TaxCalculateDTO> taxCalculateDTOs;
	
	private List<TaxRelatedDetailsDTO> gstAndPan ;
	
	private String note;

	private List<CandidateInvoiceDTO> candidateInvoices;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDueDate() {
		return dueDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getInvoiceGeneratedDate() {
		return invoiceGeneratedDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setInvoiceGeneratedDate(Date invoiceGeneratedDate) {
		this.invoiceGeneratedDate = invoiceGeneratedDate;
	}
}
