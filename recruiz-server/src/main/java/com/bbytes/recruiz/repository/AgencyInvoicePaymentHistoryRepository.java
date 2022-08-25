package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;

public interface AgencyInvoicePaymentHistoryRepository extends JpaRepository<AgencyInvoicePaymentHistory, Long> {

	
	//List<AgencyInvoicePaymentHistory> findByAgencyInvoiceOrderByCreationDateAsc(AgencyInvoice agencyInvoice);
	
}
