package com.bbytes.recruiz.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.domain.InterviewFile;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.OfferLetterForCandidate;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.IcsFileType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.CandidateFileRepository;
import com.bbytes.recruiz.repository.OfferLetterForCandidateRepository;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.VelocityTemplateUtils;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

@Service
public class CalendarService {

	private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

	// @Value("${meeting.invite.email}")
	// private String organizerEmail;

	@Value("${email.interview.cancelled.subject}")
	private String interviewCancelledSubject;

	@Value("${interviewer.invite.summary}")
	private String interviewerInviteSummary;

	@Value("${candidate.invite.summary}")
	private String candidateInviteSummary;

	@Value("${levelbar.server.url}")
	protected String levelbarBaseUrl;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private IEmailService emailService;
	
	@Autowired
	CandidateFileRepository candidateFileRepository;

	@Autowired
	OfferLetterForCandidateRepository offerLetterForCandidateRepository;
	
	@Autowired
	private InterviewScheduleService scheduleService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private GenericInterviewerService genericInterviewerService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${logo.recruiz}")
	private String logoPath;

	@Value("${allowed.external.user.links.validity}")
	private String extendTokenForDays;

	@Value("${file.upload.temp.path}")
	private String tempFolderPath;

	final String scheduleInterviewerTemplate = GlobalConstants.EMAIL_SCHEDULE_INTERVIEWER_TEMPLATE;

	// final String scheduleCandidateTemplate =
	// GlobalConstants.EMAIL_SCHEDULE_CANDIDATE_TEMPLATE;

	final String rescheduleInterviewerTemplate = GlobalConstants.EMAIL_RESCHEDULE_INTERVIEWER_TEMPLATE;

	final String rescheduleCandidateTemplate = GlobalConstants.EMAIL_RESCHEDULE_CANDIDATE_TEMPLATE;

	final String cancelInterviewTemplate = GlobalConstants.EMAIL_INTERVIEW_CANCEL_TEMPLATE;

	/**
	 * 
	 * @param position
	 * @param scheduleDTO
	 * @param selectedFiles 
	 * @param round
	 * @param interviewer
	 * @param candidate
	 * @param hrExecutive
	 * @param attendeeList
	 * @throws Exception
	 */
	public void sendCalenderInvite(InterviewScheduleDTO scheduleDTO, Set<Feedback> expectedFeedbacks,
			String feedbackSharedId, String selectedFiles) throws Exception {

		Position position = positionService.getPositionByCode(scheduleDTO.getPositionCode());
		if (position == null)
			throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		// creating jd file here in temp folder, it will be deleted after

		// sending to email

		File jdFile = null;
		if(scheduleDTO.getIsJDAttached()) {
			jdFile = createPositionTempJdFile(position);
		}


		Round round = roundService.findOne(Long.parseLong(scheduleDTO.getRoundId()));
		if (round == null)
			throw new RecruizWarnException(ErrorHandler.ROUND_NOT_PRESENT, ErrorHandler.ROUND_NOT_EXIST);

		Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());
		if (candidate == null)
			throw new RecruizWarnException(ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.CANDIDATE_NOT_FOUND);

		Set<EventAttendee> interviewers = new HashSet<EventAttendee>();
		for (String email : scheduleDTO.getInterviewerEmails()) {
			String interviewerName = email;

			// fetching from generic interviewer if found then name will be set
			// otherwise in name email will go
			GenericInterviewer genericInterviewer = genericInterviewerService.getInterviewerByEmail(email);
			if (null != genericInterviewer) {
				interviewerName = genericInterviewer.getName();
			}

			EventAttendee interviewer = new EventAttendee();
			interviewer.setEmail(email);
			interviewer.setName(interviewerName);
			interviewers.add(interviewer);
		}

		DateTime start = new DateTime(scheduleDTO.getStartTime().getTime());
		DateTime end = new DateTime(scheduleDTO.getEndTime().getTime());

		String candidateEventId = generatedEventUid();
		String interviewerEventId = generatedEventUid();

		String calenderEventTitle = scheduleDTO.getInterviewerTemplateSubject() + " Candidate Name : "
				+ candidate.getFullName() + " ,Candidate Mobile :"
				+ (candidate.getMobile() != null ? candidate.getMobile() : "N/A");
		File interviewerFile = createCalenderEventFile(calenderEventTitle, start, end, interviewers,
				interviewerInviteSummary, interviewerEventId);

		String candidateCalenderDescription = scheduleDTO.getTemplateSubject();

		File candidateFile = createCalenderEventFileForCandidate(scheduleDTO.getTemplateSubject(), start, end,
				candidate.getEmail(), candidate.getFullName(), candidateCalenderDescription, candidateEventId,
				userService.getLoggedInUserName(), userService.getLoggedInUserEmail());

		Set<String> newEmailList = scheduleDTO.getInterviewerEmails();
		// newEmailList.add(organizerEmail);
		
		List<String> candidateFiles = new ArrayList<>();
		if(selectedFiles!=null)
		candidateFiles = addSelectedFilesWithAttachment(selectedFiles, candidate.getCid());
		

		for (String email : newEmailList) {
			if (scheduleDTO.getExpectFeedback()) {
				// if feedback is expected then create link with valid feedback
				// id and send it
				for (Feedback feedback : expectedFeedbacks) {
					if (feedback.getFeedbackBy().equalsIgnoreCase(email)) {
						createFeedbackLinkAndSendEmailToInterviewer(scheduleDTO, position, jdFile, round, candidate,
								interviewerFile, email, feedback.getId(), feedback.getLevelbarFeedbackShareResultId(), candidateFiles);
					}
				}
			} else {
				// if feedback is not expected then create link with -1 feedback
				// id and send it and also break the loop not to send other
				// emails
				createFeedbackLinkAndSendEmailToInterviewer(scheduleDTO, position, jdFile, round, candidate,
						interviewerFile, email, -1L, null, candidateFiles);

				break;
			}

		}

