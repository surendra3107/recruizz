package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectVendorDTO;
import com.auklabs.recruiz.connect.core.enums.Status;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.RecruizConnectService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class RecruizConnectController {

	@Autowired
	private UserService userService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	private RestResponse restResponse;

	@RequestMapping(value = "/api/v1/recruiz/connect/corporate/add", method = RequestMethod.GET)
	public RestResponse connectToRecruizConnect() throws Exception {

		User loggedInuser = userService.getLoggedInUserObject();
		if (loggedInuser == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		recruizConnectService.addRecruizConnectCorporate(loggedInuser);

		restResponse = new RestResponse(RestResponse.SUCCESS, "Recruiz Connect for corporate added successfully");
		return restResponse;
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor/add", method = RequestMethod.POST)
	public RestResponse addVendorRecruizConnect(@RequestBody ConnectVendorDTO connectVendorDTO) throws Exception {

		String tenantId = getTenantFromCurrentUser();
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		recruizConnectService.addRecruizConnectVendor(connectVendorDTO);

		restResponse = new RestResponse(RestResponse.SUCCESS, "Recruiz Connect for vendor added successfully");
		return restResponse;
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor/update", method = RequestMethod.PUT)
	public RestResponse updateVendorRecruizConnect(@RequestBody ConnectVendorDTO connectVendorDTO) throws Exception {

		String tenantId = getTenantFromCurrentUser();
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);

		recruizConnectService.updateRecruizConenctVendor(connectVendorDTO, tenantId);

		restResponse = new RestResponse(RestResponse.SUCCESS, "Recruiz Connect for vendor updated successfully");
		return restResponse;
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor", method = RequestMethod.GET)
	public RestResponse getVendorRecruizConnect() throws Exception {

		String tenantId = getTenantFromCurrentUser();
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		RestResponse response = recruizConnectService.getRecruizConnectVendor(tenantId);

		return response;
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor/all", method = RequestMethod.GET)
	public RestResponse getAllVendorRecruizConnect() throws Exception {

		List<ConnectVendorDTO> response = recruizConnectService.getRecruizConnectAllVendor();

		return new RestResponse(RestResponse.SUCCESS, response);
	}

	private String getTenantFromCurrentUser() {
		String tenantId = null;
		User loggedInUser = userService.getLoggedInUserObject();
		if (loggedInUser != null)
			tenantId = loggedInUser.getOrganization().getOrgId();
		return tenantId;
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor/position/pending", method = RequestMethod.GET)
	public RestResponse getAllVendorPendingPositionRecruizConnect() throws Exception {

		String tenantId = getTenantFromCurrentUser();
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		List<ConnectPositionDTO> response = recruizConnectService.getAllPendingPositionVendor(tenantId);

		return new RestResponse(RestResponse.SUCCESS, response);
	}

	@RequestMapping(value = "/api/v1/recruiz/connect/vendor/position/{positionCode}", method = RequestMethod.PUT)
	public RestResponse ChangeVendorPositionStatus(@PathVariable("positionCode") String positionCode,
			@RequestParam("corporateId") String corporateId,
			@RequestParam("corporateInstanceId") String corporateInstanceId, @RequestParam("status") String status)
			throws Exception {

		String tenantId = getTenantFromCurrentUser();
		Status getStatus = Status.valueOf(status);
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		recruizConnectService.sendVendorPositionStatus(positionCode, corporateId, corporateInstanceId, tenantId,
				getStatus.toString());

		return new RestResponse(RestResponse.SUCCESS, "Successfully " + status + " the position");
	}

	@RequestMapping(value = "/auth/recruiz/connect/candidate/source", method = RequestMethod.POST)
	public RestResponse sourceCandidate(@RequestBody ConnectCandidateEventDTO connectCandidateEventDTO)
			throws Exception {

		String tenantId = connectCandidateEventDTO.getCorporateId();
		if (tenantId == null)
			return new RestResponse(RestResponse.FAILED, "Tenant information missing", ErrorHandler.TENANT_NOT_PRESENT);
		TenantContextHolder.setTenant(tenantId);
		int duplicateCandidateCount = recruizConnectService.sourceCandidate(connectCandidateEventDTO);
		if (duplicateCandidateCount > 0)
			return new RestResponse(RestResponse.FAILED, "Looks like " + duplicateCandidateCount + " found",
					ErrorHandler.CONNECT_DUPLICATE_CANDIDATE);
		else
			return new RestResponse(RestResponse.SUCCESS, "All candidates sourced successfully");
	}

}
