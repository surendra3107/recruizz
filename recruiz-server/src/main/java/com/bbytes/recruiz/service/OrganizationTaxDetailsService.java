package com.bbytes.recruiz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.OrganizationTaxDetails;
import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.OrganizationTaxDetailsRepository;
import com.bbytes.recruiz.rest.dto.models.OrganizationTaxDetailsDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class OrganizationTaxDetailsService extends AbstractService<OrganizationTaxDetails, Long> {

	private OrganizationTaxDetailsRepository organizationTaxDetailsRepository;

	@Autowired
	private InvoiceSettingsService invoiceSettingsService;
	
	@Autowired
	private TaxService taxService;
	
	@Autowired
	public OrganizationTaxDetailsService(OrganizationTaxDetailsRepository organizationTaxDetailsRepository) {
		super(organizationTaxDetailsRepository);
		this.organizationTaxDetailsRepository = organizationTaxDetailsRepository;
	}

	@Transactional(readOnly = true)
	public boolean isTaxName(String taxName) {
		return organizationTaxDetailsRepository.findByTaxName(taxName) == null ? false : true;
	}

	/*************************************
	 * To add OrganizationTaxDetails******
	 *************************************
	 * @param organizationTaxDetailsDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public OrganizationTaxDetails addTaxDetails(OrganizationTaxDetailsDTO organizationTaxDetailsDTO)
			throws RecruizException {

		if (organizationTaxDetailsDTO == null)
			throw new RecruizException(ErrorHandler.TAX_DETAILS_DTO_NULL, ErrorHandler.TAX_DETAILS_DTO__NULL);

		if (organizationTaxDetailsDTO.getTaxName() == null || organizationTaxDetailsDTO.getTaxName().isEmpty())
			throw new RecruizException(ErrorHandler.TAX_NAME_MISSING, ErrorHandler.TAX_NAME_NOT_FOUND);

		if (isTaxName(organizationTaxDetailsDTO.getTaxName()))
			throw new RecruizException(ErrorHandler.TAX_NAME_ALREADY_EXIST, ErrorHandler.TAX_NAME_EXIST);

		if (organizationTaxDetailsDTO.getTaxValue() == null || organizationTaxDetailsDTO.getTaxValue().isEmpty())
			throw new RecruizException(ErrorHandler.TAX_VALUE_MISSING, ErrorHandler.TAX_VALUE_NOT_FOUND);

		try {
			OrganizationTaxDetails organizationTaxDetails = new OrganizationTaxDetails(
					organizationTaxDetailsDTO.getTaxName(), organizationTaxDetailsDTO.getTaxValue());
			save(organizationTaxDetails);
			
			 InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			    if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			    }
			
			    Map<String, String> taxRelatedDetails = new HashMap<String, String>();
			    if (organizationTaxDetailsDTO.getTaxName() != null) {
				taxRelatedDetails.put(organizationTaxDetailsDTO.getTaxName(), organizationTaxDetailsDTO.getTaxValue());
			    }
			    
			    invoiceSettings.getTaxRelatedDetails().clear();
			    invoiceSettings.getTaxRelatedDetails().putAll(taxRelatedDetails);
			    invoiceSettingsService.save(invoiceSettings);
			    
			    //check in Tax table 
			    if (taxService.isTaxNameExist(organizationTaxDetailsDTO.getTaxName())) {
			    	Tax tax = taxService.getByTaxName(organizationTaxDetailsDTO.getTaxName());
			    	tax.setTaxNumber(organizationTaxDetailsDTO.getTaxValue());
			    	taxService.save(tax);
			    }else{
			    	Tax tax = new Tax();
			    	tax.setTaxName(organizationTaxDetailsDTO.getTaxName());
			    	tax.setTaxNumber(organizationTaxDetailsDTO.getTaxValue());
			    	taxService.save(tax);
			    }
			return organizationTaxDetails;
		} catch (Exception e) {
			throw new RecruizException(ErrorHandler.TAX_ADD_FAILED, ErrorHandler.TAX_ADD_FAILURE, e);
		}
	}
	
	
	
	/***********************************
	 * To update OrganizationTaxDetails*
	 ***********************************
	 * @param organizationTaxDetailsDTO
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public OrganizationTaxDetails updateTaxDetails(OrganizationTaxDetailsDTO organizationTaxDetailsDTO,long id)throws RecruizException{
		
		if (organizationTaxDetailsDTO == null)
			throw new RecruizException(ErrorHandler.TAX_DETAILS_DTO_NULL, ErrorHandler.TAX_DETAILS_DTO__NULL);
        
		if(!exists(id))
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);
		
		if (organizationTaxDetailsDTO.getTaxName() == null || organizationTaxDetailsDTO.getTaxName().isEmpty())
			throw new RecruizException(ErrorHandler.TAX_NAME_MISSING, ErrorHandler.TAX_NAME_NOT_FOUND);

		if (isTaxName(organizationTaxDetailsDTO.getTaxName()))
			throw new RecruizException(ErrorHandler.TAX_NAME_ALREADY_EXIST, ErrorHandler.TAX_NAME_EXIST);

		if (organizationTaxDetailsDTO.getTaxValue() == null || organizationTaxDetailsDTO.getTaxValue().isEmpty())
			throw new RecruizException(ErrorHandler.TAX_VALUE_MISSING, ErrorHandler.TAX_VALUE_NOT_FOUND);
		
		try{
			OrganizationTaxDetails organizationTaxDetails = findOne(id);
			organizationTaxDetails.setTaxName(organizationTaxDetailsDTO.getTaxName());
			organizationTaxDetails.setTaxValue(organizationTaxDetailsDTO.getTaxValue());
			return save(organizationTaxDetails);
		}catch(Exception e){
			throw new RecruizException(ErrorHandler.TAX_UPDATE_FAILED, ErrorHandler.TAX_UPDATE_FAILURE, e);
		}
	}
	
	/************************************
	 * To get All OrganizationTaxDetails*
	 ************************************
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<OrganizationTaxDetails> getAllOrganizationTaxDetails(){
		return findAll();
	}
	
	/********************************
	 * To get OrganizationTaxDetails*
	 ********************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public OrganizationTaxDetails getOrganizationTaxDetails(long id) throws RecruizException{
		if(!exists(id))
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);
		else
			return findOne(id);
	}
	
	/***********************************
	 * To delete OrganizationTaxDetails*
	 ***********************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public boolean deleteOrganizationTaxDetails(long id) throws RecruizException{
		if (getOrganizationTaxDetails(id) == null)
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);
		
		OrganizationTaxDetails organizationTaxDetails=getOrganizationTaxDetails(id);
		Tax tax = taxService.getByTaxName(organizationTaxDetails.getTaxName());
		if(tax!=null){
			taxService.delete(tax);
		}
		delete(id);
		if (exists(id))
			return false;
		else
			return true;
	}
}
