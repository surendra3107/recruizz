package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;

public interface AgencyInvoiceRepository extends JpaRepository<AgencyInvoice, Long> {

    List<AgencyInvoice> findByInvoiceStatus(String status);

    List<AgencyInvoice> findByInvoiceStatusIn(List<String> status);

    List<AgencyInvoice> findAllByOrderByCreationDateDesc();

    // AgencyInvoice findByCandidateInvoicesCandidateEmail(String
    // candidateEmail);

    List<AgencyInvoice> findByClientName(String clientName);

    List<AgencyInvoice> findAllByOrderByInvoiceIdDesc();
    
    AgencyInvoice findByInvoiceNumber(String invoiceNumber);

    AgencyInvoice findByAgencyInvoicePaymentHistoriesIn(Set<AgencyInvoicePaymentHistory> paymentHistories);
    
    // AgencyInvoice findByCandidateEmailAndPositionCode(String candidateEmail
    // ,String positionCode);

    // List<AgencyInvoice> findByCandidateEmailAndInvoiceStatus(String
    // candidateEmail, String status);

    // AgencyInvoice findByCandidateEmailAndPositionCodeAndInvoiceStatus(String
    // candidateEmail, String postionCode ,String status);

}
