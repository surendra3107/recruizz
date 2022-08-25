package com.bbytes.recruiz.web.controller;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.enums.MetricsTimePeriod;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.Metrics;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.MetricsService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.visualization.datasource.base.TypeMismatchException;

/**
 * Metrics(Statistics) Controller - Used to all graph related data manipulations
 * 
 * @author Akshay
 *
 */
@RestController
@RequestMapping(value = "/api/v1/metrics")
public class MetricsController {

	public static final String CLIENT_NAME = "clientName";
	public static final String CLIENT_ID = "clientId";
	public static final String POSITION_NAME = "positionName";
	public static final String POSITION_ID = "positionId";

	@Autowired
	private MetricsService metricsService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * API to get graph data per position opened
	 * 
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/opened", method = RequestMethod.GET)
	public RestResponse getOpenPositionStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOpenPositionStats.name());*/

		Metrics metrics = metricsService.perPositionDaysSincePostionOpened(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall open position
	 * 
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/opened", method = RequestMethod.GET)
	public RestResponse getOverallOpenPositionStats(@RequestParam(value = "status", required = false) String status)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallOpenPositionStats.name());*/

		String positionStatus = null;
		if (status == null || status.isEmpty()) {
			positionStatus = Status.Active.getDisplayName();
		}
		Metrics metrics = metricsService.getDaysSincePostionOpened(positionStatus);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for clientwise open position
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/client/{clientId}/opened", method = RequestMethod.GET)
	public RestResponse getClientwiseOpenPositionStats(@PathVariable("clientId") String clientId)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseOpenPositionStats.name());*/

		ClientOpeningCountDTO client = clientService.getClient(Long.parseLong(clientId));
		Metrics metrics = metricsService.clientwiseDaysSincePostionOpened(clientId);

		final Map<String, Object> clientMap = new LinkedHashMap<String, Object>();
		if (client != null) {
			clientMap.put(CLIENT_NAME, client.getClient().getClientName());
			clientMap.put(CLIENT_ID, client.getClient().getId());
		}
		metrics.setDetails(clientMap);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data per position closed
	 * 
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/closed", method = RequestMethod.GET)
	public RestResponse getClosePositionStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClosePositionStats.name());*/

		Metrics metrics = metricsService.perPositionDaysSincePostionClosed(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall close position
	 * 
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/closed", method = RequestMethod.GET)
	public RestResponse getOverallClosePositionStats(@RequestParam(value = "status", required = false) String status)
			throws RecruizException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallClosePositionStats.name());*/
	    
		String positionStatus = null;
		if (status == null || status.isEmpty()) {
			positionStatus = Status.Active.getDisplayName();
		}
		Metrics metrics = metricsService.getDaysSincePostionClosed(positionStatus);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for clientwise close position
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/client/{clientId}/closed", method = RequestMethod.GET)
	public RestResponse getClientwiseClosePositionStats(@PathVariable("clientId") String clientId)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseClosePositionStats.name());*/

		Metrics metrics = metricsService.clientwiseDaysSincePostionClosed(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall sourcing channels of all positions
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/sourcing", method = RequestMethod.GET)
	public RestResponse getOverallPositionSourcingChannelStats() throws RecruizException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionSourcingChannelStats.name());*/
	    
		Metrics metrics = metricsService.overallPositionSoucingChannelMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for per position sourcing channels
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/sourcing", method = RequestMethod.GET)
	public RestResponse getPerPositionSourcingChannelStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetPerPositionSourcingChannelStats.name());*/

		Metrics metrics = metricsService.perPositionSoucingChannelMix(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for clientwise sourcing channels
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/client/{clientId}/sourcing", method = RequestMethod.GET)
	public RestResponse getClientwiseSourcingChannelStats(@PathVariable("clientId") String clientId)
			throws RecruizException {
	    
/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseSourcingChannelStats.name());*/

		Metrics metrics = metricsService.clientwiseSoucingChannelMix(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for gender mix of a position
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/gender", method = RequestMethod.GET)
	public RestResponse getPerPositionGenderMixStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetPerPositionGenderMixStats.name());*/
	    
		Position position = positionService.getPositionByCode(positionCode);

		Metrics metrics = metricsService.perPositionGenderMix(positionCode);

		final Map<String, Object> positionMap = new LinkedHashMap<String, Object>();
		if (position != null) {
			positionMap.put(CLIENT_NAME, position.getClient().getClientName());
			positionMap.put(CLIENT_ID, position.getClient().getId());
			positionMap.put(POSITION_NAME, position.getTitle());
			positionMap.put(POSITION_ID, position.getId());
		}
		metrics.setDetails(positionMap);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for gender mix client wise
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/client/{clientId}/gender", method = RequestMethod.GET)
	public RestResponse getClientwiseGenderMixStats(@PathVariable("clientId") String clientId)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseGenderMixStats.name());*/
		
		Metrics metrics = metricsService.clientwiseGenderMix(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall gender mix all position
	 * 
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/gender", method = RequestMethod.GET)
	public RestResponse getOverallPositionGenderMixStats() throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionGenderMixStats.name());*/
		
		Metrics metrics = metricsService.overallPositionGenderMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall clientwise interview schedule
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/client/{clientId}/interviewSchedule", method = RequestMethod.GET)
	public RestResponse getClientwiseInterviewScheduleStats(@PathVariable("clientId") String clientId)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseInterviewScheduleStats.name());*/
	    
		Metrics metrics = metricsService.clientwisePositionInterviewSchedule(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall interview schedule of all positions
	 * 
	 * @param status
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/interviewSchedule", method = RequestMethod.GET)
	public RestResponse getOverallPositionInterviewScheduleStats(
			@RequestParam(value = "status", required = false) String status)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionInterviewScheduleStats.name());*/
	    
		String positionStatus = null;
		if (status == null || status.isEmpty()) {
			positionStatus = Status.Active.getDisplayName();
		}
		Metrics metrics = metricsService.overallPositionInterviewSchedule(positionStatus);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for interview schedule of a position
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/{positionCode}/interviewSchedule", method = RequestMethod.GET)
	public RestResponse getPerPositionInterviewScheduleStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetPerPositionInterviewScheduleStats.name());*/
	    
		Metrics metrics = metricsService.perPositionInterviewSchedule(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for new candidate sourced for overall clientwise
	 * (Daily,Weekly,Bi-weekly and Monthly)
	 * 
	 * @param timePeriod
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/client/{clientId}/candidate/sourced", method = RequestMethod.GET)
	public RestResponse getClientwiseNewSourcedCandidateStats(
			@RequestParam(value = "timePeriod", required = false) String timePeriod,
			@PathVariable("clientId") String clientId) throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseNewSourcedCandidateStats.name());*/
	    
		Integer timePeriodValue;
		String aggrType;

		// if timeperiod is not provided from UI it will set as 'Weekly'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriodValue = MetricsTimePeriod.Weekly.getDays();
			aggrType = MetricsTimePeriod.Weekly.getAggrType();
		} else {
			timePeriodValue = MetricsTimePeriod.valueOf(timePeriod).getDays();
			aggrType = MetricsTimePeriod.valueOf(timePeriod).getAggrType();
		}
		Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
		Date startDate = startEndDates[0];
		Date endDate = startEndDates[1];

		Metrics metrics = metricsService.clientwisePositionCandidateSourced(startDate, endDate, aggrType, clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for new candidate sourced for overall position
	 * (Daily,Weekly,Bi-weekly and Monthly)
	 * 
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/candidate/sourced", method = RequestMethod.GET)
	public RestResponse getOverallPositionNewSourcedCandidateStats(
			@RequestParam(value = "timePeriod", required = false) String timePeriod)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionNewSourcedCandidateStats.name());*/
	    
		Integer timePeriodValue;
		String aggrType;

		// if timeperiod is not provided from UI it will set as 'Weekly'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriodValue = MetricsTimePeriod.Weekly.getDays();
			aggrType = MetricsTimePeriod.Weekly.getAggrType();
		} else {
			timePeriodValue = MetricsTimePeriod.valueOf(timePeriod).getDays();
			aggrType = MetricsTimePeriod.valueOf(timePeriod).getAggrType();
		}
		Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
		Date startDate = startEndDates[0];
		Date endDate = startEndDates[1];

		Metrics metrics = metricsService.overallPositionCandidateSourced(startDate, endDate, aggrType);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for new candidate sourced per position
	 * (Daily,Weekly,Bi-weekly and Monthly)
	 * 
	 * @param timePeriod
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/{positionCode}/candidate/sourced", method = RequestMethod.GET)
	public RestResponse getPerPositionNewSourcedCandidateStats(
			@RequestParam(value = "timePeriod", required = false) String timePeriod,
			@PathVariable("positionCode") String positionCode) throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetPerPositionNewSourcedCandidateStats.name());*/
	    
		Integer timePeriodValue;
		String aggrType;

		// if timeperiod is not provided from UI it will set as 'Weekly'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriodValue = MetricsTimePeriod.Weekly.getDays();
			aggrType = MetricsTimePeriod.Weekly.getAggrType();
		} else {
			timePeriodValue = MetricsTimePeriod.valueOf(timePeriod).getDays();
			aggrType = MetricsTimePeriod.valueOf(timePeriod).getAggrType();
		}
		Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
		Date startDate = startEndDates[0];
		Date endDate = startEndDates[1];

		Metrics metrics = metricsService.perPositionCandidateSourced(startDate, endDate, aggrType, positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall candidate rejected with reason of
	 * clientwise
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/client/{clientId}/candidate/rejection", method = RequestMethod.GET)
	public RestResponse getClientwiseCandidateRejectionMixStats(@PathVariable("clientId") String clientId)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseCandidateRejectionMixStats.name());*/
		
		Metrics metrics = metricsService.clientwiseCandidateRejectionMix(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall candidate rejected with reason of all
	 * positions
	 * 
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/candidate/rejection", method = RequestMethod.GET)
	public RestResponse getOverallPositionCandidateRejectionMixStats() throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionCandidateRejectionMixStats.name());*/
	    
		Metrics metrics = metricsService.overallPositionCandidateRejectionMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for candidate rejected of a position
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/{positionCode}/candidate/rejection", method = RequestMethod.GET)
	public RestResponse getPerPositionCandidateRejectionMixStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionCandidateRejectionMixStats.name());*/
	    
		Metrics metrics = metricsService.perPositionCandadidateRejectionMix(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall candidate sourced by recruiter
	 * clientwise
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/client/{clientId}/candidate/recruiter/sourced", method = RequestMethod.GET)
	public RestResponse getClientwiseSourcedbyRecruiterStats(@PathVariable("clientId") String clientId)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetClientwiseSourcedbyRecruiterStats.name());*/
	    
		Metrics metrics = metricsService.clientwiseCandidateSourcedbyRecruiterMix(clientId);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for overall candidate sourced by recruiter of all
	 * positions
	 * 
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/candidate/recruiter/sourced", method = RequestMethod.GET)
	public RestResponse getOverallPositionSourcedbyRecruiterStats() throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetOverallPositionSourcedbyRecruiterStats.name());*/
	    
		Metrics metrics = metricsService.overallPositionSourcedbyRecruiterMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}

	/**
	 * API to get graph data for candidate sourced by recruiter of a position
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/position/{positionCode}/candidate/recruiter/sourced", method = RequestMethod.GET)
	public RestResponse getPerPositionSourcedbyRecruiterStats(@PathVariable("positionCode") String positionCode)
			throws RecruizException, TypeMismatchException {

/*	    // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetPerPositionSourcedbyRecruiterStats.name());*/
	    
		Metrics metrics = metricsService.perPositionSourcedbyRecruiterMix(positionCode);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, metrics);
		return response;
	}
}
