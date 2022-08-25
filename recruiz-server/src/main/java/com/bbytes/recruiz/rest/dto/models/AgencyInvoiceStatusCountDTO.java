package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class AgencyInvoiceStatusCountDTO implements Serializable {

	private static final long serialVersionUID = 8418342548650200686L;

	private int overDueInvoice;

	private int partialPaymentInvoice;

	private int pendingInvoice;

	private int paidInvoice;
	
	private int archiveInvoice;
	
	private int yetToProcessInvoiceCandidate;

}
