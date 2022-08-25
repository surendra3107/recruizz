package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.text.ParseException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionRequestService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Position Controller
 */
@RestController
public class PositionRequestController {

    @Autowired
    RoundCandidateService roundCandidateService;

    @Autowired
    private UserService userService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private PositionRequestService positionRequestService;

    @Autowired
    private CheckUserPermissionService checkUserPermissionService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    /**
     * 
     * @param position
     * @param file
     * @param clientName
     * @param fileName
     * @return
     * @throws RecruizException
     * @throws ParseException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/position/request", method = RequestMethod.POST)
    public RestResponse raisePositionRequest(@RequestPart("json") @Valid PositionDTO position,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam("clientName") String clientName,
	    @RequestParam(value = "fileName", required = false) String fileName)
	    throws RecruizException, ParseException, IOException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.RaisePositionRequest.name());

	PositionRequest positionRequest = positionRequestService.requestPosition(position, fileName, file, clientName);
	if (positionRequest == null) {
	    return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
	}
	return new RestResponse(RestResponse.SUCCESS, positionRequest, null);
    }

    /**
     * 
     * @param pageNo
     * @param sortField
     * @return
     * @throws RecruizException
     * @throws ParseException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/position/request/all", method = RequestMethod.GET)
    public RestResponse getAllRequestedPosition(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder)
	    throws RecruizException, ParseException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllRequestedPosition.name());*/

	Page<PositionRequest> allRequestedPosition = positionRequestService.getAllPositionRequest(
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
	return new RestResponse(RestResponse.SUCCESS, allRequestedPosition, null);
    }

    @RequestMapping(value = "/api/v1/position/request/all/app/user", method = RequestMethod.GET)
    public RestResponse getAllRequestedPositionForAppUser(
	    @RequestParam(value = "clientName", required = false) String clientName,
	    @RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder)
	    throws RecruizException, ParseException, IOException {
/*
	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllRequestedPosition.name());*/

	Page<PositionRequest> allRequestedPosition = null;
	if (clientName != null && !clientName.isEmpty()) {
	    allRequestedPosition = positionRequestService.getAllPositionRequestForAppUserByClient(clientName,
		    pageableService.getPageRequestObject(pageNo, sortField,
			    pageableService.getSortDirection(sortOrder)));
	} else {
	    allRequestedPosition = positionRequestService.getAllPositionRequestForAppUser(pageableService
		    .getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
	}

	return new RestResponse(RestResponse.SUCCESS, allRequestedPosition, null);
    }

    /**
     * 
     * @param pageNo
     * @param sortField
     * @return
     * @throws RecruizException
     * @throws ParseException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/position/request/inprocess", method = RequestMethod.GET)
    public RestResponse getAllInprocessRequestedPosition(
	    @RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField)
	    throws RecruizException, ParseException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllInProcessRequestedPosition.name());*/

	Page<PositionRequest> allRequestedPosition = positionRequestService
		.getAllInProcessPositionRequest(pageableService.getPageRequestObject(pageNo, sortField));
	return new RestResponse(RestResponse.SUCCESS, allRequestedPosition, null);
    }

    /**
     * 
     * @param pid
     * @return
     * @throws RecruizException
     * @throws ParseException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/position/request/detail/{pid}", method = RequestMethod.GET)
    public RestResponse getRequestedPositionDetails(@PathVariable("pid") String pid)
	    throws RecruizException, ParseException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetRequestedPositionDetails.name());
*/
	PositionRequest requestedPosition = positionRequestService.getPositionRequestDetails(pid);
	if (requestedPosition == null)
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS,
		    ErrorHandler.POSITION_NOT_FOUND);
	if (requestedPosition != null) {

	    positionRequestService.getEmploymentTypeDisplayName(requestedPosition);

	    requestedPosition.getGoodSkillSet().size();
	    requestedPosition.getReqSkillSet().size();
	    requestedPosition.getEducationalQualification().size();
	    requestedPosition.getPositionRequestNotes().size();
	}
	return new RestResponse(RestResponse.SUCCESS, requestedPosition, null);
    }

    @RequestMapping(value = "/api/v1/position/request/new", method = RequestMethod.GET)
    public RestResponse getAllNewRequestedPosition(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "clientName", required = false) String clientName,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder)
	    throws RecruizException, ParseException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllNewRequestedPosition.name());
*/
	Page<PositionRequest> requestedPosition = null;
	if (clientName != null && !clientName.isEmpty()) {
	    requestedPosition = positionRequestService.getAllNewRequestByClient(pageableService
		    .getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)), clientName);
	} else {
	    requestedPosition = positionRequestService.getAllNewRequest(pageableService.getPageRequestObject(pageNo,
		    sortField, pageableService.getSortDirection(sortOrder)));
	}
	if (requestedPosition == null || requestedPosition.getContent().isEmpty()) {
	    return new RestResponse(false, ErrorHandler.NO_REQUESTED_POSITION_EXISTS, ErrorHandler.NO_REQUEST_POSITION);
	}

	return new RestResponse(RestResponse.SUCCESS, requestedPosition, null);
    }

    @RequestMapping(value = "/api/v1/position/request/edit", method = RequestMethod.POST)
    public RestResponse editRequestedPosition(@RequestPart("json") @Valid PositionRequest positionRequest,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam(value = "fileName", required = false) String fileName)
	    throws RecruizException, ParseException, IOException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.EditRequestedPosition.name());

	PositionRequest updatedPositionRequest = positionRequestService.updatePositionRequest(positionRequest, fileName,
		file);
	if (updatedPositionRequest == null) {
	    return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
	}
	return new RestResponse(RestResponse.SUCCESS, updatedPositionRequest, null);
    }

    @RequestMapping(value = "/api/v1/position/request/status", method = RequestMethod.PUT)
    public RestResponse changeRequestStatus(@RequestParam("pid") String pid, @RequestParam("status") String status,
	    @RequestParam(required = false) String comment) throws RecruizException, ParseException, IOException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ChangeRequestedPositionStatus.name());

	if (pid == null || pid.isEmpty() || status == null || status.isEmpty())
	    return null;
	PositionRequest requestedPosition = positionRequestService.updateRequestStatus(Long.parseLong(pid), status,
		comment);
	return new RestResponse(RestResponse.SUCCESS, requestedPosition, null);
    }

    @RequestMapping(value = "/api/v1/position/request/delete", method = RequestMethod.DELETE)
    public RestResponse deleteRequestedPosition(@RequestParam("pid") String pid)
	    throws RecruizException, ParseException, IOException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteRequestedPosition.name());

	if (pid == null || pid.isEmpty())
	    return null;
	PositionRequest positionRequest = positionRequestService.findOne(Long.parseLong(pid));
	if (positionRequest == null) {
	    return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.NO_POSITION);
	}
	if (!positionRequest.getRequestedByEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())
		&& !checkUserPermissionService.isSuperAdmin()) {
	    return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
	} else if (positionRequest.getStatus().equalsIgnoreCase(PositionRequestStatus.InProcess.toString())) {
	    return new RestResponse(false, ErrorHandler.CANNOT_DELETE_INPROCESS,
		    ErrorHandler.INPROCESS_REQUEST_CANNOT_BE_DELETED);
	} else {
	    positionRequestService.delete(positionRequest);
	    return new RestResponse(RestResponse.SUCCESS, SuccessHandler.POSITION_DELETED);
	}
    }
}
