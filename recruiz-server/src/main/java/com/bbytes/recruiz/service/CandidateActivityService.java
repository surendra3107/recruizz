package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateActivity;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateActivityRepository;
import com.bbytes.recruiz.rest.dto.models.CandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.FeedbackDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class CandidateActivityService extends AbstractService<CandidateActivity, Long> {

	private static Logger logger = LoggerFactory.getLogger(CandidateActivityService.class);

	private CandidateActivityRepository candidateActivityRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private PositionActivityService positionActivityService;

	@Autowired
	public CandidateActivityService(CandidateActivityRepository candidateActivityRepository) {
		super(candidateActivityRepository);
		this.candidateActivityRepository = candidateActivityRepository;
	}

	
	public void addActivity(String what, String who, String candidateId, String type) {
		CandidateActivity newActivity = new CandidateActivity();
		newActivity.setWhat(what);
		newActivity.setWho(who);
		newActivity.setCandidateId(candidateId);
		newActivity.setType(type);
		if (type.equalsIgnoreCase(CandidateActivityType.Added.getDisplayName())
				&& candidateActivityRepository.findByTypeAndCandidateId(type, candidateId) != null
				&& !candidateActivityRepository.findByTypeAndCandidateId(type, candidateId).isEmpty()) {
			return;
		}
		save(newActivity);
	}

	
	public void addActivityForApproval(String what, String who, String candidateId, String type,String approvalId) {
		CandidateActivity newActivity = new CandidateActivity();
		newActivity.setWhat(what);
		newActivity.setWho(who);
		newActivity.setCandidateId(candidateId);
		newActivity.setType(type);
		if (type.equalsIgnoreCase(CandidateActivityType.Added.getDisplayName())
				&& candidateActivityRepository.findByTypeAndCandidateId(type, candidateId) != null
				&& !candidateActivityRepository.findByTypeAndCandidateId(type, candidateId).isEmpty()) {
			return;
		}
		newActivity.setOfferApprovalId(approvalId);
		save(newActivity);
	}
	
	
	/**
	 * to get list of all candidate activity by candidate id
	 * 
	 * @param candidateId
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<CandidateActivity> getCandidateActivityById(String candidateId, Pageable pageable) {
		return candidateActivityRepository.findByCandidateIdOrderByWhatTimeDesc(candidateId, pageable);
	}

	@Transactional
	public void rescheduleEvent(Candidate candidate, InterviewSchedule interview) throws RecruizException {
		String message = "Interview rescheduled for round : " + interview.getRoundName() + " for position : $@"
				+ positionService.getPositionByCode(interview.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(interview.getPositionCode()).getClient().getClientName() + "$#"
				+ positionService.getPositionByCode(interview.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(interview.getPositionCode()).getTitle() + "$#";
		
		if(interview.getNotes()!=null && !interview.getNotes().isEmpty()){
			message = message+"<br><br>"+interview.getNotes();
		}
		addActivity(message,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidate.getCid() + "", CandidateActivityType.InterviewReScheduled.getDisplayName());

		// make entry to notification table on interview re-schedule
		interviewScheduleEventNotification(interview, candidate, userService.getLoggedInUserObject().getName(),
				userService.getLoggedInUserEmail(),
				notificationService.getMessageForInterviewReScheduled(
						positionService.getPositionByCode(interview.getPositionCode()).getId(), candidate.getFullName(),
						interview.getRoundName()),
				CandidateActivityType.InterviewReScheduled.getDisplayName());

		// making entry to position activity
		PositionActivity positionActivity;
		try {
			positionActivity = new PositionActivity(userService.getLoggedInUserEmail(),
					userService.getLoggedInUserName(), CandidateActivityType.InterviewReScheduled.getDisplayName(),
					notificationService.getMessageForInterviewReScheduled(
							positionService.getPositionByCode(interview.getPositionCode()).getId(),
							candidate.getFullName(), interview.getRoundName()),
					new Date(), interview.getPositionCode(),positionService.getPositionByCode(interview.getPositionCode()).getTeam());
			positionActivityService.addActivity(positionActivity);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Transactional
	public void interviewScheduleEvent(Position position, Round round, Candidate candidate, InterviewScheduleDTO scheduleDTO) {
		String message = "Interview Scheduled for round : " + round.getRoundName() + " for position : $@"
				+ position.getClient().getId() + "_" + position.getClient().getClientName() + " $#" + position.getId()
				+ "_" + position.getTitle() + "$#";
		
		if(scheduleDTO.getNotes()!=null && !scheduleDTO.getNotes().isEmpty()){
			message = message+"<br><br>"+scheduleDTO.getInterviewerEmailTemplate();
		}
		
		/*if(scheduleDTO.get!=null && !scheduleDTO.getInterviewerEmailTemplate().isEmpty()){
			message = message+"<br><br>"+scheduleDTO.getInterviewerEmailTemplate();
		}*/
		
		addActivity(message,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidate.getCid() + "", CandidateActivityType.InterviewScheduled.getDisplayName());

		try {
			interviewScheduleEventNotification(
					interviewScheduleService.getScheduleByPositionCodeRoundEmail(position.getPositionCode(),
							round.getId() + "", candidate.getEmail()),
					candidate, userService.getLoggedInUserObject().getName(), userService.getLoggedInUserEmail(),
					notificationService.getMessageForInterviewScheduled(position.getId(), candidate.getFullName(),
							round.getRoundName()),
					CandidateActivityType.InterviewScheduled.getDisplayName());

			// making entry to position activity
			PositionActivity positionActivity;
			try {
				positionActivity = new PositionActivity(userService.getLoggedInUserEmail(),
						userService.getLoggedInUserName(), CandidateActivityType.InterviewScheduled.getDisplayName(),
						notificationService.getMessageForInterviewScheduled(position.getId(), candidate.getFullName(),
								round.getRoundName()),
						new Date(), position.getPositionCode(),position.getTeam());
				positionActivityService.addActivity(positionActivity);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Transactional
	public void interviewCancelledEvent(InterviewSchedule interview) throws RecruizException {
		Candidate candidate = candidateService.getCandidateByEmail(interview.getCandidateEmail());
		String message = "Interview cancel for round : " + interview.getRoundName() + " for position : $@"
				+ positionService.getPositionByCode(interview.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(interview.getPositionCode()).getClient().getClientName() + "$#"
				+ positionService.getPositionByCode(interview.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(interview.getPositionCode()).getTitle() + "$#";

		addActivity(message,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidate.getCid() + "", CandidateActivityType.InterviewCancelled.getDisplayName());

		interviewScheduleEventNotification(interview, candidate, userService.getLoggedInUserObject().getName(),
				userService.getLoggedInUserEmail(),
				notificationService.getMessageForInterviewCancelled(
						positionService.getPositionByCode(interview.getPositionCode()).getId(), candidate.getFullName(),
						interview.getRoundName()),
				CandidateActivityType.InterviewCancelled.getDisplayName());

		// making entry to position activity
		PositionActivity positionActivity;
		try {
			positionActivity = new PositionActivity(userService.getLoggedInUserEmail(),
					userService.getLoggedInUserName(), CandidateActivityType.InterviewCancelled.getDisplayName(),
					notificationService.getMessageForInterviewCancelled(
							positionService.getPositionByCode(interview.getPositionCode()).getId(),
							candidate.getFullName(), interview.getRoundName()),
					new Date(), interview.getPositionCode(),positionService.getPositionByCode(interview.getPositionCode()).getTeam());
			positionActivityService.addActivity(positionActivity);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Transactional
	public CandidateActivity addedToBoardEvent(Position position, Candidate candidate) throws RecruizException {
		CandidateActivity candidateActivity;

		try {
			String message = "Added to board for position : $@"
					+ positionService.getPositionByCode(position.getPositionCode()).getClient().getId() + "_"
					+ positionService.getPositionByCode(position.getPositionCode()).getClient().getClientName() + " $#"
					+ positionService.getPositionByCode(position.getPositionCode()).getId() + "_"
					+ positionService.getPositionByCode(position.getPositionCode()).getTitle() + "$# by "
					+ userService.getLoggedInUserObject().getName() + " (" + userService.getLoggedInUserEmail() + " )";

			candidateActivity = new CandidateActivity(
					userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
					message, candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());

			// candidate added to board add in notification
			RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate,
					position.getPositionCode());
			candidateBoardNotification(roundCandidate, userService.getLoggedInUserObject().getName(),
					userService.getLoggedInUserEmail(),
					notificationService.getMessageForSourcedToBoard(
							positionService.getPositionByCode(roundCandidate.getPositionCode()).getId(),
							candidate.getFullName()),
					CandidateActivityType.SourcedToBoard.getDisplayName(), 0);

			return candidateActivity;
		} catch (Exception ex) {
			logger.info(ex.getMessage(), ex);
			return null;
		}

	}

	@Transactional
	public void movedToRoundEvent(Round destRound, Round sourceRound, Candidate candidate,
			RoundCandidate existingCandidate) throws RecruizException {

		String message = "Moved to round : " + destRound.getRoundName() + " from round " + sourceRound.getRoundName()
				+ " for position : $@"
				+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getClient().getClientName()
				+ " $#" + positionService.getPositionByCode(existingCandidate.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getTitle() + "$#";

		CandidateActivity newActivity = new CandidateActivity(
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")", message,
				candidate.getCid() + "", CandidateActivityType.MovedInBoard.getDisplayName());
		save(newActivity);

		// on candidate moved add it to notification table
		candidateBoardNotification(existingCandidate, userService.getLoggedInUserObject().getName(),
				userService.getLoggedInUserEmail(),
				notificationService.getMessageForCandidateMoveInBoard(
						positionService.getPositionByCode(existingCandidate.getPositionCode()).getId(),
						candidate.getFullName(), sourceRound.getRoundName(), destRound.getRoundName()),
				CandidateActivityType.MovedInBoard.getDisplayName(), destRound.getId());
	}

	@Transactional
	public void boardStatusChangedEvent(CandidateToRoundDTO roundCandidateDTO, Candidate candidate,
			RoundCandidate existingCandidate, String oldStatus) throws RecruizException {
		CandidateActivity newActivity = new CandidateActivity(
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				"Changed status to : " + roundCandidateDTO.getStatus() + " for position : $@"
						+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getClient().getId()
						+ "_"
						+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getClient()
								.getClientName()
						+ " $#" + positionService.getPositionByCode(existingCandidate.getPositionCode()).getId() + "_"
						+ positionService.getPositionByCode(existingCandidate.getPositionCode()).getTitle() + "$#",
				candidate.getCid() + "", CandidateActivityType.StatusChanged.getDisplayName());
		save(newActivity);

		// making entry in notification table
		candidateBoardNotification(existingCandidate, userService.getLoggedInUserObject().getName(),
				userService.getLoggedInUserEmail(),
				notificationService.getMessageForCandidateStatusChangedInBoard(
						positionService.getPositionByCode(existingCandidate.getPositionCode()).getId(),
						candidate.getFullName(), oldStatus, roundCandidateDTO.getStatus()),
				CandidateActivityType.PipelineStatusChanged.getDisplayName(), existingCandidate.getRound().getId());
	}

	@Transactional
	public void removedFromBoard(CandidateToRoundDTO roundCandidateDTO, Candidate candidate) throws RecruizException {
		CandidateActivity newActivity = new CandidateActivity(
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				"Deleted from board for position : $@"
						+ positionService.getPositionByCode(roundCandidateDTO.getPositionCode()).getClient().getId()
						+ "_"
						+ positionService.getPositionByCode(roundCandidateDTO.getPositionCode()).getClient()
								.getClientName()
						+ " $#" + positionService.getPositionByCode(roundCandidateDTO.getPositionCode()).getId() + "_"
						+ positionService.getPositionByCode(roundCandidateDTO.getPositionCode()).getTitle() + "$#",
				candidate.getCid() + "", CandidateActivityType.RemovedFromBoard.getDisplayName());
		save(newActivity);
	}

	@Transactional
	public void feedbackRecievedEvent(FeedbackDTO feedbackDTO, RoundCandidate roundCandidate,
			ExternalUserAccessDataHolder externalUser) throws RecruizException {
		String message = feedbackDTO.getFeedbackType() + " feedback recieved in position : $@"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getClientName()
				+ " $#" + positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle() + "$#"
				+ "\n\nFeedback : \n" + feedbackDTO.getFeedback();

		addActivity(message, externalUser.getExtenalUserEmail() + "(" + externalUser.getExtenalUserName() + ")",
				roundCandidate.getCandidate().getCid() + "", CandidateActivityType.FeedbackRecieved.getDisplayName());

		// on feedback received add data to notification table
		candidateBoardNotification(roundCandidate, externalUser.getExtenalUserName(),
				externalUser.getExtenalUserEmail(),
				notificationService.getMessageForFeedbackRecieved(
						positionService.getPositionByCode(roundCandidate.getPositionCode()).getId(),
						roundCandidate.getCandidate().getFullName()),
				CandidateActivityType.FeedbackRecieved.getDisplayName(), Long.parseLong(feedbackDTO.getRoundId()));

	}

	@Transactional
	public void levelbarFeedbackRecievedEvent(String roundId, RoundCandidate roundCandidate,
			ExternalUserAccessDataHolder externalUser) throws RecruizException {
		String message = "Interview feedback recieved in position : $@"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getClientName()
				+ " $#" + positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle() + "$#";

		addActivity(message, externalUser.getExtenalUserEmail() + "(" + externalUser.getExtenalUserName() + ")",
				roundCandidate.getCandidate().getCid() + "", CandidateActivityType.FeedbackRecieved.getDisplayName());

		// on feedback received add data to notification table
		candidateBoardNotification(roundCandidate, externalUser.getExtenalUserName(),
				externalUser.getExtenalUserEmail(),
				notificationService.getMessageForFeedbackRecieved(
						positionService.getPositionByCode(roundCandidate.getPositionCode()).getId(),
						roundCandidate.getCandidate().getFullName()),
				CandidateActivityType.FeedbackRecieved.getDisplayName(), Long.parseLong(roundId));
	}

	@Transactional
	public void profileForwardEvent(CandidateProfileDTO candidateProfileDTO, Position position, String renderedTemplate,
			String candidateId) {
		addActivity("Profile forwarded to : " + StringUtils.commaSeparate(candidateProfileDTO.getInterviewerEmails())
				+ " for position : $@" + position.getClient().getId() + "_" + position.getClient().getClientName()
				+ " $#" + position.getId() + "_" + position.getTitle() + "$#" + "\n\n" + renderedTemplate,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidateId, CandidateActivityType.Forwarded.getDisplayName());
	}

	@Transactional
	public void addedToBoardEvent(Candidate candidate, RoundCandidate roundCandidate) throws RecruizException {
		try {
			User logedInUser = null;
			String sourceName = candidate.getSource();
			String sourceEmail = candidate.getSourceDetails();
			try {
				logedInUser = userService.getLoggedInUserObject();
			} catch (Exception ex) {
			}
			if (logedInUser != null) {
				sourceName = logedInUser.getName();
				sourceEmail = logedInUser.getEmail();
			}

			String message = "Added to board for position : $@"
					+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId() + "_"
					+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getClientName()
					+ " $#" + positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
					+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle() + "$#";

			addActivity(message, sourceEmail + "(" + sourceName + ")", candidate.getCid() + "",
					CandidateActivityType.SourcedToBoard.getDisplayName());

			// candidate added to board add in notification
			candidateBoardNotification(roundCandidate, userService.getLoggedInUserObject().getName(),
					userService.getLoggedInUserEmail(),
					notificationService.getMessageForSourcedToBoard(
							positionService.getPositionByCode(roundCandidate.getPositionCode()).getId(),
							candidate.getFullName()),
					CandidateActivityType.SourcedToBoard.getDisplayName(), 0);

		} catch (Exception ex) {

		}
	}

	@Transactional
	public void statusChangedEvent(String status, Candidate candidate) {
		addActivity("Candidate status modifed to : " + status,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidate.getCid() + "", CandidateActivityType.StatusChanged.getDisplayName());
	}

	
	public void detailsUpdated(Candidate candidateFromDB) {
		addActivity("Candidate details modified",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				candidateFromDB.getCid() + "", CandidateActivityType.DetailsUpdated.getDisplayName());
	}

	@Transactional
	public void emailEvent(EmailActivity emailActivity, String roundCandidateID) throws RecruizException {
		RoundCandidate roundCandidate = roundCandidateService.findOne(Long.parseLong(roundCandidateID));
		addActivity(
				"Email sent in round :" + roundService.getRoundName(roundCandidate.getRoundId()) + " for position : $@"
						+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId() + "_"
						+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
								.getClientName()
						+ " $#" + positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
						+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle() + "$#" + "\n\n"
						+ emailActivity.getBody(),
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				roundCandidate.getCandidate().getCid() + "", CandidateActivityType.Email.getDisplayName());
	}

	/**
	 * add to candidate activity and to notifications also
	 * 
	 * @param candidate
	 * @param roundCandidate
	 * @throws RecruizException
	 */
	@Transactional
	public void addedToBoardEventExternaluser(Candidate candidate, RoundCandidate roundCandidate)
			throws RecruizException {
		String sourceName = candidate.getSource();
		String sourceEmail = candidate.getSourceDetails();

		String message = "Added to board for position : $@"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getClientName()
				+ " $#" + positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
				+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle() + "$#";

		addActivity(message, sourceEmail + "(" + sourceName + ")", candidate.getCid() + "",
				CandidateActivityType.SourcedToBoard.getDisplayName());

		// candidate added to board from external source add in notification
		candidateBoardNotification(roundCandidate, sourceName, sourceEmail,
				notificationService.getMessageForSourcedToBoard(
						positionService.getPositionByCode(roundCandidate.getPositionCode()).getId(),
						candidate.getFullName()),
				CandidateActivityType.SourcedToBoard.getDisplayName(), 0);
	}

	/**
	 * to add notification to when candidate in a board is changed
	 * 
	 * @param roundCandidate
	 * @param sourceName
	 * @param sourceEmail
	 * @throws RecruizException
	 */
	@Transactional
	private void candidateBoardNotification(RoundCandidate roundCandidate, String sourceName, String sourceEmail,
			String message, String activityType, long roundId) throws RecruizException {
		Set<User> hrExecutives = positionService.getPositionByCode(roundCandidate.getPositionCode()).getHrExecutives();

		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			List<Notification> addToBoardNotifications = new ArrayList<>();
			for (User user : hrExecutives) {
				if (userService.getLoggedInUserObject() != null) {
					if (!user.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
						Notification notification = new Notification(user.getEmail(), sourceEmail, sourceName,
								activityType, message, new Date(),
								positionService.getPositionByCode(roundCandidate.getPositionCode()).getPositionCode(),
								positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId(),
								roundCandidate.getCandidate().getCid(), 0, null, roundCandidate.getId(), roundId);
						addToBoardNotifications.add(notification);
					}
				} else {
					Notification notification = new Notification(user.getEmail(), sourceEmail, sourceName, activityType,
							message, new Date(),
							positionService.getPositionByCode(roundCandidate.getPositionCode()).getPositionCode(),
							positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient().getId(),
							roundCandidate.getCandidate().getCid(), 0, null, roundCandidate.getId(), roundId);
					addToBoardNotifications.add(notification);
				}

			}
			// saving to db here
			if (addToBoardNotifications != null && !addToBoardNotifications.isEmpty()) {
				notificationService.save(addToBoardNotifications);
			}

		}
	}

	/**
	 * For emails sent to candidate using bulk
	 * 
	 * @param emailActivity
	 * @param candidateEmailList
	 */
	@Transactional
	public void emailEvent(EmailActivity emailActivity, List<String> candidateEmailList) {
		for (String email : candidateEmailList) {
			String candidateId;
			try {
				Candidate candidate = candidateService.getCandidateByEmail(email);
				if (candidate == null) {
					return;
				}
				candidateId = candidate.getCid() + "";
				if (candidateId == null || candidateId.isEmpty()) {
					return;
				}
				addActivity(
						"<b>Email Sent</b> <br/>" + "Subject : " + emailActivity.getSubject()
								+ "<br/><b>Email Content<b><br/>" + emailActivity.getBody(),
						userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
						candidateId, CandidateActivityType.Email.getDisplayName());
			} catch (RecruizException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param schedule
	 * @param candidate
	 * @param sourceName
	 * @param sourceEmail
	 * @param message
	 * @param activityType
	 * @throws RecruizException
	 */
	@Transactional
	private void interviewScheduleEventNotification(InterviewSchedule schedule, Candidate candidate, String sourceName,
			String sourceEmail, String message, String activityType) throws RecruizException {
		Set<User> hrExecutives = schedule.getScheduleHrExecutives();

		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			List<Notification> addToBoardNotifications = new ArrayList<>();
			for (User user : hrExecutives) {
				if (!user.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
					Notification notification = new Notification(user.getEmail(), sourceEmail, sourceName, activityType,
							message, new Date(), schedule.getPositionCode(),
							positionService.getPositionByCode(schedule.getPositionCode()).getClient().getId(),
							candidate.getCid(), 0, null, 0, Long.parseLong(schedule.getRoundId()), schedule.getId());
					addToBoardNotifications.add(notification);
				}
			}
			// saving to db here
			notificationService.save(addToBoardNotifications);
		}
	}

	@Transactional
	public void deleteByCandidateId(long cid) {
		List<CandidateActivity> activity = candidateActivityRepository.findByCandidateId(cid + "");
		candidateActivityRepository.delete(activity);
	}


	public void addActivityForCallLog(String what, String who, String candidateId, String type,String callId,String ivr) {
		CandidateActivity newActivity = new CandidateActivity();
		newActivity.setWhat(what);
		newActivity.setWho(who);
		newActivity.setCandidateId(candidateId);
		newActivity.setType(type);
		if (type.equalsIgnoreCase(CandidateActivityType.Added.getDisplayName())
				&& candidateActivityRepository.findByTypeAndCandidateId(type, candidateId) != null
				&& !candidateActivityRepository.findByTypeAndCandidateId(type, candidateId).isEmpty()) {
			return;
		}
		newActivity.setKnowlarityCallDetailId(callId);
		newActivity.setIvr_integration(ivr);
		save(newActivity);
		
	}
}
