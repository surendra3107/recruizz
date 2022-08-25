package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class InvoiceDTO implements Serializable {

	private static final long serialVersionUID = -3716553476585362371L;

	private Long invoiceId;

	private Date invoiceGeneratedDate;

	private int payableAmount;

	private String orgId;

	private Long resellerId;
	
	private Long planId;
	
	private String planName;
	
	private String invoiceDetails;
	
	private String paymentMode;
	
	private String totalAmountInWords;
	
	private Date dueDate;
	
	private Double dueAmount;
	
	private String taxSentence;
	
	private double taxPercentage;
	
	private double taxAmount;
	
	private double discountAmount;
	
	private double discountPercentage;
	
	private double baseAmount;
	
	private String currency;
	
	private String invoicePeriod;

}
