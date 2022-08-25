package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.InterviewFile;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.levelbar.LevelbarService;
import com.bbytes.recruiz.repository.InterviewScheduleRepository;
import com.bbytes.recruiz.rest.dto.models.DashboardDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewPanelDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackQuestionSetDTO;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareDTO;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareResultDTO;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class InterviewScheduleService extends AbstractService<InterviewSchedule, Long> {

	private static final Logger logger = LoggerFactory.getLogger(InterviewScheduleService.class);

	private InterviewScheduleRepository interviewScheduleRepository;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private DecisionMakerService decisionMakerService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private FeedbackService feeedbackSerice;

	@Autowired
	private UserService userService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private CalendarService calendarService;

	@Autowired
	private DateTimeService dateTimeService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	@Autowired
	private LevelbarService levelbarService;

	@Autowired
	private EventAttendeeService eventAttendeeService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${allowed.external.user.links.validity}")
	private String extendTokenForDays;

	@Value("${levelbar.server.url}")
	protected String levelbarBaseUrl;

	@Autowired
	public InterviewScheduleService(InterviewScheduleRepository interviewScheduleRepo) {
		super(interviewScheduleRepo);
		this.interviewScheduleRepository = interviewScheduleRepo;
	}

	/**
	 * <code>save</code> used to save interview schedule object.
	 *
	 * @param position
	 * @param round
	 * @param scheduleDTO
	 * @param interviewer
	 * @param candidate
	 * @param hrExecutive
	 * @param candidateEventId
	 * @param interviewerEventId
	 * @param interviewFile
	 */
	@Transactional
	public void save(Position position, Round round, InterviewScheduleDTO scheduleDTO, Set<EventAttendee> interviewer,
			Candidate candidate, String candidateEventId, String interviewerEventId, List<InterviewFile> interviewFile,
			String feedbackSharedId) {
		InterviewSchedule schedule = new InterviewSchedule();

		schedule.setPositionCode(position.getPositionCode());
		schedule.setPositionName(position.getTitle());
		schedule.setRoundId(scheduleDTO.getRoundId());
		schedule.setRoundName(round.getRoundName());
		schedule.setRoundType(round.getRoundType());
		schedule.setAttendee(interviewer);
		schedule.setCandidateAccepted("Not Yet Responded");
		schedule.setCandidateEmail(candidate.getEmail());
		schedule.setClientName(position.getClient().getClientName());
		schedule.setCandidateName(candidate.getFullName());
		schedule.setCandidateEventId(candidateEventId);
		schedule.setStartsAt(scheduleDTO.getStartTime());
		schedule.setEndsAt(scheduleDTO.getEndTime());
		schedule.setInterviewerEventId(interviewerEventId);
		schedule.setFile(interviewFile);
		schedule.setInterviewSchedulerEmail(userService.getLoggedInUserEmail());
		schedule.setInterviewSchedulerName(userService.getLoggedInUserObject().getName());
		schedule.setScheduleHrExecutives(userService.loggedInUserSet());
		schedule.setNotes(scheduleDTO.getNotes());
		schedule.setTemplateSubject(scheduleDTO.getTemplateSubject());
		schedule.setTemplateName(scheduleDTO.getTemplateName());
		schedule.setInterviewerTemplateData(scheduleDTO.getInterviewerEmailTemplate());
		schedule.setInterviewerTemplateSubject(scheduleDTO.getInterviewerTemplateSubject());
		schedule.setInterviewerTemplateName(scheduleDTO.getInterviewerTemplateName());
		schedule.setLevelbarFeedbackShareId(feedbackSharedId);
		schedule.setLevelbarFeedbackQueSetId(scheduleDTO.getFeedbackQueSetId());
		schedule.setFeedbackExpected(scheduleDTO.getExpectFeedback());
		for (InterviewFile file : interviewFile) {
			file.setSchedule(schedule);
		}

		save(schedule);
	}

	@Transactional
	public void setInterviewerForFeedback(List<String> interviewerEmail, String roundId, String candidateEmail,
			List<InterviewPanelDTO> otherInterviewers, String type, String positionCode) throws RecruizException {
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		String roundName = roundService.getRoundName(roundId);
		RoundCandidate roundCandidate = null;
		if (candidate != null) {
			roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, positionCode);
		}
		if (interviewerEmail.contains("recruiz.app@gmail.com"))
			interviewerEmail.remove("recruiz.app@gmail.com");
		if (roundCandidate != null) {
			Set<Feedback> requiredFeedbacks = new HashSet<Feedback>();
			Feedback feedback = null;
			Position position = positionService.getPositionByCode(roundCandidate.getPositionCode());
			for (String email : interviewerEmail) {
				feedback = new Feedback();
				ClientInterviewerPanel interviewer = interviewPanelService.getInterviewerByEmailAndClient(email,
						position.getClient());
				ClientDecisionMaker decisionMaker = decisionMakerService.getDecisionMakerByEmailAndClient(email,
						position.getClient());
				if (interviewer != null) {
					feedback.setFeedbackByMobile(interviewer.getMobile());
					feedback.setFeedbackByName(interviewer.getName());
				} else if (decisionMaker != null) {
					feedback.setFeedbackByMobile(decisionMaker.getMobile());
					feedback.setFeedbackByName(decisionMaker.getName());
				}
				feedback.setFeedbackBy(email);
				feedback.setRoundId(roundId);
				feedback.setRoundCandidateId(roundCandidate.getId() + "");
				feedback.setRoundCandidate(roundCandidate);
				feedback.setType(type);
				feedback.setPositionName(position.getTitle());
				feedback.setClientName(position.getClient().getClientName());
				feedback.setRoundName(roundName);
				feedback.setCandidateId(roundCandidate.getCandidate().getCid() + "");
				if (feeedbackSerice.isFeedbackActive(roundCandidate.getId() + "", roundId, email, true, type)) {
					// updating the entity jsut to update the time stamp
				} else {
					requiredFeedbacks.add(feedback);
				}
			}
			if (otherInterviewers != null && !otherInterviewers.isEmpty()) {
				for (InterviewPanelDTO otherInterviewer : otherInterviewers) {
					feedback = new Feedback();
					feedback.setFeedbackBy(otherInterviewer.getEmail());
					feedback.setFeedbackByMobile(otherInterviewer.getMobile());
					feedback.setFeedbackByName(otherInterviewer.getName());
					feedback.setRoundId(roundId);
					feedback.setRoundCandidateId(roundCandidate.getId() + "");
					feedback.setRoundCandidate(roundCandidate);
					feedback.setType(type);
					feedback.setPositionName(position.getTitle());
					feedback.setClientName(position.getClient().getClientName());
					feedback.setRoundName(roundName);
					feedback.setCandidateId(roundCandidate.getCandidate().getCid() + "");
					if (feeedbackSerice.isFeedbackActive(roundCandidate.getId() + "", roundId,
							otherInterviewer.getEmail(), true, type)) {
						// updating the entity jsut to update the time stamp
						feeedbackSerice.save(feedback);
					} else {
						requiredFeedbacks.add(feedback);
					}
				}
			}
			roundCandidate.getFeedback().clear();
			roundCandidate.setFeedback(requiredFeedbacks);
			roundCandidateService.save(roundCandidate);
		}
	}

	@Transactional
	public void updateInterviewSchedule(InterviewSchedule interviewSchedule) {
		save(interviewSchedule);
	}

	/**
	 * <code>getScheduleDetailsByEventId</code> used to get interview schedule
	 * object.
	 *
	 * @param interviewerId
	 * @param candidateEventId
	 * @return
	 */
	@Transactional
	public InterviewSchedule getScheduleDetailsByEventId(String interviewerId, String candidateEventId) {
		return interviewScheduleRepository.findOneByInterviewerEventIdOrCandidateEventId(interviewerId,
				candidateEventId);
	}

	@Transactional
	public boolean isInterviewScheduled(String candidateEmail) {
		boolean state = interviewScheduleRepository.countByCandidateEmail(candidateEmail) == 0 ? false : true;
		return state;
	}

	/**
	 * <code>getCalenderForPosition</code> returns interview schedule object by
	 * position.
	 *
	 * @param positionCode
	 * @return
	 */
	@Transactional
	public List<InterviewSchedule> getCalenderForPosition(String positionCode) {
		return interviewScheduleRepository.findByPositionCode(positionCode);
	}

	/**
	 * <code>getCalenderForPositionByMonth</code> returns interview schedule
	 * object for particular month by position.
	 *
	 * @param positionCode
	 * @param month
	 * @return
	 */
	@Transactional
	public List<InterviewSchedule> getCalenderForPositionByMonth(String positionCode, String month) {

		List<InterviewSchedule> schedules = interviewScheduleRepository.findByMonthAndPosition(month, positionCode);
		if (schedules != null && !schedules.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedules) {
				Set<EventAttendee> attendee = interviewSchedule.getAttendee();
				Set<EventAttendee> validAttendee = new HashSet<EventAttendee>();
				if (attendee != null && !attendee.isEmpty()) {
					for (EventAttendee eventAttendee : attendee) {
						if (interviewPanelService.interviewerExists(eventAttendee.getEmail())) {
							validAttendee.add(eventAttendee);
						}
					}
				}
				interviewSchedule.setAttendee(validAttendee);
			}
		}
		return schedules;
	}

	/**
	 * <code>getCalenderForMonth</code> returns interview schedule object for
	 * particular month.
	 *
	 * @param month
	 * @return
	 */
	@Transactional
	public List<InterviewSchedule> getCalenderForMonthByOwner(String month) {
		List<InterviewSchedule> schedules = interviewScheduleRepository.findByMonthAndOwner(month,
				userService.getLoggedInUserEmail());
		if (schedules != null && !schedules.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedules) {
				Set<EventAttendee> attendee = interviewSchedule.getAttendee();
				Set<EventAttendee> validAttendee = new HashSet<EventAttendee>();
				if (attendee != null && !attendee.isEmpty()) {
					for (EventAttendee eventAttendee : attendee) {
						if (interviewPanelService.interviewerExists(eventAttendee.getEmail())) {
							validAttendee.add(eventAttendee);
						}
					}
				}
				interviewSchedule.setAttendee(validAttendee);
			}
		}
		return schedules;
	}

	@Transactional
	public void deleteScheduleFromDatabase(InterviewSchedule schedule) {
		interviewScheduleRepository.delete(schedule);
	}

	@Transactional
	public InterviewSchedule getScheduleByPositionCodeRoundEmail(String positionCode, String roundId,
			String candidateEmail) {

		Date today = new Date();

		InterviewSchedule interviewSchedule = null;
		List<InterviewSchedule> interviewScheduleList = interviewScheduleRepository
				.findByPositionCodeAndRoundIdAndCandidateEmailAndActiveAndEndsAtAfter(positionCode, roundId,
						candidateEmail, true, new java.sql.Date(today.getTime()));

		if (interviewScheduleList != null) {
			for (InterviewSchedule interviewEvent : interviewScheduleList) {
				if (interviewEvent.getEndsAt().after(new Date()) && interviewEvent.isActive()) {
					interviewEvent.getAttendee().size();
					interviewSchedule = interviewEvent;

					Set<EventAttendee> attendee = interviewSchedule.getAttendee();
					Set<EventAttendee> validAttendee = new HashSet<EventAttendee>();
					if (attendee != null && !attendee.isEmpty()) {
						for (EventAttendee eventAttendee : attendee) {
							if (interviewPanelService.interviewerExists(eventAttendee.getEmail())) {
								validAttendee.add(eventAttendee);
							}
						}
					}
					interviewSchedule.setAttendee(validAttendee);
					break;
				}
			}
		}
		return interviewSchedule;
	}

	@Transactional
	public List<InterviewSchedule> getAllCalenderByOwner() {

		List<InterviewSchedule> schedules = new LinkedList<>();

		//Showing all interviews in calender if its a SuperAdmin
		if (checkUserPermissionService.isSuperAdmin()) { 		
		//	schedules = interviewScheduleRepository.findAll();
			List<InterviewSchedule> finalSheduleData = new ArrayList<>();
			List<User> users = new ArrayList<>();
			List<User> allUsers = userService.findAll();

			for (User user : allUsers) {
				if(user.getAccountStatus())
					users.add(user);
			}
			
			for (User user : users) {
				List<InterviewSchedule> scheduleData =  interviewScheduleRepository.findByInterviewSchedulerEmail(user.getEmail());
				finalSheduleData.addAll(scheduleData);
			}
			
			return finalSheduleData;
			
			
		} else {   		
			schedules = interviewScheduleRepository
					.findByInterviewSchedulerEmail(userService.getLoggedInUserEmail());
		}

		if (schedules != null && !schedules.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedules) {
				Set<EventAttendee> attendee = interviewSchedule.getAttendee();
				Set<EventAttendee> validAttendee = new HashSet<EventAttendee>();
				if (attendee != null && !attendee.isEmpty()) {
					for (EventAttendee eventAttendee : attendee) {
						if (interviewPanelService.interviewerExists(eventAttendee.getEmail())) {
							validAttendee.add(eventAttendee);
						}
					}
				}
				interviewSchedule.setAttendee(validAttendee);
			}
		}
		return schedules;
	}

	@Transactional(readOnly = true)
	public List<DashboardDTO> getInterviewSchedule() throws RecruizException {
		List<DashboardDTO> scheduleDetails = new LinkedList<>();
		DashboardDTO scheduleDetail = null;
		DateTime enddate = new DateTime().plusDays(7);
		java.sql.Date startDate = new java.sql.Date(new DateTime().toDate().getTime());
		java.sql.Date endDate = new java.sql.Date(enddate.toDate().getTime());

		List<InterviewSchedule> schedules = new LinkedList<>();

		if (checkUserPermissionService.isSuperAdmin()) {
			Set<String> positionCodes = positionService.getPositionCodeForLoggedInUser();
			getInterviewSchedules(startDate, endDate, schedules, positionCodes);
		} else if (checkUserPermissionService.hasOrgAdminPermission() || checkUserPermissionService
				.belongsToHrManagerGroup(userService.getLoggedInUserObject().getUserRole())) {
			Set<String> positionCodes = positionService.getPositionCodeForLoggedInUser();
			getInterviewSchedules(startDate, endDate, schedules, positionCodes);
		} else {
			schedules = interviewScheduleRepository.findTodaysScheduleByOwner(startDate, endDate,
					userService.getLoggedInUserEmail());
		}

		if (schedules != null && !schedules.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedules) {
				scheduleDetail = new DashboardDTO();
				scheduleDetail.setClientName(interviewSchedule.getClientName());
				scheduleDetail.setPositionName(interviewSchedule.getPositionName());
				scheduleDetail.setCandidateNames(interviewSchedule.getCandidateName());
				scheduleDetail.setScheduleDate(interviewSchedule.getStartsAt());
				scheduleDetails.add(scheduleDetail);

			}
		}
		Collections.sort(schedules);
		return scheduleDetails;
	}

	private void getInterviewSchedules(java.sql.Date startDate, java.sql.Date endDate,
			List<InterviewSchedule> schedules, Set<String> positionCodes) {
		if (positionCodes != null) {
			for (String positionCode : positionCodes) {
				List<InterviewSchedule> newList = interviewScheduleRepository
						.findTodaysScheduleByPositionCode(startDate, endDate, positionCode);
				if (newList != null) {
					schedules.addAll(newList);
				}
			}
		}
	}

	@Transactional
	public List<InterviewSchedule> getCalenderByOwner(String owner) {
		List<InterviewSchedule> schedules = interviewScheduleRepository.findByInterviewSchedulerEmail(owner);
		return schedules;
	}

	@Transactional(readOnly = true)
	public List<InterviewSchedule> getInterviewScheduleByHr(String hrID) {
		List<InterviewSchedule> schedules = interviewScheduleRepository.findByScheduleHrExecutivesIn(hrID);
		return schedules;
	}

	@Transactional
	public Set<Feedback> setInterviewerForExpectedFeedback(Set<String> interviewerEmail, String roundId,
			String candidateEmail, List<InterviewPanelDTO> otherInterviewers, String type, String positionCode,
			String feedbackShareId, boolean profileMasked) throws RecruizException {

		Set<Feedback> requiredFeedbacks = new HashSet<Feedback>();
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		String roundName = roundService.getRoundName(roundId);
		RoundCandidate roundCandidate = null;
		if (candidate != null) {
			roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, positionCode);
		}
		if (interviewerEmail.contains("recruiz.app@gmail.com"))
			interviewerEmail.remove("recruiz.app@gmail.com");

		if (otherInterviewers != null && !otherInterviewers.isEmpty()) {
			for (InterviewPanelDTO newInterviewer : otherInterviewers) {
				if (!interviewerEmail.contains(newInterviewer.getEmail())) {
					interviewerEmail.add(newInterviewer.getEmail());
				}
			}
		}

		if (roundCandidate != null) {
			Feedback feedback = null;
			Position position = positionService.getPositionByCode(roundCandidate.getPositionCode());
			for (String email : interviewerEmail) {
				feedback = new Feedback();
				if (type.equalsIgnoreCase("Forwarded Ignored")) {
					feedback.setFeedback("Forwarded with no Feedback");
				}
				ClientInterviewerPanel interviewer = interviewPanelService.getInterviewerByEmailAndClient(email,
						position.getClient());
				ClientDecisionMaker decisionMaker = decisionMakerService.getDecisionMakerByEmailAndClient(email,
						position.getClient());
				if (interviewer != null) {
					feedback.setFeedbackByMobile(interviewer.getMobile());
					feedback.setFeedbackByName(interviewer.getName());
				} else if (decisionMaker != null) {
					feedback.setFeedbackByMobile(decisionMaker.getMobile());
					feedback.setFeedbackByName(decisionMaker.getName());
				} else {
					feedback.setFeedbackByName(email.substring(0, email.indexOf("@")));

					// if interviewer list newly added so creating here for
					// levelbar feedback share only
					interviewer = new ClientInterviewerPanel();
					interviewer.setEmail(email);
					interviewer.setName(email.substring(0, email.indexOf("@")));

				}
				feedback.setFeedbackBy(email);
				feedback.setRoundId(roundId);
				feedback.setRoundCandidateId(roundCandidate.getId() + "");
				feedback.setRoundCandidate(roundCandidate);
				feedback.setType(type);
				feedback.setPositionName(position.getTitle());
				feedback.setClientName(position.getClient().getClientName());
				feedback.setRoundName(roundName);
				feedback.setCandidateId(roundCandidate.getCandidate().getCid() + "");
				feedback.setEventCreatedBy(
						userService.getLoggedInUserName() + " (" + userService.getLoggedInUserEmail() + ")");

				feedback.setProfileMasked(profileMasked);
				feedback.setEventCreatorEmail(userService.getLoggedInUserEmail());

				// calling level bar API to create custom feedback share
				if (feedbackShareId != null && !feedbackShareId.isEmpty()) {
					String feedbackShareResultId = createFeedbackShareResult(interviewer, feedbackShareId);
					feedback.setLevelbarFeedbackShareResultId(feedbackShareResultId);
				}

				requiredFeedbacks.add(feedback);
			}

			roundCandidate.getFeedback().clear();
			roundCandidate.setFeedback(requiredFeedbacks);
			roundCandidateService.save(roundCandidate);
		}
		return requiredFeedbacks;
	}

	/**
	 * to send interview Schedule reminder
	 *
	 * @throws ParseException
	 * @throws IOException
	 * @throws MessagingException
	 * @throws RecruizException
	 */
	
	public void sendInterviewScheduleReminder()
			throws ParseException, MessagingException, IOException, RecruizException {
		DateTime startDate = new DateTime(DateTimeZone.UTC).plusMinutes(15);
		DateTime endDate = new DateTime(DateTimeZone.UTC).plusMinutes(30);

		List<InterviewSchedule> remindableSchedules = interviewScheduleRepository
				.findByStartsAtBetween(startDate.toDate(), endDate.toDate());

		final String reminderTemplateHr = "email-template-schedule-reminder-hr.html";
		final String reminderTemplateInterviewer = "email-template-schedule-reminder-interviewer.html";

		if (remindableSchedules != null && !remindableSchedules.isEmpty()) {
			logger.debug(" total " + remindableSchedules.size() + " schedules found in tenant "
					+ TenantContextHolder.getTenant());
			for (InterviewSchedule interviewSchedule : remindableSchedules) {
				Set<User> hrExecutives = interviewSchedule.getScheduleHrExecutives();
				Set<EventAttendee> interviewers = interviewSchedule.getAttendee();
				Position position = positionService.getPositionByCode(interviewSchedule.getPositionCode());
				File jdFile = null;
				if (position != null) {
					jdFile = calendarService.createPositionTempJdFile(position);
				}
				Candidate candidate = candidateService.getCandidateByEmail(interviewSchedule.getCandidateEmail());
				String resumePath = null;
				if (candidate != null) {
					List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
							FileType.Masked_Resume_Converted.getDisplayName(), candidate.getCid() + "");
					if (interviewSchedule.isProfileMasked()) {
						if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
							resumePath = candidateMaskedResume.get(0).getFilePath();
						}
					} else {
						resumePath = candidate.getResumeLink();
					}
				}

				Map<String, Object> emailBodyMap = new HashMap<>();
				emailBodyMap.put(GlobalConstants.POSITION_NAME, interviewSchedule.getPositionName());
				emailBodyMap.put(GlobalConstants.CLIENT_NAME, interviewSchedule.getClientName());
				emailBodyMap.put(GlobalConstants.CANDIDATE_NAME, interviewSchedule.getCandidateName());
				emailBodyMap.put(GlobalConstants.CANDIDATE_EMAIL, interviewSchedule.getCandidateEmail());
				emailBodyMap.put(GlobalConstants.ROUND_NAME, interviewSchedule.getRoundName());
				emailBodyMap.put(GlobalConstants.INTERVIEW_SHCEDULE, dateTimeService.getUserTimezoneDateString(
						interviewSchedule.getStartsAt(), interviewSchedule.getInterviewSchedulerEmail()));

				String accountType = "Corporate";
				if (accountType.equalsIgnoreCase(organizationService.getCurrentOrganization().getOrgType())) {
					emailBodyMap.put(GlobalConstants.CLIENT_LABEL, "Department");
				} else {
					emailBodyMap.put(GlobalConstants.CLIENT_LABEL, "Client");
				}

				if (null != candidate && null != candidate.getMobile() && !candidate.getMobile().isEmpty()
						&& !interviewSchedule.isProfileMasked()) {
					emailBodyMap.put(GlobalConstants.CANDIDATE_MOBILE, candidate.getMobile());
				} else {
					emailBodyMap.put(GlobalConstants.CANDIDATE_MOBILE, "N/A");
				}

				sendReminderToInterviewer(reminderTemplateInterviewer, interviewSchedule, interviewers, jdFile,
						resumePath, emailBodyMap, candidate);

				sendReminderToHrExecutive(reminderTemplateHr, interviewSchedule, hrExecutives, jdFile, resumePath,
						emailBodyMap);

			}
		} else {
			logger.debug("No schedules found for tenant" + TenantContextHolder.getTenant());
		}
	}

	/**
	 * to send reminder to interviewer
	 *
	 * @param reminderTemplateInterviewer
	 * @param interviewSchedule
	 * @param interviewers
	 * @param jdFile
	 * @param resumePath
	 * @param emailBodyMap
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void sendReminderToInterviewer(final String reminderTemplateInterviewer,
			InterviewSchedule interviewSchedule, Set<EventAttendee> interviewers, File jdFile, String resumePath,
			Map<String, Object> emailBodyMap, Candidate candidate) throws MessagingException, IOException {
		String interviewerList = "";
		List<String> interviewerEmail = new ArrayList<>();
		if (interviewers != null && !interviewers.isEmpty()) {
			for (EventAttendee eventAttendee : interviewers) {
				interviewerList = interviewerList + "<br />" + eventAttendee.getName() + " (" + eventAttendee.getEmail()
				+ ")";
				interviewerEmail.add(eventAttendee.getEmail());
			}
			emailBodyMap.put(GlobalConstants.INTERVIEWER_LIST, interviewerList);
		}
		String interviewerTemplate = emailTemplateDataService.getHtmlContentFromFile(emailBodyMap,
				reminderTemplateInterviewer);
		String interviewerReminderSubject = "Reminder - " + interviewSchedule.getRoundName()
		+ ", Interview scheduled (rescheduled)";

		// sending reminder email here after adding the feedback link
		if (interviewers != null && !interviewers.isEmpty()) {
			User hrExecutiveUser = userService.getUserByEmail(interviewSchedule.getInterviewSchedulerEmail());
			String timeZone = "", locale = "";
			if (hrExecutiveUser != null) {
				timeZone = hrExecutiveUser.getTimezone();
				locale = hrExecutiveUser.getLocale();
			}
			for (EventAttendee eventAttendee : interviewers) {
				try {
					List<String> receiverEmail = new ArrayList<>();
					receiverEmail.add(eventAttendee.getEmail());

					RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate,
							interviewSchedule.getPositionCode());
					if (roundCandidate != null) {
						Feedback feedback = feedbackService.getFeedbackByRoundCandidateAndFeedbackByAndType(
								roundCandidate.getId() + "", interviewSchedule.getRoundId(), eventAttendee.getEmail(),
								true, GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW);
						if (feedback != null) {
							String isResumeMasked = interviewSchedule.isProfileMasked() ? "y" : "n";
							int linkValidityHour = Integer.parseInt(extendTokenForDays) * 24;
							String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(
									hrExecutiveUser.getEmail(), TenantContextHolder.getTenant(), WebMode.STANDALONE,
									linkValidityHour, timeZone, locale);

							String interviewerDetails = StringUtils.getUserInforToEncrypt(eventAttendee.getEmail(),
									eventAttendee.getName(), "0000000000");
							String accessToken = EncryptKeyUtils.getEncryptedKey(interviewerDetails);

							String link = null;
							if (feedback.getLevelbarFeedbackShareResultId() != null)
								link = levelbarBaseUrl + GlobalConstants.LEVELBAR_FEEDBACK_URL
								+ feedback.getLevelbarFeedbackShareResultId();

							String feedbackLink = baseUrl + GlobalConstants.INTERVIEW_FEEDBACK_URL
									+ GlobalConstants.PASSKEY + xAuthToken + GlobalConstants.CANDIDATE_ID
									+ candidate.getCid() + GlobalConstants.ROUND_ID + interviewSchedule.getRoundId()
									+ GlobalConstants.POSITION_CODE + interviewSchedule.getPositionCode()
									+ GlobalConstants.ACCESS_INFO + accessToken + GlobalConstants.FORWARD_MODE
									+ GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW + GlobalConstants.FEEDBACK_ID
									+ candidate.getCid() + ":" + feedback.getId() + GlobalConstants.MASKED_RESUME
									+ isResumeMasked + GlobalConstants.LEVELBAR_KEY + link;

							String renderedInterviewerTemplate = emailTemplateDataService
									.getMasterTemplateWithButton(interviewerTemplate, feedbackLink, "View / Feedback");

			    emailService.sendCalenderInvite(receiverEmail, renderedInterviewerTemplate,
				    interviewerReminderSubject, resumePath, null, jdFile, null, null, null);
			}
		    }
		} catch (Exception ex) {
		    logger.error("Error while sending reminder for " + interviewSchedule, ex);
		}
	    }
	}
    }

	/**
	 * to send reminder email to hr executives
	 *
	 * @param reminderTemplateHr
	 * @param interviewSchedule
	 * @param hrExecutives
	 * @param jdFile
	 * @param resumePath
	 * @param emailBodyMap
	 * @throws MessagingException
	 * @throws IOException
	 * @throws RecruizException
	 */
	private void sendReminderToHrExecutive(final String reminderTemplateHr, InterviewSchedule interviewSchedule,
			Set<User> hrExecutives, File jdFile, String resumePath, Map<String, Object> emailBodyMap)
					throws MessagingException, IOException, RecruizException {
		String hrList = "";
		List<String> hrExecutivesEmail = new ArrayList<>();
		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			for (User hr : hrExecutives) {
				hrList = hrList + "<br />" + hr.getName() + " (" + hr.getEmail() + ")";
				hrExecutivesEmail.add(hr.getEmail());
			}
			emailBodyMap.put(GlobalConstants.INTERVIEWER_LIST, hrList);
		}
		String interviewerTemplate = emailTemplateDataService.getHtmlContentFromFile(emailBodyMap, reminderTemplateHr);
		String renderedInterviewerTemplate = emailTemplateDataService
				.getMasterTemplateWithoutButton(interviewerTemplate);
		String interviewerReminderSubject = "Reminder - " + interviewSchedule.getRoundName()
		+ ", Interview scheduled (rescheduled)";

	emailService.sendCalenderInvite(hrExecutivesEmail, renderedInterviewerTemplate, interviewerReminderSubject,
		resumePath, null, jdFile, null, null, null);
    }

	@Transactional
	public void deleteByPositioncode(String positionCode) {
		List<InterviewSchedule> schedules = interviewScheduleRepository.findByPositionCode(positionCode);
		if (schedules != null && !schedules.isEmpty()) {
			interviewScheduleRepository.delete(schedules);
		}
	}

	@Transactional(readOnly = true)
	public LinkedList<InterviewSchedule> getAllScheduleByPositionCodeAndCandidateEmail(String positionCode,
			String candidateEmail) {
		Date today = new Date();
		LinkedList<InterviewSchedule> interviewScheduleList = interviewScheduleRepository
				.findByPositionCodeAndCandidateEmailAndActiveAndEndsAtAfter(positionCode, candidateEmail, true,
						new java.sql.Date(today.getTime()));

		if (interviewScheduleList != null) {
			for (InterviewSchedule interviewEvent : interviewScheduleList) {
				if (interviewEvent.getEndsAt().after(new Date()) && interviewEvent.isActive()) {
					interviewEvent.getAttendee().size();
				}
			}
		}
		return interviewScheduleList;
	}

	@Transactional(readOnly = true)
	public Long getAllScheduleCountByPositionCodeAndCandidateEmail(String positionCode, String candidateEmail) {
		Date today = new Date();
		Long scheduleCount = interviewScheduleRepository.countByPositionCodeAndCandidateEmailAndActiveAndEndsAtAfter(
				positionCode, candidateEmail, true, new java.sql.Date(today.getTime()));
		return scheduleCount;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public String createFeedbackShare(InterviewScheduleDTO scheduleDTO, String feedbackQueSetId, String type)
			throws RecruizException {

		String feedbackShareId = null;
		FeedbackShareDTO feedbackShareDTO = getFeedbackShareDTO(scheduleDTO, type);
		if (feedbackShareDTO == null)
			return null;

		ResponseEntity<RestResponse> response = levelbarService.createFeedbackShare(feedbackShareDTO, feedbackQueSetId);

		if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
			Map<String, String> responseFeedbackShare = (LinkedHashMap<String, String>) response.getBody().getData();

			feedbackShareId = responseFeedbackShare.get("id");
			return feedbackShareId;
		}
		return feedbackShareId;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public String updateFeedbackShare(InterviewScheduleDTO scheduleDTO, String feedbackQueSetId, String feedbackShareId,
			String type) throws RecruizException {

		String shareId = null;
		FeedbackShareDTO feedbackShareDTO = getFeedbackShareDTO(scheduleDTO, type);
		if (feedbackShareDTO != null) {
			ResponseEntity<RestResponse> response = levelbarService.updateFeedbackShare(feedbackShareDTO,
					feedbackQueSetId, feedbackShareId);

			if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
				if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
					Map<String, String> responseFeedbackShare = (LinkedHashMap<String, String>) response.getBody()
							.getData();

					shareId = responseFeedbackShare.get("id");
					return shareId;
				}
			}
		}
		return shareId;
	}

	@Transactional(readOnly = true)
	public void deleteFeedbackShare(String feedbackShareId) throws RecruizException {

		levelbarService.deleteFeedbackShare(feedbackShareId);

	}

	/**
	 * @param scheduleDTO
	 * @throws RecruizException
	 * @throws NumberFormatException
	 */
	@Transactional(readOnly = true)
	private FeedbackShareDTO getFeedbackShareDTO(InterviewScheduleDTO scheduleDTO, String type)
			throws RecruizException, NumberFormatException {

		FeedbackShareDTO feedbackShareDTO = null;

		Organization org = organizationService.getCurrentOrganization();

		Position position = positionService.getPositionByCode(scheduleDTO.getPositionCode());
		Round round = roundService.findOne(Long.parseLong(scheduleDTO.getRoundId()));
		Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());

		if (position != null && round != null && candidate != null) {
			feedbackShareDTO = new FeedbackShareDTO();
			feedbackShareDTO.setCandidateName(candidate.getFullName());
			feedbackShareDTO.setCandidateEmail(candidate.getEmail());
			feedbackShareDTO.setCandidateMobile(candidate.getMobile());
			feedbackShareDTO.setClientName(position.getClient().getClientName());
			feedbackShareDTO.setPositionCode(position.getPositionCode());
			feedbackShareDTO.setPositionName(position.getTitle());
			feedbackShareDTO.setRoundName(round.getRoundName());
			feedbackShareDTO.setScheduleDate(scheduleDTO.getScheduleDate());
			feedbackShareDTO.setTenant(TenantContextHolder.getTenant());
			feedbackShareDTO.setType(type);
			feedbackShareDTO.setOrgType(org.getOrgType());
			feedbackShareDTO.setMaskedResume(scheduleDTO.getResumeMasked().equalsIgnoreCase("n") ? false : true);
		}
		return feedbackShareDTO;
	}

	@SuppressWarnings("unchecked")
	public String createFeedbackShareResult(ClientInterviewerPanel interviewer, String feedbackShareId)
			throws RecruizException {

		String feedbackShareResultId = null;

		if (interviewer != null) {
			FeedbackShareResultDTO feedbackShareResultDTO = new FeedbackShareResultDTO();
			feedbackShareResultDTO.setInterviewerName(interviewer.getName());
			feedbackShareResultDTO.setInterviewerEmail(interviewer.getEmail());
			feedbackShareResultDTO.setInterviewerMobile(interviewer.getMobile());

			ResponseEntity<RestResponse> response = levelbarService.createFeedbackShareResult(feedbackShareResultDTO,
					feedbackShareId);

			if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
				Map<String, String> responseFeedbackShare = (LinkedHashMap<String, String>) response.getBody()
						.getData();
				feedbackShareResultId = responseFeedbackShare.get("id");
				return feedbackShareResultId;
			}
		}
		return feedbackShareResultId;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<FeedbackQuestionSetDTO> getFeedbackQueSets() throws RecruizException {

		List<FeedbackQuestionSetDTO> feedbackQuestionSetDTOList = new ArrayList<FeedbackQuestionSetDTO>();
		ResponseEntity<RestResponse> response = levelbarService.getFeedbackQueSets();

		if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
			List<Map<String, Object>> restResponse = (ArrayList<Map<String, Object>>) response.getBody().getData();

			if (restResponse != null && !restResponse.isEmpty()) {
				for (Map<String, Object> map : restResponse) {
					FeedbackQuestionSetDTO feedbackQuestionSetDTO = new FeedbackQuestionSetDTO();

					feedbackQuestionSetDTO.setId((String) map.get("id"));
					feedbackQuestionSetDTO.setTitle((String) map.get("title"));
					feedbackQuestionSetDTO.setDescription((String) map.get("description"));
					feedbackQuestionSetDTO.setOrgId((String) map.get("orgId"));
					feedbackQuestionSetDTO.setOrgName((String) map.get("orgName"));
					feedbackQuestionSetDTO.setQuestionSetType((String) map.get("questionSetType").toString());
					feedbackQuestionSetDTO.setTags((Object) map.get("tags"));

					feedbackQuestionSetDTOList.add(feedbackQuestionSetDTO);
				}

			}
		}
		return feedbackQuestionSetDTOList;
	}

	/**
	 * To send feedback reminder email
	 *
	 * @param interviewSchedule
	 * @param feedbackId
	 * @throws MessagingException
	 * @throws IOException
	 * @throws RecruizException
	 */
	public void sendReminderToGiveFeedback(String positionCode, Feedback feedback)
			throws MessagingException, IOException, RecruizException {

		final String reminderTemplateInterviewer = GlobalConstants.EMAIL_FEEDBACK_REMINDER_TEMPLATE;
		try {
			Position position = positionService.getPositionByCode(positionCode);
			File JdFile = calendarService.createPositionTempJdFile(position);

			Candidate candidate = candidateService.findOne(Long.parseLong(feedback.getCandidateId()));
			String resumePath = null;
			if (candidate != null) {
				List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
						FileType.Masked_Resume_Converted.getDisplayName(), candidate.getCid() + "");
				if (feedback.isProfileMasked()) {
					if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
						resumePath = candidateMaskedResume.get(0).getFilePath();
					}
				} else {
					resumePath = candidate.getResumeLink();
				}
			}

			Map<String, Object> emailBodyMap = new HashMap<>();
			emailBodyMap.put("feedbackPerson", feedback.getFeedbackByName());
			emailBodyMap.put("positionName", feedback.getPositionName());

			String interviewerTemplate = emailTemplateDataService.getHtmlContentFromFile(emailBodyMap,
					reminderTemplateInterviewer);
			String interviewerReminderSubject = "Feedback Reminder - " + feedback.getRoundName() + " for position "
					+ feedback.getPositionName();

			User hrExecutiveUser = userService.getLoggedInUserObject();
			String timeZone = "", locale = "";
			if (hrExecutiveUser != null) {
				timeZone = hrExecutiveUser.getTimezone();
				locale = hrExecutiveUser.getLocale();
			}

			List<String> receiverEmail = new ArrayList<>();
			receiverEmail.add(feedback.getFeedbackBy());

			if (feedback != null) {
				String isResumeMasked = feedback.isProfileMasked() ? "y" : "n";
				int linkValidityHour = Integer.parseInt(extendTokenForDays) * 24;

				String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(hrExecutiveUser.getEmail(),
						TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour, timeZone, locale);

				String interviewerDetails = StringUtils.getUserInforToEncrypt(feedback.getFeedbackBy(),
						feedback.getFeedbackByName(), "0000000000");
				String accessToken = EncryptKeyUtils.getEncryptedKey(interviewerDetails);

				String link = null;
				if (feedback.getLevelbarFeedbackShareResultId() != null)
					link = levelbarBaseUrl + GlobalConstants.LEVELBAR_FEEDBACK_URL
					+ feedback.getLevelbarFeedbackShareResultId();

				String feedbackLink = baseUrl + GlobalConstants.INTERVIEW_FEEDBACK_URL + GlobalConstants.PASSKEY
						+ xAuthToken + GlobalConstants.CANDIDATE_ID + feedback.getCandidateId()
						+ GlobalConstants.ROUND_ID + feedback.getRoundId() + GlobalConstants.POSITION_CODE
						+ positionCode + GlobalConstants.ACCESS_INFO + accessToken + GlobalConstants.FORWARD_MODE
						+ feedback.getType() + GlobalConstants.FEEDBACK_ID + feedback.getCandidateId() + ":"
						+ feedback.getId() + GlobalConstants.MASKED_RESUME + isResumeMasked
						+ GlobalConstants.LEVELBAR_KEY + link;

				String renderedInterviewerTemplate = emailTemplateDataService
						.getMasterTemplateWithButton(interviewerTemplate, feedbackLink, "View / Feedback");

		emailService.sendCalenderInvite(receiverEmail, renderedInterviewerTemplate, interviewerReminderSubject,
			resumePath, null, JdFile, null, null, null);
	    }

		} catch (Exception ex) {
			logger.error("Error while sending reminder for " + feedback, ex);
			throw new RecruizException(ErrorHandler.FAILED_TO_SEND_FEEDBACK_REMINDER,
					ErrorHandler.FAILED_SENDING_FEEDBACK_REMINDER);
		}
	}

	/**
	 * to untag interviewer from a interview schedule
	 *
	 * @param email
	 */
	@Transactional
	public void untagInterviewer(String email) {
		List<InterviewSchedule> schedulesByInterviewerIn = interviewScheduleRepository
				.getSchedulesByInterviewerEmail(email);

		if (schedulesByInterviewerIn != null && !schedulesByInterviewerIn.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedulesByInterviewerIn) {
				try {
					// if size of event attendee is only one then cance the
					// schedule
					if (interviewSchedule.getAttendee().size() == 1) {
						calendarService.cancelInterviewerInvite(interviewSchedule.getId());
						continue;
					}
					Set<EventAttendee> attendeeToRemove = new HashSet<>();
					for (EventAttendee attendee : interviewSchedule.getAttendee()) {
						if (attendee.getEmail().equalsIgnoreCase(email)) {
							attendeeToRemove.add(attendee);
						}
					}

					// removing interviewer from schedule and deleting them
					if (null != attendeeToRemove && !attendeeToRemove.isEmpty()) {
						interviewSchedule.getAttendee().removeAll(attendeeToRemove);
						interviewScheduleRepository.save(interviewSchedule);
						eventAttendeeService.delete(attendeeToRemove);
					}

				} catch (Exception ex) {
					logger.warn(ex.getMessage(), ex);
				}
			}
		}
	}

	// to get count of total interviews for a position
	public Long getInterviewCountByPosition(String positionCode) {
		return interviewScheduleRepository.countByPositionCode(positionCode);
	}

	// to get count of total interviews for a position
	public Long getInterviewCountByPositionAndHr(String positionCode, String hrEmail) {
		return interviewScheduleRepository.countByPositionCodeAndInterviewSchedulerEmail(positionCode, hrEmail);
	}
	// to get count of total interviews for a position
	public Long getInterviewCountByPositionInAndHr(List<String> positionCodes, String hrEmail) {
		return interviewScheduleRepository.countByPositionCodeInAndInterviewSchedulerEmail(positionCodes, hrEmail);
	}

	// to get count of total interviews for a position
	public Long getInterviewCountByPositionAndHrAndDateRange(String positionCode, String hrEmail,Date startDate,Date endDate) {
		return interviewScheduleRepository.countByPositionCodeAndInterviewSchedulerEmailAndModificationDateBetween(positionCode, hrEmail,startDate,endDate);
	}

	// to get count of total interviews for a position
	public Long getInterviewCountByPositionAndDateRange(String positionCode,Date startDate,Date endDate) {
		return interviewScheduleRepository.countByPositionCodeAndModificationDateBetween(positionCode,startDate,endDate);
	}

	// to get count of total interviews for a position betwwen date range
	public Long getInterviewCountByPositionForDateRange(String positionCode, Date startDate, Date endDate) {
		return interviewScheduleRepository.countByPositionCodeAndCreationDateBetween(positionCode, startDate, endDate);
	}

	public InterviewSchedule getScheduleByPositionCodeRoundNameAndCandidateEmail(String pcode, String rname,
			String cemail) {
		List<InterviewSchedule> schedules = interviewScheduleRepository.getScheduleByPcodAndRoundNameAndCanEmail(pcode,
				rname, cemail);
		if (schedules != null && !schedules.isEmpty()) {
			return schedules.get(0);
		}
		return null;
	}

	public Long getInterviewCountByPositionCodes(List<String> positionCodes) {
		return interviewScheduleRepository.countByPositionCodeIn(positionCodes);
	}

	@Transactional(readOnly = true)
	public List<DashboardDTO> getInterviewScheduleForSuperAdmin() throws RecruizException {

		List<DashboardDTO> scheduleDetails = new LinkedList<>();
		DashboardDTO scheduleDetail = null;
		DateTime enddate = new DateTime().plusDays(7);
		java.sql.Date startDate = new java.sql.Date(new DateTime().toDate().getTime());
		java.sql.Date endDate = new java.sql.Date(enddate.toDate().getTime());

		List<InterviewSchedule> schedules = new LinkedList<>();

		if (checkUserPermissionService.isSuperAdmin()) {
			schedules =  interviewScheduleRepository.findByStartsAtBetween(startDate, endDate);
		} 

		if (schedules != null && !schedules.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedules) {
				scheduleDetail = new DashboardDTO();
				scheduleDetail.setClientName(interviewSchedule.getClientName());
				scheduleDetail.setPositionName(interviewSchedule.getPositionName());
				scheduleDetail.setCandidateNames(interviewSchedule.getCandidateName());
				scheduleDetail.setScheduleDate(interviewSchedule.getStartsAt());
				scheduleDetails.add(scheduleDetail);

			}
		}
		Collections.sort(schedules);
		return scheduleDetails;
	}

	public RestResponse getAllInterviewerAndDecisionMaker(String positionCode, String searchText) throws RecruizException {

		Position position = positionService.getOneByPositionCode(positionCode);
		List<ClientInterviewerPanel> interviewerList = interviewPanelService.getInterviewerListByClient(position.getClient());
		List<ClientDecisionMaker> decisionList =  decisionMakerService.getDecisionMakerByClient(position.getClient());
		List<User> userList = userService.getAllActiveAppUsers();
		
		Set<String> emailList = new HashSet<>();

		if(interviewerList!=null)
		for (ClientInterviewerPanel interw : interviewerList) {
			
			if(interw.getEmail().toLowerCase().contains(searchText.toLowerCase()))
			emailList.add(interw.getEmail());
		}

		if(decisionList!=null)
		for (ClientDecisionMaker decision : decisionList) {
			
			if(decision.getEmail().toLowerCase().contains(searchText.toLowerCase()))
			emailList.add(decision.getEmail());
		}
		
		if(userList!=null)
			for (User user : userList) {
				if(user.getEmail().toLowerCase().contains(searchText.toLowerCase()))
					emailList.add(user.getEmail());
			}

		return new RestResponse(RestResponse.SUCCESS, emailList);
	}

}
