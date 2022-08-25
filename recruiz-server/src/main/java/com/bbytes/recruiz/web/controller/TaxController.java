package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.TaxDTO;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TaxService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
@RequestMapping(value = "/api/v1/tax")
public class TaxController {

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private TaxService taxService;

	/*************
	 * To add Tax*
	 *************
	 * @param taxDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(method = RequestMethod.POST)
	public RestResponse addTax(@RequestBody TaxDTO taxDTO) throws RecruizException {
		Tax tax = taxService.addTax(taxDTO);
		TaxDTO convertedTax = dataModelToDTOConversionService.convertTax(tax);
		return new RestResponse(true, convertedTax, SuccessHandler.TAX_ADD_SUCCESS);
	}

	/****************
	 * To update Tax*
	 ****************
	 * @param id
	 * @param taxDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public RestResponse updateTax(@PathVariable("id") long id, @RequestBody TaxDTO taxDTO) throws RecruizException {
		Tax tax = taxService.updateTax(id, taxDTO);
		TaxDTO convertedTax = dataModelToDTOConversionService.convertTax(tax);
		return new RestResponse(true, convertedTax, SuccessHandler.TAX_UPDATE_SUCCESS);
	}

	/****************
	 * To delete Tax*
	 ****************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteTax(@PathVariable("id") long id) throws RecruizException {
		Boolean isDeleted = taxService.deleteTax(id);
		if (isDeleted)
			return new RestResponse(RestResponse.SUCCESS, isDeleted, SuccessHandler.DELETE_SUCCESS);
		else
			return new RestResponse(RestResponse.FAILED, isDeleted, ErrorHandler.DELETE_FAILED);
	}

	/********************
	 * To delete all Tax*
	 ********************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public RestResponse deleteAllTax() throws RecruizException {
		Boolean isDeleted = taxService.deleteAllTax();
		if (isDeleted)
			return new RestResponse(RestResponse.SUCCESS, isDeleted, SuccessHandler.DELETE_SUCCESS);
		else
			return new RestResponse(RestResponse.FAILED, isDeleted, ErrorHandler.DELETE_FAILED);
	}

	/*************
	 * To get Tax*
	 *************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public RestResponse getTax(@PathVariable("id") long id) throws RecruizException {
		Tax tax = taxService.getTax(id);
		TaxDTO convertedTax = dataModelToDTOConversionService.convertTax(tax);
		return new RestResponse(true, convertedTax, SuccessHandler.GET_SUCCESS);
	}

	/*****************
	 * To get all Tax*
	 *****************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public RestResponse getAllTax() throws RecruizException {
		List<Tax> taxs = taxService.getTax();
		List<TaxDTO> convertedTaxs = dataModelToDTOConversionService.convertTax(taxs);
		return new RestResponse(true, convertedTaxs, SuccessHandler.GET_SUCCESS);
	}

}
