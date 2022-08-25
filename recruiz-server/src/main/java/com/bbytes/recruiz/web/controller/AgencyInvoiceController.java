package com.bbytes.recruiz.web.controller;


import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.AgencyInvoicePaymentHistoryDTO;
import com.bbytes.recruiz.rest.dto.models.AgencyInvoiceStatusCountDTO;
import com.bbytes.recruiz.rest.dto.models.AgencyMultipleInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.CalculationDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.DiscountDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceIdOrgDTO;
import com.bbytes.recruiz.rest.dto.models.JoinedCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.AgencyInvoicePaymentHistoryService;
import com.bbytes.recruiz.service.AgencyInvoiceService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CandidateStatusService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
public class AgencyInvoiceController {

	private static final Logger logger = LoggerFactory.getLogger(AgencyInvoiceController.class);

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private AgencyInvoiceService agencyInvoiceService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CandidateStatusService candidateStatusService;
	
	@Autowired
	private CandidateService candidateService;
	
	
	@Autowired
	private AgencyInvoicePaymentHistoryService agencyInvoicePaymentHistoryService;

	

	/***************************************************
	 * To Update Agency Invoice , pass id in dto itself*
	 ***************************************************
	 * @param agencyInvoiceDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice", method = RequestMethod.PUT)
	public RestResponse updateAgencyInvoice(@RequestBody AgencyMultipleInvoiceDTO agencyInvoiceDTO)
			throws RecruizException {
		AgencyInvoice agencyInvoice = agencyInvoiceService.updateInvoiceForInvoice(agencyInvoiceDTO);
		AgencyMultipleInvoiceDTO convertedAgencyInvoice = dataModelToDTOConversionService
				.convertAgencyInvoice(agencyInvoice);
		logger.info("invoice updated successfully");
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, convertedAgencyInvoice,
				SuccessHandler.UPDATE_SUCCESS);
		return agencyInvoiceResponse;

	}

	/************************
	 * To Get Agency Invoice*
	 ************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/{id}", method = RequestMethod.GET)
	public RestResponse getAgencyInvoice(@PathVariable("id") long id) throws RecruizException {
		AgencyInvoice agencyInvoice = agencyInvoiceService.getAgencyInvoice(id);
		AgencyMultipleInvoiceDTO convertedAgencyInvoice = dataModelToDTOConversionService
				.convertAgencyInvoice(agencyInvoice);
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, convertedAgencyInvoice,
				SuccessHandler.GET_SUCCESS);
		return agencyInvoiceResponse;

	}

	/**********************************
	 * To Get Agency Invoice(Pageable)*
	 **********************************
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice", method = RequestMethod.GET)
	public RestResponse getAgencyInvoice(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		Page<AgencyMultipleInvoiceDTO> agencyInvoice = agencyInvoiceService.getPagebleAgencyInvoice(
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				agencyInvoiceService.getAllInvoice());
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, agencyInvoice,
				SuccessHandler.GET_SUCCESS);
		return agencyInvoiceResponse;
	}

	/***************************
	 * To Delete AgencyInvoice**
	 ***************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteAgencyInvoice(@PathVariable("id") long id) throws RecruizException {
		Boolean isDeleted = agencyInvoiceService.deleteAgencyInvoice(id);
		RestResponse agencyInvoiceResponse = null;
		if (isDeleted)
			agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, isDeleted, SuccessHandler.DELETE_SUCCESS);
		else
			agencyInvoiceResponse = new RestResponse(RestResponse.FAILED, isDeleted, ErrorHandler.DELETE_FAILED);
		return agencyInvoiceResponse;
	}

	/********************************
	 * To Get Joined Candidates *****
	 ********************************
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return Page<JoinedCandidateDTO>
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/agency/joined/candidate", method = RequestMethod.GET)
	public Page<JoinedCandidateDTO> getJoinedCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
			throws RecruizException, IOException {

		Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField,
				pageableService.getSortDirection(sortOrder));
		// List<RoundCandidate> roundCandidates =
		// agencyInvoiceService.getJoinedCandidate();
		// List<JoinedCandidateDTO> joinedCandidateDTOs =
		// agencyInvoiceService.convertRoundCandidate(roundCandidates);

		List<CandidateStatus> candidateStatus = candidateStatusService.getJoinedCandidateStatus();
		List<JoinedCandidateDTO> joinedCandidateDTOs = agencyInvoiceService.convertCandidateStatus(candidateStatus);
		Page<JoinedCandidateDTO> page = agencyInvoiceService.getPagebleJoinedCandidateDTO(pageable,
				joinedCandidateDTOs);
		
		for (JoinedCandidateDTO joinedCandidateDTO : page) {
		    Candidate candidate = candidateService.findOne(joinedCandidateDTO.getCid());
		    candidateService.attachCurrentPosition(candidate);
		    joinedCandidateDTO.setCurrentPositionMap(candidate.getCurrentPositionMap());
		}
		
		
		return page;

	}
	
	/*****************************************
	 * To get non generated invoice candidate*
	 *****************************************
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/yet/proceess", method = RequestMethod.GET)
	public Page<JoinedCandidateDTO> getYetToProcessCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
			throws RecruizException, IOException {
		
		Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField,
				pageableService.getSortDirection(sortOrder));
		
		List<CandidateStatus> candidateStatus = candidateStatusService.getJoinedCandidateStatus();
		List<JoinedCandidateDTO> joinedCandidateDTOs = agencyInvoiceService.convertCandidateStatus(candidateStatus);
		List<JoinedCandidateDTO> yetToProcessCandidate = agencyInvoiceService.getYetToProcessInvoiceCandidate(joinedCandidateDTOs);
		Page<JoinedCandidateDTO> page = agencyInvoiceService.getPagebleJoinedCandidateDTO(pageable,
				yetToProcessCandidate);
		
		for (JoinedCandidateDTO joinedCandidateDTO : page) {
		    Candidate candidate = candidateService.findOne(joinedCandidateDTO.getCid());
		    candidateService.attachCurrentPosition(candidate);
		    joinedCandidateDTO.setCurrentPositionMap(candidate.getCurrentPositionMap());
		}
		
		return page;
		
	}

	/**********************************
	 * To get agency invoice by status*
	 **********************************
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/by/status", method = RequestMethod.GET)
	public RestResponse AgencyInvoiceByStatus(@RequestParam(required = false) List<String> status,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		Page<AgencyMultipleInvoiceDTO> agencyInvoice = agencyInvoiceService.getPagebleAgencyInvoice(
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				agencyInvoiceService.getAgencyInvoice(status));
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, agencyInvoice,
				SuccessHandler.GET_SUCCESS);
		return agencyInvoiceResponse;
	}

	/******************************************************************
	 * To get New , Issued , Pending , Closed Count for Agency Invoice*
	 ******************************************************************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/status/count", method = RequestMethod.GET)
	public RestResponse AgencyInvoiceStatusCount() throws RecruizException {
		AgencyInvoiceStatusCountDTO agencyInvoiceStatusCountDTO = agencyInvoiceService.getInvoiceStatusCount();
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, agencyInvoiceStatusCountDTO,
				SuccessHandler.GET_SUCCESS);
		return agencyInvoiceResponse;
	}

	/***********************************************************
	 * To pay payment for agency Invoice, pass id in dto itself*
	 **********************************************************
	 * @param agencyInvoiceDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/payment", method = RequestMethod.PUT)
	public RestResponse payPayment(@RequestBody AgencyMultipleInvoiceDTO agencyInvoiceDTO) throws RecruizException {
		AgencyInvoice agencyInvoice = agencyInvoiceService.payPayment(agencyInvoiceDTO);
		AgencyMultipleInvoiceDTO convertedAgencyInvoice = dataModelToDTOConversionService
				.convertAgencyInvoice(agencyInvoice);
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, convertedAgencyInvoice,
				SuccessHandler.GET_SUCCESS);
		return agencyInvoiceResponse;
	}

	/*************************
	 * To get archive Invoice*
	 *************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/invoice/archive/{id}", method = RequestMethod.GET)
	public RestResponse getInvoicePayment(@PathVariable("id") long id) throws RecruizException {
		AgencyInvoice agencyInvoice = agencyInvoiceService.changeStatusToArchive(id);
		AgencyMultipleInvoiceDTO convertedAgencyInvoice = dataModelToDTOConversionService
				.convertAgencyInvoice(agencyInvoice);
		RestResponse agencyInvoiceResponse = new RestResponse(RestResponse.SUCCESS, convertedAgencyInvoice,
				SuccessHandler.CHANGED_SUCCESS);
		return agencyInvoiceResponse;
	}

	@RequestMapping(value = "/api/v1/agency/invoice/data/generate", method = RequestMethod.GET)
	public RestResponse getDataToGenerateInvoice(@RequestParam long clientId, @RequestParam long candidateId,
			@RequestParam String positionCode) throws RecruizException {

		CandidateStatus candidateStaus = candidateStatusService.getJoinedCandidateStatusForPosition(clientId,
				candidateId, positionCode, BoardStatus.Joined.getDisplayName());
		CandidateInvoiceDTO responseDTO = dataModelToDTOConversionService
				.convertCandidateStatusToAgencyInvoiceDTO(candidateStaus);
		return new RestResponse(true, responseDTO);
	}

	/***********************************
	 * To create multiple agency invoice*
	 ***********************************
	 * @param agencyMultipleInvoiceCommonDataDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/multiple/invoice", method = RequestMethod.POST)
	public RestResponse createMultipleAgencyInvoice(@RequestBody AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO)
			throws RecruizException {
		boolean isSuccess = agencyInvoiceService.createMultipleInvoiceForAgency(agencyMultipleInvoiceDTO);
		if (isSuccess) {
			return new RestResponse(true, SuccessHandler.MULTIPLE_INVOICE_SUCCESSFULLY_GENERATED,
					SuccessHandler.INVOICE_SUCCESSFULLY_CREATED);
		} else {
			return new RestResponse(false, ErrorHandler.GENERATING_OF_INVOICE_FAILED,
					ErrorHandler.INVOICE_NOT_GENERATED);
		}
	}

	/****************************************************************
	 * To check same candidate in Multiple Client for Agency Invoice*
	 ****************************************************************
	 * @param clientName
	 * @param candidateEmail
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/multiple/client/invoice/check", method = RequestMethod.GET)
	public RestResponse checkAgencyInvoiceForMultipleClient(@RequestParam(value = "clientName") String clientName,
			@RequestParam(value = "candidateEmail") String candidateEmail) throws RecruizException {
		Boolean isSuccess = agencyInvoiceService.checkCandidateEmailExistInMultipleClint(clientName, candidateEmail);

		if (isSuccess)
			return new RestResponse(true, isSuccess);
		else {
			return new RestResponse(false, isSuccess);
		}
	}
	
	
	/*************************
	 * To calculate Percent***
	 *************************
	 * @param calculationDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/calculate/percentage", method = RequestMethod.POST)
	public RestResponse calculatePercent(@RequestBody CalculationDTO calculationDTO) throws RecruizException {
		CalculationDTO calculateDTO = agencyInvoiceService.calculatePercentage(calculationDTO);
		return new RestResponse(true, calculateDTO);
	}
	
	@RequestMapping(value = "/api/v1/agency/calculate/discount/percentage", method = RequestMethod.POST)
	public RestResponse calculateDiscountPercent(@RequestBody DiscountDTO discountDTO) throws RecruizException {
		DiscountDTO calculateDTO = agencyInvoiceService.calculateDiscount(discountDTO);
		return new RestResponse(true, calculateDTO);
	}
	
	/************************************************
	 * To get All Payment History of agency invoice**
	 ************************************************
	 * @param agencyInvoiceId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/payment/history/{agencyInvoiceId}", method = RequestMethod.GET)
	public RestResponse getAllPaymentHistoryOfAgencyInvoice(@PathVariable("agencyInvoiceId") long agencyInvoiceId) throws RecruizException {

		List<AgencyInvoicePaymentHistory> agencyInvoicePaymentHistorys = agencyInvoicePaymentHistoryService
				.getAllByAgencyInvoice(agencyInvoiceId);
		List<AgencyInvoicePaymentHistoryDTO> agencyInvoicePaymentHistoryDTOs = dataModelToDTOConversionService
				.converAgencyInvoicePaymentHistory(agencyInvoicePaymentHistorys);
		return new RestResponse(true, agencyInvoicePaymentHistoryDTOs,
				SuccessHandler.AGENCY_INVOICE_PAYMENT_HISTORY_SUCCESS);
	}
	
	/**************************************
	 * To update history of agency Invoice*
	 **************************************
	 * @param historyId
	 * @param receivedAmount
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/payment/history/update/{historyId}", method = RequestMethod.PUT)
	public RestResponse updateHistoryOfAgencyInvoice(@PathVariable("historyId") long historyId,
			@RequestParam double receivedAmount) throws RecruizException {
		
		
		boolean isSuccess = agencyInvoicePaymentHistoryService.updateHistory(historyId, receivedAmount);
	
		if(isSuccess){
			return new RestResponse(isSuccess, SuccessHandler.UPDATE_PAYMENT_HISTORY_SUCCESS);
		}else
			return new RestResponse(isSuccess, ErrorHandler.UPDATE_PAYMENT_HISTORY_FAILED);
	}
	
	
	/*****************
	 *To get Receipt**
	 *****************
	 * @param historyId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/payment/history/receipt/{historyId}", method = RequestMethod.GET)
	public RestResponse getReceiptOfPayment(@PathVariable("historyId") long historyId) throws RecruizException {

		AgencyInvoicePaymentHistory agencyInvoicePaymentHistory = agencyInvoicePaymentHistoryService
				.getPaymentHistory(historyId);
		AgencyInvoicePaymentHistoryDTO agencyInvoicePaymentHistoryDTO = dataModelToDTOConversionService.converAgencyInvoicePaymentHistory(agencyInvoicePaymentHistory);
		return new RestResponse(true, agencyInvoicePaymentHistoryDTO,
				SuccessHandler.AGENCY_INVOICE_PAYMENT_HISTORY_SUCCESS);
	}
	
	
	/**************************************************
	 * To check for invoice is generated for candidate*
	 **************************************************
	 * @param ids
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/agency/check/candidate/invoice", method = RequestMethod.GET)
	public RestResponse checkInvoiceGeneretedForCandidate(@RequestParam("ids") List<String> ids)
			throws RecruizException {
		boolean isGenerated = agencyInvoiceService.checkInvoiceGeneratedForCandidate(ids);

		if (isGenerated) {
			return new RestResponse(isGenerated, SuccessHandler.INVOICE_EXIST);
		} else
			return new RestResponse(isGenerated, ErrorHandler.INVOICE_NOT_EXIST_FOR_CANDIATE);
	}
	
	@RequestMapping(value = "/api/v1/agency/invoice/get/next/id", method = RequestMethod.GET)
	public RestResponse getNextInvoiceId() throws RecruizException{
		
		InvoiceIdOrgDTO invoiceIdOrgDTO = new InvoiceIdOrgDTO();
		invoiceIdOrgDTO.setId(agencyInvoiceService.getNextInvoiceId());
		invoiceIdOrgDTO.setOrgLogoUrl(agencyInvoiceService.getOrgLogoUrl());
		return new RestResponse(true, invoiceIdOrgDTO);
	}
	
	@RequestMapping(value = "/api/v1/agency/check/invoice/number/exist/{invoiceNumber}", method = RequestMethod.GET)
	public RestResponse checkInvoiceNumberExist(@PathVariable("invoiceNumber") String invoiceNumber,@RequestParam(value = "id", required = false) String id) throws RecruizException{
		boolean isSuccess = agencyInvoiceService.isInvoiceNumberExist(invoiceNumber,id);
		if(isSuccess){
			return new RestResponse(isSuccess, ErrorHandler.INVOICE_NUMBER_ALREADY_EXIST);
		}else
			return new RestResponse(isSuccess, ErrorHandler.INVOICE_NUMBER_NOT_EXIST);
	}
}