		// sending to HR executive
		List<String> emailList = new ArrayList<String>();
		emailList.add(userService.getLoggedInUserEmail());

		String renderedTemplateForHr = emailTemplateDataService.getRenderedTemplateForInterviewSchedule(
				GlobalConstants.HR_TEMPLATE, scheduleDTO.getPositionCode(), scheduleDTO.getCandidateEmail(),
				new HashSet<>(scheduleDTO.getInterviewerEmails()), round.getRoundName(), start);

		Map<String, Object> masterTemplateBody = new HashMap<>();
		masterTemplateBody.put(GlobalConstants.MasterEmailTemplate, renderedTemplateForHr);
		String renderedMastertemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplateForHr);

		emailService.sendCalenderInvite(emailList, renderedMastertemplate, scheduleDTO.getInterviewerTemplateSubject(),
				interviewerFile.getAbsolutePath(), userService.getLoggedInUserEmail(), scheduleDTO.getCcEmails(), candidateFiles);

		// adding to email repository
		try {
			emailActivityService.saveEmail(renderedMastertemplate, scheduleDTO.getInterviewerTemplateSubject(),
					emailList, null, userService.getLoggedInUserEmail(), interviewerFile.getPath());
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}

		InterviewFile candidatesFile = new InterviewFile();
		candidatesFile.setFileType("candidate");

		if (!scheduleDTO.getIsCandidateInviteSkiped().equalsIgnoreCase("true")) {
			// sending event invite to candidate
			List<String> candidateList = new ArrayList<String>();
			candidateList.add(candidate.getEmail());
			// here making email body for candidate
			Map<String, Object> candidateEmailBody = getEmailBodyForCandidate(position, scheduleDTO.getStartTime(),
					userService.getLoggedInUserEmail(), candidate.getFullName());

			// taking template from user (get template from popup in UI)
			String renderedCandidateTemplate = emailTemplateDataService
					.getMasterTemplateWithoutButton(scheduleDTO.getNotes());
			emailService.sendCandidateCalenderInvite(candidateList, candidateEmailBody,
					scheduleDTO.getTemplateSubject(), renderedCandidateTemplate, candidateFile.getAbsolutePath(),
					userService.getLoggedInUserEmail(), candidateFiles);

			// adding to email repository
			try {

				String jdPath = null;
				if(jdFile != null) {
					jdPath = jdFile.getPath();
				}
				emailActivityService.saveEmail(renderedCandidateTemplate, scheduleDTO.getTemplateSubject(),
						candidateList, null, userService.getLoggedInUserEmail(), candidateFile.getPath(),
						jdPath);
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}

			candidatesFile.setFile(Files.readAllBytes(Paths.get(candidateFile.getAbsolutePath())));
			candidatesFile.setFileType("candidate");
		}

		InterviewFile interviewersFile = new InterviewFile();
		interviewersFile.setFile(Files.readAllBytes(Paths.get(interviewerFile.getAbsolutePath())));
		interviewersFile.setFileType("interviewer");

		List<InterviewFile> eventFiles = new ArrayList<InterviewFile>();
		eventFiles.add(candidatesFile);
		eventFiles.add(interviewersFile);

		scheduleService.save(position, round, scheduleDTO, interviewers, candidate, candidateEventId,
				interviewerEventId, eventFiles, feedbackSharedId);

		// making entry to candidate activity
		candidateActivityService.interviewScheduleEvent(position, round, candidate,scheduleDTO);
	}

	public void createFeedbackLinkAndSendEmailToInterviewer(InterviewScheduleDTO scheduleDTO, Position position,
			File jdFile, Round round, Candidate candidate, File interviewerFile, String email, Long feedbackID,
			String levelbarFeedbackShareResultId, List<String> candidateFiles)
					throws ParseException, MessagingException, IOException, RecruizException {
		Map<String, Object> interviewerEmailBody;
		List<String> emailList = new ArrayList<String>();
		emailList.add(email);

		if (!scheduleDTO.getExpectFeedback()) {
			emailList.clear();
			emailList.addAll(scheduleDTO.getInterviewerEmails());
		}

		List<String> candidateFeedbackIdMap = new ArrayList<>();
		candidateFeedbackIdMap.add(candidate.getCid() + ":" + feedbackID);

		interviewerEmailBody = getEmailBodyForInterviewer(position, round.getId(),
				DateTimeUtils.getDateTimeAsString(scheduleDTO.getStartTime()), email, Long.toString(candidate.getCid()),
				scheduleDTO.getInterviewerEmailTemplate(), StringUtils.commaSeparate(candidateFeedbackIdMap),
				scheduleDTO.getResumeMasked(), levelbarFeedbackShareResultId,scheduleDTO.getMaskedCtc());

		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithButton(
				scheduleDTO.getInterviewerEmailTemplate(),
				interviewerEmailBody.get(GlobalConstants.NOTE_LINK).toString(), "View/Feedback");

		// if resume is masked pass the masked resume file path
		String maskedResumeFilePath = "";
		if (scheduleDTO.getResumeMasked().equalsIgnoreCase("y")) {
			// why list ? -> bcoz all candidate related files belong
			// to one object called candidate files.
			List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
					FileType.Masked_Resume_Converted.getDisplayName(), candidate.getCid() + "");
			if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
				maskedResumeFilePath = candidateMaskedResume.get(0).getFilePath();
			} else {
				maskedResumeFilePath = "";
			}
		} else {
			maskedResumeFilePath = candidate.getResumeLink();
		}

		emailService.sendCalenderInvite(emailList, renderedMasterTemplate, scheduleDTO.getInterviewerTemplateSubject(),
				interviewerFile.getAbsolutePath(), maskedResumeFilePath, jdFile, userService.getLoggedInUserEmail(),
				scheduleDTO.getCcEmails(), candidateFiles);

		try {
			String jdPath = null;
			if(null != jdFile) {
				jdPath = jdFile.getPath();
			}
			emailActivityService.saveEmail(renderedMasterTemplate, scheduleDTO.getInterviewerTemplateSubject(),
					emailList, scheduleDTO.getCcEmails(), userService.getLoggedInUserEmail(), interviewerFile.getPath(),
					maskedResumeFilePath,jdPath);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}

	}

	public File createPositionTempJdFile(Position position) throws IOException {
		String renderedJdTemplate = emailTemplateDataService.getRenderedTemplateForJD(position);

		// create JD file here and delete after sending email
		File tempFileLocation = new File(tempFolderPath + "/" + TenantContextHolder.getTenant());
		if (tempFileLocation.isDirectory()) {
			tempFileLocation.mkdirs();
		}
		if (!tempFileLocation.exists())
			tempFileLocation.mkdirs();

		File jdFile = new File(tempFileLocation + "/" + position.getPositionCode() + "/" + UUID.randomUUID().toString()
				+ "/" + GlobalConstants.JOB_DESC_FILE_NAME);
		if (jdFile.exists()) {
			jdFile.delete();
		}

		jdFile.getParentFile().mkdirs();
		jdFile.createNewFile();
		FileWriter fileWriter = new FileWriter(jdFile);
		fileWriter.write(renderedJdTemplate);
		fileWriter.flush();
		fileWriter.close();
		return jdFile;
	}

	/**
	 * <code>getEmailBodyForInterviewer</code> returns the email body for
	 * interview schedule event.
	 * 
	 * @param positionTile
	 * @param date
	 * @param interviewerEmail
	 * @param  
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	private Map<String, Object> getEmailBodyForInterviewer(Position position, Long roundId, String date,
			String interviewerEmail, String candidateId, String notes, String feedbackId, String resumeMasked,
			String levelbarFeedbackShareId, Boolean maskedCtc) throws ParseException {

		final String email = userService.getLoggedInUserEmail();
		String interviewerName = StringUtils.getNameFromEmail(interviewerEmail);
		String interviewerMobile = "0000000000";

		// finding difference between interview date and current date
		Date today = new Date();
		Date scheduleDate = new Date(Date.parse(date));

		long diff = scheduleDate.getTime() - today.getTime();

		long diffHours = diff / (60 * 60 * 1000);

		int linkValidityHour = (int) (diffHours + (Integer.parseInt(extendTokenForDays) * 24));

		String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(email, TenantContextHolder.getTenant(),
				WebMode.STANDALONE, linkValidityHour, userService.getLoggedInUserObject().getTimezone(),
				userService.getLoggedInUserObject().getLocale());

		ClientInterviewerPanel interviewer = interviewPanelService.getInterviewerByEmailAndClient(interviewerEmail,
				position.getClient());
		if (interviewer != null) {
			interviewerName = interviewer.getName();
			interviewerMobile = interviewer.getMobile();
		}
		String interviewerDetails = StringUtils.getUserInforToEncrypt(interviewerEmail, interviewerName,
				interviewerMobile);

		String accessToken = EncryptKeyUtils.getEncryptedKey(interviewerDetails);

		String link = null;
		if (levelbarFeedbackShareId != null)
			link = levelbarBaseUrl + GlobalConstants.LEVELBAR_FEEDBACK_URL + levelbarFeedbackShareId;


		String token = baseUrl + GlobalConstants.INTERVIEW_FEEDBACK_URL + GlobalConstants.PASSKEY + xAuthToken
				+ GlobalConstants.CANDIDATE_ID + candidateId + GlobalConstants.ROUND_ID + roundId
				+ GlobalConstants.POSITION_CODE + position.getPositionCode() + GlobalConstants.ACCESS_INFO + accessToken
				+ GlobalConstants.FORWARD_MODE + GlobalConstants.PROFILE_FORWARD_MODE_INTERVIEW
				+ GlobalConstants.FEEDBACK_ID + feedbackId + GlobalConstants.MASKED_RESUME + resumeMasked
				+ GlobalConstants.LEVELBAR_KEY + link + GlobalConstants.MASKED_CTC +maskedCtc;

		logger.debug("url is : " + token);

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.INTERVIEW_DATE, date);
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.NOTE_LINK, token);
		emailBody.put(GlobalConstants.MasterEmailTemplateLogo, logoPath);
		emailBody.put(GlobalConstants.MasterInterviewScheduleTemplate, notes);
		return emailBody;
	}

	/**
	 * <code>getEmailBodyForCandidate</code> returns the email body for
	 * candidate schedule event.
	 * 
	 * @param position
	 * @param date
	 * @param email
	 * @return
	 * @throws ParseException
	 */
	private Map<String, Object> getEmailBodyForCandidate(Position position, Date date, String email,
			String candidateName) throws ParseException {

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.INTERVIEW_DATE, DateTimeUtils.getDateTimeAsString(date));
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.EVENT_ORGANIZER, email);
		emailBody.put(GlobalConstants.CANDIDATE_NAME, candidateName);

		return emailBody;
	}

	File createTaskFile(String decription, DateTime start, DateTime end, Set<EventAttendee> interviewer,
			String summary, String eventUid) throws IOException {
		Calendar icalendar = new Calendar();
		icalendar.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		icalendar.getProperties().add(Version.VERSION_2_0);
		icalendar.getProperties().add(Method.PUBLISH);
		icalendar.getProperties().add(Method.REQUEST);

		Organizer organizer = new Organizer(URI.create("mailto:hello@beyondbytes.co.in"));
		organizer.getParameters().add(new Cn("Recruiz"));

		VEvent vevent = new VEvent(start, end, decription);
		setAttendeeToEvent(vevent, interviewer);

		vevent.getProperties().add(organizer);
		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description(decription));
		vevent.getProperties().add(new Uid(eventUid));

		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().add(new Duration(start, end));
		vevent.getProperties().add(new Sequence(0));
		icalendar.getComponents().add(vevent);

		File file = new File("task_schedule.ics");

		if(file.exists()) {
			file.delete();
		}

		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(icalendar, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * <code>createCalenderEventFile</code> returns the calendar invitation file
	 * for interviewer.
	 * 
	 * @param decription
	 * @param location
	 * @param start
	 * @param end
	 * @param interviewer
	 * @param summary
	 * @param eventUid
	 * @return
	 * @throws IOException
	 */
	File createCalenderEventFile(String decription, DateTime start, DateTime end, Set<EventAttendee> interviewer,
			String summary, String eventUid) throws IOException {
		Calendar icalendar = new Calendar();
		icalendar.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		icalendar.getProperties().add(Version.VERSION_2_0);
		icalendar.getProperties().add(Method.PUBLISH);
		icalendar.getProperties().add(Method.REQUEST);

		// Adding organizer
		// needs to remove this option if decided to change the organizer
		Organizer organizer = new Organizer(URI.create("mailto:" + userService.getLoggedInUserEmail()));

		// Organizer organizer = new
		// Organizer(URI.create("mailto:hello@beyondbytes.co.in"));
		// organizer.getParameters().add(new
		// Cn(userService.getLoggedInUserName()));

		VEvent vevent = new VEvent(start, end, decription);
		setAttendeeToEvent(vevent, interviewer);

		vevent.getProperties().add(organizer);
		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description(decription));
		vevent.getProperties().add(new Uid(eventUid));

		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().add(new Duration(start, end));
		vevent.getProperties().add(new Sequence(0));
		icalendar.getComponents().add(vevent);

		File file = new File("iCalInvite_interviewer.ics");
		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(icalendar, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * <code>generatedEventUid</code> returns random event id string.
	 * 
	 * @return
	 */
	String generatedEventUid() {
		return UUID.randomUUID().toString() + "@beyondbytes.co.in";
	}

	/**
	 * <code>createCalenderEventFileForCandidate</code> returns the calendar
	 * invitation file for candidate.
	 * 
	 * @param decription
	 * @param location
	 * @param start
	 * @param end
	 * @param candidateEmail
	 * @param candidateName
	 * @param summary
	 * @param eventUid
	 * @return
	 * @throws IOException
	 */
	private File createCalenderEventFileForCandidate(String decription, DateTime start, DateTime end,
			String candidateEmail, String candidateName, String summary, String eventUid, String hrName, String hrEmail)
					throws IOException {
		Calendar icalendar = new Calendar();
		icalendar.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		icalendar.getProperties().add(Version.VERSION_2_0);
		icalendar.getProperties().add(Method.PUBLISH);
		icalendar.getProperties().add(Method.REQUEST);

		// decided to add HR as organizer
		Organizer organizer = new Organizer(URI.create("mailto:" + hrEmail));
		organizer.getParameters().add(new Cn(hrName));

		VEvent vevent = new VEvent(start, end, summary);

		ParameterList plist = new ParameterList();
		plist.add(new PartStat("NEED-ACTION"));

		Attendee attendee = new Attendee(plist, URI.create("mailto:" + candidateEmail));
		attendee.getParameters().add(Role.REQ_PARTICIPANT);
		attendee.getParameters().add(new Cn(candidateName));
		attendee.getParameters().add(Rsvp.TRUE);

		vevent.getProperties().add(organizer);
		vevent.getProperties().add(attendee);

		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description(decription));
		vevent.getProperties().add(new Uid(eventUid));

		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().add(new Duration(start, end));
		icalendar.getComponents().add(vevent);

		File file = new File(
				SystemUtils.JAVA_IO_TMPDIR + "/" + System.currentTimeMillis() + "iCalInvite_candidate.ics");
		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(icalendar, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * <code>setAttendeeToEvent</code> method used to set the Attendee for an
	 * event.
	 * 
	 * @param event
	 * @param interviewer
	 */
	private void setAttendeeToEvent(VEvent event, Set<EventAttendee> interviewer) {

		for (EventAttendee eventAttendee : interviewer) {
			Attendee attendee = new Attendee(URI.create("mailto:" + eventAttendee.getEmail()));
			attendee.getParameters().add(Role.REQ_PARTICIPANT);
			attendee.getParameters().add(new Cn(eventAttendee.getName()));
			attendee.getParameters().add(Rsvp.TRUE);
			attendee.getParameters().add(new PartStat("NEED-ACTION"));
			event.getProperties().add(attendee);
		}
		// adding HR executive for RSVP
		Attendee attendee = new Attendee(URI.create("mailto:" + userService.getLoggedInUserEmail()));
		attendee.getParameters().add(Role.REQ_PARTICIPANT);
		attendee.getParameters().add(new Cn(userService.getLoggedInUserObject().getName()));
		attendee.getParameters().add(Rsvp.TRUE);
		event.getProperties().add(attendee);
	}

	/**
	 * <code>cancelInterviewerInvite</code> method used to cancel interview
	 * invite.
	 * 
	 * @param interviewScheduleId
	 * @throws Exception
	 *             transactional bcoz saving emails sent to email repository
	 */

	@Transactional
	public void cancelInterviewerInvite(long interviewScheduleId) throws Exception {

		InterviewSchedule interview = scheduleService.findOne(interviewScheduleId);
		Set<EventAttendee> attendee = interview.getAttendee();
		List<String> emailList = new ArrayList<String>();
		for (EventAttendee eventAttendee : attendee) {
			emailList.add(eventAttendee.getEmail());
		}

		DateTime start = new DateTime(interview.getStartsAt().getTime());
		DateTime end = new DateTime(interview.getEndsAt().getTime());

		List<InterviewFile> icsFiles = interview.getFile();
		InputStream interviewerStream = null, candidateStream = null;
		for (InterviewFile interviewFile : icsFiles) {
			if (interviewFile.getFileType().equalsIgnoreCase("interviewer"))
				interviewerStream = new ByteArrayInputStream(interviewFile.getFile());
			if (interviewFile.getFileType().equalsIgnoreCase("candidate")) {
				if (interviewFile.getFile() != null) {
					candidateStream = new ByteArrayInputStream(interviewFile.getFile());
				}
			}

		}

		Map<String, Object> masterTemplateBody = new HashMap<String, Object>();
		masterTemplateBody.put(GlobalConstants.MasterEmailTemplateLogo, logoPath);

		// For Interviewers
		// Interviewer Cancel Template
		String renderedInterviewerCancelTemplate = emailTemplateDataService.getRenderedTemplateForCancelInterview(
				interview.getPositionCode(), interview.getCandidateName(),
				GlobalConstants.INTERVIEWER_INTERVIEW_CANCEL_TEMPLATE_NAME);
		String renderedMasterTemplate = emailTemplateDataService
				.getMasterTemplateWithoutButton(renderedInterviewerCancelTemplate);
		cancelInvite(interview.getNotes(), emailList, start, end, interviewerStream, interviewerInviteSummary,
				renderedMasterTemplate, interviewCancelledSubject);

		// For Candidate
		// Candidate Cancel Template
		if (candidateStream != null) {
			String renderedCandidateCancelTemplate = emailTemplateDataService.getRenderedTemplateForCancelInterview(
					interview.getPositionCode(), interview.getCandidateName(),
					GlobalConstants.CANDIDATE_INTERVIEW_CANCEL_TEMPLATE_NAME);
			renderedMasterTemplate = VelocityTemplateUtils.getTemplateString(renderedCandidateCancelTemplate,
					masterTemplateBody);

			emailList.clear();
			emailList.add(interview.getCandidateEmail());
			cancelInvite(interview.getNotes(), emailList, start, end, candidateStream, candidateInviteSummary,
					renderedMasterTemplate, interviewCancelledSubject);
		}
		scheduleService.delete(interview);

		// making entry to candidate activity
		candidateActivityService.interviewCancelledEvent(interview);
	}

	@SuppressWarnings("unchecked")
	void cancelInvite(String description, List<String> emailList, DateTime start, DateTime end, InputStream stream,
			String summaryType, String templateBody, String subject)
					throws IOException, ParserException, MessagingException, ParseException, RecruizException {

		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(stream);
		VEvent event = (VEvent) calendar.getComponent(VEvent.VEVENT);

		List<Attendee> attnedees = event.getProperties(Attendee.ATTENDEE);
		for (Attendee atnd : attnedees) {
			atnd.getParameters().add(PartStat.DECLINED);
		}
		// Organizer organizer = event.getOrganizer();

		Calendar cancelCalendar = new Calendar();
		cancelCalendar.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		cancelCalendar.getProperties().add(Version.VERSION_2_0);
		cancelCalendar.getProperties().add(Method.REQUEST);
		cancelCalendar.getProperties().add(Method.CANCEL);

		VEvent vevent = new VEvent(start, end, summaryType);
		// vevent.getProperties().add(new Uid());
		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description(description));
		vevent.getProperties().add(new Uid(event.getUid().getValue()));
		for (Attendee attende : attnedees) {
			vevent.getProperties().add(attende);
		}
		// vevent.getProperties().add(organizer);
		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().add(Status.VEVENT_CANCELLED);
		cancelCalendar.getComponents().add(vevent);

		File file = new File("iCalInvite.ics");
		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(cancelCalendar, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}
		if (null != emailList && !emailList.isEmpty()) {
			emailService.sendCalenderInvite(emailList, templateBody, subject, file.getAbsolutePath(),
					userService.getLoggedInUserEmail(), null, null);
			// after sending email add it to email repository
			emailActivityService.saveEmail(templateBody, subject, emailList, null, userService.getLoggedInUserEmail(),
					file.getAbsolutePath());
		}

	}

	/**
	 * for rescheduling event
	 * 
	 * @param interviewScheduleId
	 * @param attendeeList
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */

	@SuppressWarnings({ "unchecked", "unused" })
	public void rescheduleInterviewInvite(InterviewScheduleDTO scheduleDTO, long interviewScheduleId,
			Candidate candidate, Set<Feedback> expectedFeedbacks) throws RecruizException, Exception {

		InterviewSchedule interview = scheduleService.findOne(interviewScheduleId);
		Position position = positionService.getPositionByCode(interview.getPositionCode());

		Round round = roundService.findOne(Long.parseLong(scheduleDTO.getRoundId()));
		if (round == null)
			throw new RecruizWarnException(ErrorHandler.ROUND_NOT_PRESENT, ErrorHandler.ROUND_NOT_EXIST);

		Set<String> newEmailList = new HashSet<String>();

		// adding new event attendee
		for (String newAttendee : scheduleDTO.getInterviewerEmails()) {
			newEmailList.add(newAttendee);
		}

		// adding HR executive to new attendee list
		// newEmailList.add(userService.getLoggedInUserEmail());

		DateTime start = new DateTime(scheduleDTO.getStartTime().getTime());
		DateTime end = new DateTime(scheduleDTO.getEndTime().getTime());

		List<InterviewFile> icsFiles = interview.getFile();
		InterviewFile interviewerFile = null, candidateFile = null;
		for (InterviewFile interviewFile : icsFiles) {
			if (interviewFile.getFileType().equalsIgnoreCase(IcsFileType.Interviewer.getDisplayName()))
				interviewerFile = interviewFile;
			if (interviewFile.getFileType().equalsIgnoreCase(IcsFileType.Candidate.getDisplayName()))
				candidateFile = interviewFile;
		}

		// getting calendar from interviewer file stream
		InputStream stream = new ByteArrayInputStream(interviewerFile.getFile());
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(stream);
		VEvent event = (VEvent) calendar.getComponent(VEvent.VEVENT);

		VEvent candidateEvent = null;
		// getting calendar from candidate file stream
		if (!scheduleDTO.getIsCandidateInviteSkiped().equalsIgnoreCase("true")) {
			if (candidateFile.getFile() != null) {
				InputStream candidateFileStream = new ByteArrayInputStream(candidateFile.getFile());
				Calendar candidateCalendar = builder.build(candidateFileStream);
				candidateEvent = (VEvent) candidateCalendar.getComponent(VEvent.VEVENT);
			} else {
				String candidateEventId = generatedEventUid();

				File candidateNewFile = createCalenderEventFileForCandidate(scheduleDTO.getTemplateSubject(), start,
						end, candidate.getEmail(), candidate.getFullName(), scheduleDTO.getTemplateSubject(),
						candidateEventId, userService.getLoggedInUserName(), userService.getLoggedInUserEmail());
				InputStream candidateFileStream = new ByteArrayInputStream(
						Files.readAllBytes(Paths.get(candidateNewFile.getAbsolutePath())));
				Calendar candidateCalendar = builder.build(candidateFileStream);
				candidateEvent = (VEvent) candidateCalendar.getComponent(VEvent.VEVENT);
			}
		}

		Sequence sequence = event.getSequence();

		int seqVal = sequence.getSequenceNo();

		List<Attendee> existingAttenedees = event.getProperties(Attendee.ATTENDEE);

		Organizer organizer = event.getOrganizer();

		Calendar rescheduleCalender = new Calendar();
		rescheduleCalender.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		rescheduleCalender.getProperties().add(Version.VERSION_2_0);
		rescheduleCalender.getProperties().add(Method.PUBLISH);
		rescheduleCalender.getProperties().add(Method.REQUEST);

		String calenderEventTitle = scheduleDTO.getInterviewerTemplateSubject() + " Candidate Name : "
				+ candidate.getFullName() + " ,Candidate Mobile :"
				+ (candidate.getMobile() != null ? candidate.getMobile() : "N/A");

		VEvent vevent = new VEvent(start, end, calenderEventTitle);
		if (null != organizer) {
			vevent.getProperties().add(organizer);
		}

		// if someone is removed then a cancel event has to be sent to them so
		// adding them to removed attendee list
		Set<String> removedAttendee = new HashSet<String>();
		for (Attendee attende : existingAttenedees) {
			String existingAttendeeEmail = attende.getValue().replace("mailto:", "");
			if (!newEmailList.contains(existingAttendeeEmail)) {
				removedAttendee.add(existingAttendeeEmail);
			}
		}

		Set<EventAttendee> interviewers = new HashSet<EventAttendee>();
		for (String email : newEmailList) {
			String interviewerName = email;
			// fetching from generic interviewer if found then name will be set
			// otherwise in name email will go
			GenericInterviewer genericInterviewer = genericInterviewerService.getInterviewerByEmail(email);
			if (null != genericInterviewer) {
				interviewerName = genericInterviewer.getName();
			}
			EventAttendee interviewer = new EventAttendee();
			interviewer.setEmail(email);
			interviewer.setName(interviewerName);
			interviewers.add(interviewer);
		}

		setAttendeeToEvent(vevent, interviewers);

		// if any event attendee is removed then a cancel event will be sent to
		// them
		if (removedAttendee != null && !removedAttendee.isEmpty())
			removeAttendee(position, interview, removedAttendee, start, end, calendar);
		boolean scheduled = reschedule(position, scheduleDTO, interview, end, event, sequence, seqVal, null,
				rescheduleCalender, vevent, candidate, expectedFeedbacks, candidateEvent);
		if (scheduled) {

			// updating the schedule record here after rescheduling
			interview.setEndsAt(scheduleDTO.getEndTime());
			interview.setStartsAt(scheduleDTO.getStartTime());
			interview.setInterviewSchedulerEmail(userService.getLoggedInUserEmail());
			interview.setInterviewSchedulerName(userService.getLoggedInUserObject().getName());
			interview.setAttendee(interviewers);
			interview.getScheduleHrExecutives().add(userService.getLoggedInUserObject());
			interview.setNotes(scheduleDTO.getNotes());
			interview.setTemplateName(scheduleDTO.getTemplateName());
			interview.setTemplateSubject(scheduleDTO.getTemplateSubject());
			interview.setInterviewerTemplateData(scheduleDTO.getInterviewerEmailTemplate());
			interview.setInterviewerTemplateSubject(scheduleDTO.getInterviewerTemplateSubject());
			interview.setInterviewerTemplateName(scheduleDTO.getInterviewerTemplateName());
			interview.setRoundId(scheduleDTO.getRoundId());
			interview.setRoundName(round.getRoundName());
			interview.setRoundType(round.getRoundType());
			interview.setLevelbarFeedbackQueSetId(scheduleDTO.getFeedbackQueSetId());
			scheduleService.save(interview);

			// making entry to candidate activity
			candidateActivityService.rescheduleEvent(candidate, interview);
		}
	}

	private boolean reschedule(Position position, InterviewScheduleDTO scheduleDTO, InterviewSchedule interview,
			DateTime end, VEvent event, Sequence sequence, int seqVal, Organizer organizer, Calendar rescheduleCalender,
			VEvent vevent, Candidate candidate, Set<Feedback> expectedFeedbacks, VEvent candidateEvent)
					throws RecruizException, IOException, MessagingException, Exception {

		// creating temp jd file here
		File jdFile = null;
		if(scheduleDTO.getIsJDAttached()) {
			jdFile = createPositionTempJdFile(position);    
		}


		Map<String, Object> interviewerEmailBody = new HashMap<>();
		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description(
				"Interview Rescheduled for " + candidate.getFullName() + ", for " + interview.getPositionName()));
		vevent.getProperties().add(new Uid(event.getUid().getValue()));

		// vevent.getProperties().add(organizer);
		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().remove(sequence);
		vevent.getProperties().add(new Sequence(seqVal + 1));

		rescheduleCalender.getComponents().add(vevent);

		File file = new File("iCalInvite.ics");
		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;
		File candidateEventFile = null;

		if (candidateEvent != null)
			candidateEventFile = createCalenderEventFileForCandidate(scheduleDTO.getTemplateSubject(),
					new DateTime(scheduleDTO.getStartTime()), new DateTime(end), candidate.getEmail(),
					candidate.getFullName(), scheduleDTO.getTemplateSubject(), candidateEvent.getUid().getValue(),
					userService.getLoggedInUserName(), userService.getLoggedInUserEmail());

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (candidateEventFile.exists()) {
				candidateEventFile.delete();
			}
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(rescheduleCalender, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		List<String> candidateList = new ArrayList<String>();
		candidateList.add(interview.getCandidateEmail());

		List<String> newEmailList = new ArrayList<String>();

		Set<User> scheduleExistingHr = interview.getScheduleHrExecutives();
		// adding existing HR Executive to emailList
		for (User user : scheduleExistingHr) {
			newEmailList.add(user.getEmail());
		}

		for (String interviewerEmail : scheduleDTO.getInterviewerEmails()) {
			if (!newEmailList.contains(interviewerEmail)) {
				newEmailList.add(interviewerEmail);
			}

		}

		Round round = roundService.findOne(Long.parseLong(scheduleDTO.getRoundId()));

		if(scheduleDTO.getInterviewerTemplateName()!=null){

			for (String email : newEmailList) {
				if (scheduleDTO.getExpectFeedback() && expectedFeedbacks != null && !expectedFeedbacks.isEmpty()) {
					for (Feedback feedback : expectedFeedbacks) {
						if (feedback.getFeedbackBy().equalsIgnoreCase(email)) {

							createFeedbackLinkAndSendEmailToInterviewer(scheduleDTO, position, jdFile, round, candidate,
									file, email, feedback.getId(), feedback.getLevelbarFeedbackShareResultId(), null);

							/**
							 * 
							 * List<String> emailList = new ArrayList<String>();
							 * emailList.add(email);
							 * 
							 * List<String> candidateFeedbackIdMap = new
							 * ArrayList<>();
							 * candidateFeedbackIdMap.add(candidate.getCid() + ":" +
							 * feedback.getId());
							 * 
							 * interviewerEmailBody =
							 * getEmailBodyForInterviewer(position,
							 * Long.valueOf(interview.getRoundId()),
							 * DateTimeUtils.getDateTimeAsString(interview.getStartsAt()),
							 * email, Long.toString(candidate.getCid()),
							 * interviewerTemplate,
							 * StringUtils.commaSeparate(candidateFeedbackIdMap),
							 * maskedResume,
							 * feedback.getLevelbarFeedbackShareResultId());
							 * 
							 * String renderedMasterTemplate =
							 * emailTemplateDataService.getMasterTemplateWithButton(
							 * interviewerTemplate,
							 * interviewerEmailBody.get(GlobalConstants.NOTE_LINK).toString(),
							 * "View/Feedback");
							 * 
							 * // if resume is masked pass the masked resume file
							 * path String maskedResumeFilePath = ""; if
							 * (maskedResume.equalsIgnoreCase("y")) { // why list ?
							 * -> bcoz all candidate related files // belong // to
							 * one object called candidate files.
							 * List<CandidateFile> candidateMaskedResume =
							 * candidateFileService
							 * .getCandidateFileByTypeAndId(FileType.Masked_Resume_Converted.getDisplayName(),
							 * candidate.getCid() + ""); if (candidateMaskedResume
							 * != null && !candidateMaskedResume.isEmpty()) {
							 * maskedResumeFilePath =
							 * candidateMaskedResume.get(0).getFilePath(); } else {
							 * maskedResumeFilePath = ""; } } else {
							 * maskedResumeFilePath = candidate.getResumeLink(); }
							 * 
							 * emailService.sendCalenderInvite(emailList,
							 * renderedMasterTemplate, interviewerSubject,
							 * file.getAbsolutePath(), maskedResumeFilePath, jdFile,
							 * userService.getLoggedInUserEmail());
							 * 
							 * 
							 */
						}
					}
				} else if (null == scheduleDTO.getExpectFeedback() || !scheduleDTO.getExpectFeedback()) {
					createFeedbackLinkAndSendEmailToInterviewer(scheduleDTO, position, jdFile, round, candidate, file,
							email, -1L, null, null);
					// breaking the loop to send only one email if expect feeedback
					// is false
					break;

				} else {
					return false;
				}
			}
		}
		// sending to HR executive
		List<String> emailList = new ArrayList<String>();
		emailList.add(userService.getLoggedInUserEmail());

		// getting rendered template for HR Executive
		String renderedTemplateForHr = emailTemplateDataService.getRenderedTemplateForInterviewSchedule(
				GlobalConstants.HR_TEMPLATE, position.getPositionCode(), candidate.getEmail(),
				new HashSet<>(scheduleDTO.getInterviewerEmails()), interview.getRoundName(),
				scheduleDTO.getStartTime());
		Map<String, Object> masterTemplateBody = new HashMap<>();
		masterTemplateBody.put(GlobalConstants.MasterEmailTemplate, renderedTemplateForHr);

		// rendering to master template here
		String renderedMastertemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplateForHr);

		// sending email to HR executive (As discussed this should go only to
		// current loggen in HR)
		emailService.sendCalenderInvite(emailList, renderedMastertemplate, scheduleDTO.getInterviewerTemplateSubject(),
				file.getAbsolutePath(), userService.getLoggedInUserEmail(), null, null);

		// if candidate is marked to send interview it will be processed below
		if (!scheduleDTO.getIsCandidateInviteSkiped().equalsIgnoreCase("true")) {
			// initializing map object for candidate email body value
			Map<String, Object> candidateEmailBody = getEmailBodyForCandidate(position, interview.getStartsAt(),
					userService.getLoggedInUserEmail(), scheduleDTO.getNotes());

			// sending to candidate
			String rescheduleTemplate = scheduleDTO.getNotes();
			String renderedMasterTemplateCandidate = emailTemplateDataService
					.getMasterTemplateWithoutButton(rescheduleTemplate);

			emailService.sendCandidateCalenderInvite(candidateList, candidateEmailBody,
					scheduleDTO.getTemplateSubject(), renderedMasterTemplateCandidate,
					candidateEventFile.getAbsolutePath(), userService.getLoggedInUserEmail(), null);

			// adding to email repository
			try {
				emailActivityService.saveEmail(renderedMasterTemplateCandidate, scheduleDTO.getTemplateSubject(),
						candidateList, null, userService.getLoggedInUserEmail(), candidateEventFile.getAbsolutePath());
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}

			if (candidateEventFile.exists()) {
				candidateEventFile.delete();
			}
		}

		InterviewFile interviewersFile = new InterviewFile();
		interviewersFile.setFile(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		interviewersFile.setFileType("interviewer");
		interviewersFile.setSchedule(interview);
		List<InterviewFile> filesToRemove = new ArrayList<InterviewFile>();
		for (InterviewFile files : interview.getFile()) {
			if (files.getFileType().equalsIgnoreCase("interviewer"))
				filesToRemove.add(files);

		}
		interview.getFile().removeAll(filesToRemove);
		interview.getFile().add(interviewersFile);

		// deleting temp jd file here
		if (null != jdFile && jdFile.exists())
			jdFile.delete();

		return true;
	}

	/**
	 * removing the changed interview attendee from the list and db
	 * 
	 * @param position
	 * @param interview
	 * @param attendeeList
	 * @param start
	 * @param end
	 * @param cal
	 * @throws IOException
	 * @throws ParserException
	 * @throws MessagingException
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@SuppressWarnings("unchecked")
	private void removeAttendee(Position position, InterviewSchedule interview, Set<String> attendeeList,
			DateTime start, DateTime end, Calendar cal)
					throws IOException, ParserException, MessagingException, ParseException, RecruizException {
		Calendar calendar = cal;
		VEvent event = (VEvent) calendar.getComponent(VEvent.VEVENT);

		List<Attendee> attnedees = event.getProperties(Attendee.ATTENDEE);
		for (Attendee atnd : (List<Attendee>) event.getProperties(Attendee.ATTENDEE)) {
			boolean exists = false;
			for (String removerList : attendeeList) {
				if (atnd.getValue().contains(removerList)) {
					atnd.getParameters().add(PartStat.DECLINED);
					exists = true;
					break;
				} else {
					exists = false;
				}
			}
			if (!exists) {
				attnedees.remove(atnd);
			}
		}
		// Organizer organizer = event.getOrganizer();

		Calendar cancelCalendar = new Calendar();
		cancelCalendar.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
		cancelCalendar.getProperties().add(Version.VERSION_2_0);
		cancelCalendar.getProperties().add(Method.REQUEST);
		cancelCalendar.getProperties().add(Method.CANCEL);

		VEvent vevent = new VEvent(start, end, interviewerInviteSummary);
		vevent.getProperties().add(new Transp("OPAQUE"));
		vevent.getProperties().add(new DtEnd(end));
		vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
		vevent.getProperties().add(new Description("Interview Cancelled"));
		vevent.getProperties().add(new Uid(event.getUid().getValue()));
		for (Attendee attende : attnedees) {
			vevent.getProperties().add(attende);
		}
		// vevent.getProperties().add(organizer);
		vevent.getProperties().add(Priority.HIGH);
		vevent.getProperties().add(Status.VEVENT_CANCELLED);
		cancelCalendar.getComponents().add(vevent);

		File file = new File("iCalInvite.ics");
		file.createNewFile();
		String calFile = file.getPath();
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(calFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		try {
			outputter.output(cancelCalendar, fout);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		List<String> emailIds = new ArrayList<>();
		emailIds.addAll(attendeeList);

		// Master Template
		Map<String, Object> masterTemplateBody = new HashMap<String, Object>();
		masterTemplateBody.put(GlobalConstants.MasterEmailTemplateLogo, logoPath);

		// For Interviewers
		// Interviewer Cancel Template
		String renderedInterviewerCancelTemplate = emailTemplateDataService.getRenderedTemplateForCancelInterview(
				interview.getPositionCode(), interview.getCandidateName(),
				GlobalConstants.INTERVIEWER_INTERVIEW_CANCEL_TEMPLATE_NAME);
		masterTemplateBody.put(GlobalConstants.MasterEmailTemplate, renderedInterviewerCancelTemplate);
		String renderedMasterTemplate = emailTemplateDataService
				.getMasterTemplateWithoutButton(renderedInterviewerCancelTemplate);

		emailService.sendCalenderInvite(emailIds, renderedMasterTemplate, interviewCancelledSubject,
				file.getAbsolutePath(), userService.getLoggedInUserEmail(), null, null);
	}
	
	 
    private List<String> addSelectedFilesWithAttachment(String selectedFiles, long cid) {
  		
    	List<String> filePath = new ArrayList<>();
    	
    	String[] fileName = selectedFiles.split(",");
    	Set<String> uniqueWords = new HashSet<String>(Arrays.asList(fileName));
    	
    	for (String name : uniqueWords) {
			
    		if(name.equalsIgnoreCase("offer.pdf")){
    			 List<OfferLetterForCandidate> list= offerLetterForCandidateRepository.getListOfOfferLetterByCandidateId(cid);
    			 if(list.get(0).getFinalOfferLetterPath()!=null){
    				 filePath.add(list.get(0).getFinalOfferLetterPath());
    			 }
    		}
    		
    		List<CandidateFile> candidateFile = candidateFileRepository.findByCandidateIdAndFileName(String.valueOf(cid).trim(),name.trim());
    		
    		if(candidateFile.size()>0){
    			filePath.add(candidateFile.get(0).getFilePath());
    		}
		}
    	
    	return filePath;
    	
  	}
	
}
