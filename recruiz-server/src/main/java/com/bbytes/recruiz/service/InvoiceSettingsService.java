package com.bbytes.recruiz.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.InvoiceSettingsRepository;
import com.bbytes.recruiz.rest.dto.models.AgencyMultipleInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceSettingsDTO;
import com.bbytes.recruiz.rest.dto.models.TaxRelatedDetailsDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class InvoiceSettingsService extends AbstractService<InvoiceSettings, Long>{
	
	@Autowired
	private InvoiceSettingsRepository invoiceSettingsRepository;
	
	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;
	
	@Autowired
	public InvoiceSettingsService(InvoiceSettingsRepository invoiceSettingsRepository) {
		super(invoiceSettingsRepository);
		this.invoiceSettingsRepository = invoiceSettingsRepository;	
	}
	
	
	
	/**************************
	 * To get invoice settings*
	 **************************
	 * @return
	 * @throws RecruizException
	 */
	public InvoiceSettings getInvoiceSettings() throws RecruizException {
		List<InvoiceSettings> invoiceSettings = invoiceSettingsRepository.findAll();
		if (!invoiceSettings.isEmpty())
			return invoiceSettings.get(0);
		else
			return null;
	}
	
	
	public InvoiceSettingsDTO convertInvoiceDTO(AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO, Map<String,Double> taxDetails,Map<String,String> taxRelatedDetails){
		
		InvoiceSettingsDTO invoiceSettingsDTO =  new InvoiceSettingsDTO();
		//invoiceSettingsDTO.setGstin(agencyMultipleInvoiceDTO.getGstin());
		//invoiceSettingsDTO.setPan(agencyMultipleInvoiceDTO.getPan());
		invoiceSettingsDTO.setOrganizationName(agencyMultipleInvoiceDTO.getOrganizationName());
		invoiceSettingsDTO.setOrganization_address_1(agencyMultipleInvoiceDTO.getOrganization_address_1());
		invoiceSettingsDTO.setOrganization_address_2(agencyMultipleInvoiceDTO.getOrganization_address_2());
		invoiceSettingsDTO.setOrganizationCity(agencyMultipleInvoiceDTO.getOrganizationCity());
		invoiceSettingsDTO.setOrganizationState(agencyMultipleInvoiceDTO.getOrganizationState());
		invoiceSettingsDTO.setOrganizationCountry(agencyMultipleInvoiceDTO.getOrganizationCountry());
		invoiceSettingsDTO.setOrganizationPin(agencyMultipleInvoiceDTO.getOrganizationPin());
		invoiceSettingsDTO.setOrganizationPhone(agencyMultipleInvoiceDTO.getOrganizationPhone());
		invoiceSettingsDTO.setChequePayable(agencyMultipleInvoiceDTO.getChequePayable());
		invoiceSettingsDTO.setOrganizationAccountName(agencyMultipleInvoiceDTO.getOrganizationAccountName());
		invoiceSettingsDTO.setOrganizationAccountNumber(agencyMultipleInvoiceDTO.getOrganizationAccountNumber());
		invoiceSettingsDTO.setOrganizationBankName(agencyMultipleInvoiceDTO.getOrganizationBankName());
		invoiceSettingsDTO.setOrganizationBankBranchName(agencyMultipleInvoiceDTO.getOrganizationBankBranchName());
		invoiceSettingsDTO.setOrganizationBankIfsc(agencyMultipleInvoiceDTO.getOrganizationBankIfsc());
		invoiceSettingsDTO.setNote(agencyMultipleInvoiceDTO.getNote());
		invoiceSettingsDTO.setBillClientName(agencyMultipleInvoiceDTO.getBillClientName());
		invoiceSettingsDTO.setBillContactName(agencyMultipleInvoiceDTO.getBillContactName());
		invoiceSettingsDTO.setBill_address_1(agencyMultipleInvoiceDTO.getBill_address_1());
		invoiceSettingsDTO.setBill_address_2(agencyMultipleInvoiceDTO.getBill_address_2());
		invoiceSettingsDTO.setBillCity(agencyMultipleInvoiceDTO.getBillCity());
		invoiceSettingsDTO.setBillState(agencyMultipleInvoiceDTO.getBillState());
		invoiceSettingsDTO.setBillCountry(agencyMultipleInvoiceDTO.getBillCountry());
		invoiceSettingsDTO.setBillPin(agencyMultipleInvoiceDTO.getBillPin());
        invoiceSettingsDTO.setBillPhone(agencyMultipleInvoiceDTO.getBillPhone());
        invoiceSettingsDTO.setTaxDetails(taxDetails);
        invoiceSettingsDTO.setTaxRelatedDetailsDTOs(dataModelToDTOConversionService.convertTaxRelatedDetails(taxRelatedDetails));
        return invoiceSettingsDTO;
	}
	
	@Transactional
	public InvoiceSettings addInvoiceSettings(InvoiceSettingsDTO invoiceSettingsDTO) throws RecruizException{
		
		if(invoiceSettingsDTO == null)
			throw new RecruizException(ErrorHandler.SETTING_CAN_NOT_BE_NULL, ErrorHandler.SETTING_CAN_NOT_BE_EMPTY);
		
		try{
			InvoiceSettings invoiceSettings = new InvoiceSettings();
			//invoiceSettings.setGstin(invoiceSettingsDTO.getGstin());
			//invoiceSettings.setPan(invoiceSettingsDTO.getPan());
			invoiceSettings.setOrganizationName(invoiceSettingsDTO.getOrganizationName());
			invoiceSettings.setOrganization_address_1(invoiceSettingsDTO.getOrganization_address_1());
			invoiceSettings.setOrganization_address_2(invoiceSettingsDTO.getOrganization_address_2());
			invoiceSettings.setOrganizationCity(invoiceSettingsDTO.getOrganizationCity());
			invoiceSettings.setOrganizationState(invoiceSettingsDTO.getOrganizationState());
			invoiceSettings.setOrganizationCountry(invoiceSettingsDTO.getOrganizationCountry());
			invoiceSettings.setOrganizationPin(invoiceSettingsDTO.getOrganizationPin());
			invoiceSettings.setOrganizationPhone(invoiceSettingsDTO.getOrganizationPhone());
			invoiceSettings.setChequePayable(invoiceSettingsDTO.getChequePayable());
			invoiceSettings.setOrganizationAccountName(invoiceSettingsDTO.getOrganizationAccountName());
			invoiceSettings.setOrganizationAccountNumber(invoiceSettingsDTO.getOrganizationAccountNumber());
			invoiceSettings.setOrganizationBankName(invoiceSettingsDTO.getOrganizationBankName());
			invoiceSettings.setOrganizationBankBranchName(invoiceSettingsDTO.getOrganizationBankBranchName());
			invoiceSettings.setOrganizationBankIfsc(invoiceSettingsDTO.getOrganizationBankIfsc());
			invoiceSettings.setNote(invoiceSettingsDTO.getNote());
			invoiceSettings.setBillClientName(invoiceSettingsDTO.getBillClientName());
			invoiceSettings.setBillContactName(invoiceSettingsDTO.getBillContactName());
			invoiceSettings.setBill_address_1(invoiceSettingsDTO.getBill_address_1());
			invoiceSettings.setBill_address_2(invoiceSettingsDTO.getBill_address_2());
			invoiceSettings.setBillCity(invoiceSettingsDTO.getBillCity());
			invoiceSettings.setBillState(invoiceSettingsDTO.getBillState());
			invoiceSettings.setBillCountry(invoiceSettingsDTO.getBillCountry());
			invoiceSettings.setBillPin(invoiceSettingsDTO.getBillPin());
			invoiceSettings.setBillPhone(invoiceSettingsDTO.getBillPhone());
			invoiceSettings.setTaxDetails(invoiceSettingsDTO.getTaxDetails());
			Map<String,String> taxReatiledDetails = dataModelToDTOConversionService.convertTaxRelatedDetails(invoiceSettingsDTO.getTaxRelatedDetailsDTOs());
			if(taxReatiledDetails !=null){
				invoiceSettings.setTaxRelatedDetails(taxReatiledDetails);
			}
			return save(invoiceSettings);
		}catch(Exception e){
			throw new RecruizException(ErrorHandler.INVOICE_SETTING_ADDED_FAILED, ErrorHandler.INVOICE_SETTING_ADDED_FAILURE, e);
		}
	}
	
	@Transactional
	public void deleteTaxRelatedDetails(List<TaxRelatedDetailsDTO> taxRelatedDetailsDTOs) throws RecruizException{
		InvoiceSettings invoiceSettings = getInvoiceSettings();
		if (invoiceSettings != null) {
			invoiceSettings.getTaxRelatedDetails().putAll(dataModelToDTOConversionService.convertTaxRelatedDetails(taxRelatedDetailsDTOs));
			save(invoiceSettings);
		}
	}
}
