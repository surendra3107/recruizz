package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.OrganizationBankInfo;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.OrganizationBankInfoDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.OrganizationBankInfoService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
public class OrganizationBankInfoController {

	@Autowired
	private OrganizationBankInfoService organizationBankInfoService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	/**********************
	 * To Add Bank details*
	 **********************
	 * @param organizationBankInfoDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank/add", method = RequestMethod.POST)
	public RestResponse addOrganizationBankInfo(@RequestBody OrganizationBankInfoDTO organizationBankInfoDTO)
			throws RecruizException {

		OrganizationBankInfo organizationBankInfo = organizationBankInfoService.addBankDetails(organizationBankInfoDTO);
		OrganizationBankInfoDTO convertedOrganizationBankInfo = dataModelToDTOConversionService
				.convertOrganizationBankDetails(organizationBankInfo);
		RestResponse organizationBankInfoResponse = new RestResponse(RestResponse.SUCCESS,
				convertedOrganizationBankInfo, SuccessHandler.BANK_DETAILS_ADDED_SUCCESS);
		return organizationBankInfoResponse;
	}

	/**************************
	 * To get all bank details*
	 **************************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank", method = RequestMethod.GET)
	public RestResponse getAllBankDetails() throws RecruizException {
		List<OrganizationBankInfo> organizationBankInfos = organizationBankInfoService.getAllBankDetails();
		List<OrganizationBankInfoDTO> convertedOrganizationBankInfo = dataModelToDTOConversionService
				.convertOrganizationBankDetails(organizationBankInfos);
		RestResponse organizationBankInfoResponse = new RestResponse(RestResponse.SUCCESS,
				convertedOrganizationBankInfo, SuccessHandler.BANK_DETAILS_FETCHED_SUCCESS);
		return organizationBankInfoResponse;
	}

	/*********************
	 * To get bank detail*
	 *********************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank/{id}", method = RequestMethod.GET)
	public RestResponse getBankDetails(@PathVariable("id") long id) throws RecruizException {
		OrganizationBankInfo organizationBankInfo = organizationBankInfoService.getBankDetails(id);
		OrganizationBankInfoDTO convertedOrganizationBankInfo = dataModelToDTOConversionService
				.convertOrganizationBankDetails(organizationBankInfo);
		RestResponse organizationBankInfoResponse = new RestResponse(RestResponse.SUCCESS,
				convertedOrganizationBankInfo, SuccessHandler.BANK_DETAILS_FETCHED_SUCCESS);
		return organizationBankInfoResponse;
	}

	/*************************
	 * To update Bank details*
	 *************************
	 * @param id
	 * @param organizationBankInfoDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank/{id}", method = RequestMethod.PUT)
	public RestResponse updateBankDetails(@PathVariable("id") long id,
			@RequestBody OrganizationBankInfoDTO organizationBankInfoDTO) throws RecruizException {
		OrganizationBankInfo organizationBankInfo = organizationBankInfoService.updateBankDetails(id,
				organizationBankInfoDTO);
		OrganizationBankInfoDTO convertedOrganizationBankInfo = dataModelToDTOConversionService
				.convertOrganizationBankDetails(organizationBankInfo);
		RestResponse organizationBankInfoResponse = new RestResponse(RestResponse.SUCCESS,
				convertedOrganizationBankInfo, SuccessHandler.BANK_DETAILS_UPDATE_SUCCESS);
		return organizationBankInfoResponse;
	}

	/*************************
	 * To delete bank details*
	 *************************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteBankDetails(@PathVariable("id") long id) throws RecruizException {
		Boolean isDeleted = organizationBankInfoService.deleteBankDetails(id);
		if (isDeleted)
			return new RestResponse(RestResponse.SUCCESS, SuccessHandler.BANK_DETAILS_DELETED_SUCCESS,
					SuccessHandler.BANK_DETAILS_DELETED_SUCCESS);
		return new RestResponse(RestResponse.FAILED, ErrorHandler.BANK_DETAILS_DELETE_FAILED,
				ErrorHandler.BANK_DETAILS_DELETE_FAILED);
	}

	/****************************
	 * To delete all bank detail*
	 ****************************
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank", method = RequestMethod.DELETE)
	public RestResponse deleteAllBankDetails() throws RecruizException {
		Boolean isDeleted = organizationBankInfoService.deleteAllBankDetails();
		if (isDeleted)
			return new RestResponse(RestResponse.SUCCESS, SuccessHandler.BANK_DETAILS_DELETED_SUCCESS,
					SuccessHandler.BANK_DETAILS_DELETED_SUCCESS);
		return new RestResponse(RestResponse.FAILED, ErrorHandler.BANK_DETAILS_DELETE_FAILED,
				ErrorHandler.BANK_DETAILS_DELETE_FAILED);
	}
	
	/********************************
	 * To set bank detail as default*
	 ********************************
	 * @param id
	 * @param flag
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/bank/set/default/{id}", method = RequestMethod.GET)
	public RestResponse setDefaultBankDetails(@PathVariable("id") long id ,@RequestParam boolean flag) throws RecruizException {
		OrganizationBankInfo organizationBankInfo = organizationBankInfoService.setAsDefaultBankDetails(id, flag);
		OrganizationBankInfoDTO convertedOrganizationBankInfo = dataModelToDTOConversionService
				.convertOrganizationBankDetails(organizationBankInfo);
		RestResponse organizationBankInfoResponse = new RestResponse(RestResponse.SUCCESS,
				convertedOrganizationBankInfo, SuccessHandler.BANK_DETAILS_UPDATE_SUCCESS);
		return organizationBankInfoResponse;
	}
}
