package com.bbytes.recruiz.web.controller;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.enums.AppUsageStatRange;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.ReportDropdownDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.BoardService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.FeedbackService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.ReportService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class AppUsageController {

	@Autowired
	private BoardService boardService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private UserService userService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private ReportService reportService;

	/**
	 * API used to get all candidates for sourcing apart from board.
	 * 
	 * @param boardId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/usage/report", method = RequestMethod.GET)
	public RestResponse getUserUsageReport(@RequestParam(value = "userEmail", required = false) String userEmail,
			@RequestParam(value = "longDate") Long longDate) throws RecruizException {

		// making entry to usage stat table
		Date queryDate = new Date(longDate);

		Long loggedInDuration = tenantUsageStatService.getUserLoggedInHourForDate(TenantContextHolder.getTenant(),
				userEmail, queryDate, new Date());
		Long idleDuration = tenantUsageStatService.getIdleDurationForAUserForGivenDate(TenantContextHolder.getTenant(),
				userEmail, GlobalConstants.USER_IDLE_TIMEOUT, queryDate, new Date());

		Map<String, String> loggedInInfo = new HashMap<String, String>();
		loggedInInfo.put("Total Logged time",
				String.format("%.02f", (float) (loggedInDuration.floatValue() / 3600)) + "hour(s)");
		loggedInInfo.put("Total Idle Time",
				String.format("%.02f", (float) (idleDuration.floatValue() / 3600)) + "hour(s)");
		loggedInInfo.put("Total Active Time",
				String.format("%.02f", (float) ((loggedInDuration.floatValue() - idleDuration.floatValue()) / 3600))
						+ "hour(s)");

		RestResponse response = new RestResponse(RestResponse.SUCCESS, loggedInInfo, null);
		return response;
	}

	// get usage report for multiple user on a date
	@RequestMapping(value = "/api/v1/user/list/usage/report", method = RequestMethod.GET)
	public RestResponse getUserUsageReport(
			@RequestParam(value = "userEmails", required = false) List<String> userEmails,
			@RequestParam(value = "longDate") Long longDate) throws RecruizException {

		// making entry to usage stat table
		Date queryDate = new Date(longDate);

		Map<String, Object> usageReport = new HashMap<>();
		for (String userEmail : userEmails) {
			Long loggedInDuration = tenantUsageStatService.getUserLoggedInHourForDate(TenantContextHolder.getTenant(),
					userEmail, queryDate, new Date());
			Long idleDuration = tenantUsageStatService.getIdleDurationForAUserForGivenDate(
					TenantContextHolder.getTenant(), userEmail, GlobalConstants.USER_IDLE_TIMEOUT, queryDate,
					new Date());

			Map<String, String> loggedInInfo = new HashMap<String, String>();
			loggedInInfo.put("Total Logged time",
					String.format("%.02f", (float) (loggedInDuration.floatValue() / 3600)) + "hour(s)");
			loggedInInfo.put("Total Idle Time",
					String.format("%.02f", (float) (idleDuration.floatValue() / 3600)) + "hour(s)");
			loggedInInfo.put("Total Active Time",
					String.format("%.02f", (float) ((loggedInDuration.floatValue() - idleDuration.floatValue()) / 3600))
							+ "hour(s)");

			usageReport.put(userEmail, loggedInInfo);
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, usageReport, null);
		return response;
	}

	// get usage report for user on a given interval
	@RequestMapping(value = "/api/v1/user/interval/usage/report", method = RequestMethod.GET)
	public RestResponse getUserUsageReportForAInterval(
			@RequestParam(value = "userEmail", required = false) String userEmail,
			@RequestParam(value = "range") String range,
			@RequestParam(value = "startDate", required = false) String startDateCustom,
			@RequestParam(value = "endDate", required = false) String endDateCustom) throws RecruizException {

		LocalDateTime endDate = new DateTime().plusDays(1).toLocalDateTime();
		org.joda.time.LocalDateTime dateToCheck = null;

		if (range.equalsIgnoreCase(AppUsageStatRange.OneHour.name())) {
			dateToCheck = new org.joda.time.LocalDateTime().minusHours(1);
			endDate = new org.joda.time.LocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last12Hour.name())) {
			dateToCheck = new org.joda.time.LocalDateTime().minusHours(12);
			endDate = new org.joda.time.LocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneDay.name())) {
			dateToCheck = new DateTime().minusHours(24).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneWeek.name())) {
			dateToCheck = new DateTime().minusDays(7).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneMonth.name())) {
			dateToCheck = new DateTime().minusDays(30).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last3Months.name())) {
			dateToCheck = new DateTime().minusDays(90).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last6Months.name())) {
			dateToCheck = new DateTime().minusDays(180).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Custom.name())) {

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			LocalDate startdateformatted = org.joda.time.LocalDate.parse(startDateCustom, formatter);
			LocalDate enddateformatted = org.joda.time.LocalDate.parse(endDateCustom, formatter);

			dateToCheck = new org.joda.time.LocalDateTime(startdateformatted.toString());
			endDate = new org.joda.time.LocalDateTime(enddateformatted.toString());

		}

		Long totalIdleTime = 0L;
		Long totalUsageTime = 0L;
		String tenant = TenantContextHolder.getTenant();
		for (LocalDateTime date = dateToCheck; date.isBefore(endDate); date = date.plusDays(1)) {
			Date endTime = new Date();
			if (date.plusDays(1).isBefore(endDate)) {
				endTime = date.plusDays(1).toDate();
			}
			Long usageTime = 0L;
			if (range.equalsIgnoreCase(AppUsageStatRange.OneHour.name())) {
				usageTime = tenantUsageStatService.getUserLoggedInHourForHourInterval(tenant, userEmail, date.toDate(),
						endTime);
			} else {
				usageTime = tenantUsageStatService.getUserLoggedInHourForDate(tenant, userEmail, date.toDate(),
						endTime);
			}

			System.out.println("usage sec bet " + endTime + " - " + date + " -> " + usageTime);
			if (null != usageTime) {
				totalUsageTime = totalUsageTime + usageTime;
			}
		}

		Long idleTime = tenantUsageStatService.getIdleDurationForAUserForGivenDate(tenant, userEmail,
				GlobalConstants.USER_IDLE_TIMEOUT, dateToCheck.toDate(), new LocalDateTime().toDate());
		if (null != idleTime) {
			totalIdleTime = idleTime;
		}

		Float ttlIdleTime = (float) (totalIdleTime.floatValue() / 3600);
		Float activetime = (float) ((totalUsageTime.floatValue() - totalIdleTime.floatValue()) / 3600);
		Float usageTime = (float) (totalUsageTime.floatValue() / 3600);

		if (activetime < 0) {
			activetime = 0F;
			ttlIdleTime = 0F;
		}

		Map<String, String> loggedInInfo = new HashMap<String, String>();
		loggedInInfo.put("Total Logged time", String.format("%.02f", usageTime) + " hour(s)");
		loggedInInfo.put("Total Idle Time", String.format("%.02f", ttlIdleTime) + " hour(s)");
		loggedInInfo.put("Total Active Time", String.format("%.02f", activetime) + " hour(s)");

		RestResponse response = new RestResponse(RestResponse.SUCCESS, loggedInInfo, null);
		return response;
	}

	// to get active user count for within given interval i.e 1 Day, 1 week, 1
	// Month etc...
	@RequestMapping(value = "/api/v1/user/active/count/usage/report", method = RequestMethod.GET)
	public RestResponse getUserUsageReportForAInterval(@RequestParam(value = "range") String range)
			throws RecruizException {

		Date dateToCheck = null;

		if (range.equalsIgnoreCase(AppUsageStatRange.OneHour.name())) {
			dateToCheck = new Date();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneDay.name())) {
			dateToCheck = new DateTime().minusDays(1).toDate();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneWeek.name())) {
			dateToCheck = new DateTime().minusDays(7).toDate();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneMonth.name())) {
			dateToCheck = new DateTime().minusDays(30).toDate();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last3Months.name())) {
			dateToCheck = new DateTime().minusDays(90).toDate();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last6Months.name())) {
			dateToCheck = new DateTime().minusDays(180).toDate();
		}

		Map<String, Object> loggedInInfo = new HashMap<String, Object>();
		loggedInInfo.put("Total Active User Count",
				tenantUsageStatService.getActiveUserCount(TenantContextHolder.getTenant(), dateToCheck));

		RestResponse response = new RestResponse(RestResponse.SUCCESS, loggedInInfo, null);
		return response;
	}

	// to get average usgae of recruiz for tenant
	@RequestMapping(value = "/api/v1/user/all/usage/report", method = RequestMethod.GET)
	public RestResponse getAllUserUsagereport(@RequestParam(value = "range") String range,
			@RequestParam(value = "startDate", required = false) String startDateCustom,
			@RequestParam(value = "endDate", required = false) String endDateCustom) throws RecruizException {

		org.joda.time.LocalDateTime dateToCheck = null;

		int interval = 0;
		LocalDateTime endDate = new DateTime().plusDays(1).toLocalDateTime();
		if (range.equalsIgnoreCase(AppUsageStatRange.OneHour.name())) {
			dateToCheck = new org.joda.time.LocalDateTime().minusHours(1);
			endDate = new org.joda.time.LocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last12Hour.name())) {
			dateToCheck = new org.joda.time.LocalDateTime().minusHours(12);
			endDate = new org.joda.time.LocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneDay.name())) {
			dateToCheck = new DateTime().minusHours(24).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneWeek.name())) {
			dateToCheck = new DateTime().minusDays(7).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.OneMonth.name())) {
			dateToCheck = new DateTime().minusDays(30).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last3Months.name())) {
			dateToCheck = new DateTime().minusDays(90).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Last6Months.name())) {
			dateToCheck = new DateTime().minusDays(180).toLocalDateTime();
		} else if (range.equalsIgnoreCase(AppUsageStatRange.Custom.name())) {

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			LocalDate startdateformatted = org.joda.time.LocalDate.parse(startDateCustom, formatter);
			LocalDate enddateformatted = org.joda.time.LocalDate.parse(endDateCustom, formatter);

			dateToCheck = new org.joda.time.LocalDateTime(startdateformatted.toString());
			endDate = new org.joda.time.LocalDateTime(enddateformatted.toString());

		}

		Long totalIdleTime = 0L;
		Long totalUsageTime = 0L;
		String tenant = TenantContextHolder.getTenant();
		for (LocalDateTime date = dateToCheck; date.isBefore(endDate); date = date.plusDays(1)) {
			Date endTime = new Date();
			if (date.plusDays(1).isBefore(endDate)) {
				endTime = date.plusDays(1).toDate();
			}
			Long usageTime = 0L;
			if (range.equalsIgnoreCase(AppUsageStatRange.OneHour.name())) {
				usageTime = tenantUsageStatService.getTotalLoggedinTimeForTenantForHour(tenant, date.toDate(), endTime);
			} else {
				usageTime = tenantUsageStatService.getTotalLoggedinTiimeFrTenantForDate(tenant, date.toDate(), endTime);
			}
			if (null != usageTime) {
				totalUsageTime = totalUsageTime + usageTime;
			}
		}

		Long idleTime = tenantUsageStatService.getIdleTimeForAllUser(tenant,
				AppUsageStatRange.valueOf(range).getIntervalMinutes() + "");
		if (null == idleTime) {
			idleTime = 0L;
		}

		Long totalActiveUser = tenantUsageStatService.getActiveUserCount(tenant, dateToCheck.toDate());

		Float avgIdleTime = (idleTime.floatValue() / totalActiveUser) / 3600;
		Float avgUsgaeTime = (totalUsageTime.floatValue() / totalActiveUser) / 3600;
		Float avgActiveTime = ((totalUsageTime.floatValue() - idleTime.floatValue()) / totalActiveUser) / 3600;

		if (avgActiveTime < 0) {
			avgActiveTime = 0F;
			avgIdleTime = 0F;
		}

		Map<String, Object> loggedInInfo = new HashMap<String, Object>();
		loggedInInfo.put("Total Active User Count", totalActiveUser + " User(s)");
		loggedInInfo.put("Total Average Usage", String.format("%.02f", avgUsgaeTime) + "hour(s)");
		loggedInInfo.put("Total Average Active", String.format("%.02f", avgActiveTime) + "hour(s)");
		loggedInInfo.put("Total Average Idle", String.format("%.02f", avgIdleTime) + "hour(s)");

		RestResponse response = new RestResponse(RestResponse.SUCCESS, loggedInInfo, null);
		return response;
	}

}
