package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;
import com.bbytes.recruiz.enums.InvoiceStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.AgencyInvoicePaymentHistoryRepository;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class AgencyInvoicePaymentHistoryService extends AbstractService<AgencyInvoicePaymentHistory, Long> {

	@Autowired
	private AgencyInvoicePaymentHistoryRepository agencyInvoicePaymentHistoryRepository;

	@Autowired
	private AgencyInvoiceService agencyInvoiceService;

	@Autowired
	public AgencyInvoicePaymentHistoryService(
			AgencyInvoicePaymentHistoryRepository agencyInvoicePaymentHistoryRepository) {
		super(agencyInvoicePaymentHistoryRepository);
		this.agencyInvoicePaymentHistoryRepository = agencyInvoicePaymentHistoryRepository;
	}

	public List<AgencyInvoicePaymentHistory> getAllByAgencyInvoice(long agencyInvoiceId) throws RecruizException {

		if (!agencyInvoiceService.exists(agencyInvoiceId)) {
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.NOT_VALID_ID);
		} else {
			AgencyInvoice agencyInvoice = agencyInvoiceService.findOne(agencyInvoiceId);
			List<AgencyInvoicePaymentHistory> paymentHistories = new ArrayList<>();
			paymentHistories.addAll(agencyInvoice.getAgencyInvoicePaymentHistories());
			paymentHistories.sort(new Comparator<AgencyInvoicePaymentHistory>() {
				@Override
				public int compare(AgencyInvoicePaymentHistory o1, AgencyInvoicePaymentHistory o2) {
					return (int) (o2.getId() - o1.getId());
				}
			});

			return paymentHistories;
		}
	}

	@Transactional
	public boolean updateHistory(long historyId, double recievedAmount) throws RecruizException {
		AgencyInvoicePaymentHistory agencyInvoicePaymentHistory = null;
		AgencyInvoice agencyInvoice = null;
		if (!exists(historyId))
			throw new RecruizException(ErrorHandler.HISTORY_NOT_EXIST, ErrorHandler.NOT_VALID_ID);
		else {
			agencyInvoicePaymentHistory = findOne(historyId);
			Set<AgencyInvoicePaymentHistory> histories = new HashSet<>();
			histories.add(agencyInvoicePaymentHistory);
			agencyInvoice = agencyInvoiceService.getInvoiceByPayemntHistory(histories); // agencyInvoicePaymentHistory.getAgencyInvoice();
			double amount = (agencyInvoice.getPaymentReceived() - agencyInvoicePaymentHistory.getRecivedAmount())
					+ recievedAmount;
			if (agencyInvoice.getTotalAmount() - amount <= 0.0) {
				agencyInvoice.setDelayDay(0);
				agencyInvoice.setInvoiceStatus(InvoiceStatus.Paid.getDisplayName());
			} else {
				agencyInvoice.setInvoiceStatus(InvoiceStatus.PartialPayment.getDisplayName());
			}
			agencyInvoice.setPaymentReceived(amount);
			agencyInvoicePaymentHistory.setRecivedAmount(recievedAmount);
			agencyInvoiceService.save(agencyInvoice);
			save(agencyInvoicePaymentHistory);
			return true;
		}
	}

	public AgencyInvoicePaymentHistory getPaymentHistory(long historyId) throws RecruizException {
		if (!exists(historyId))
			throw new RecruizException(ErrorHandler.HISTORY_NOT_EXIST, ErrorHandler.NOT_VALID_ID);
		else
			return agencyInvoicePaymentHistoryRepository.findOne(historyId);
	}

}
