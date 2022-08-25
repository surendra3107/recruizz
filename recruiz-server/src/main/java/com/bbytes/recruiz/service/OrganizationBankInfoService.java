package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.OrganizationBankInfo;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.OrganizationBankInfoRepository;
import com.bbytes.recruiz.rest.dto.models.OrganizationBankInfoDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class OrganizationBankInfoService extends AbstractService<OrganizationBankInfo, Long> {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationBankInfoService.class);

	private OrganizationBankInfoRepository organizationBankInfoRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@Autowired
	private InvoiceSettingsService invoiceSettingsService;

	@Autowired
	public OrganizationBankInfoService(OrganizationBankInfoRepository organizationBankInfoRepository) {
		super(organizationBankInfoRepository);
		this.organizationBankInfoRepository = organizationBankInfoRepository;
	}

	public Boolean isAccountNumberExist(String accountNumber) {
		return organizationBankInfoRepository.findByAccountNumber(accountNumber) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public List<OrganizationBankInfo> getAllBankDetails() throws RecruizException {
		if (organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY)) {
			return organizationBankInfoRepository.findAll();
		} else {
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);
		}
	}

	@Transactional(readOnly = true)
	public List<OrganizationBankInfo> getBankDetailsByWhoAdded(String email) throws RecruizException {
		if (organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY)) {
			return organizationBankInfoRepository.findByAddedBy(email);
		} else {
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);
		}
	}

	@Transactional(readOnly = true)
	public OrganizationBankInfo getBankDetails(long id) throws RecruizException {
		if (organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY)) {
			if (exists(id))
				return organizationBankInfoRepository.findOne(id);
			else
				throw new RecruizException(ErrorHandler.BANK_DETAILS_NOT_FOUND, ErrorHandler.BANK_DETAILS_NOT_EXIST);
		} else {
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);
		}
	}

	@Transactional
	public OrganizationBankInfo addBankDetails(OrganizationBankInfoDTO organizationBankInfoDTO)
			throws RecruizException {

		if (!organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY))
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);

		if (organizationBankInfoDTO == null)
			throw new RecruizException(ErrorHandler.BANK_DETAILS_CAN_NOT_EMPTY, ErrorHandler.BANK_DETAILS_CAN_NOT_NULL);

		if (organizationBankInfoDTO.getBankName() == null)
			throw new RecruizException(ErrorHandler.BANK_NAME_CAN_NOT_EMPTY, ErrorHandler.BANK_NAME_CAN_NOT_NULL);

		if (organizationBankInfoDTO.getAccountNumber() == null)
			throw new RecruizException(ErrorHandler.BANK_ACCOUNT_NUMBER_CAN_NOT_EMPTY,
					ErrorHandler.BANK_ACCOUNT_NUMBER_CAN_NOT_NULL);

		if (isAccountNumberExist(organizationBankInfoDTO.getAccountNumber()))
			throw new RecruizException(ErrorHandler.BANK_ACCOUNT_NUMBER_ALREADY_EXIST,
					ErrorHandler.BANK_ACCOUNT_NUMBER_EXIST);

		try {
			List<OrganizationBankInfo> organizationBankInfos = findAll();
			OrganizationBankInfo organizationBankInfo = new OrganizationBankInfo(organizationBankInfoDTO.getBankName(),
					organizationBankInfoDTO.getBranch(), organizationBankInfoDTO.getAccountNumber(),
					organizationBankInfoDTO.getIfscCode());
			organizationBankInfo.setAccountName(organizationBankInfoDTO.getAccountName());
			organizationBankInfo.setAddedBy(userService.getLoggedInUserEmail());
			
			
			if(organizationBankInfos.isEmpty()){
				organizationBankInfo.setDefaultBankDetails(true);
			}
			else if(organizationBankInfoDTO.isDefaultBankDetails() ==true){
				setAsDefaultBankDetails(organizationBankInfos, false);
			}else{
				organizationBankInfo.setDefaultBankDetails(organizationBankInfoDTO.isDefaultBankDetails());
			}
			
			organizationBankInfo = save(organizationBankInfo);
			if(organizationBankInfo.getDefaultBankDetails().booleanValue() == true){
				
				InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
				if (invoiceSettings == null) {
					invoiceSettings = new InvoiceSettings();
				}
				invoiceSettings.setOrganizationAccountName(organizationBankInfoDTO.getAccountName());
				invoiceSettings.setOrganizationAccountNumber(organizationBankInfoDTO.getAccountNumber());
				invoiceSettings.setOrganizationBankName(organizationBankInfoDTO.getBankName());
				invoiceSettings.setOrganizationBankBranchName(organizationBankInfoDTO.getBranch());
				invoiceSettings.setOrganizationBankIfsc(organizationBankInfoDTO.getIfscCode());
				invoiceSettingsService.save(invoiceSettings);
			}
			
			return organizationBankInfo;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(ErrorHandler.ADD_BANK_DETAILS_FAILED, ErrorHandler.ADD_BANK_DETAILS_FAILURE);
		}
	}

	@Transactional
	public OrganizationBankInfo updateBankDetails(long id, OrganizationBankInfoDTO organizationBankInfoDTO)
			throws RecruizException {
		OrganizationBankInfo updateBankDetails = null;

		if (!organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY))
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);

		if ((updateBankDetails = getBankDetails(id)) == null)
			throw new RecruizException(ErrorHandler.BANK_DETAILS_NOT_FOUND, ErrorHandler.BANK_DETAILS_NOT_EXIST);

		if (organizationBankInfoDTO == null)
			throw new RecruizException(ErrorHandler.BANK_DETAILS_CAN_NOT_EMPTY, ErrorHandler.BANK_DETAILS_CAN_NOT_NULL);

		if (organizationBankInfoDTO.getBankName() == null)
			throw new RecruizException(ErrorHandler.BANK_NAME_CAN_NOT_EMPTY, ErrorHandler.BANK_NAME_CAN_NOT_NULL);

		if (organizationBankInfoDTO.getAccountNumber() == null)
			throw new RecruizException(ErrorHandler.BANK_ACCOUNT_NUMBER_CAN_NOT_EMPTY,
					ErrorHandler.BANK_ACCOUNT_NUMBER_CAN_NOT_NULL);

		if (isAccountNumberExist(organizationBankInfoDTO.getAccountNumber())) {
			if (!updateBankDetails.getAccountNumber().equals(organizationBankInfoDTO.getAccountNumber()))
				throw new RecruizException(ErrorHandler.BANK_ACCOUNT_NUMBER_ALREADY_EXIST,
						ErrorHandler.BANK_ACCOUNT_NUMBER_EXIST);
		}

		try {
			updateBankDetails.setAccountNumber(organizationBankInfoDTO.getAccountNumber());
			updateBankDetails.setBankName(organizationBankInfoDTO.getBankName());
			updateBankDetails.setAccountName(organizationBankInfoDTO.getAccountName());
			updateBankDetails.setBranch(organizationBankInfoDTO.getBranch());
			updateBankDetails.setIfscCode(organizationBankInfoDTO.getIfscCode());
			updateBankDetails = save(updateBankDetails);
			// Adding entry into invoice setting
			InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			}
			invoiceSettings.setOrganizationAccountName(organizationBankInfoDTO.getAccountName());
			invoiceSettings.setOrganizationAccountNumber(organizationBankInfoDTO.getAccountNumber());
			invoiceSettings.setOrganizationBankName(organizationBankInfoDTO.getBankName());
			invoiceSettings.setOrganizationBankBranchName(organizationBankInfoDTO.getBranch());
			invoiceSettings.setOrganizationBankIfsc(organizationBankInfoDTO.getIfscCode());
			invoiceSettingsService.save(invoiceSettings);
			return updateBankDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(ErrorHandler.BANK_DETAILS_UPDATE_FAILED,
					ErrorHandler.BANK_DETAILS_UPDATE_FAILURE);
		}
	}

	@Transactional
	public Boolean deleteBankDetails(long id) throws RecruizException {

		if (getBankDetails(id) == null)
			throw new RecruizException(ErrorHandler.BANK_DETAILS_NOT_FOUND, ErrorHandler.BANK_DETAILS_NOT_EXIST);
		delete(id);

		if (exists(id))
			return false;
		else
			return true;
	}

	@Transactional
	public Boolean deleteAllBankDetails() throws RecruizException {

		if (!organizationService.getCurrentOrganization().getOrgType().equals(GlobalConstants.SIGNUP_MODE_AGENCY))
			throw new RecruizException(ErrorHandler.NOT_AGENCY_TYPE, ErrorHandler.NOT_AGENCY_TYPE_);
		deleteAll();
		if (findAll().size() == 0)
			return true;
		return false;
	}
	
	@Transactional
	public void setAsDefaultBankDetails(List<OrganizationBankInfo> organizationBankInfos, boolean flag) {

		if (!organizationBankInfos.isEmpty()) {
			for (OrganizationBankInfo organizationBankInfo : organizationBankInfos) {
				organizationBankInfo.setDefaultBankDetails(flag);
				save(organizationBankInfo);
			}
		}
	}
	
	/*********************************
	 * To set Bank details as default*
	 *********************************
	 * @param id
	 * @param flag
	 * @return
	 * @throws RecruizException
	 */
	public OrganizationBankInfo setAsDefaultBankDetails(long id , boolean flag) throws RecruizException{
		OrganizationBankInfo organizationBank = null; 
		List<OrganizationBankInfo> organizationBankInfos = findAll();	
		for(OrganizationBankInfo organizationBankInfo : organizationBankInfos){
			if(organizationBankInfo.getId() == id && flag == true){
				organizationBankInfo.setDefaultBankDetails(flag);
				organizationBank = save(organizationBankInfo);
			}else{
				organizationBankInfo.setDefaultBankDetails(false);
				save(organizationBankInfo);
			}
		}
		
		if(flag == true && organizationBank.getDefaultBankDetails().booleanValue()== true){
			InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			}
			invoiceSettings.setOrganizationAccountName(organizationBank.getAccountName());
			invoiceSettings.setOrganizationAccountNumber(organizationBank.getAccountNumber());
			invoiceSettings.setOrganizationBankName(organizationBank.getBankName());
			invoiceSettings.setOrganizationBankBranchName(organizationBank.getBranch());
			invoiceSettings.setOrganizationBankIfsc(organizationBank.getIfscCode());
			invoiceSettingsService.save(invoiceSettings);
		}
		return organizationBank;
	}

}
