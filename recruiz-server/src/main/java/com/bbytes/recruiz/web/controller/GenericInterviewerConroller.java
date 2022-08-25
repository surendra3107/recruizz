package com.bbytes.recruiz.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.GenericInterviewerService;
import com.bbytes.recruiz.service.InterviewPanelService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class GenericInterviewerConroller {

    Logger logger = LoggerFactory.getLogger(GenericInterviewerConroller.class);

    @Autowired
    private GenericInterviewerService genericInterviewerService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private InterviewPanelService interviewPanelService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/api/v1/generic/interviewer/add", method = RequestMethod.POST)
    public RestResponse addGenericInterviewer(@RequestBody List<GenericInterviewer> interviewer)
	    throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddGerericInterviewer.name());

	RestResponse response = null;
	try {
	    List<GenericInterviewer> savedInterviewer = genericInterviewerService.saveInterviewer(interviewer);
	    response = new RestResponse(RestResponse.SUCCESS, savedInterviewer, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(),
		    ErrorHandler.FAILED_ADDING_GENERIC_INTERVIEWER);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/update/{email:.+}", method = RequestMethod.PUT)
    public RestResponse updateGenericInterviewer(@RequestBody GenericInterviewer interviewer,
	    @PathVariable String email) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.UpdateGenericInterviewer.name());*/

	RestResponse response = null;
	try {
	    GenericInterviewer savedInterviewer = genericInterviewerService.updateInterviewer(email, interviewer);
	    response = new RestResponse(RestResponse.SUCCESS, savedInterviewer, null);
	} catch (RecruizException rex) {
	    logger.error(rex.getMessage(), rex);
	    response = new RestResponse(RestResponse.FAILED, rex.getMessage(), rex.getErrConstant());
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(),
		    ErrorHandler.FAILED_ADDING_GENERIC_INTERVIEWER);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/get/all", method = RequestMethod.GET)
    public RestResponse getAllInterviewer(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllGenericInterviewer.name());*/

	List<GenericInterviewer> interviewerPage = genericInterviewerService.getAllInterviewer();

	List<GenericInterviewer> dummyInterviewer = genericInterviewerService.getDummyIngterviewer();
	
	if(null != interviewerPage && ! interviewerPage.isEmpty() && dummyInterviewer != null && !dummyInterviewer.isEmpty()) {
	    interviewerPage.removeAll(dummyInterviewer);
	}
	
	RestResponse response = new RestResponse(RestResponse.SUCCESS, interviewerPage, null);
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/position/{pid}/add", method = RequestMethod.POST)
    public RestResponse addGenericInterviewerToPosition(@RequestBody List<GenericInterviewer> interviewers,
	    @PathVariable Long pid) throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddGenericInterviewerToPosition.name());

	RestResponse response = null;
	try {
	    List<GenericInterviewer> savedInterviewers = genericInterviewerService.saveInterviewer(interviewers);
	    for (GenericInterviewer genericInterviewer : savedInterviewers) {
		genericInterviewerService.addGenericInterviewerToPosition(genericInterviewer, pid);
	    }
	    response = new RestResponse(RestResponse.SUCCESS, savedInterviewers, null);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(),
		    ErrorHandler.FAILED_ADDING_GENERIC_INTERVIEWER);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/client/{cid}/add", method = RequestMethod.POST)
    public RestResponse addGenericInterviewerToClient(@RequestBody List<GenericInterviewer> interviewers,
	    @PathVariable Long cid) throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteGenericInterviewerFromClient.name());

	RestResponse response = null;
	try {
	    List<GenericInterviewer> savedInterviewer = genericInterviewerService.saveInterviewer(interviewers);
	    for (GenericInterviewer genericInterviewer : savedInterviewer) {
		genericInterviewerService.addGenericInterviewerToClient(genericInterviewer, cid);
	    }

	    response = new RestResponse(RestResponse.SUCCESS, savedInterviewer, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(),
		    ErrorHandler.FAILED_ADDING_GENERIC_INTERVIEWER);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/delete/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteGenericInterviewer(@PathVariable Long id) throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteGenericInterviewer.name());

	RestResponse response = null;
	try {
	    GenericInterviewer savedInterviewer = genericInterviewerService.findOne(id);
	    List<ClientInterviewerPanel> clientInterviewer = interviewPanelService
		    .getInterviewerByEmail(savedInterviewer.getEmail());
	    if (null != clientInterviewer && !clientInterviewer.isEmpty()) {
		Set<ClientInterviewerPanel> exisitingPanel = new HashSet<>();
		exisitingPanel.addAll(clientInterviewer);
		clientService.deleteInterviewerFormClientAndPosition(exisitingPanel);
	    }

	    genericInterviewerService.delete(savedInterviewer);

	    response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.INTERVIEWER_DELETED, null);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(),
		    ErrorHandler.FAILED_TO_DELETE_GENERIC_INTERVIEWER);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/email/check", method = RequestMethod.GET)
    public RestResponse emailIdExistsForGenericInterviewer(@RequestParam String emailId) throws RecruizException {

	RestResponse response = null;
	try {
	    boolean exists = false;
	    if (genericInterviewerService.getInterviewerByEmail(emailId) != null)
		exists = true;

	    response = new RestResponse(RestResponse.SUCCESS, exists, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_TO_CHECK_EMAIL);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/generic/interviewer/mobile/check", method = RequestMethod.GET)
    public RestResponse mobileExistsForGenericInterviewer(@RequestParam String mobile) throws RecruizException {

	RestResponse response = null;
	try {
	    boolean exists = false;
	    if (genericInterviewerService.getInterviewerByMobile(mobile) != null
		    && !genericInterviewerService.getInterviewerByMobile(mobile).isEmpty())
		exists = true;

	    response = new RestResponse(RestResponse.SUCCESS, exists, null);
	} catch (Exception ex) {
	    response = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FAILED_TO_CHECK_MOBILE);
	}
	return response;
    }

}
