package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.OrganizationTaxDetails;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.OrganizationTaxDetailsDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.OrganizationTaxDetailsService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class OrganizationTaxDetailsController {

    @Autowired
    private OrganizationTaxDetailsService organizationTaxDetailsService;

    @Autowired
    private DataModelToDTOConversionService dataModelToDTOConversionService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @Autowired
    private UserService userService;

    /********************************
     * To get OrganizationTaxDetails*
     ********************************
     * @param id
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/org/tax/{id}", method = RequestMethod.GET)
    public RestResponse getOrganizationTaxDetails(@PathVariable("id") long id) throws RecruizException {
	OrganizationTaxDetails organizationTaxDetails = organizationTaxDetailsService.getOrganizationTaxDetails(id);
	OrganizationTaxDetailsDTO convertedOrganizationTaxDetailsDTO = dataModelToDTOConversionService
		.convertOrganizationTaxDetails(organizationTaxDetails);
	RestResponse organizationTaxDetailsResponse = new RestResponse(RestResponse.SUCCESS,
		convertedOrganizationTaxDetailsDTO, SuccessHandler.TAX_DETAILS_FETCHED_SUCCESS);
	return organizationTaxDetailsResponse;
    }

    /************************************
     * To get All OrganizationTaxDetails*
     ************************************
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/org/tax", method = RequestMethod.GET)
    public RestResponse getAllOrganizationTaxDetails() throws RecruizException {
	List<OrganizationTaxDetails> organizationTaxDetails = organizationTaxDetailsService
		.getAllOrganizationTaxDetails();
	List<OrganizationTaxDetailsDTO> convertedOrganizationTaxDetailsDTO = dataModelToDTOConversionService
		.convertOrganizationTaxDetails(organizationTaxDetails);
	RestResponse organizationTaxDetailsResponse = new RestResponse(RestResponse.SUCCESS,
		convertedOrganizationTaxDetailsDTO, SuccessHandler.TAX_DETAILS_FETCHED_SUCCESS);
	return organizationTaxDetailsResponse;
    }

    /********************************
     * To add OrganizationTaxDetails*
     ********************************
     * @param organizationTaxDetailsDTO
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/org/tax/add", method = RequestMethod.POST)
    public RestResponse addOrganizationTaxDetails(@RequestBody OrganizationTaxDetailsDTO organizationTaxDetailsDTO)
	    throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddTaxDetails.name());

	OrganizationTaxDetails organizationTaxDetails = organizationTaxDetailsService
		.addTaxDetails(organizationTaxDetailsDTO);
	OrganizationTaxDetailsDTO convertedOrganizationTaxDetailsDTO = dataModelToDTOConversionService
		.convertOrganizationTaxDetails(organizationTaxDetails);
	RestResponse organizationTaxDetailsResponse = new RestResponse(RestResponse.SUCCESS,
		convertedOrganizationTaxDetailsDTO, SuccessHandler.TAX_DETAILS_ADDED_SUCCESS);
	return organizationTaxDetailsResponse;
    }

    /***********************************
     * To update OrganizationTaxDetails*
     ***********************************
     * @param id
     * @param organizationTaxDetailsDTO
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/org/tax/{id}", method = RequestMethod.PUT)
    public RestResponse updateOrganizationTaxDetails(@PathVariable("id") long id,
	    @RequestBody OrganizationTaxDetailsDTO organizationTaxDetailsDTO) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.UpdateTaxDetails.name());*/

	OrganizationTaxDetails organizationTaxDetails = organizationTaxDetailsService
		.updateTaxDetails(organizationTaxDetailsDTO, id);
	OrganizationTaxDetailsDTO convertedOrganizationTaxDetailsDTO = dataModelToDTOConversionService
		.convertOrganizationTaxDetails(organizationTaxDetails);
	RestResponse organizationTaxDetailsResponse = new RestResponse(RestResponse.SUCCESS,
		convertedOrganizationTaxDetailsDTO, SuccessHandler.TAX_DETAILS_UPDATE_SUCCESS);
	return organizationTaxDetailsResponse;
    }

    /***********************************
     * To delete OrganizationTaxDetails*
     ***********************************
     * @param id
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/org/tax/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteOrganizationTaxDetails(@PathVariable("id") long id) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteTaxDetails.name());*/

	Boolean isDeleted = organizationTaxDetailsService.deleteOrganizationTaxDetails(id);
	if (isDeleted)
	    return new RestResponse(RestResponse.SUCCESS, SuccessHandler.TAX_DETAILS_DELETED_SUCCESS,
		    SuccessHandler.TAX_DETAILS_DELETED_SUCCESS);
	return new RestResponse(RestResponse.FAILED, ErrorHandler.TAX_DETAILS_DELETE_FAILED,
		ErrorHandler.TAX_DETAILS_DELETE__FAILED);
    }
}
