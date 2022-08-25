package com.bbytes.recruiz.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateInvoice;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.EmailTemplateData;
import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.InvoiceStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.AgencyInvoiceRepository;
import com.bbytes.recruiz.repository.CandidateRepository;
import com.bbytes.recruiz.rest.dto.models.AgencyInvoiceStatusCountDTO;
import com.bbytes.recruiz.rest.dto.models.AgencyMultipleInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.CalculatePercentDTO;
import com.bbytes.recruiz.rest.dto.models.CalculationDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.DiscountDTO;
import com.bbytes.recruiz.rest.dto.models.JoinedCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.TaxRelatedDetailsDTO;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

@SuppressWarnings("deprecation")
@Service
public class AgencyInvoiceService extends AbstractService<AgencyInvoice, Long> {

	private static final Logger logger = LoggerFactory.getLogger(AgencyInvoiceService.class);

	@Autowired
	private AgencyInvoiceRepository agencyInvoiceRepository;

	@Autowired
	private CandidateInvoiceService candidateInvoiceService;

	@Autowired
	private UserService userService;

	@Autowired
	private VelocityEngine templateEngine;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private CandidateStatusService candidateStatusService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private CandidateRepository candidateRepository;

	@Autowired
	private InvoiceSettingsService invoiceSettingsService;

	@Autowired
	private TaxService taxService;

	@Value("${base.url}")
	private String baseUrl;

	@Autowired
	public AgencyInvoiceService(AgencyInvoiceRepository agencyInvoiceRepository) {
		super(agencyInvoiceRepository);
		this.agencyInvoiceRepository = agencyInvoiceRepository;
	}

	public Double calculateTax(Double amount, Double tax, Double discount) {
		double calculatedAmount = amount - (amount * discount) / 100;
		calculatedAmount = calculatedAmount + (calculatedAmount * tax) / 100;
		return roundHalfDown(calculatedAmount);
	}

	// 2.89 -> 3.00 , 2.33-> 2.00 ,2.50 -> 2.00
	public static double roundHalfDown(double amount) {
		return new BigDecimal(amount).setScale(0, RoundingMode.HALF_DOWN).doubleValue();
	}

	@Transactional(readOnly = true)
	public boolean isInvoiceNumberExist(String invoiceNumber) {
		return agencyInvoiceRepository.findByInvoiceNumber(invoiceNumber) == null ? false : true;
	}

