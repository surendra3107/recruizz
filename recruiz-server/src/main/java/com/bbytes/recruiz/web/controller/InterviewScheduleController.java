package com.bbytes.recruiz.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.ScheduleInterviewResponse;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackQuestionSetDTO;
import com.bbytes.recruiz.service.CalendarService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.FeedbackService;
import com.bbytes.recruiz.service.InterviewPanelService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class InterviewScheduleController {
	private static final Logger logger = LoggerFactory.getLogger(InterviewScheduleController.class);

	@Autowired
	private CalendarService calendarService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private InterviewScheduleService scheduleService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private UserService userService;

	/**
	 * Method is used to send invite for scheduling interview.
	 * 
	 * @param scheduleDTO
	 * @return
	 * @throws RecruizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/interview/schedule", method = RequestMethod.POST)
	public RestResponse scheduleInterview(@RequestBody List<InterviewScheduleDTO> scheduleDTOs, @RequestParam(value ="selectedFiles",required=false) String selectedFiles)
			throws RecruizException, Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ScheduleInterview.name());

		String positionCode = scheduleDTOs.get(0).getPositionCode();
		Position position = positionService.getPositionByCode(positionCode);
		if (position == null)
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		positionService.calculateFinalStatusForPosition(position);

		if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.toString())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_STATUS_OPERATION,
					ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		Map<String, String> scheduleFaliureReason = new HashMap<>();

		for (InterviewScheduleDTO scheduleDTO : scheduleDTOs) {

			if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null)
				return new RestResponse(RestResponse.FAILED, ErrorHandler.SCHEDULE_TIME_NULL,
						ErrorHandler.INVALID_REQUEST);

			scheduleDTO.setStartTime(scheduleDTO.getStartTime());
			scheduleDTO.setEndTime(scheduleDTO.getEndTime());

			// checking if previous date is selected
			Date today = new Date(); 
			Date scheduleDate = scheduleDTO.getStartTime();
			long diff = scheduleDate.getTime() - today.getTime();

			if (diff <= 0) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.BACK_DATE_NOT_ALLOWED);
				continue;
			}
			if (scheduleDTO.getStartTime().after(scheduleDTO.getEndTime())) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.START_TIME_GREATER_THAN_END_TIME);
				continue;
			}
			try {
				// since multiple interview schedule for same candidate so
				// removing the check
				/*
				 * InterviewSchedule existingSchedule =
				 * scheduleService.getScheduleByPositionCodeRoundEmail(
				 * scheduleDTO.getPositionCode(), scheduleDTO.getRoundId(),
				 * scheduleDTO.getCandidateEmail());
				 * 
				 * if (existingSchedule != null && existingSchedule.isActive())
				 * { scheduleFaliureReason.put(scheduleDTO.getRoundId(),
				 * ErrorHandler.INTERVIEW_IS_ACTIVE); continue; }
				 */

				// saving new interviewer to client and adding it to position if
				// any
				// new entry is passed from UI while scheduling interview

				scheduleFaliureReason = scheduleInterview(positionCode, scheduleFaliureReason, scheduleDTO, selectedFiles);

				scheduleFaliureReason.put(scheduleDTO.getRoundId(), RestResponseConstant.INTERVIEW_SCHEDULED);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}
			} catch (Exception e) {
				logger.error("Schedule failed : " + scheduleDTO, e);
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), e.getMessage());
			}
		}

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, scheduleFaliureReason, null);
		return userReponse;
	}


	/**
	 * Method is used to send invite for scheduling interview for candidate.
	 * 
	 * @param scheduleDTO
	 * @return
	 * @throws RecruizException
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate/interview/schedule", method = RequestMethod.POST)
	public RestResponse candidateScheduleInterview(@RequestBody List<InterviewScheduleDTO> scheduleDTOs)
			throws RecruizException, Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ScheduleInterview.name());

		String positionCode = scheduleDTOs.get(0).getPositionCode();
		Position position = positionService.getPositionByCode(positionCode);
		if (position == null)
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		positionService.calculateFinalStatusForPosition(position);

		if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.toString())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_STATUS_OPERATION,
					ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		Map<String, String> scheduleFaliureReason = new HashMap<>();

		for (InterviewScheduleDTO scheduleDTO : scheduleDTOs) {

			if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null)
				return new RestResponse(RestResponse.FAILED, ErrorHandler.SCHEDULE_TIME_NULL,
						ErrorHandler.INVALID_REQUEST);

			scheduleDTO.setStartTime(scheduleDTO.getStartTime());
			scheduleDTO.setEndTime(scheduleDTO.getEndTime());

			// checking if previous date is selected
			Date today = new Date(); 
			Date scheduleDate = scheduleDTO.getStartTime();
			long diff = scheduleDate.getTime() - today.getTime();

			if (diff <= 0) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.BACK_DATE_NOT_ALLOWED);
				continue;
			}
			if (scheduleDTO.getStartTime().after(scheduleDTO.getEndTime())) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.START_TIME_GREATER_THAN_END_TIME);
				continue;
			}
			try {
				// since multiple interview schedule for same candidate so
				// removing the check
				/*
				 * InterviewSchedule existingSchedule =
				 * scheduleService.getScheduleByPositionCodeRoundEmail(
				 * scheduleDTO.getPositionCode(), scheduleDTO.getRoundId(),
				 * scheduleDTO.getCandidateEmail());
				 * 
				 * if (existingSchedule != null && existingSchedule.isActive())
				 * { scheduleFaliureReason.put(scheduleDTO.getRoundId(),
				 * ErrorHandler.INTERVIEW_IS_ACTIVE); continue; }
				 */

				// saving new interviewer to client and adding it to position if
				// any
				// new entry is passed from UI while scheduling interview

				scheduleFaliureReason = scheduleInterview(positionCode, scheduleFaliureReason, scheduleDTO, null);

				scheduleFaliureReason.put(scheduleDTO.getRoundId(), RestResponseConstant.INTERVIEW_SCHEDULED);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}
			} catch (Exception e) {
				logger.error("Schedule failed : " + scheduleDTO, e);
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), e.getMessage());
			}
		}

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, scheduleFaliureReason, null);
		return userReponse;
	}


	/**
	 * @param positionCode
	 * @param scheduleFaliureReason
	 * @param scheduleDTO
	 * @param selectedFiles 
	 * @throws Exception
	 */
	private Map<String, String> scheduleInterview(String positionCode, Map<String, String> scheduleFaliureReason,
			InterviewScheduleDTO scheduleDTO, String selectedFiles) throws Exception {

		if (!scheduleDTO.getInterviewerList().isEmpty() && scheduleDTO.getInterviewerList() != null)
			clientService.saveNewInterviewer(scheduleDTO);

		String feedbackSharedId = null;
		Set<Feedback> expectedFeedbacks = new HashSet<>();

		if (scheduleDTO.getExpectFeedback()) {
			// calling feedback share levelbar API for custom feedback form
			feedbackSharedId = scheduleService.createFeedbackShare(scheduleDTO, scheduleDTO.getFeedbackQueSetId(),
					GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW);

			// setting expected feedback for this schedule in feedback entry
			// table

			boolean profileMaksed = false;

			if (scheduleDTO.getResumeMasked() != null && scheduleDTO.getResumeMasked().equalsIgnoreCase("y")) {
				profileMaksed = true;
			}

			expectedFeedbacks = scheduleService.setInterviewerForExpectedFeedback(scheduleDTO.getInterviewerEmails(),
					scheduleDTO.getRoundId(), scheduleDTO.getCandidateEmail(), scheduleDTO.getInterviewerList(),
					GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW, positionCode, feedbackSharedId, profileMaksed);
		}

		if (scheduleDTO.getExpectFeedback() && (expectedFeedbacks == null || expectedFeedbacks.isEmpty())) {
			scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.FEEDBACK_CREATE_ERROR);
		}

		calendarService.sendCalenderInvite(scheduleDTO, expectedFeedbacks, feedbackSharedId, selectedFiles);


		return scheduleFaliureReason;
	}

	private void sendCalendarInvitation(InterviewScheduleDTO scheduleDTO, List<String> attendeeList) throws Exception {
		String roundId = scheduleDTO.getRoundId();
		Round round = roundService.findOne(Long.parseLong(roundId));
		Position position = positionService.getPositionByCode(scheduleDTO.getPositionCode());
		Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());

		EventAttendee interviewer = null;
		Set<EventAttendee> interviewers = new HashSet<EventAttendee>();
		for (String email : attendeeList) {
			ClientInterviewerPanel interviewPanel = interviewPanelService.getInterviewerByEmailAndClient(email,
					position.getClient());
			interviewer = new EventAttendee();
			interviewer.setEmail(email);
			if (interviewPanel != null)
				interviewer.setName(interviewPanel.getName());
			else
				interviewer.setName(email);
			interviewers.add(interviewer);
		}
		// EventAttendee hrExecutive = new EventAttendee();
		// hrExecutive.setEmail(userService.getLoggedInUserEmail());
		// hrExecutive.setName(userService.getLoggedInUserObject().getName());
		// interviewers.add(hrExecutive);

	}

	@RequestMapping(value = "/api/v1/interview/schedule/{eventId}", method = RequestMethod.DELETE)
	public RestResponse cancelInterviewSchdule(@PathVariable("eventId") String eventId)
			throws NumberFormatException, Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CancelInterview.name());*/
		InterviewSchedule schedule = null;
		if (eventId != null && !eventId.isEmpty()) {
			schedule = scheduleService.findOne(Long.parseLong(eventId));
			Candidate candidate = candidateService.getCandidateByEmail(schedule.getCandidateEmail());
			RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate,
					schedule.getPositionCode());
			List<Feedback> feedbacks = feedbackService.getFeedbackByRoundIdAndCandidateIdAndType(roundCandidate,
					schedule.getRoundId(), "interview");
			if (feedbacks != null && !feedbacks.isEmpty()) {
				for (Feedback feedback : feedbacks) {
					if (feedback.isActive() && "N/A".equalsIgnoreCase(feedback.getStatus())) {
						feedback.setActive(false);
						feedback.setStatus(GlobalConstants.CANCELLED);
						feedbackService.save(feedback);
					}
				}
			}
			// deleting custom feedback form on interview cancel
			scheduleService.deleteFeedbackShare(schedule.getLevelbarFeedbackShareId());

			calendarService.cancelInterviewerInvite(Long.parseLong(eventId));
		}
		if(schedule!=null){
			Position position = positionService.getPositionByCode(schedule.getPositionCode());
			if(position!=null){
				position.setModificationDate(new Date());
				positionService.save(position);
			}
		}
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.INTERVIEW_CANCELLED, null);
	}

	@RequestMapping(value = "/api/v1/interview/schedule", method = RequestMethod.PUT)
	public RestResponse rescheduleInterview(@RequestBody List<InterviewScheduleDTO> scheduleDTOs)
			throws NumberFormatException, Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.RescheduleInterview.name());

		Position position = positionService.getPositionByCode(scheduleDTOs.get(0).getPositionCode());

		if (position == null)
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		positionService.calculateFinalStatusForPosition(position);

		if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.toString())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_STATUS_OPERATION,
					ErrorHandler.POSITION_ONHOLD_CLOSED);
		}
		Map<String, String> scheduleFaliureReason = new HashMap<>();

		for (InterviewScheduleDTO scheduleDTO : scheduleDTOs) {

			if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_TIME_NULL);
				continue;
			}

			scheduleDTO.setStartTime(scheduleDTO.getStartTime());
			scheduleDTO.setEndTime(scheduleDTO.getEndTime());

			// checking if previous date is selected
			Date today = new Date();
			Date scheduleDate = scheduleDTO.getStartTime();
			long diff = scheduleDate.getTime() - today.getTime();

			if (diff <= 0) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.BACK_DATE_NOT_ALLOWED);
				continue;
			}
			if (scheduleDTO.getStartTime().after(scheduleDTO.getEndTime())) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.START_TIME_GREATER_THAN_END_TIME);
				continue;
			}

			if (scheduleDTO.getId() == null || scheduleDTO.getId().isEmpty()) {
				scheduleFaliureReason = scheduleInterview(scheduleDTO.getPositionCode(), scheduleFaliureReason,
						scheduleDTO, null);
				continue;
			}

			InterviewSchedule schedule = scheduleService.findOne(Long.parseLong(scheduleDTO.getId()));

			if (schedule == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_NOT_FOUND);
				continue;
			}

			Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());

			if (candidate == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.CANDIDATE_NOT_EXISTS);
				continue;
			}

			if (schedule.getPositionCode() == null || schedule.getPositionCode().isEmpty()) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_DATA_CORRUPTED);
				continue;
			}

			try {

				// checking the existing interview whether any changes or not
				if (scheduleDTO.getId() != null && !scheduleDTO.getId().isEmpty()) {

					Set<String> interviewerEmails = new HashSet<String>();
					for (EventAttendee eventAttendee : schedule.getAttendee()) {
						interviewerEmails.add(eventAttendee.getEmail());
					}

					if (schedule.getRoundId().equals(scheduleDTO.getRoundId())
							&& schedule.getStartsAt().compareTo(scheduleDTO.getStartTime()) == 0
							&& schedule.getEndsAt().compareTo(scheduleDTO.getEndTime()) == 0
							&& interviewerEmails.equals(scheduleDTO.getInterviewerEmails())
							&& schedule.getInterviewerTemplateSubject()
							.equals(scheduleDTO.getInterviewerTemplateSubject())
							&& schedule.getInterviewerTemplateData().equals(scheduleDTO.getInterviewerEmailTemplate())
							&& schedule.getTemplateSubject().equals(scheduleDTO.getTemplateSubject())
							&& schedule.getNotes().equals(scheduleDTO.getNotes())
							&& scheduleDTO.getInterviewerList().size() <= 0
							&& (scheduleDTO.getFeedbackQueSetId() != null
							? schedule.getLevelbarFeedbackQueSetId().equals(scheduleDTO.getFeedbackQueSetId())
									: true)) {
						scheduleFaliureReason.put(scheduleDTO.getRoundId(),
								RestResponseConstant.NO_CHANGES_IN_INTERVIEW);
						continue;
					}

				}

				// closing the existing feedback if any before rescheduling the
				// event
				RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate,
						schedule.getPositionCode());
				List<Feedback> feedbacks = feedbackService.getFeedbackByRoundIdAndCandidateIdAndType(roundCandidate,
						schedule.getRoundId(), "interview");
				if (feedbacks != null && !feedbacks.isEmpty()) {
					for (Feedback feedback : feedbacks) {
						if (feedback.isActive() && "N/A".equalsIgnoreCase(feedback.getStatus())) {
							feedback.setActive(false);
							feedback.setStatus(GlobalConstants.CANCELLED);
							feedbackService.save(feedback);
						}
					}
				}

				clientService.saveNewInterviewer(scheduleDTO);

				// calling feedback share levelbar API for custom feedback form
				String shareId = scheduleService.updateFeedbackShare(scheduleDTO, scheduleDTO.getFeedbackQueSetId(),
						schedule.getLevelbarFeedbackShareId(), GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW);

				boolean profileMasked = false;
				if (scheduleDTO.getResumeMasked() != null && scheduleDTO.getResumeMasked().equalsIgnoreCase("y")) {
					profileMasked = true;
				}

				// setting new expected feedback after rescheduling
				Set<Feedback> expectedFeedbacks = scheduleService.setInterviewerForExpectedFeedback(
						scheduleDTO.getInterviewerEmails(), scheduleDTO.getRoundId(), schedule.getCandidateEmail(),
						scheduleDTO.getInterviewerList(), GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW,
						position.getPositionCode(), shareId, profileMasked);

				if (expectedFeedbacks == null || expectedFeedbacks.isEmpty()) {
					scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.FEEDBACK_CREATE_ERROR);
					continue;
				}

				calendarService.rescheduleInterviewInvite(scheduleDTO, Long.parseLong(scheduleDTO.getId()), candidate,
						expectedFeedbacks);

				scheduleFaliureReason.put(scheduleDTO.getRoundId(), RestResponseConstant.INTERVIEW_SCHEDULE_CHANGED);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}

			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
				if (ex instanceof RecruizException) {
					RecruizException reczException = (RecruizException) ex;
					scheduleFaliureReason.put(scheduleDTO.getRoundId(), reczException.getMessage());
				} else {
					scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.RESCHEDULE_FAILED);
				}
			}
		}

		return new RestResponse(RestResponse.SUCCESS, scheduleFaliureReason);
	}



	@RequestMapping(value = "/api/v1/interview/candidateSchedule", method = RequestMethod.PUT)
	public RestResponse rescheduleInterviewForCandidate(@RequestBody List<InterviewScheduleDTO> scheduleDTOs)
			throws NumberFormatException, Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.RescheduleInterview.name());

		Position position = positionService.getPositionByCode(scheduleDTOs.get(0).getPositionCode());

		if (position == null)
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		positionService.calculateFinalStatusForPosition(position);

		if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.toString())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_STATUS_OPERATION,
					ErrorHandler.POSITION_ONHOLD_CLOSED);
		}
		Map<String, String> scheduleFaliureReason = new HashMap<>();

		for (InterviewScheduleDTO scheduleDTO : scheduleDTOs) {

			if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_TIME_NULL);
				continue;
			}

			scheduleDTO.setStartTime(scheduleDTO.getStartTime());
			scheduleDTO.setEndTime(scheduleDTO.getEndTime());

			// checking if previous date is selected
			Date today = new Date();
			Date scheduleDate = scheduleDTO.getStartTime();
			long diff = scheduleDate.getTime() - today.getTime();

			if (diff <= 0) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.BACK_DATE_NOT_ALLOWED);
				continue;
			}
			if (scheduleDTO.getStartTime().after(scheduleDTO.getEndTime())) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.START_TIME_GREATER_THAN_END_TIME);
				continue;
			}

			if (scheduleDTO.getId() == null || scheduleDTO.getId().isEmpty()) {
				scheduleFaliureReason = scheduleInterview(scheduleDTO.getPositionCode(), scheduleFaliureReason,
						scheduleDTO, null);
				continue;
			}

			InterviewSchedule schedule = scheduleService.findOne(Long.parseLong(scheduleDTO.getId()));

			if (schedule == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_NOT_FOUND);
				continue;
			}

			Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());

			if (candidate == null) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.CANDIDATE_NOT_EXISTS);
				continue;
			}

			if (schedule.getPositionCode() == null || schedule.getPositionCode().isEmpty()) {
				scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.SCHEDULE_DATA_CORRUPTED);
				continue;
			}

			try {

				// checking the existing interview whether any changes or not
				if (scheduleDTO.getId() != null && !scheduleDTO.getId().isEmpty()) {

					Set<String> interviewerEmails = new HashSet<String>();
					for (EventAttendee eventAttendee : schedule.getAttendee()) {
						interviewerEmails.add(eventAttendee.getEmail());
					}

					if (schedule.getRoundId().equals(scheduleDTO.getRoundId())
							&& schedule.getStartsAt().compareTo(scheduleDTO.getStartTime()) == 0
							&& schedule.getEndsAt().compareTo(scheduleDTO.getEndTime()) == 0
							&& schedule.getTemplateSubject().equals(scheduleDTO.getTemplateSubject())
							&& schedule.getNotes().equals(scheduleDTO.getNotes())
							&& (scheduleDTO.getFeedbackQueSetId() != null
							? schedule.getLevelbarFeedbackQueSetId().equals(scheduleDTO.getFeedbackQueSetId())
									: true)) {
						scheduleFaliureReason.put(scheduleDTO.getRoundId(),
								RestResponseConstant.NO_CHANGES_IN_INTERVIEW);
						continue;
					}

				}

				// closing the existing feedback if any before rescheduling the
				// event
				RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate,
						schedule.getPositionCode());
				List<Feedback> feedbacks = feedbackService.getFeedbackByRoundIdAndCandidateIdAndType(roundCandidate,
						schedule.getRoundId(), "interview");
				if (feedbacks != null && !feedbacks.isEmpty()) {
					for (Feedback feedback : feedbacks) {
						if (feedback.isActive() && "N/A".equalsIgnoreCase(feedback.getStatus())) {
							feedback.setActive(false);
							feedback.setStatus(GlobalConstants.CANCELLED);
							feedbackService.save(feedback);
						}
					}
				}

				clientService.saveNewInterviewer(scheduleDTO);

				// calling feedback share levelbar API for custom feedback form
				String shareId = scheduleService.updateFeedbackShare(scheduleDTO, scheduleDTO.getFeedbackQueSetId(),
						schedule.getLevelbarFeedbackShareId(), GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW);

				boolean profileMasked = false;
				if (scheduleDTO.getResumeMasked() != null && scheduleDTO.getResumeMasked().equalsIgnoreCase("y")) {
					profileMasked = true;
				}

				// setting new expected feedback after rescheduling
				Set<Feedback> expectedFeedbacks = scheduleService.setInterviewerForExpectedFeedback(
						scheduleDTO.getInterviewerEmails(), scheduleDTO.getRoundId(), schedule.getCandidateEmail(),
						scheduleDTO.getInterviewerList(), GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW,
						position.getPositionCode(), shareId, profileMasked);

				calendarService.rescheduleInterviewInvite(scheduleDTO, Long.parseLong(scheduleDTO.getId()), candidate,
						expectedFeedbacks);

				scheduleFaliureReason.put(scheduleDTO.getRoundId(), RestResponseConstant.INTERVIEW_SCHEDULE_CHANGED);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}

			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
				if (ex instanceof RecruizException) {
					RecruizException reczException = (RecruizException) ex;
					scheduleFaliureReason.put(scheduleDTO.getRoundId(), reczException.getMessage());
				} else {
					scheduleFaliureReason.put(scheduleDTO.getRoundId(), ErrorHandler.RESCHEDULE_FAILED);
				}
			}
		}

		return new RestResponse(RestResponse.SUCCESS, scheduleFaliureReason);
	}




	@RequestMapping(value = "/api/v1/interview/schedule", method = RequestMethod.GET)
	public RestResponse getInterviewSchedule(@RequestParam("positionCode") String positionCode,
			@RequestParam("roundId") String roundId, @RequestParam("candidateEmail") String candidateEmail)
					throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetInterviewSchedule.name());*/

		InterviewSchedule schedule = scheduleService.getScheduleByPositionCodeRoundEmail(positionCode, roundId,
				candidateEmail);

		if (schedule != null) {
			return new RestResponse(RestResponse.SUCCESS, schedule, null);
		}
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NO_INTERVIEW_SCHEDULED, null);
	}

	/**
	 * to get list of all active interview for a candidate
	 * 
	 * @param positionCode
	 * @param roundId
	 * @param candidateEmail
	 * @return
	 * @throws Exception
	 */

	@RequestMapping(value = "/api/v1/interview/schedule/all", method = RequestMethod.GET)
	public RestResponse getInterviewSchedule(@RequestParam("positionCode") String positionCode,
			@RequestParam("candidateEmail") String candidateEmail) throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetInterviewSchedule.name());*/

		LinkedList<InterviewSchedule> schedules = scheduleService
				.getAllScheduleByPositionCodeAndCandidateEmail(positionCode, candidateEmail);

		ScheduleInterviewResponse res = new ScheduleInterviewResponse();

		res.setSchedules(schedules);
		if(schedules.size()==1){
			for (InterviewSchedule interviewSchedule : schedules) {
				if(interviewSchedule.getInterviewerTemplateName()==null){
					res.setInterviewerData(false);
				}
			}
		}

		if (schedules != null) {
			return new RestResponse(RestResponse.SUCCESS, res, null);
		}
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NO_INTERVIEW_SCHEDULED, null);
	}

	@RequestMapping(value = "/api/v1/feedback/questionSet", method = RequestMethod.GET)
	public RestResponse getAllFeedbackQueSets() throws Exception {
		List<FeedbackQuestionSetDTO> feedbackQuestionSetDTOList = scheduleService.getFeedbackQueSets();
		return new RestResponse(RestResponse.SUCCESS, feedbackQuestionSetDTOList);
	}
	
	
	
	@RequestMapping(value = "/api/v1/interview/schedule/getAllInterviewerAndDecisionMaker", method = RequestMethod.GET)
	public RestResponse getAllInterviewerAndDecisionMaker(@RequestParam("positionCode") String positionCode, @RequestParam("searchText") String searchText) throws Exception {

	if(positionCode==null || positionCode.equalsIgnoreCase("") || positionCode.isEmpty() || searchText==null || searchText.equalsIgnoreCase("") || searchText.isEmpty())
		return new RestResponse(RestResponse.FAILED, "Position code required !!", null);
		
		return scheduleService.getAllInterviewerAndDecisionMaker(positionCode, searchText);
	}
	
	
}
