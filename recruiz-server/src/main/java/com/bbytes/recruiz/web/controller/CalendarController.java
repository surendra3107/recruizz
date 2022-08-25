package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class CalendarController {

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/api/v1/mailgun/storedEmail", method = RequestMethod.GET)
	public void getStoreMessage() throws NumberFormatException, Exception {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod("http://requestb.in/11f1swv1");
		try {
			int statusCode = client.executeMethod(method);
			byte[] responseBody = method.getResponseBody();
		} catch (Exception e) {
			System.err.println("Fatal error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * API is used to get complete calendar for interview schedule.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/calender/all", method = RequestMethod.GET)
	public RestResponse getCompleteCalender() throws RecruizException {
		
	 // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetCompleteCalender.name());
	    
		if(!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);
		
		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}
		List<InterviewSchedule> allCalender = interviewScheduleService.getAllCalenderByOwner();
		for (InterviewSchedule interviewSchedule : allCalender) {
			interviewSchedule.getAttendee().size();
		}
		return new RestResponse(RestResponse.SUCCESS, allCalender, null);
	}

	/**
	 * API is used to get calendar by monthly for interview schedule.
	 * 
	 * @param month
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/calender/monthly", method = RequestMethod.GET)
	public RestResponse getMonthlyCalender(@RequestParam(value = "month", required = false) String month)
			throws RecruizException {
	    
/*	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
	 			UsageActionType.GetMonthlyCalender.name());*/
		
		if(!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (month == null || month.isEmpty())
			return null;
		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}
		List<InterviewSchedule> allCalender = interviewScheduleService.getCalenderForMonthByOwner(month);
		for (InterviewSchedule interviewSchedule : allCalender) {
			interviewSchedule.getAttendee().size();
		}
		return new RestResponse(RestResponse.SUCCESS, allCalender, null);
	}

	/**
	 * API is used to get calendar for interview schedule by position.
	 * 
	 * @param positionCode
	 * @return
	 */
	@RequestMapping(value = "/api/v1/calender/position", method = RequestMethod.GET)
	public RestResponse getCalenderForPosition(
			@RequestParam(value = "positionCode", required = false) String positionCode) {

	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
	 			UsageActionType.GetCalenderForPosition.name());
	 		
		if(!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);
		
		if (positionCode == null || positionCode.isEmpty())
			return null;
		List<InterviewSchedule> allCalender = interviewScheduleService.getCalenderForPosition(positionCode);
		for (InterviewSchedule interviewSchedule : allCalender) {
			interviewSchedule.getAttendee().size();
		}
		return new RestResponse(RestResponse.SUCCESS, allCalender, null);
	}

	/**
	 * API is used to get monthly calendar for interview schedule by position.
	 * 
	 * @param positionCode
	 * @param month
	 * @return
	 */
	@RequestMapping(value = "/api/v1/calender/position/month", method = RequestMethod.GET)
	public RestResponse getCalenderForPosition(
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "month", required = false) String month) {

/*	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
	 			UsageActionType.GetMonthlyCalenderByPosition.name());*/
	 		
		if(!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);
		
		if (positionCode == null || positionCode.isEmpty() || month == null || month.isEmpty())
			return null;
		List<InterviewSchedule> allCalender = interviewScheduleService.getCalenderForPositionByMonth(positionCode,
				month);
		for (InterviewSchedule interviewSchedule : allCalender) {
			interviewSchedule.getAttendee().size();
		}
		return new RestResponse(RestResponse.SUCCESS, allCalender, null);
	}
}
