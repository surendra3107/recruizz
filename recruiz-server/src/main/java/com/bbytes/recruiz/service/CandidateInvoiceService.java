package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.CandidateInvoice;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateInvoiceRepository;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class CandidateInvoiceService extends AbstractService<CandidateInvoice, Long> {

    @Autowired
    private CandidateInvoiceRepository candidateInvoiceRepository;

    @Autowired
    public CandidateInvoiceService(CandidateInvoiceRepository candidateInvoiceRepository) {
	super(candidateInvoiceRepository);
	this.candidateInvoiceRepository = candidateInvoiceRepository;
    }

    public Boolean candidateEmailExist(String candidateEmail) {
	return candidateInvoiceRepository.findByCandidateEmail(candidateEmail).isEmpty() ? false : true;
    }

    public CandidateInvoice getByCandidateEmailAndPostionCodeAndClientName(String candidateEmail, String positionCode,
	    String clientName) {
	List<CandidateInvoice> invoiceList = candidateInvoiceRepository
		.findByCandidateEmailAndPositionCodeAndAgencyInvoiceClientName(candidateEmail, positionCode,
			clientName);
	if (null != invoiceList && !invoiceList.isEmpty()) {
	    return invoiceList.get(0);
	}
	return null;
    }

    public CandidateInvoice getByCandidateInvoiceId(long id) throws RecruizException {
	if (exists(id)) {
	    return findOne(id);
	} else {
	    throw new RecruizException(ErrorHandler.CANDIDATE_INVOICE_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);
	}
    }

    public List<CandidateInvoice> getByCandidateInvoiceEmail(String candidateEmail) {
	return candidateInvoiceRepository.findByCandidateEmail(candidateEmail);
    }

}
