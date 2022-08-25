package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CandidateInvoice;

public interface CandidateInvoiceRepository extends JpaRepository<CandidateInvoice, Long>{

	List<CandidateInvoice> findByCandidateEmail(String candidateEmail);
	
	List<CandidateInvoice> findByCandidateEmailAndPositionCodeAndAgencyInvoiceClientName(String candidateEmail,String positionCode,String clientName);
	
}
