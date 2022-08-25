package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.InvoiceSettingsDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.TaxRelatedDetailsDTO;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.InvoiceSettingsService;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
public class InvoiceSettingsController {

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;
	
	@Autowired
	private InvoiceSettingsService invoiceSettingsService;
	
	/**************************
	 * To get invoice settings*
	 **************************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/invoice/settings" , method = RequestMethod.GET)
	public RestResponse getInvoiceSettings() throws RecruizException{
		InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
		InvoiceSettingsDTO convertedInvoiceSettings = dataModelToDTOConversionService.convertInvoiceSettings(invoiceSettings);
		return new RestResponse(true, convertedInvoiceSettings, SuccessHandler.GET_SUCCESS);
	}
	
	/**
	 * @param taxRelatedDetailsDTOs
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/invoice/detete/tax/related/details" , method = RequestMethod.PUT)
	public RestResponse updateTaxRelatedDetails(@RequestBody List<TaxRelatedDetailsDTO> taxRelatedDetailsDTOs) throws RecruizException{
		invoiceSettingsService.deleteTaxRelatedDetails(taxRelatedDetailsDTOs);
		return new RestResponse(true, SuccessHandler.DELETE_SUCCESS, SuccessHandler.DELETE_SUCCESS);
	}
	
}
