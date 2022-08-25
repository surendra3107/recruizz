package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.GenericDecisionMaker;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DecisionMakerService;
import com.bbytes.recruiz.service.GenericDecisionMakerService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class GenericDecisionMakerController {

    @Autowired
    private GenericDecisionMakerService genericDecisionMakerService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private DecisionMakerService decisionMakerService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/api/v1/generic/dm/add", method = RequestMethod.POST)
    public RestResponse addGenericDM(@RequestBody List<GenericDecisionMaker> dms) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddGenericDM.name());*/
	
	RestResponse response = null;
	try {
	    List<GenericDecisionMaker> savedDM = genericDecisionMakerService.saveDM(dms);
	    response = new RestResponse(RestResponse.SUCCESS, savedDM, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_ADDING_GENERIC_DM);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/dm/client/{clientId}/add", method = RequestMethod.POST)
    public RestResponse addGenericDMFromClient(@RequestBody List<GenericDecisionMaker> dms, @PathVariable Long clientId)
	    throws RecruizException {

/*	// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.AddGenericDMFromClient.name());*/
	
	RestResponse response = null;
	try {
	    List<GenericDecisionMaker> savedDM = genericDecisionMakerService.saveDM(dms);

	    // adding to client
	    for (GenericDecisionMaker genericDecisionMaker : savedDM) {
		genericDecisionMakerService.addDMToClient(clientId, genericDecisionMaker);
	    }

	    response = new RestResponse(RestResponse.SUCCESS, savedDM, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_ADDING_GENERIC_DM);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/dm/update/{email:.+}", method = RequestMethod.PUT)
    public RestResponse updateGenericDM(@RequestBody GenericDecisionMaker dm, @PathVariable String email)
	    throws RecruizException {

/*	// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.UpdateGenericDM.name());*/
	
	RestResponse response = null;
	try {
	    GenericDecisionMaker savedDM = genericDecisionMakerService.updateDM(email, dm);
	    response = new RestResponse(RestResponse.SUCCESS, savedDM, null);
	} catch (RecruizException rex) {
	    response = new RestResponse(RestResponse.FAILED, rex.getMessage(), rex.getErrConstant());
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_UPDATING_GENERIC_DM);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/dm/get/all", method = RequestMethod.GET)
    public RestResponse getAllDM(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {
	
/*	// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetAllDM.name());*/
	
	List<GenericDecisionMaker> dmPage = genericDecisionMakerService.getAllDecisionMaker();

	RestResponse response = new RestResponse(RestResponse.SUCCESS, dmPage, null);
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/dm/delete/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteGenericDecisionMaker(@PathVariable Long id) throws RecruizException {

/*	// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.DeleteGenericDM.name());*/
	
	RestResponse response = null;
	try {
	    GenericDecisionMaker savedDm = genericDecisionMakerService.findOne(id);
	    genericDecisionMakerService.delete(savedDm);
	    response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.GENERIC_DM_DELETED, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_TO_DELETE_GENERIC_DM);
	}
	return response;

    }

}