	public boolean isInvoiceNumberExist(String invoiceNumber, String id) {

		AgencyInvoice agencyInvoice = agencyInvoiceRepository.findByInvoiceNumber(invoiceNumber);
		if (agencyInvoice != null) {
			if (agencyInvoice.getInvoiceId() == Long.parseLong(id) && agencyInvoice.getInvoiceNumber().equals(invoiceNumber)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	/*
	 * public Boolean candidateEmailExistAndStatusIsPaid(String candidateEmail,
	 * String status) { return
	 * agencyInvoiceRepository.findByCandidateEmailAndInvoiceStatus(
	 * candidateEmail, status).isEmpty() ? false : true; }
	 */

	/*
	 * public Boolean candidateEmailExistAndPostionCodeAndStatusIsPaid(String
	 * candidateEmail, String status, String postionCode) { return
	 * agencyInvoiceRepository.
	 * findByCandidateEmailAndPositionCodeAndInvoiceStatus(candidateEmail,
	 * postionCode, status) == null ? false : true; }
	 */

	/*
	 * public Boolean candidateEmailAndPostionCodeExist(String candidateEmail,
	 * String postionCode) { return
	 * agencyInvoiceRepository.findByCandidateEmailAndPositionCode(
	 * candidateEmail, postionCode) == null ? false : true; }
	 */

	public Long getNextInvoiceId() throws RecruizException {
		List<AgencyInvoice> agencyInvoices = agencyInvoiceRepository.findAllByOrderByInvoiceIdDesc();
		if (agencyInvoices != null && !agencyInvoices.isEmpty()) {
			return agencyInvoices.get(0).getInvoiceId() + 1;
		} else {
			return 1L;
		}
	}

	public String getOrgLogoUrl() {
		try {
			String url = baseUrl + "/pubset/" + userService.getLoggedInUserObject().getOrganization().getLogoUrlPath();
			return url;
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public AgencyInvoice getAgencyInvoice(long invoiceId) throws RecruizException {
		if (!exists(invoiceId))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		AgencyInvoice agencyInvoice = agencyInvoiceRepository.findOne(invoiceId);
		if (checkDueDay(agencyInvoice.getDueDate(), agencyInvoice.getTotalAmount())) {
			agencyInvoice.setDelayDay(calculatePayementDelayDate(agencyInvoice.getDueDate()));
		}
		agencyInvoice.setPendingAmount(agencyInvoice.getTotalAmount() - agencyInvoice.getPaymentReceived());
		return agencyInvoice;
	}

	@Transactional(readOnly = true)
	public Page<AgencyInvoice> getAgencyInvoice(Pageable pageable) {
		return agencyInvoiceRepository.findAll(pageable);
	}

	public Boolean checkCandidateEmailExistInMultipleClint(String clientName, String candidateEmail) {
		Boolean isSucces = false;
		if (candidateInvoiceService.candidateEmailExist(candidateEmail)) {
			AgencyInvoice agencyInvoice = getByCandidateEmail(candidateEmail);
			if (!clientName.equals(agencyInvoice.getClientName())) {
				isSucces = true;
			} else {
				isSucces = false;
			}
		}
		return isSucces;
	}

	public List<AgencyInvoice> getAllInvoice() {
		return agencyInvoiceRepository.findAllByOrderByCreationDateDesc();
	}

	public List<AgencyInvoice> getAllInvoiceByClient(String clientName) {
		return agencyInvoiceRepository.findByClientName(clientName);
	}

	public int calculatePayementDelayDate(Date dateOfDuePayementDate) {

		DateTime dueDate = new DateTime(dateOfDuePayementDate);
		DateTime todaysDate = new DateTime();
		return Days.daysBetween(dueDate.toLocalDate(), todaysDate.toLocalDate()).getDays();
	}

	public Date ff() {
		return null;
	}

	/**********************************
	 * To get agency invoice by status*
	 **********************************
	 * @param status
	 * @return List<AgencyInvoice>
	 */
	@Transactional(readOnly = true)
	public List<AgencyInvoice> getAgencyInvoiceByStatus(String status) {
		return agencyInvoiceRepository.findByInvoiceStatus(status);
	}

	/**
	 * it will find by status from repository
	 * 
	 * @param status
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<AgencyInvoice> getAgencyInvoiceByStatus(List<String> status) {
		List<AgencyInvoice> agencyInvoices = new ArrayList<AgencyInvoice>();
		if (status != null) {
			for (AgencyInvoice agencyInvoice : agencyInvoiceRepository.findByInvoiceStatusIn(status)) {
				agencyInvoice.setPendingAmount(agencyInvoice.getTotalAmount() - agencyInvoice.getPaymentReceived());
				agencyInvoices.add(agencyInvoice);
			}
			return agencyInvoices;
		} else
			return agencyInvoiceRepository.findAll();
	}

	/**
	 * It will find dynamically overdue and set the staus to overDue & return
	 * list of all invoice including overDue
	 * 
	 * @param status
	 * @return
	 */
	public List<AgencyInvoice> getAgencyInvoice(List<String> status) {
		List<AgencyInvoice> agencyInvoices = findAll();
		for (AgencyInvoice agencyInvoice : agencyInvoices) {
			if (checkDueDay(agencyInvoice.getDueDate(), agencyInvoice.getTotalAmount() - agencyInvoice.getPaymentReceived())) {
				agencyInvoice.setDelayDay(calculatePayementDelayDate(agencyInvoice.getDueDate()));
				agencyInvoice.setInvoiceStatus(InvoiceStatus.OverDue.getDisplayName());
			}
			save(agencyInvoice);
		}
		return getAgencyInvoiceByStatus(status);
	}

	/***********************************************************************************
	 * To check when dueDate exceeds and pending amount > 0 then return true
	 * else false*
	 ***********************************************************************************
	 * @param dueDate
	 * @param pendingAmount
	 * @return Boolean
	 */
	public Boolean checkDueDay(Date dueDate, double pendingAmount) {
		DateTime todaysDate = new DateTime();
		DateTime paymentDate = new DateTime(dueDate);
		LocalDate todaysDateLocale = todaysDate.toLocalDate();
		LocalDate dueDateLocale = paymentDate.toLocalDate();

		if (todaysDateLocale.compareTo(dueDateLocale) < 0)
			return false;

		else if (todaysDateLocale.compareTo(dueDateLocale) > 0 && pendingAmount > 0)
			return true;
		else
			return false;

	}

	/******************************
	 * To update PaymentDelayDate**
	 ******************************
	 */
	@Transactional
	public void updatePaymentDelayDate() {
		List<AgencyInvoice> agencyInvoices = findAll();
		for (AgencyInvoice agencyInvoice : agencyInvoices) {
			if (checkDueDay(agencyInvoice.getDueDate(), agencyInvoice.getAmount())) {
				agencyInvoice.setDelayDay(calculatePayementDelayDate(agencyInvoice.getDueDate()));
				save(agencyInvoice);
			}
		}
	}

	@Transactional(rollbackFor = { RecruizException.class })
	public Boolean createMultipleInvoiceForAgency(AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO) throws RecruizException {

		boolean isSuccess = false;
		String clientName = null;
		Long clientId = null;
		if (agencyMultipleInvoiceDTO == null)
			throw new RecruizException(ErrorHandler.INVOICE_DTO_EMPTY, ErrorHandler.INVOICE_DTO_CANT_NOT_EMPTY);

		if (agencyMultipleInvoiceDTO.getCandidateInvoices() == null)
			throw new RecruizException(ErrorHandler.INVOICE_DATA_EMPTY, ErrorHandler.INVOICE_DTO_CANT_NOT_EMPTY);

		if (agencyMultipleInvoiceDTO.getDueDate() == null)
			throw new RecruizException(ErrorHandler.DUE_DATE_MISSING, ErrorHandler.DUE_DATE_MANDATORY);

		if (agencyMultipleInvoiceDTO.getDueDate().before(new Date())) {
			throw new RecruizException(ErrorHandler.BACK_DATE_NOT_ALLOWED, ErrorHandler.BACK_DATE_SELECTED);
		}

		if (isInvoiceNumberExist(agencyMultipleInvoiceDTO.getInvoiceNumber()))
			throw new RecruizException(ErrorHandler.INVOICE_NUMBER_ALREADY_EXIST, ErrorHandler.INVOICE_NUMBER_ALREADY__EXIST);

		Set<CandidateInvoice> candidateInvoices = new HashSet<CandidateInvoice>();
		AgencyInvoice nwAgencyInvoice = new AgencyInvoice();
		for (CandidateInvoiceDTO candidateInvoiceDTO : agencyMultipleInvoiceDTO.getCandidateInvoices()) {

			if (candidateInvoiceDTO.getClientName() == null)
				throw new RecruizException(ErrorHandler.CLIENT_MISSING, ErrorHandler.CLIENT_NAME_MANDATORY);

			if (candidateInvoiceDTO.getClientId() == 0)
				throw new RecruizException(ErrorHandler.CLIENT_ID_MISSING, ErrorHandler.CLIENT_ID_MANDATORY);

			if (candidateInvoiceDTO.getPostionName() == null)
				throw new RecruizException(ErrorHandler.POSITION_NAME_MISSING, ErrorHandler.POSITION_NAME_MANDATORY);

			if (candidateInvoiceDTO.getPositionCode() == null)
				throw new RecruizException(ErrorHandler.POSTION_CODE_MISSING, ErrorHandler.POSITION_CODE_MANDATORY);

			if (candidateInvoiceDTO.getCandidateName() == null)
				throw new RecruizException(ErrorHandler.CANDIDATE_NAME_MISSING, ErrorHandler.CANDIDATE_NAME_MANDATORY);

			if (candidateInvoiceDTO.getCandidateEmail() == null)
				throw new RecruizException(ErrorHandler.CANDIDATE_EMAIL_MISSING, ErrorHandler.CANDIDATE_EMAIL_MANDATORY);

			clientName = candidateInvoiceDTO.getClientName();
			clientId = candidateInvoiceDTO.getClientId();

			CandidateInvoice candidateInvoice = new CandidateInvoice(candidateInvoiceDTO.getCandidateName(),
					candidateInvoiceDTO.getCandidateEmail(), candidateInvoiceDTO.getPostionName(), candidateInvoiceDTO.getPositionCode(),
					candidateInvoiceDTO.getJoiningDate());
			candidateInvoice.setAmount(candidateInvoiceDTO.getAmount());
			candidateInvoice.setAgencyInvoice(nwAgencyInvoice);
			candidateInvoice.setClientId(clientId);
			candidateInvoice.setClientName(clientName);
			candidateInvoices.add(candidateInvoice);
		}

		if (candidateInvoices != null && !candidateInvoices.isEmpty()) {
			try {
				nwAgencyInvoice.setInvoiceId(agencyMultipleInvoiceDTO.getId());
				nwAgencyInvoice.setInvoiceNumber(agencyMultipleInvoiceDTO.getInvoiceNumber());
				nwAgencyInvoice.setClientName(clientName);
				nwAgencyInvoice.setClientId(clientId);
				nwAgencyInvoice.setDueDate(agencyMultipleInvoiceDTO.getDueDate());
				nwAgencyInvoice.setCurrency(agencyMultipleInvoiceDTO.getCurrency());
				nwAgencyInvoice.setCreationDate(agencyMultipleInvoiceDTO.getInvoiceGeneratedDate());
				nwAgencyInvoice.setModificationDate(new Date());
				nwAgencyInvoice.setTotalAmount(agencyMultipleInvoiceDTO.getTotalAmount());
				if (agencyMultipleInvoiceDTO.getTotalAmountAfterDiscount() != null) {
					nwAgencyInvoice.setTotalAmountAfterDiscount(
							Double.parseDouble(new DecimalFormat("##.##").format(agencyMultipleInvoiceDTO.getTotalAmountAfterDiscount())));
				}
				// nwAgencyInvoice.setTotalAmountInWords(agencyMultipleInvoiceDTO.getTotalAmountInWords());
				nwAgencyInvoice.setDiscount(agencyMultipleInvoiceDTO.getDiscount());
				nwAgencyInvoice.setInformationFilledByUser(userService.getLoggedInUserEmail());
				nwAgencyInvoice.setInvoiceStatus(InvoiceStatus.Pending.getDisplayName());
				nwAgencyInvoice.setCandidateInvoices(candidateInvoices);
				nwAgencyInvoice.setAmount(agencyMultipleInvoiceDTO.getSubTotal());
				nwAgencyInvoice.setOrganizationName(agencyMultipleInvoiceDTO.getOrganizationName());
				nwAgencyInvoice.setOrganization_address_1(agencyMultipleInvoiceDTO.getOrganization_address_1());
				nwAgencyInvoice.setOrganization_address_2(agencyMultipleInvoiceDTO.getOrganization_address_2());
				nwAgencyInvoice.setOrganizationCity(agencyMultipleInvoiceDTO.getOrganizationCity());
				nwAgencyInvoice.setOrganizationState(agencyMultipleInvoiceDTO.getOrganizationState());
				nwAgencyInvoice.setOrganizationCountry(agencyMultipleInvoiceDTO.getOrganizationCountry());
				nwAgencyInvoice.setOrganizationPin(agencyMultipleInvoiceDTO.getOrganizationPin());
				nwAgencyInvoice.setOrganizationPhone(agencyMultipleInvoiceDTO.getOrganizationPhone());
				nwAgencyInvoice.setChequePayable(agencyMultipleInvoiceDTO.getChequePayable());
				nwAgencyInvoice.setOrganizationAccountName(agencyMultipleInvoiceDTO.getOrganizationAccountName());
				nwAgencyInvoice.setOrganizationAccountNumber(agencyMultipleInvoiceDTO.getOrganizationAccountNumber());
				nwAgencyInvoice.setOrganizationBankName(agencyMultipleInvoiceDTO.getOrganizationBankName());
				nwAgencyInvoice.setOrganizationBankBranchName(agencyMultipleInvoiceDTO.getOrganizationBankBranchName());
				nwAgencyInvoice.setOrganizationBankIfsc(agencyMultipleInvoiceDTO.getOrganizationBankIfsc());
				nwAgencyInvoice.setNote(agencyMultipleInvoiceDTO.getNote());
				nwAgencyInvoice.setBillClientName(agencyMultipleInvoiceDTO.getBillClientName());
				nwAgencyInvoice.setBillContactName(agencyMultipleInvoiceDTO.getBillContactName());
				nwAgencyInvoice.setBill_address_1(agencyMultipleInvoiceDTO.getBill_address_1());
				nwAgencyInvoice.setBill_address_2(agencyMultipleInvoiceDTO.getBill_address_2());
				nwAgencyInvoice.setBillCity(agencyMultipleInvoiceDTO.getBillCity());
				nwAgencyInvoice.setBillState(agencyMultipleInvoiceDTO.getBillState());
				nwAgencyInvoice.setBillCountry(agencyMultipleInvoiceDTO.getBillCountry());
				nwAgencyInvoice.setBillPin(agencyMultipleInvoiceDTO.getBillPin());
				nwAgencyInvoice.setBillPhone(agencyMultipleInvoiceDTO.getBillPhone());
				Map<String, Double> taxDetails = dataModelToDTOConversionService
						.convertTaxDetails(agencyMultipleInvoiceDTO.getTaxCalculateDTOs());
				Map<String, String> taxRelatedDetails = dataModelToDTOConversionService
						.convertTaxRelatedDetails(agencyMultipleInvoiceDTO.getGstAndPan());
				if (taxRelatedDetails != null) {
					nwAgencyInvoice.setTaxRelatedDetails(taxRelatedDetails);
				} else {
					nwAgencyInvoice.setTaxRelatedDetails(new HashMap<String, String>());
				}
				nwAgencyInvoice.setTaxDetails(taxDetails);
				save(nwAgencyInvoice);

				// add tax info in tax table but not make duplicate
				if (agencyMultipleInvoiceDTO.getGstAndPan() != null) {
					for (TaxRelatedDetailsDTO gstAndPan : agencyMultipleInvoiceDTO.getGstAndPan()) {
						if (!taxService.isTaxNameExist(gstAndPan.getName())) {
							Tax tax = new Tax();
							tax.setTaxName(gstAndPan.getName());
							if (gstAndPan.getValue() != null && !gstAndPan.getValue().isEmpty()) {
								tax.setTaxNumber(gstAndPan.getValue());
							} else {
								// tax value is not present then don't save tax
								continue;
							}
							taxService.save(tax);
						}
					}
				}
				// checking invoice setting having information or not. if not
				// just keep
				// in invoice setting . and if it is already then just update
				// the invoice setting
				if (invoiceSettingsService.getInvoiceSettings() == null) {
					invoiceSettingsService.addInvoiceSettings(
							invoiceSettingsService.convertInvoiceDTO(agencyMultipleInvoiceDTO, taxDetails, taxRelatedDetails));
				} else {
					InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
					invoiceSettings.setOrganizationName(agencyMultipleInvoiceDTO.getOrganizationName());
					invoiceSettings.setOrganization_address_1(agencyMultipleInvoiceDTO.getOrganization_address_1());
					invoiceSettings.setOrganization_address_2(agencyMultipleInvoiceDTO.getOrganization_address_2());
					invoiceSettings.setOrganizationCity(agencyMultipleInvoiceDTO.getOrganizationCity());
					invoiceSettings.setOrganizationState(agencyMultipleInvoiceDTO.getOrganizationState());
					invoiceSettings.setOrganizationCountry(agencyMultipleInvoiceDTO.getOrganizationCountry());
					invoiceSettings.setOrganizationPin(agencyMultipleInvoiceDTO.getOrganizationPin());
					invoiceSettings.setOrganizationPhone(agencyMultipleInvoiceDTO.getOrganizationPhone());
					invoiceSettings.setChequePayable(agencyMultipleInvoiceDTO.getChequePayable());
					invoiceSettings.setOrganizationAccountName(agencyMultipleInvoiceDTO.getOrganizationAccountName());
					invoiceSettings.setOrganizationAccountNumber(agencyMultipleInvoiceDTO.getOrganizationAccountNumber());
					invoiceSettings.setOrganizationBankName(agencyMultipleInvoiceDTO.getOrganizationBankName());
					invoiceSettings.setOrganizationBankBranchName(agencyMultipleInvoiceDTO.getOrganizationBankBranchName());
					invoiceSettings.setOrganizationBankIfsc(agencyMultipleInvoiceDTO.getOrganizationBankIfsc());
					invoiceSettings.setNote(agencyMultipleInvoiceDTO.getNote());
					invoiceSettings.setBillClientName(agencyMultipleInvoiceDTO.getBillClientName());
					invoiceSettings.setBillContactName(agencyMultipleInvoiceDTO.getBillContactName());
					invoiceSettings.setBill_address_1(agencyMultipleInvoiceDTO.getBill_address_1());
					invoiceSettings.setBill_address_2(agencyMultipleInvoiceDTO.getBill_address_2());
					invoiceSettings.setBillCity(agencyMultipleInvoiceDTO.getBillCity());
					invoiceSettings.setBillState(agencyMultipleInvoiceDTO.getBillState());
					invoiceSettings.setBillCountry(agencyMultipleInvoiceDTO.getBillCountry());
					invoiceSettings.setBillPin(agencyMultipleInvoiceDTO.getBillPin());
					invoiceSettings.setBillPhone(agencyMultipleInvoiceDTO.getBillPhone());
					if (taxDetails != null) {
						invoiceSettings.getTaxDetails().clear();
						invoiceSettings.getTaxDetails().putAll(taxDetails);
					}

					if (taxRelatedDetails != null) {
						invoiceSettings.getTaxRelatedDetails().clear();
						invoiceSettings.getTaxRelatedDetails().putAll(taxRelatedDetails);
					}

					invoiceSettingsService.save(invoiceSettings);
				}
				isSuccess = true;

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RecruizException(ErrorHandler.GENERATING_OF_INVOICE_FAILED, ErrorHandler.INVOICE_NOT_GENERATED);
			}
		}
		return isSuccess;
	}

	/***************************************
	 * To update Agency Invoice ************
	 ***************************************
	 * @param agencyInvoiceDTO
	 * @return AgencyInvoice
	 * @throws RecruizException
	 */
	@Transactional
	public AgencyInvoice updateInvoiceForInvoice(AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO) throws RecruizException {

		AgencyInvoice toUpdateAgencyInvoice = null;

		String clientName = null;
		Long clientId = null;

		if (!exists(agencyMultipleInvoiceDTO.getInvoiceId()))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		if (agencyMultipleInvoiceDTO.getCandidateInvoices() == null)
			throw new RecruizException(ErrorHandler.INVOICE_DATA_EMPTY, ErrorHandler.INVOICE_DTO_CANT_NOT_EMPTY);

		if (agencyMultipleInvoiceDTO.getDueDate() == null)
			throw new RecruizException(ErrorHandler.DUE_DATE_MISSING, ErrorHandler.DUE_DATE_MANDATORY);

		if (agencyMultipleInvoiceDTO.getDueDate().before(new Date())) {
			throw new RecruizException(ErrorHandler.BACK_DATE_NOT_ALLOWED, ErrorHandler.BACK_DATE_SELECTED);
		}

		toUpdateAgencyInvoice = findOne(agencyMultipleInvoiceDTO.getInvoiceId());
		if (toUpdateAgencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Archive.getDisplayName()))
			throw new RecruizException(ErrorHandler.CAN_NOT_UPDATE_ARCHIVE_INVOICE, ErrorHandler.UPDATE_ARCHIVE_INVOICE);

		if (isInvoiceNumberExist(agencyMultipleInvoiceDTO.getInvoiceNumber())) {
			if (!toUpdateAgencyInvoice.getInvoiceNumber().equals(agencyMultipleInvoiceDTO.getInvoiceNumber()))
				throw new RecruizException(ErrorHandler.INVOICE_NUMBER_ALREADY_EXIST, ErrorHandler.INVOICE_NUMBER_ALREADY__EXIST);

		}

		Set<CandidateInvoice> candidateInvoices = new HashSet<CandidateInvoice>();

		// double amount = 0;
		for (CandidateInvoiceDTO candidateInvoiceDTO : agencyMultipleInvoiceDTO.getCandidateInvoices()) {

			if (candidateInvoiceDTO.getClientName() == null)
				throw new RecruizException(ErrorHandler.CLIENT_MISSING, ErrorHandler.CLIENT_NAME_MANDATORY);

			if (candidateInvoiceDTO.getClientId() == 0)
				throw new RecruizException(ErrorHandler.CLIENT_ID_MISSING, ErrorHandler.CLIENT_ID_MANDATORY);

			if (candidateInvoiceDTO.getPostionName() == null)
				throw new RecruizException(ErrorHandler.POSITION_NAME_MISSING, ErrorHandler.POSITION_NAME_MANDATORY);

			if (candidateInvoiceDTO.getPositionCode() == null)
				throw new RecruizException(ErrorHandler.POSTION_CODE_MISSING, ErrorHandler.POSITION_CODE_MANDATORY);

			if (candidateInvoiceDTO.getCandidateName() == null)
				throw new RecruizException(ErrorHandler.CANDIDATE_NAME_MISSING, ErrorHandler.CANDIDATE_NAME_MANDATORY);

			if (candidateInvoiceDTO.getCandidateEmail() == null)
				throw new RecruizException(ErrorHandler.CANDIDATE_EMAIL_MISSING, ErrorHandler.CANDIDATE_EMAIL_MANDATORY);

			clientName = candidateInvoiceDTO.getClientName();
			clientId = candidateInvoiceDTO.getClientId();

			CandidateInvoice candidateInvoice = new CandidateInvoice(candidateInvoiceDTO.getCandidateName(),
					candidateInvoiceDTO.getCandidateEmail(), candidateInvoiceDTO.getPostionName(), candidateInvoiceDTO.getPositionCode(),
					candidateInvoiceDTO.getJoiningDate());
			candidateInvoice.setAgencyInvoice(toUpdateAgencyInvoice);
			candidateInvoice.setId(candidateInvoiceDTO.getCandidateInvoiceId());
			candidateInvoice.setAmount(candidateInvoiceDTO.getAmount());
			candidateInvoice.setCreationDate(
					candidateInvoiceService.getByCandidateInvoiceId(candidateInvoiceDTO.getCandidateInvoiceId()).getCreationDate());
			candidateInvoice.setClientId(clientId);
			candidateInvoice.setClientName(clientName);
			candidateInvoices.add(candidateInvoice);
		}
		try {

			toUpdateAgencyInvoice.setInvoiceId(agencyMultipleInvoiceDTO.getId());
			toUpdateAgencyInvoice.setInvoiceNumber(agencyMultipleInvoiceDTO.getInvoiceNumber());
			toUpdateAgencyInvoice.setClientName(clientName);
			toUpdateAgencyInvoice.setClientId(clientId);
			toUpdateAgencyInvoice.setDueDate(agencyMultipleInvoiceDTO.getDueDate());
			toUpdateAgencyInvoice.setCurrency(agencyMultipleInvoiceDTO.getCurrency());
			toUpdateAgencyInvoice.setCreationDate(agencyMultipleInvoiceDTO.getInvoiceGeneratedDate());
			toUpdateAgencyInvoice.setModificationDate(new Date());

			toUpdateAgencyInvoice.setTotalAmount(agencyMultipleInvoiceDTO.getTotalAmount());
			if (agencyMultipleInvoiceDTO.getTotalAmountAfterDiscount() != null) {
				toUpdateAgencyInvoice.setTotalAmountAfterDiscount(
						Double.parseDouble(new DecimalFormat("##.##").format(agencyMultipleInvoiceDTO.getTotalAmountAfterDiscount())));
			}
			// nwAgencyInvoice.setTotalAmountInWords(agencyMultipleInvoiceDTO.getTotalAmountInWords());
			toUpdateAgencyInvoice.setDiscount(agencyMultipleInvoiceDTO.getDiscount());
			toUpdateAgencyInvoice.setInformationFilledByUser(userService.getLoggedInUserEmail());
			// nwAgencyInvoice.setInvoiceStatus(InvoiceStatus.Pending.getDisplayName());
			toUpdateAgencyInvoice.getCandidateInvoices().clear();
			toUpdateAgencyInvoice.getCandidateInvoices().addAll(candidateInvoices);
			toUpdateAgencyInvoice.setAmount(agencyMultipleInvoiceDTO.getSubTotal());
			toUpdateAgencyInvoice.setOrganizationName(agencyMultipleInvoiceDTO.getOrganizationName());
			toUpdateAgencyInvoice.setOrganization_address_1(agencyMultipleInvoiceDTO.getOrganization_address_1());
			toUpdateAgencyInvoice.setOrganization_address_2(agencyMultipleInvoiceDTO.getOrganization_address_2());
			toUpdateAgencyInvoice.setOrganizationCity(agencyMultipleInvoiceDTO.getOrganizationCity());
			toUpdateAgencyInvoice.setOrganizationState(agencyMultipleInvoiceDTO.getOrganizationState());
			toUpdateAgencyInvoice.setOrganizationCountry(agencyMultipleInvoiceDTO.getOrganizationCountry());
			toUpdateAgencyInvoice.setOrganizationPin(agencyMultipleInvoiceDTO.getOrganizationPin());
			toUpdateAgencyInvoice.setOrganizationPhone(agencyMultipleInvoiceDTO.getOrganizationPhone());
			toUpdateAgencyInvoice.setChequePayable(agencyMultipleInvoiceDTO.getChequePayable());
			toUpdateAgencyInvoice.setOrganizationAccountName(agencyMultipleInvoiceDTO.getOrganizationAccountName());
			toUpdateAgencyInvoice.setOrganizationAccountNumber(agencyMultipleInvoiceDTO.getOrganizationAccountNumber());
			toUpdateAgencyInvoice.setOrganizationBankName(agencyMultipleInvoiceDTO.getOrganizationBankName());
			toUpdateAgencyInvoice.setOrganizationBankBranchName(agencyMultipleInvoiceDTO.getOrganizationBankBranchName());
			toUpdateAgencyInvoice.setOrganizationBankIfsc(agencyMultipleInvoiceDTO.getOrganizationBankIfsc());
			toUpdateAgencyInvoice.setNote(agencyMultipleInvoiceDTO.getNote());
			toUpdateAgencyInvoice.setBillClientName(agencyMultipleInvoiceDTO.getBillClientName());
			toUpdateAgencyInvoice.setBillContactName(agencyMultipleInvoiceDTO.getBillContactName());
			toUpdateAgencyInvoice.setBill_address_1(agencyMultipleInvoiceDTO.getBill_address_1());
			toUpdateAgencyInvoice.setBill_address_2(agencyMultipleInvoiceDTO.getBill_address_2());
			toUpdateAgencyInvoice.setBillCity(agencyMultipleInvoiceDTO.getBillCity());
			toUpdateAgencyInvoice.setBillState(agencyMultipleInvoiceDTO.getBillState());
			toUpdateAgencyInvoice.setBillCountry(agencyMultipleInvoiceDTO.getBillCountry());
			toUpdateAgencyInvoice.setBillPin(agencyMultipleInvoiceDTO.getBillPin());
			toUpdateAgencyInvoice.setBillPhone(agencyMultipleInvoiceDTO.getBillPhone());
			Map<String, Double> taxDetails = dataModelToDTOConversionService
					.convertTaxDetails(agencyMultipleInvoiceDTO.getTaxCalculateDTOs());
			Map<String, String> taxRelatedDetails = dataModelToDTOConversionService
					.convertTaxRelatedDetails(agencyMultipleInvoiceDTO.getGstAndPan());
			if (taxRelatedDetails != null) {
				toUpdateAgencyInvoice.getTaxRelatedDetails().clear();
				toUpdateAgencyInvoice.getTaxRelatedDetails().putAll(taxRelatedDetails);

			} else {
				toUpdateAgencyInvoice.getTaxRelatedDetails().putAll(new HashMap<String, String>());
			}

			if (taxDetails != null && !taxDetails.isEmpty()) {
				toUpdateAgencyInvoice.getTaxDetails().clear();
				toUpdateAgencyInvoice.getTaxDetails().putAll(taxDetails);
			}
			toUpdateAgencyInvoice = save(toUpdateAgencyInvoice);
			// here while updating if you update the amount when invoice in paid
			// state for that useCase written code below
			Double amount = checkCalculateOnToatalAmountOrTotalAmountAfterDiscount(toUpdateAgencyInvoice);
			Double pendingAmount = amount - toUpdateAgencyInvoice.getPaymentReceived();
			if (pendingAmount <= 0.0) {
				toUpdateAgencyInvoice.setDelayDay(0);
				toUpdateAgencyInvoice.setInvoiceStatus(InvoiceStatus.Paid.getDisplayName());
			} else if (toUpdateAgencyInvoice.getPaymentReceived() > 0.0) {
				toUpdateAgencyInvoice.setInvoiceStatus(InvoiceStatus.PartialPayment.getDisplayName());
			} else {
				toUpdateAgencyInvoice.setInvoiceStatus(InvoiceStatus.Pending.getDisplayName());
			}

			toUpdateAgencyInvoice = save(toUpdateAgencyInvoice);
			if (agencyMultipleInvoiceDTO.getGstAndPan() != null) {
				for (TaxRelatedDetailsDTO gstAndPan : agencyMultipleInvoiceDTO.getGstAndPan()) {
					if (!taxService.isTaxNameExist(gstAndPan.getName())) {
						Tax tax = new Tax();
						tax.setTaxName(gstAndPan.getName());
						if (gstAndPan.getValue() != null && !gstAndPan.getValue().isEmpty()) {
							tax.setTaxNumber(gstAndPan.getValue());
						} else {
							// tax value is not present then don't save tax
							continue;
						}
						taxService.save(tax);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return toUpdateAgencyInvoice;
	}

	/****************************
	 * To delete Agency Invoice**
	 ****************************
	 * @param invoiceId
	 * @return Boolean
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean deleteAgencyInvoice(long invoiceId) throws RecruizException {
		Boolean isDeleted = false;

		if (!exists(invoiceId))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		try {

			// CandidateStatus candidateStatus =
			// findOne(invoiceId).getCandidateStatusId();
			delete(invoiceId);

			// candidateStatusService.deleteByCandidateIdAndPositionCode(candidateStatus.getCandidate().getCid(),
			// candidateStatus.getPosition().getPositionCode());
			isDeleted = !exists(invoiceId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return isDeleted;
	}

	/***************************
	 * To generate pdf invoice**
	 ***************************
	 * @param invoiceId
	 * @return File
	 * @throws RecruizException
	 */
	public File getPdfInvoice(long invoiceId) throws RecruizException {

		if (!exists(invoiceId))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		final String invoiceTemplate = GlobalConstants.AGENCY_INVOICE_TEMPLATE_NAME;
		AgencyInvoice agencyInvoice = getAgencyInvoice(invoiceId);
		Map<String, Object> valueMap = new HashMap<>();

		valueMap.put("invoice", agencyInvoice.getId());
		valueMap.put("date", DateUtil.formateDate(agencyInvoice.getCreationDate()));
		valueMap.put("dueDate", DateUtil.formateDate(agencyInvoice.getDueDate()));
		valueMap.put("amount", agencyInvoice.getAmount());
		valueMap.put("currency", agencyInvoice.getCurrency());
		valueMap.put("clientName", agencyInvoice.getClientName());
		// valueMap.put("candidateName", agencyInvoice.getCandidateName());
		// valueMap.put("postionName", agencyInvoice.getPostionName());
		// valueMap.put("offeredDate",
		// DateUtil.formateDate(agencyInvoice.getOfferedDate()));
		// valueMap.put("joiningDate",
		// agencyInvoice.getJoiningDate() != null ?
		// DateUtil.formateDate(agencyInvoice.getJoiningDate()) : "");
		valueMap.put("invoiceStatus", agencyInvoice.getInvoiceStatus());
		// valueMap.put("offeredSalary", agencyInvoice.getOfferedSalary());
		// valueMap.put("taxes", agencyInvoice.getTaxes());
		valueMap.put("discount", agencyInvoice.getDiscount());
		valueMap.put("totalAmounts", agencyInvoice.getTotalAmount());
		valueMap.put("totalAmountsInWords", agencyInvoice.getTotalAmountInWords());
		// valueMap.put("comments", agencyInvoice.getComments());

		String emailHTMLContent = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, invoiceTemplate, "UTF-8", valueMap);

		try {
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File pdfInvoice = new File(tempDir, "AgencyInvoice" + agencyInvoice.getId() + ".pdf");
			if (!pdfInvoice.exists()) {
				pdfInvoice.createNewFile();
			}
			OutputStream file = new FileOutputStream(pdfInvoice);
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, file);
			document.open();
			InputStream stream = new ByteArrayInputStream(emailHTMLContent.getBytes(StandardCharsets.UTF_8));

			// HTMLWorker htmlWorker = new HTMLWorker(document);
			// htmlWorker.parse(new StringReader(emailHTMLContent));
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, stream);
			document.close();
			file.close();
			return pdfInvoice;
		} catch (Exception e) {
			logger.error("Error creating invoice + \n" + e.getMessage(), e);
			return null;
		}

	}

	/****************************************
	 * To get List of RoundCandidate joined**
	 ****************************************
	 * @return
	 * @throws RecruizException
	 */
	public List<RoundCandidate> getJoinedCandidate() throws RecruizException {
		List<RoundCandidate> newRoundCandidates = new ArrayList<RoundCandidate>();
		List<Position> positions = positionService.findAll();
		for (Position position : positions) {
			for (RoundCandidate roundCandidate : roundCandidateService.getByPostionCodeAndStatus(position.getPositionCode(),
					BoardStatus.Joined.getDisplayName())) {
				newRoundCandidates.add(roundCandidate);
			}
		}
		return newRoundCandidates;
	}

	public JoinedCandidateDTO convertCandidateStatus(CandidateStatus candidateStatus) throws RecruizException {

		JoinedCandidateDTO joinedCandidateDTO = new JoinedCandidateDTO();
		Candidate candidate = candidateStatus.getCandidate();
		Position position = candidateStatus.getPosition();
		Client client = candidateStatus.getClient();
		joinedCandidateDTO.setCid(candidate.getCid());
		joinedCandidateDTO.setFullName(candidate.getFullName());
		joinedCandidateDTO.setMobile(candidate.getMobile());
		joinedCandidateDTO.setEmail(candidate.getEmail());
		joinedCandidateDTO.setPreferredLocation(candidate.getPreferredLocation());
		joinedCandidateDTO.setKeySkills(candidate.getKeySkills());
		joinedCandidateDTO.setResumeLink(candidate.getResumeLink());
		joinedCandidateDTO.setCurrentCompany(candidate.getCurrentCompany());
		joinedCandidateDTO.setCurrentTitle(candidate.getCurrentTitle());
		joinedCandidateDTO.setCurrentLocation(candidate.getCurrentLocation());
		joinedCandidateDTO.setHighestQual(candidate.getHighestQual());
		joinedCandidateDTO.setTotalExp(candidate.getTotalExp());
		joinedCandidateDTO.setEmploymentType(candidate.getEmploymentType());
		joinedCandidateDTO.setCurrentCtc(candidate.getCurrentCtc());
		joinedCandidateDTO.setExpectedCtc(candidate.getExpectedCtc());
		joinedCandidateDTO.setCtcUnit(candidate.getCtcUnit());
		joinedCandidateDTO.setNoticePeriod(candidate.getNoticePeriod());
		joinedCandidateDTO.setNoticeStatus(candidate.isNoticeStatus());
		joinedCandidateDTO.setOfferedDate(candidateStatusService.getOfferedDate(client.getId(), position.getPositionCode(),
				candidate.getCid(), BoardStatus.Offered.getDisplayName()));
		joinedCandidateDTO.setJoinedDate(candidateStatus.getStatusChangedDate());
		joinedCandidateDTO.setAlternateEmail(candidate.getAlternateEmail());
		joinedCandidateDTO.setAlternateMobile(candidate.getAlternateMobile());
		joinedCandidateDTO.setSourceName(candidate.getSourceName());
		joinedCandidateDTO.setSourceEmail(candidate.getSourceEmail());
		joinedCandidateDTO.setSourceMobile(candidate.getSourceMobile());
		joinedCandidateDTO.setSource(candidate.getSource());
		joinedCandidateDTO.setSourceDetails(candidate.getSourceDetails());
		joinedCandidateDTO.setSourcedOnDate(candidate.getSourcedOnDate());
		joinedCandidateDTO.setCreationDate(candidate.getCreationDate());
		joinedCandidateDTO.setModificationDate(candidate.getModificationDate());
		joinedCandidateDTO.setDob(candidate.getDob());
		joinedCandidateDTO.setCandidateOwner(candidate.getOwner());
		joinedCandidateDTO.setClientId(client.getId());
		joinedCandidateDTO.setClientName(client.getClientName());
		joinedCandidateDTO.setClientAddress(client.getAddress());
		joinedCandidateDTO.setClientWebsite(client.getWebsite());
		joinedCandidateDTO.setClientLocation(client.getClientLocation());
		joinedCandidateDTO.setEmpSize(client.getEmpSize());
		joinedCandidateDTO.setTurnOvr(client.getTurnOvr());
		joinedCandidateDTO.setNotes(client.getNotes());
		joinedCandidateDTO.setClientStatus(client.getStatus());
		joinedCandidateDTO.setClientOwner(client.getOwner());
		joinedCandidateDTO.setPostionId(position.getId());
		joinedCandidateDTO.setPositionCode(position.getPositionCode());
		joinedCandidateDTO.setPostionTitle(position.getTitle());
		joinedCandidateDTO.setPostionOpenedDate(position.getOpenedDate());
		joinedCandidateDTO.setPostionClosedDate(position.getClosedDate());
		joinedCandidateDTO.setPostionCloseByDate(position.getCloseByDate());
		joinedCandidateDTO.setPostionIndustry(position.getIndustry());
		joinedCandidateDTO.setPostionFunctionalArea(position.getFunctionalArea());

		/*
		 * if (candidateStatus.getAgencyInvoices() != null &&
		 * !candidateStatus.getAgencyInvoices().isEmpty()) {
		 * joinedCandidateDTO.setInvoiceId( new
		 * ArrayList<AgencyInvoice>(candidateStatus.getAgencyInvoices()).get(0).
		 * getInvoiceId() + ""); }
		 */

		CandidateInvoice candidateInvoice = candidateInvoiceService.getByCandidateEmailAndPostionCodeAndClientName(candidate.getEmail(),
				position.getPositionCode(), client.getClientName());
		if (candidateInvoice != null) {
			joinedCandidateDTO.setInvoiceId(candidateInvoice.getAgencyInvoice().getId() + "");
		}
		return joinedCandidateDTO;
	}

	/********************************************
	 * To get Page object of JoinedCandidateDTO**
	 ********************************************
	 * @param pageable
	 * @param joinedCandidateDTOs
	 * @return Page<JoinedCandidateDTO>
	 * @throws RecruizException
	 */
	public Page<JoinedCandidateDTO> getPagebleJoinedCandidateDTO(Pageable pageable, List<JoinedCandidateDTO> joinedCandidateDTOs)
			throws RecruizException {
		int start = pageable.getOffset();
		int end = (start + pageable.getPageSize()) > joinedCandidateDTOs.size() ? joinedCandidateDTOs.size()
				: (start + pageable.getPageSize());
		final Page<JoinedCandidateDTO> page = new PageImpl<JoinedCandidateDTO>(joinedCandidateDTOs.subList(start, end), pageable,
				joinedCandidateDTOs.size());
		return page;
	}

	/**
	 * This method is used to calculate everyThing on Total Amount or Total
	 * Amount After Discount
	 * 
	 * @param agencyInvoice
	 * @return
	 */
	public double checkCalculateOnToatalAmountOrTotalAmountAfterDiscount(AgencyInvoice agencyInvoice) {

		if (agencyInvoice.getTotalAmountAfterDiscount() == 0)
			return agencyInvoice.getTotalAmount();
		else
			return agencyInvoice.getTotalAmountAfterDiscount();
	}

	/**
	 * This will give you Page<AgencyInvoice> with delayDay calculation
	 * 
	 * @param pageable
	 * @param agencyInvoices
	 * @return Page<AgencyInvoice>
	 * @throws RecruizException
	 */
	public Page<AgencyMultipleInvoiceDTO> getPagebleAgencyInvoice(Pageable pageable, List<AgencyInvoice> agencyInvoices)
			throws RecruizException {

		List<AgencyInvoice> modifiedAgencyInvoices = new ArrayList<AgencyInvoice>();
		for (AgencyInvoice agencyInvoice : agencyInvoices) {

			double calculateOnWhichAmount = checkCalculateOnToatalAmountOrTotalAmountAfterDiscount(agencyInvoice);
			if (checkDueDay(agencyInvoice.getDueDate(), calculateOnWhichAmount - agencyInvoice.getPaymentReceived())) {
				if (!agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Archive.getDisplayName())) {
					agencyInvoice.setDelayDay(calculatePayementDelayDate(agencyInvoice.getDueDate()));
					agencyInvoice.setInvoiceStatus(InvoiceStatus.OverDue.getDisplayName());
				}
			}
			Float pendingAmountInFloat = (float) (calculateOnWhichAmount - agencyInvoice.getPaymentReceived());

			agencyInvoice.setPendingAmount(pendingAmountInFloat);
			modifiedAgencyInvoices.add(agencyInvoice);
		}

		List<AgencyMultipleInvoiceDTO> agencyInvoiceDTOs = new ArrayList<AgencyMultipleInvoiceDTO>();

		for (AgencyInvoice agencyInvoice : modifiedAgencyInvoices) {
			AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO = dataModelToDTOConversionService.convertAgencyInvoice(agencyInvoice);
			agencyInvoiceDTOs.add(agencyMultipleInvoiceDTO);
		}

		int start = pageable.getOffset();
		int end = (start + pageable.getPageSize()) > agencyInvoiceDTOs.size() ? agencyInvoiceDTOs.size() : (start + pageable.getPageSize());
		final Page<AgencyMultipleInvoiceDTO> page = new PageImpl<AgencyMultipleInvoiceDTO>(agencyInvoiceDTOs.subList(start, end), pageable,
				agencyInvoiceDTOs.size());
		return page;
	}

	/******************************************************************
	 * To get New , Issued , Pending , Closed Count for Agency Invoice*
	 ******************************************************************
	 * @return AgencyInvoiceStatusCountDTO
	 * @throws RecruizException
	 */
	public AgencyInvoiceStatusCountDTO getInvoiceStatusCount() throws RecruizException {
		AgencyInvoiceStatusCountDTO agencyInvoiceStatusCountDTO = new AgencyInvoiceStatusCountDTO();
		List<AgencyInvoice> agencyInvoices = findAll();
		int partialPayment = 0;
		int paid = 0;
		int archive = 0;
		int overDue = 0;
		int pending = 0;
		for (AgencyInvoice agencyInvoice : agencyInvoices) {
			double calculateOnWhichAmount = checkCalculateOnToatalAmountOrTotalAmountAfterDiscount(agencyInvoice);
			if (checkDueDay(agencyInvoice.getDueDate(), calculateOnWhichAmount - agencyInvoice.getPaymentReceived())) {
				if (agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Archive.getDisplayName())) {
					archive++;
				} else {
					agencyInvoice.setDelayDay(calculatePayementDelayDate(agencyInvoice.getDueDate()));
					agencyInvoice.setInvoiceStatus(InvoiceStatus.OverDue.getDisplayName());
					overDue++;
				}
			} else if (agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Paid.getDisplayName())) {
				paid++;
			} else if (agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.PartialPayment.getDisplayName())) {
				partialPayment++;
			} else if (agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Pending.getDisplayName())) {
				pending++;
			} else if (agencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Archive.getDisplayName())) {
				archive++;
			}

		}
		agencyInvoiceStatusCountDTO.setArchiveInvoice(archive);
		agencyInvoiceStatusCountDTO.setOverDueInvoice(overDue);
		agencyInvoiceStatusCountDTO.setPaidInvoice(paid);
		agencyInvoiceStatusCountDTO.setPartialPaymentInvoice(partialPayment);
		agencyInvoiceStatusCountDTO.setPendingInvoice(pending);
		agencyInvoiceStatusCountDTO.setYetToProcessInvoiceCandidate(countYetToProcessInvoiceCandidate());
		return agencyInvoiceStatusCountDTO;
	}

	/************************************
	 * To pay Payment for Agency Invoice*
	 ************************************
	 * @param agencyInvoiceDTO
	 * @return
	 * @throws RecruizException
	 */

	@Transactional
	public AgencyInvoice payPayment(AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO) throws RecruizException {

		AgencyInvoice payAgencyInvoice = null;
		if (!exists(agencyMultipleInvoiceDTO.getInvoiceId()))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		payAgencyInvoice = findOne(agencyMultipleInvoiceDTO.getInvoiceId());

		if (payAgencyInvoice.getInvoiceStatus().equals(InvoiceStatus.Archive.getDisplayName()))
			throw new RecruizException(ErrorHandler.CAN_NOT_PAY_FOR_ARCHIVE_INVOICE, ErrorHandler.PAYMENT_NOT_ALLOWED_ARCHIVE_INVOICE);

		try {

			Double amount = checkCalculateOnToatalAmountOrTotalAmountAfterDiscount(payAgencyInvoice);
			Double pendingAmount = amount - (agencyMultipleInvoiceDTO.getTotalAmount() + payAgencyInvoice.getPaymentReceived());
			if (pendingAmount <= 0.0) {
				payAgencyInvoice.setDelayDay(0);
				payAgencyInvoice.setInvoiceStatus(InvoiceStatus.Paid.getDisplayName());
			} else {
				payAgencyInvoice.setInvoiceStatus(InvoiceStatus.PartialPayment.getDisplayName());
			}
			payAgencyInvoice.setPaymentReceivedDate(new Date());
			payAgencyInvoice.setPaymentReceived(payAgencyInvoice.getPaymentReceived() + agencyMultipleInvoiceDTO.getTotalAmount());
			// Adding entry to AgencyInvoicePaymentHistory
			AgencyInvoicePaymentHistory agencyInvoicePaymentHistory = new AgencyInvoicePaymentHistory(amount,
					agencyMultipleInvoiceDTO.getTotalAmount(), payAgencyInvoice.getCurrency());
			agencyInvoicePaymentHistory.setPaymentReceivedBy(userService.getLoggedInUserEmail());
			// agencyInvoicePaymentHistory =
			// agencyInvoicePaymentHistoryService.save(agencyInvoicePaymentHistory);
			Set<AgencyInvoicePaymentHistory> paymentHistories = new LinkedHashSet<AgencyInvoicePaymentHistory>();
			paymentHistories.add(agencyInvoicePaymentHistory);
			payAgencyInvoice.getAgencyInvoicePaymentHistories().addAll(paymentHistories);
			payAgencyInvoice = save(payAgencyInvoice);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return payAgencyInvoice;
	}

	/******************************
	 * To change status to archive*
	 ******************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public AgencyInvoice changeStatusToArchive(long id) throws RecruizException {
		if (!exists(id))
			throw new RecruizException(ErrorHandler.AGENCY_INVOICE_ID_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);
		try {
			AgencyInvoice agencyInvoice = findOne(id);
			agencyInvoice.setInvoiceStatus(InvoiceStatus.Archive.getDisplayName());
			return save(agencyInvoice);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**************************************************************
	 * To convert List<CandidateStatus> to List<JoinedCandidateDTO>*
	 **************************************************************
	 * @param roundCandidates
	 * @return List<JoinedCandidateDTO>
	 * @throws RecruizException
	 */
	public List<JoinedCandidateDTO> convertCandidateStatus(List<CandidateStatus> candidateStatuses) throws RecruizException {
		List<JoinedCandidateDTO> joinedCandidateDTOs = new ArrayList<JoinedCandidateDTO>();
		for (CandidateStatus candidateStatus : candidateStatuses) {
			joinedCandidateDTOs.add(convertCandidateStatus(candidateStatus));
		}
		return joinedCandidateDTOs;
	}

	public List<AgencyInvoice> getCandidateInvoices(Candidate candidate) {
		List<AgencyInvoice> agencyInvoices = new ArrayList<AgencyInvoice>();
		List<CandidateInvoice> candidateInvoices = candidateInvoiceService.getByCandidateInvoiceEmail(candidate.getEmail());
		for (CandidateInvoice candidateInvoice : candidateInvoices) {
			agencyInvoices.add(candidateInvoice.getAgencyInvoice());
		}
		return agencyInvoices;
	}

	/*******************************************************
	 * calculate percentage and get 2 digit after decimal***
	 *******************************************************
	 * @param calculationDTO
	 * @return
	 */
	public CalculationDTO calculatePercentage(CalculationDTO calculationDTO) {
		double percentageValues = 0;
		List<CalculatePercentDTO> calculatePercentDTOs = new ArrayList<CalculatePercentDTO>();
		if (calculationDTO != null) {
			for (CalculatePercentDTO calculatePercentDTO : calculationDTO.getCalculatePercentDTOs()) {
				if (calculatePercentDTO != null) {
					double percentValue = Double.parseDouble(
							new DecimalFormat("##.##").format(calculationDTO.getSubAmount() * calculatePercentDTO.getPercent() / 100));
					CalculatePercentDTO calculatePercent = new CalculatePercentDTO(percentValue, calculatePercentDTO.getPercent(),
							calculatePercentDTO.getTaxName());
					percentageValues += percentValue;
					calculatePercentDTOs.add(calculatePercent);
				}
			}
		}

		double discountValue = Double
				.parseDouble(new DecimalFormat("##.##").format(calculationDTO.getSubAmount() * calculationDTO.getDiscount() / 100));
		CalculationDTO nwCalculationDTO = new CalculationDTO();
		nwCalculationDTO.setSubAmount(calculationDTO.getSubAmount());
		nwCalculationDTO.setTotalAmount(
				Double.parseDouble(new DecimalFormat("##.##").format((percentageValues + calculationDTO.getSubAmount()) - discountValue)));
		nwCalculationDTO.setCalculatePercentDTOs(calculatePercentDTOs);
		nwCalculationDTO.setDiscount(calculationDTO.getDiscount());
		nwCalculationDTO.setDiscountPercentValue(discountValue);
		return nwCalculationDTO;
	}

	public DiscountDTO calculateDiscount(DiscountDTO discountDTO) throws RecruizException {
		DiscountDTO nwDiscountDTO = new DiscountDTO();
		double discountValue = Double
				.parseDouble(new DecimalFormat("##.##").format(discountDTO.getSubTotal() * discountDTO.getDiscount() / 100));
		nwDiscountDTO.setDiscountPercentageValue(discountValue);
		nwDiscountDTO.setDiscount(discountDTO.getDiscount());
		nwDiscountDTO.setSubTotal(discountDTO.getSubTotal());
		nwDiscountDTO.setTotal(Double.parseDouble(new DecimalFormat("##.##").format(discountDTO.getTotal() - discountValue)));
		return nwDiscountDTO;
	}

	public void sendStatusOfAgencyInvoice() throws RecruizException {
		// Mail has to send superAdmin and Hr Manager
		List<String> allTenant = tenantResolverService.findAllTenants();
		for (String tenant : allTenant) {
			try {
				TenantContextHolder.setTenant(tenant);
			
				Set<String> emailList = new HashSet<String>();
				Map<String, Object> bodyMap = new HashMap<>();
				Organization organization = organizationService.findByOrgId(tenant);
				if (organization.getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY) && !organization.getDisableStatus()
						&& !organization.getMarkForDelete()) {

					for (User user : userService.getAllByRoleName(GlobalConstants.SUPER_ADMIN_USER_ROLE)) {
						if (user != null && userService.isActiveUser(user))
							emailList.add(user.getEmail());
					}
					for (User user : userService.getAllByRoleName(GlobalConstants.HR_MANAGER_USER_ROLE)) {
						if (user != null && userService.isActiveUser(user))
							emailList.add(user.getEmail());
					}

					EmailTemplateData emailTemplateData = emailTemplateDataService
							.getTemplateByNameAndCategory(GlobalConstants.INVOICE_STATUS, GlobalConstants.AGENCY_INVOICE);
					AgencyInvoiceStatusCountDTO agencyInvoiceStatusCountDTO = getInvoiceStatusCount();

					List<String> pendingInvoice = new ArrayList<>();
					pendingInvoice.add(InvoiceStatus.Pending.getDisplayName());
					List<String> overDueInvoice = new ArrayList<>();
					overDueInvoice.add(InvoiceStatus.OverDue.getDisplayName());

					bodyMap.put(GlobalConstants.CUSTOMER_NAME, organization.getOrgName());
					bodyMap.put(GlobalConstants.PENDING_INVOICE_COUNT, agencyInvoiceStatusCountDTO.getPendingInvoice());
					bodyMap.put(GlobalConstants.OVERDUE_INVOICE_COUNT, agencyInvoiceStatusCountDTO.getOverDueInvoice());
					bodyMap.put(GlobalConstants.PENDING_INVOICE, getAgencyInvoice(pendingInvoice));
					bodyMap.put(GlobalConstants.OVERDUE_INVOICE, getAgencyInvoice(overDueInvoice));
					bodyMap.put("dates", DateUtil.class);

					String renderedData = emailTemplateDataService.getRenderedTemplate(emailTemplateData.getBody(), null, null,
							GlobalConstants.INVOICE_STATUS, bodyMap);
					String data = emailTemplateDataService.getMasterTemplateWithoutButton(renderedData);
					if (!((agencyInvoiceStatusCountDTO.getPendingInvoice() == 0) && (agencyInvoiceStatusCountDTO.getOverDueInvoice() == 0)))
						emailService.sendEmail(new ArrayList<String>(emailList), data, emailTemplateData.getSubject(), true);

				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				TenantContextHolder.clearContext();
			}
		}

	}

	public boolean checkInvoiceGeneratedForCandidate(List<String> ids) throws RecruizException {
		for (String id : ids) {
			Candidate candidate = candidateRepository.findOne(Long.parseLong(id));
			if (candidate != null) {
				List<AgencyInvoice> candidateInvoices = getCandidateInvoices(candidate);
				if (candidateInvoices != null && !candidateInvoices.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public AgencyInvoice getByCandidateEmail(String candidateEmail) {
		List<CandidateInvoice> candidateInvoices = candidateInvoiceService.getByCandidateInvoiceEmail(candidateEmail);
		if (candidateInvoices != null) {
			return candidateInvoices.get(0).getAgencyInvoice();
		} else {
			return null;
		}

	}

	public AgencyInvoice getInvoiceByPayemntHistory(Set<AgencyInvoicePaymentHistory> histories) {
		return agencyInvoiceRepository.findByAgencyInvoicePaymentHistoriesIn(histories);
	}
	
	
	public List<JoinedCandidateDTO> getYetToProcessInvoiceCandidate(List<JoinedCandidateDTO> joinedCandidateDTOs) throws RecruizException{
		List<JoinedCandidateDTO> yetToProcessInvoiceCandidateList = new ArrayList<JoinedCandidateDTO>();
		for(JoinedCandidateDTO joinedCandidateDTO : joinedCandidateDTOs){
			if(joinedCandidateDTO.getInvoiceId() == null)
				yetToProcessInvoiceCandidateList.add(joinedCandidateDTO);
		}
		return yetToProcessInvoiceCandidateList;
	}
	
	public int countYetToProcessInvoiceCandidate() throws RecruizException{
		List<CandidateStatus> candidateStatus = candidateStatusService.getJoinedCandidateStatus();
		List<JoinedCandidateDTO> joinedCandidateDTOs = convertCandidateStatus(candidateStatus);
		List<JoinedCandidateDTO> yetToProcessedCandidateDTOs =getYetToProcessInvoiceCandidate(joinedCandidateDTOs);
		return yetToProcessedCandidateDTOs.size();
	}

}
