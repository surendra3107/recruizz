package com.bbytes.recruiz.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.CustomRounds;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.PositionOfferCost;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.DefaultRounds;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.PositionCandidateDataRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.rest.dto.models.PositionClientNameDTO;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.rest.dto.models.PositionOfferCostDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ActivityMessageConstants;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.common.base.Joiner;

/**
 * @author sourav-bb
 *
 */
@Service
public class PositionService extends AbstractService<Position, Long> {
	private static Logger logger = LoggerFactory.getLogger(PositionService.class);

	private PositionRepository positionRepository;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private PositionRequestService positionRequestService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private VelocityEngine templateEngine;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private PositionActivityService positionActivityService;

	@Autowired
	private ClientActivityService clientActivityService;

	@Autowired
	private GenericInterviewerService genericInterviewerService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private PositionOfferCostService positionOfferCostService;

	@Autowired
	private CustomRoundService customRoundService;

	@Value("${recruiz.fb.app.id}")
	private String fbAppId;

	@Value("${recruiz.twitter.username}")
	private String twitterUsername;

	@Value("${base.url}")
	private String baseUrl;

	@Autowired
	public PositionService(PositionRepository positionRepository) {
		super(positionRepository);
		this.positionRepository = positionRepository;
	}

	/**
	 * <code>addPosition</code> method creates new position object with client.
	 *
	 * @param newPosition
	 * @throws RecruizException
	 */
	@Transactional
	public void addPosition(Position newPosition) throws RecruizException {

		if (!checkPermissionService.hasAddEditPositionPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		addPositionToDB(newPosition, userService.getLoggedInUserEmail());
	}

	@Transactional
	public Position addPositionToDB(Position newPosition, String loggedInUserEmail) throws RecruizException {
		/**
		 * creating Board for the position
		 */
		Board board = new Board();
		board.setStatus(true);
		// board.setPosition(position);

		/**
		 * Creating default rounds for the position which will be add to the
		 * board
		 */

		// creating default round
		int roundStartOrder = 1;
		for (DefaultRounds round : DefaultRounds.values()) {
			Round roundOne = new Round();
			if (round.getDisplayName().equalsIgnoreCase("Sourcing")) {
				roundOne.setRoundType(GlobalConstants.ROUND_DEFAULT_TYPE_SOURCE);
			}
			roundOne.setRoundName(round.getDisplayName());
			roundOne.setOrderNo(roundStartOrder);
			roundOne.setBoard(board);
			board.getRounds().add(roundOne);
			roundStartOrder++;
		}

		// add custom rounds here given in admin setting
		LinkedList<CustomRounds> customRounds = customRoundService.getAllRounds();
		if (null != customRounds && !customRounds.isEmpty()) {
			roundStartOrder = roundStartOrder - 1;
			for (CustomRounds customRound : customRounds) {
				Round round = new Round();
				round.setRoundName(customRound.getName());
				round.setOrderNo(roundStartOrder + customRound.getOrderNo());
				round.setBoard(board);
				board.getRounds().add(round);
			}
		}

		newPosition.setBoard(board);
		newPosition.setOwner(loggedInUserEmail);
		// as per new requirement adding logged in user as HR executive
		newPosition.getHrExecutives().add(userService.getUserByEmail(loggedInUserEmail));
		// saving the position to DB
		save(newPosition);

		Set<User> newHRs = newPosition.getHrExecutives();
		List<String> emailList = new ArrayList<>();

		String userName = "App User";
		try {
			userName = userService.getLoggedInUserObject().getName();
		} catch (Exception ex) {

		}

		if (newHRs != null && !newHRs.isEmpty()) {
			for (User hr : newHRs) {
				if (!hr.getEmail().equalsIgnoreCase(loggedInUserEmail)) {
					emailList.add(hr.getEmail());

					notificationService.sendNotification(new Notification(hr.getEmail(), userService.getLoggedInUserEmail(), userName,
							NotificationEvent.POSITION_CREATED.getDisplayName(),
							notificationService.getMessageForPositionAdded(newPosition.getId()), new Date(), newPosition.getPositionCode(),
							newPosition.getClient().getId(), 0, 0, null));
				}
			}
		}

		// send email to HR executive and vendors here
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_CREATED_HR;
		String emailSubject = "New Position '" + newPosition.getTitle() + "' created.";
		sendEmailToHR_Vendor_OnCreate_StatusChange(newPosition, emailTemplate, emailSubject);

		return newPosition;
	}

	@Transactional
	public Position addPositionFromRecruizConnect(Position newPosition, String loggedInUserEmail, List<Round> rounds)
			throws RecruizException {

		Board board = new Board();
		board.setStatus(true);

		/**
		 * Creating rounds for the position which will be add to the board from
		 * recruiz connect
		 */
		for (Round round : rounds) {
			round.setBoard(board);
			board.getRounds().add(round);
		}

		newPosition.setBoard(board);
		newPosition.setOwner(loggedInUserEmail);
		// as per new requirement adding logged in user as HR executive
		newPosition.getHrExecutives().add(userService.getUserByEmail(loggedInUserEmail));
		// saving the position to DB
		save(newPosition);

		Set<User> newHRs = newPosition.getHrExecutives();
		List<String> emailList = new ArrayList<>();

		String userName = "App User";
		try {
			userName = userService.getLoggedInUserObject().getName();
		} catch (Exception ex) {

		}

		if (newHRs != null && !newHRs.isEmpty()) {
			for (User hr : newHRs) {
				if (!hr.getEmail().equalsIgnoreCase(loggedInUserEmail)) {
					emailList.add(hr.getEmail());

					notificationService.sendNotification(new Notification(hr.getEmail(), userService.getLoggedInUserEmail(), userName,
							NotificationEvent.POSITION_CREATED.getDisplayName(),
							notificationService.getMessageForPositionAdded(newPosition.getId()), new Date(), newPosition.getPositionCode(),
							newPosition.getClient().getId(), 0, 0, null));
				}
			}
		}

		// send email to HR executive and vendors here
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_CREATED_HR;
		String emailSubject = "New Position '" + newPosition.getTitle() + "' created.";
		sendEmailToHR_Vendor_OnCreate_StatusChange(newPosition, emailTemplate, emailSubject);

		return newPosition;
	}

	/**
	 * To send email to HR and vendors when a position is created or status of
	 * the position is changed
	 *
	 * @param newPosition
	 * @param emailTemplate
	 * @param emailSubject
	 * @throws RecruizException
	 */
	public void sendEmailToHR_Vendor_OnCreate_StatusChange(Position newPosition, final String emailTemplate, String emailSubject)
			throws RecruizException {

		List<String> emailList = new ArrayList<>();
		List<String> vendorUserEmail = new ArrayList<>();

		// get all HR executive here
		Set<User> hrExecutives = newPosition.getHrExecutives();
		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			for (User user : hrExecutives) {
				emailList.add(user.getEmail());
			}
		}

		// getting all the vendor users associated with this
		Set<Vendor> positionVendors = newPosition.getVendors();
		if (positionVendors != null && !positionVendors.isEmpty()) {
			for (Vendor vendor : positionVendors) {
				List<User> users = userService.getAllUserByVendor(vendor.getId() + "");
				if (users != null && !users.isEmpty()) {
					for (User user : users) {
						vendorUserEmail.add(user.getEmail());
					}
				}
			}
		}

		Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(newPosition);
		String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
		String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + newPosition.getId();
		String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
		emailService.sendEmail(emailList, masterRenderedTemplate, emailSubject, true);

		// sending email to vendor users
		// if decided to use different template for vendors then has to be done
		// here
		if (vendorUserEmail != null && !vendorUserEmail.isEmpty()) {
			emailService.sendEmail(vendorUserEmail, masterRenderedTemplate, emailSubject, true);
		}
	}

	@Transactional(readOnly = true)
	public boolean isPositionExists(String positionCode) {
		boolean state = positionRepository.findOneByPositionCode(positionCode) == null ? false : true;
		return state;
	}

	@Transactional(readOnly = true)
	public boolean isPositionExists(long positionId) {
		boolean state = positionRepository.findOne(positionId) == null ? false : true;
		return state;
	}

	
	public Position getOneByPositionCode(String positionCode) {
		Position position = positionRepository.findOneByPositionCode(positionCode);
		return position;
	}


	public Position getPositionByCode(String positionCode) throws RecruizException {
		Position position = getOneByPositionCode(positionCode);
		calculateFinalStatusForPosition(position);
		return position;
	}
	
	// Added by Sajin
	public Position getPositionById(Long pid) throws RecruizException {
		Position position = getOneByPositionID(pid);
		calculateFinalStatusForPosition(position);
		return position;
	}
	
	// Added by Sajin
	public Position getPositionById(BigInteger pid) throws RecruizException {
		Position position = getOneByPositionID(pid);
		calculateFinalStatusForPosition(position);
		return position;
	}
	
	// Added by Sajin
	public Position getOneByPositionID(Long pid) {
		Position position = positionRepository.findOneById(pid);
		return position;
	}
	
	// Added by Sajin
	public Position getOneByPositionID(BigInteger pid) {
		Position position = positionRepository.findOneById(pid);
		return position;
	}

	
	public Position getPositionByImportIdentifier(String importIdentifier) throws RecruizException {
		Position position = positionRepository.findOneByImportIdentifier(importIdentifier);
		calculateFinalStatusForPosition(position);
		return position;
	}


	public Position updatePosition(String positionId, PositionDTO positionDTO, String clientName) throws RecruizException {

		Long oldTeamId = null;

		Position existingPosition = positionRepository.findOne(Long.valueOf(positionId));

		if (existingPosition.getTeam() != null) {
			oldTeamId = existingPosition.getTeam().getId();
		}

		Set<User> removedHr = existingPosition.getHrExecutives();
//		removedHr = existingPosition.getHrExecutives();

		Set<User> newHRs = new HashSet<User>();
		List<String> newHrVendorEmailIds = new ArrayList<>();

		boolean updatable = false;
		if (checkPermissionService.isSuperAdmin()) {
			updatable = true;
		} else if (existingPosition.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				|| checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditPositionPermission())
			throw new RecruizException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		// if user don't have permission to edit position then throwing a
		// exception here
		if (!updatable) {
			throw new RecruizException(ErrorHandler.NO_GLOBAL_EDIT_PERMISSION, ErrorHandler.NO_OWNERSHIP);
		}

		List<String> hrId = positionDTO.getHrExexutivesId();
		if (null == hrId || hrId.isEmpty() || !hrId.contains(userService.getLoggedInUserObject().getUserId() + "")) {
			hrId.add(userService.getLoggedInUserObject().getUserId() + "");
		}

	
		// getting list of HRExecutives
		Set<User> hrList = new LinkedHashSet<User>();
		if (hrId != null && !(hrId.isEmpty())) {
			for (String id : hrId) {
				User hr = userService.findOne(Long.parseLong(id));
				hrList.add(hr);
				// finding new HRs
				if (!removedHr.contains(hr)) {
					newHRs.add(hr);
					newHrVendorEmailIds.add(hr.getEmail());
				}
			}
		}

		List<String> interviewerIdList = positionDTO.getInterviewerPanelsId();

		// getting list of InterviewerList
		Set<ClientInterviewerPanel> interviewerList = new LinkedHashSet<ClientInterviewerPanel>();
		if (interviewerIdList != null && !(interviewerIdList.isEmpty())) {
			for (String id : interviewerIdList) {
				ClientInterviewerPanel interviewPanel = interviewPanelService.findOne(Long.parseLong(id));
				interviewerList.add(interviewPanel);
			}
		}

		// getting list of vendors
		Set<Vendor> existingVendor = existingPosition.getVendors();
		Set<Vendor> vendors = new LinkedHashSet<Vendor>();
		if (positionDTO.getVendorIds() != null && !(positionDTO.getVendorIds().isEmpty())) {
			for (String vendorId : positionDTO.getVendorIds()) {
				if (StringUtils.isNumeric(vendorId)) {
					long id = Long.parseLong(vendorId);
					if (id > 0) {
						Vendor vendor = vendorService.findOne(id);
						if (vendor != null) {
							vendors.add(vendor);
							// if thr r some existing vendor then check if
							// current vendor belongs to them or not and
							// populate the email list accordingly
							if (existingVendor != null && !existingVendor.isEmpty()) {
								if (!existingVendor.contains(vendor)) {
									List<String> newVendorEmailList = userService.getAllUserEmailByVendor(vendor.getId() + "");
									if (newVendorEmailList != null) {
										newHrVendorEmailIds.addAll(newVendorEmailList);
									}
								}
							} else {
								// if thr r no existing vendors all vendor will
								// be added to email list
								List<String> newVendorEmailList = userService.getAllUserEmailByVendor(vendor.getId() + "");
								if (newVendorEmailList != null) {
									newHrVendorEmailIds.addAll(newVendorEmailList);
								}
							}
						}
					}
				}
			}
		}

		existingPosition.setClient(clientService.getClientByName(clientName));
		existingPosition.setTitle(positionDTO.getTitle());
		existingPosition.setPositionCode(positionDTO.getPositionCode());
		existingPosition.setLocation(positionDTO.getLocation());
		existingPosition.setTotalPosition(positionDTO.getTotalPosition());
		existingPosition.setCloseByDate(positionDTO.getCloseByDate());
		existingPosition.setVerticalCluster(positionDTO.getVerticalCluster());
		existingPosition.setEndClient(positionDTO.getEndClient());
		existingPosition.setHiringManager(positionDTO.getHiringManager());
		existingPosition.setScreener(positionDTO.getScreener());
		existingPosition.setRequisitionId(positionDTO.getRequisitionId());
		existingPosition.setSpoc(positionDTO.getSpoc());

		if (null != positionDTO.getCustomField()) {
			existingPosition.setCustomField(positionDTO.getCustomField());
		} else {
			existingPosition.setCustomField(null);
		}

		if (null == positionDTO.getDescription() || positionDTO.getDescription().trim().isEmpty()) {
			existingPosition.setDescription(null);
		} else {
			existingPosition.setDescription(positionDTO.getDescription());
		}

		if (null == positionDTO.getNotes() || positionDTO.getNotes().trim().isEmpty()) {
			existingPosition.setNotes(null);
		} else {
			existingPosition.setNotes(positionDTO.getNotes());
		}

		if (null != positionDTO.getTeamId()) {
			existingPosition.setTeam(teamService.findOne(positionDTO.getTeamId()));
		} else {
			existingPosition.setTeam(null);
		}

		existingPosition.setPositionUrl(positionDTO.getPositionUrl());
		existingPosition.setReqSkillSet(positionDTO.getReqSkillSet());
		existingPosition.setGoodSkillSet(positionDTO.getGoodSkillSet());
		existingPosition.setType(positionDTO.getType());
		existingPosition.setRemoteWork(Boolean.parseBoolean(positionDTO.getRemoteWork()));
		existingPosition.setSalUnit(positionDTO.getSalUnit());
		existingPosition.setMaxSal(positionDTO.getMaxSal());
		existingPosition.setStatus(Status.Active.toString());
		existingPosition.setHrExecutives(hrList);
		existingPosition.setDecisionMakers(null);
		existingPosition.setInterviewers(interviewerList);
		existingPosition.setJdPath(positionDTO.getJdLink());
		existingPosition.setExperienceRange(positionDTO.getExperienceRange());
		existingPosition.setEducationalQualification(positionDTO.getEducationalQualification());
		existingPosition.setFunctionalArea(positionDTO.getFunctionalArea());
		existingPosition.setIndustry(positionDTO.getIndustry());
		existingPosition.setMinSal(positionDTO.getMinSal());
		existingPosition.setVendors(vendors);
		existingPosition.setNationality(positionDTO.getNationality());
		existingPosition = positionRepository.save(existingPosition);

		// if entries are there in generic interviewer list then adding it to
		// ClientInterviewerPanel
		if (null != positionDTO.getGenericInterviewerList() && !positionDTO.getGenericInterviewerList().isEmpty()) {
			positionDTO.setGenericInterviewerList(genericInterviewerService.saveInterviewer(positionDTO.getGenericInterviewerList()));
			for (GenericInterviewer genericInterviewer : positionDTO.getGenericInterviewerList()) {
				genericInterviewerService.addGenericInterviewerToPosition(genericInterviewer, existingPosition.getId());
			}
		}

		PositionRequest requestedPosition = positionRequestService.getPositionRequestBypositionId(existingPosition.getId());
		if (requestedPosition != null) {
			requestedPosition.setClientName(clientName);
			requestedPosition.setTitle(positionDTO.getTitle());
			requestedPosition.setPositionCode(positionDTO.getPositionCode());
			requestedPosition.setLocation(positionDTO.getLocation());
			requestedPosition.setTotalPosition(positionDTO.getTotalPosition());
			requestedPosition.setCloseByDate(positionDTO.getCloseByDate());
			requestedPosition.setDescription(positionDTO.getDescription());
			requestedPosition.setPositionUrl(positionDTO.getPositionUrl());
			requestedPosition.setReqSkillSet(positionDTO.getReqSkillSet());
			requestedPosition.setGoodSkillSet(positionDTO.getGoodSkillSet());
			requestedPosition.setType(positionDTO.getType());
			requestedPosition.setRemoteWork(Boolean.parseBoolean(positionDTO.getRemoteWork()));
			requestedPosition.setSalUnit(positionDTO.getSalUnit());
			requestedPosition.setMaxSal(positionDTO.getMaxSal());
			requestedPosition.setNotes(positionDTO.getNotes());
			requestedPosition.setJdPath(positionDTO.getJdLink());
			requestedPosition.setExperienceRange(positionDTO.getExperienceRange());
			requestedPosition.setEducationalQualification(positionDTO.getEducationalQualification());
			requestedPosition.setFunctionalArea(positionDTO.getFunctionalArea());
			requestedPosition.setIndustry(positionDTO.getIndustry());
			requestedPosition.setMinSal(positionDTO.getMinSal());
			positionRequestService.save(requestedPosition);
		}

		// send email to HR executive and vendors here on if they are added to
		// position
		if (newHrVendorEmailIds != null && !newHrVendorEmailIds.isEmpty()) {
			final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
			String emailSubject = "You have been added to '" + existingPosition.getTitle() + "'";
			Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(existingPosition);
			String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + positionId;
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(newHrVendorEmailIds, masterRenderedTemplate, emailSubject, true);
		}

		// getting list of existing vendor user
		List<String> existingVendorEmails = new ArrayList<>();
		if (existingVendor != null && !existingVendor.isEmpty()) {
			for (Vendor vendor : existingVendor) {
				List<User> vendorUser = userService.getAllUserByVendor(vendor.getId() + "");
				if (vendorUser != null && !vendorUser.isEmpty()) {
					for (User user : vendorUser) {
						existingVendorEmails.add(user.getEmail());
					}
				}
			}
		}

		// getting list of existing hr executive
		List<String> existingHrEmails = new ArrayList<>();
		if (existingPosition.getHrExecutives()!= null && !existingPosition.getHrExecutives().isEmpty()) {
			for (User user : existingPosition.getHrExecutives()) {
				existingHrEmails.add(user.getEmail());
			}
		}

		// sending update email to vendors
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_UPDATE;
		String emailSubject = "Position '" + existingPosition.getTitle() + "' details updated.";
		Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(existingPosition);
		String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);

		// sending update email to vendors
		if (existingVendorEmails != null && !existingVendorEmails.isEmpty()) {
			// removing the currently added vendor from the list;
			existingVendorEmails.removeAll(newHrVendorEmailIds);
			String link = baseUrl + GlobalConstants.VENDOR_POSITION_DETAILS_URL + existingPosition.getPositionCode();
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(existingVendorEmails, masterRenderedTemplate, emailSubject, true);
		}

		// removing the current logged in user from the list as he is making
		// the change
		existingHrEmails.remove(userService.getLoggedInUserEmail());

		// sending update email to hr executives
		if (existingHrEmails != null && !existingHrEmails.isEmpty()) {
			// removing the currently added vendor/ hr executive from the list;
			existingHrEmails.removeAll(newHrVendorEmailIds);
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + positionId;
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(existingHrEmails, masterRenderedTemplate, emailSubject, true);
		}

		// adding in position activity
		PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				NotificationEvent.POSITION_MODIFIED.getDisplayName(),
				ActivityMessageConstants.getUpdatedByMsg(userService.getLoggedInUserObject()), new Date(),
				existingPosition.getPositionCode(), existingPosition.getTeam());
		positionActivityService.addActivity(positionActivity);

//		// sending email to team members
//		if (positionDTO.getTeamId() != null && oldTeamId != positionDTO.getTeamId()) {
//			final String template = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
//			String subject = "You have been added to position " + existingPosition.getTitle();
//			sendEmailToTeam(subject, template, positionDTO.getTeamId(), existingPosition);
//		} else if (existingPosition.getTeam() != null) {
//			final String updateTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_UPDATE;
//			String updateSubject = "Position " + existingPosition.getTitle() + " is updated";
//			sendEmailToTeam(updateSubject, updateTemplate, existingPosition.getTeam().getId(), existingPosition);
//		}

		// sending web socket notification on position update
		notificationOnPositionUpdate(existingPosition, removedHr, newHRs);
		calculateFinalStatusForPosition(existingPosition);
		return existingPosition;
	}

	/**
	 * this will send notifications to all the Hrs of the position on position
	 * update, this will also send notification to newly added HRs, and removed
	 * HRs
	 *
	 * @param newPosition
	 * @param removedHr
	 * @param newHRs
	 * @throws RecruizException
	 */
	public void notificationOnPositionUpdate(Position newPosition, Set<User> removedHr, Set<User> newHRs) throws RecruizException {

		// sending notification to newly added HRs of the position
		if (newHRs != null && !newHRs.isEmpty()) {
			for (User hr : newHRs) {
				Notification notification = new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
						userService.getLoggedInUserObject().getName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
						notificationService.getMessageForPositionDetailsUpdated(newPosition.getId()), new Date(),
						newPosition.getPositionCode(), newPosition.getClient().getId(), 0, 0, null);

				notificationService.sendNotification(notification);
			}
		}

		Set<User> positionHr = newPosition.getHrExecutives();
		positionHr.add(userService.getUserByEmail(newPosition.getOwner()));
		if (positionHr != null && !positionHr.isEmpty()) {
			for (User hr : positionHr) {
				if (removedHr != null && !removedHr.isEmpty()) {
					removedHr.remove(hr);
				}
				// if the user is not the logged in user
				if (!hr.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
					Notification notification = new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
							userService.getLoggedInUserObject().getName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
							notificationService.getMessageForPositionDetailsUpdated(newPosition.getId()), new Date(),
							newPosition.getPositionCode(), newPosition.getClient().getId(), 0, 0, null);

					notificationService.save(notification);
					notificationService.sendNotification(notification);
				}
			}
		}

		// sending notification to removed HRs of the position
		if (removedHr != null && !removedHr.isEmpty()) {
			for (User hr : removedHr) {
				Notification notification = new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
						userService.getLoggedInUserObject().getName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
						"You are removed from a position " + notificationService.getFormattedPositionClientData(newPosition.getId()),
						new Date(), newPosition.getPositionCode(), newPosition.getClient().getId(), 0, 0, null);

				notificationService.sendNotification(notification);
			}
		}

	}

	/**
	 * This method is used to get all positions.
	 *
	 * @return
	 * @throws RecruizException
	 */
	
	public List<Position> getAllPosition() throws RecruizException {
		if (checkPermissionService.belongsToHrExecGroup(userService.getLoggedInUserObject().getUserRole())) {
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			return positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(userService.getLoggedInUserEmail(),
					userService.loggedInUserSet(), teams);
		}
		List<Position> getAllPosition = positionRepository.findAll();
		return getAllPosition;
	}

	/**
	 * This method is used to get all positions page wise.
	 *
	 * @return
	 * @throws RecruizException
	 */
	
	public Page<Position> getAllPosition(Pageable pageable) throws RecruizException {
		Page<Position> positionList = null;
		// as per new requirement only respective positions should be displayed
		// for HR Manager and HR executive
		/*
		 * if (checkPermissionService.belongsToHrGroup(userService.
		 * getLoggedInUserObject().getUserRole())) { String count =
		 * positionRepository.getTotalSize(userService.getLoggedInUserEmail(),
		 * userService.loggedInUserSet()); positionList =
		 * positionRepository.findDistinctByOwnerOrHrExecutivesIn(userService.
		 * getLoggedInUserEmail(), userService.loggedInUserSet(), pageable);
		 * positionList = new PageImpl<Position>(positionList.getContent(),
		 * pageable, Long.parseLong(count)); } else { positionList =
		 * positionRepository.findAll(pageable); }
		 */
		String count = null;
		// this if condition is added after decided to display all the position
		// for super admin
		if (checkPermissionService.isSuperAdmin()) {
			count = positionRepository.count() + "";
			positionList = positionRepository.findAll(pageable);
		} else if (checkPermissionService.belongsToHrManagerGroup(userService.getLoggedInUserObject().getUserRole())) {
			count = positionRepository.getTotalSize(userService.getLoggedInUserEmail(), userService.loggedInUserSet());
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			positionList = positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(userService.getLoggedInUserEmail(),
					userService.loggedInUserSet(),teams, pageable);
		} else {
			count = positionRepository.getTotalSize(userService.getLoggedInUserEmail(), userService.loggedInUserSet());
//			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			positionList = positionRepository.findDistinctByOwnerOrHrExecutivesIn(userService.getLoggedInUserEmail(),
					userService.loggedInUserSet(), pageable);
		}

		// positionList = new PageImpl<Position>(positionList.getContent(),
		// pageable, Long.parseLong(count));
		logger.error("positionService  getAllpositions  start = "+new Date());
		if (positionList.getContent() != null && !positionList.getContent().isEmpty()) {
			for (Position position : positionList) {
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getVendors().size();
				// adding board candidate count here
				Map<String, String> boardCandidateCount = roundCandidateService.getBoardCandidateCount(position.getPositionCode());
				position.setBoardCandidateCount(boardCandidateCount);
			}
		}
		logger.error("positionService  getAllpositions  end = "+new Date());

		return positionList;
	}

	public List<String> getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(String ownerEmail, User user, List<Team> teams, Date start,
			Date end) {
		Set<User> users = new HashSet<>();
		users.add(user);
		return positionRepository.getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(ownerEmail, users, teams, start, end);
	}
	
	public List<String> getPositionCodesForOwnerOrHrExecutivesIn(String ownerEmail, User user,  Date start,
			Date end) {
		Set<User> users = new HashSet<>();
		users.add(user);
		return positionRepository.getPositionCodesForOwnerOrHrExecutivesIn(ownerEmail, users, start, end);
	}
	
	//Added by Sajin
	public List<Long> getActivePositionIdsForOwnerOrHrExecutivesIn(String ownerEmail, User user,  Date start,
			Date end) {
		Set<User> users = new HashSet<>();
		users.add(user);
		return positionRepository.getActivePositionIDsForOwnerOrHrExecutivesIn(ownerEmail, users);
	}
	
	
	
	public List<String> getPositionCodesForOwnerOrHrExecutivesIn(String ownerEmail, User user) {
		Set<User> users = new HashSet<>();
		users.add(user);
		return positionRepository.getPositionCodesForOwnerOrHrExecutivesIn(ownerEmail, users);
	}
	
	public List<String> getPositionHrExecutivesIn(User user) {
		Set<User> users = new HashSet<>();
		users.add(user);
		return positionRepository.getPositionHrExecutivesIn(users);
	}
	
	/**
	 * all position for specific client page wise
	 *
	 * @param client
	 * @return
	 * @throws RecruizException
	 */
	//@Transactional(readOnly = true)
	public Page<Position> getAllPositionByClient(Client client, Pageable pageable, String sortField) throws RecruizException {
		Page<Position> positionList = null;

		if (checkPermissionService.isSuperAdmin()) {
			positionList = positionRepository.findByClient(client, pageable);
		} else {
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			if (null != teams && !teams.isEmpty()) {
				positionList = positionRepository.findPositionForClientAndOwnerOrClientAndHrExecutivesIn(client,
						userService.getLoggedInUserEmail(), userService.loggedInUserSet(), teams, pageable);
			} else {
				positionList = positionRepository.findPositionForClientAndOwnerOrClientAndHrExecutivesIn(client,
						userService.getLoggedInUserEmail(), userService.loggedInUserSet(), pageable);
			}

		}

		// loading lazy properties of position
		if (positionList.getContent() != null && !positionList.getContent().isEmpty()) {
			for (Position position : positionList.getContent()) {
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getVendors().size();
				// adding board candidate count here
				Map<String, String> boardCandidateCount = roundCandidateService.getBoardCandidateCount(position.getPositionCode());
				position.setBoardCandidateCount(boardCandidateCount);
			}
			// making page object of fetched client position
			// positionList = new PageImpl<>(allPosition, pageable,
			// allPosition.size());
		}
		return positionList;
	}

	/**
	 * all position for specific client
	 *
	 * @param client
	 * @return
	 * @throws RecruizException
	 */
	//@Transactional(readOnly = true)
	public List<Position> getAllPositionByClient(Client client) throws RecruizException {
		if (checkPermissionService.belongsToHrExecGroup(userService.getLoggedInUserObject().getUserRole())) {
			return positionRepository.findByClientAndOwnerOrClientAndHrExecutivesIn(client, userService.getLoggedInUserEmail(), client,
					userService.loggedInUserSet());
		}

		List<Position> positionList = positionRepository.findByClient(client);
		return positionList;
	}

	/**
	 * <code>updatePositionStatus</code> method is used to change the status of
	 * position by positionId
	 *
	 * @param positionId
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Position updatePositionStatus(long positionId, String status) throws RecruizException {

		Position position = positionRepository.findOne(positionId);
		String oldStatus = position.getStatus();

		boolean updatable = false;
		if (position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditPositionPermission())
			throw new RecruizException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		position.setStatus(status);
		if (Status.Closed.toString().equals(status)) {
			position.setClosedDate(new Date());
			position.setClosedByUser(userService.getLoggedInUserEmail());
		} else {
			position.setClosedDate(null);
			position.setClosedByUser(null);
		}
		position.getBoard().setPositionStatus(status);
		position = positionRepository.save(position);

		PositionRequest requestedPosition = positionRequestService.getPositionRequestBypositionId(position.getId());
		if (requestedPosition != null) {
			if (status.equalsIgnoreCase(Status.Active.getDisplayName())) {
				requestedPosition.setStatus(PositionRequestStatus.InProcess.toString());
			} else {
				requestedPosition.setStatus(status);
			}
			positionRequestService.save(requestedPosition);
		}

		// send email to HR executive and vendors here on status change
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_STATUS_CHANGED;
		String emailSubject = "Status Changed for Position '" + position.getTitle() + "' to " + Status.valueOf(status).getDisplayName();
		sendEmailToHR_Vendor_OnCreate_StatusChange(position, emailTemplate, emailSubject);

		// adding in position activity
		PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				NotificationEvent.POSITION_MODIFIED.getDisplayName(), ActivityMessageConstants
						.getStatusChangedMsg(userService.getLoggedInUserObject(), oldStatus, Status.valueOf(status).getDisplayName()),
				new Date(), position.getPositionCode(), position.getTeam());
		positionActivityService.addActivity(positionActivity);

		notificationOnPositionUpdate(position, null, null);
		calculateFinalStatusForPosition(position);
		return position;
	}

	/**
	 * <code>publishCareerSitePosition</code> method is used to publish the
	 * position to the careersite by positionId
	 *
	 * @param positionId
	 * @param publish
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Position publishCareerSitePosition(long positionId, boolean publish) throws RecruizException {

		Position position = positionRepository.findOne(positionId);

		boolean updatable = false;
		if (position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditPositionPermission())
			throw new RecruizException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		position.setPublishCareerSite(publish);
		//position.setPublishedDate(new Date());
		position = positionRepository.save(position);

		return position;
	}

	/**
	 * <code>publishRecruizConnectPosition</code> method is used to publish the
	 * position to the recruiz connect by positionId
	 *
	 * @param positionId
	 * @param publish
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = { RecruizWarnException.class })
	public Position publishRecruizConnectPosition(long positionId, boolean publish) throws Exception {

		Position position = positionRepository.findOne(positionId);

		boolean updatable = false;
		if (position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditPositionPermission())
			throw new RecruizException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		// publishing the position into recruiz connect
		recruizConnectService.publishRecruizConnectPosition(position);

		position.setPublishRecruizConnect(publish);
		position = positionRepository.save(position);

		return position;
	}

	/**
	 * <code>deletePosition</code> method is used to delete position by
	 * positionId
	 *
	 * @param positionId
	 * @throws RecruizException
	 */
	@Transactional
	public void deletePosition(long positionId) throws RecruizException {

		Position position = positionRepository.findOne(positionId);
		String positionCode = position.getPositionCode();
		long clientId = position.getClient().getId();

		boolean deletable = false;
		if (position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				|| checkPermissionService.hasGlobalDeletePermission()) {
			deletable = true;
		} else if (!deletable && !checkPermissionService.hasDeletePositionPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		Set<User> newHRs = position.getHrExecutives();
		String formattedMsg = notificationService.getFormattedPositionClientData(positionId);
		long requestedPositionId = position.getId();
		delete(position);

		// update the requested position status to removed if the current
		// positin is related to any.
		PositionRequest requestedPosition = positionRequestService.getPositionRequestBypositionId(requestedPositionId);
		long existingRequestedPositionId = 0;
		if (requestedPosition != null) {
			existingRequestedPositionId = requestedPosition.getId();
			requestedPosition.setStatus(PositionRequestStatus.Removed.toString());
			positionRequestService.save(requestedPosition);
		}

		if (newHRs != null && !newHRs.isEmpty()) {
			for (User hr : newHRs) {
				if (!hr.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
					notificationService.sendNotification(new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
							userService.getLoggedInUserObject().getName(), NotificationEvent.POSITION_CREATED.getDisplayName(),
							"position " + formattedMsg + " is deleted.", new Date(), positionCode, clientId, 0, existingRequestedPositionId,
							null));
				}
			}
		}
	}

	@Transactional
	public Set<ClientInterviewerPanel> getPositionInterviewer(String PositionCode) throws RecruizException {
		Position position = positionRepository.findOneByPositionCode(PositionCode);
		position.getInterviewers().size();
		List<ClientInterviewerPanel> interviewPanelList = new java.util.LinkedList<ClientInterviewerPanel>();
		interviewPanelList.addAll(position.getInterviewers());

		// doing sorting here
		Collections.sort(interviewPanelList, new Comparator<ClientInterviewerPanel>() {
			public int compare(ClientInterviewerPanel dto1, ClientInterviewerPanel dto2) {

				int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
				if (res == 0) {
					res = dto1.getName().compareTo(dto2.getName());
				}
				return res;
			}
		});

		return new LinkedHashSet<>(interviewPanelList);
	}

	@Transactional(readOnly = true)
	public Set<ClientDecisionMaker> getPositionDecisionMaker(String PositionCode) {
		Position position = positionRepository.findOneByPositionCode(PositionCode);
		return position.getDecisionMakers();
	}

	@Transactional(readOnly = true)
	public Set<User> getPositionHRList(String PositionCode) throws RecruizException {
		Position position = positionRepository.findOneByPositionCode(PositionCode);
		position.getHrExecutives().size();
		return position.getHrExecutives();
	}

	
	public Board getPositionBoard(String positionCode) throws RecruizException {
		Position position = positionRepository.findOneByPositionCode(positionCode);
		if (position == null)
			throw new RecruizException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		calculateFinalStatusForPosition(position);
		return position.getBoard();
	}

	/**
	 * <code>getPositionById</code> method return position object by passing
	 * positionId as argument.
	 *
	 * @param positionId
	 * @return
	 * @throws RecruizException
	 */

	public Position getPositionById(long positionId) throws RecruizException {
		Position position = positionRepository.findOne(positionId);

		if (position == null)
			return null;

		List<ClientInterviewerPanel> interviewerList = new java.util.LinkedList<ClientInterviewerPanel>();
		if (position.getInterviewers() != null && position.getInterviewers().size() > 0) {
			interviewerList.addAll(position.getInterviewers());
		}
		if (interviewerList != null && !interviewerList.isEmpty()) {
			Collections.sort(interviewerList, new Comparator<ClientInterviewerPanel>() {
				public int compare(ClientInterviewerPanel dto1, ClientInterviewerPanel dto2) {
					int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
					if (res == 0) {
						res = dto1.getName().compareTo(dto2.getName());
					}
					return res;
				}
			});
			position.setInterviewers(new LinkedHashSet<>(interviewerList));
		}

		List<User> hrList = new java.util.LinkedList<User>();
		hrList.addAll(position.getHrExecutives());
		if (hrList != null && !hrList.isEmpty()) {
			Collections.sort(hrList, new Comparator<User>() {

				public int compare(User hr1, User hr2) {

					int res = String.CASE_INSENSITIVE_ORDER.compare(hr1.getName(), hr2.getName());
					if (res == 0) {
						res = hr1.getName().compareTo(hr2.getName());
					}
					return res;
				}
			});
			position.setHrExecutives(new LinkedHashSet<>(hrList));
		}

		List<Vendor> vendorList = new java.util.LinkedList<Vendor>();
		if (position.getVendors() != null && !position.getVendors().isEmpty()) {
			vendorList.addAll(position.getVendors());
		}

		if (vendorList != null && !vendorList.isEmpty()) {
			Collections.sort(vendorList, new Comparator<Vendor>() {
				public int compare(Vendor v1, Vendor v2) {

					int res = String.CASE_INSENSITIVE_ORDER.compare(v1.getName(), v2.getName());
					if (res == 0) {
						res = v1.getName().compareTo(v2.getName());
					}
					return res;
				}
			});
			position.setVendors(new LinkedHashSet<>(vendorList));
		}

		calculateFinalStatusForPosition(position);
		return position;
	}

	@Transactional(readOnly = true)
	public int getActivePositionCount() {
		return positionRepository.getActivePositionCount();
	}

	
	public List<Position> getAllPositionForSearchIndex(Pageable pageable) {
		List<Position> result = new ArrayList<>();
		Page<Position> positions = positionRepository.findAll(pageable);

		if (positions == null)
			return result;

		for (Position position : positions) {
			position.getGoodSkillSet().size();
			position.getReqSkillSet().size();
			position.getVendors().size();
			result.add(position);
		}
		calculateFinalStatusForPositions(result);
		return result;
	}

	@Transactional(readOnly = true)
	public List<Position> getPositionListByInterviewerAndClient(ClientInterviewerPanel interviewer, Client client) {
		return positionRepository.findByInterviewersAndClientOrderByCreationDateDesc(interviewer, client);
	}


	public List<Position> getPositionByHrExecutive() throws RecruizException {
		Set<User> users = new HashSet<User>();
		users.add(userService.getLoggedInUserObject());

//		List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());

		List<Position> positionList = positionRepository.findDistinctByOwnerOrHrExecutivesIn(userService.getLoggedInUserEmail(),
				users);
		calculateFinalStatusForPositions(positionList);
		return positionList;
	}

	
	public List<Position> getPositionListByUser(User user) throws RecruizException {
		Set<User> users = new HashSet<User>();
		users.add(user);

		List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());

		List<Position> positionList = positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(user.getEmail(), users, teams);
		calculateFinalStatusForPositions(positionList);
		return positionList;
	}

	
	public List<Position> getPositionByStatus(String status) {
		List<Position> result = new ArrayList<>();
		List<Position> positionList = positionRepository.findByStatus(status);

		calculateFinalStatusForPositions(positionList);

		return getPosition(status, result, positionList);
	}

	
	public List<Position> getPositionByStatusAndPublishCareersite(String status) {

		List<Position> result = new ArrayList<>();
		List<Position> positionList = positionRepository.findByStatusAndPublishCareerSiteIsTrueOrderByCreationDateDesc(status);

		calculateFinalStatusForPositions(positionList);

		return getPosition(status, result, positionList);
	}

	
	public List<Position> getPositionByStatusAndPublishCareersite(String status, Set<Long> ids) {

		List<Position> result = new ArrayList<>();
		List<Position> positionList = positionRepository.findByStatusAndPublishCareerSiteIsTrueAndIdInOrderByCreationDateDesc(status, ids);

		calculateFinalStatusForPositions(positionList);

		return getPosition(status, result, positionList);
	}

	/**
	 * @param status
	 * @param result
	 * @param positionList
	 * @return
	 */
	private List<Position> getPosition(String status, List<Position> result, List<Position> positionList) {

		for (Position position : positionList) {
			position.getGoodSkillSet().size();
			position.getReqSkillSet().size();
			position.getEducationalQualification().size();
			position.getClient().getClientName();
			if (position.getFinalStatus().equalsIgnoreCase(status)) {
				result.add(position);
			}
		}
		return result;
	}

	
	public List<Position> getAllPositionByClientAndHrExecutive(Client client) throws RecruizException {
		Set<User> users = new HashSet<User>();
		users.add(userService.getLoggedInUserObject());
		List<Position> positionList = positionRepository.findByClientAndHrExecutivesIn(client, users);
		calculateFinalStatusForPositions(positionList);
		return positionList;
	}

	@Transactional(readOnly = true)
	public String getStatusCountByClient(Long clientId, String status) {
		return positionRepository.getStatusCountByClient(clientId, status);
	}
	
	@Transactional(readOnly = true)
	public String getStatusCountByClient(Long clientId, List<String> statuses) {
		return positionRepository.getStatusCountByClient(clientId, statuses);
	}

	@Transactional(readOnly = true)
	public String getStatusCountByHRClient(Long clientId, String status) {
		return positionRepository.getStatusCountByClientPositionHrAndOwner(clientId, status, userService.getLoggedInUserEmail(),
				userService.loggedInUserSet());
	}

	@Transactional(readOnly = true)
	public String getNearClosurePosition(Long clientId) {
		Calendar calendar = Calendar.getInstance();
		Date mindate = calendar.getTime(); // new Date(today.getTime());
		Date maxdate = new Date(mindate.getTime() + TimeUnit.DAYS.toMillis(5));

		// converting java date to sql date to compare
		java.sql.Date mindat = new java.sql.Date(mindate.getTime());
		java.sql.Date maxdat = new java.sql.Date(maxdate.getTime());
		return positionRepository.getnearClosureCountByClientAndHRPosition(clientId, Status.Closed.toString(), mindat, maxdat,
				userService.getLoggedInUserEmail(), userService.loggedInUserSet());
	}

	@Transactional(readOnly = true)
	public String getPostClosurePosition(Long clientId) {
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime(); // new Date(today.getTime());

		// converting java date to sql date to compare
		java.sql.Date mindat = new java.sql.Date(today.getTime());
		return positionRepository.getPostClosedCountByClient(clientId, Status.Closed.toString(), mindat, userService.getLoggedInUserEmail(),
				userService.loggedInUserSet());
	}

	@Transactional(readOnly = true)
	public String getPostClosurePositionForSuperAdmin(Long clientId) {
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime(); // new Date(today.getTime());

		// converting java date to sql date to compare
		java.sql.Date mindat = new java.sql.Date(today.getTime());
		return positionRepository.getPositionPostClosedCount(clientId, Status.Closed.toString(), mindat);
	}

	@Transactional(readOnly = true)
	public String getNearClosurePositionForSuperAdmin(Long clientId) {
		Calendar calendar = Calendar.getInstance();
		Date mindate = calendar.getTime(); // new Date(today.getTime());
		Date maxdate = new Date(mindate.getTime() + TimeUnit.DAYS.toMillis(5));

		// converting java date to sql date to compare
		java.sql.Date mindat = new java.sql.Date(mindate.getTime());
		java.sql.Date maxdat = new java.sql.Date(maxdate.getTime());
		return positionRepository.getnearClosureCountByClientAndHRPosition(clientId, Status.Closed.toString(), mindat, maxdat);
	}

	/**
	 * to check if logged in user is working on this position
	 *
	 * @param position
	 * @return
	 */
	public boolean isLoggedInUserWorkingOn(Position position) {
		Set<User> hrExecutive = position.getHrExecutives();
		if (hrExecutive == null || hrExecutive.isEmpty())
			return false;
		for (User user : hrExecutive) {
			if (user.equals(userService.getLoggedInUserObject()))
				return true;
		}
		return false;
	}

	@Transactional(readOnly = true)
	public Long getTotalOpeningByClient(long clientId) {
		Long totalOpening = positionRepository.findTotalPositionByClient(clientId);
		if (totalOpening != null)
			return totalOpening;
		return (long) 0;
	}


	public Position getPositionByBoard(Board board) {
		Position position = positionRepository.findByBoard(board);
		calculateFinalStatusForPosition(position);
		return position;
	}

	public String getPositionUrl(String positionCode, String sourceMode) {
		String hrEmail = userService.getLoggedInUserEmail();
		String currentTenant = TenantContextHolder.getTenant();
		String positionUrlCode = positionCode;
		String url = baseUrl + "/public/position/" + sourceMode + "/" + positionUrlCode + "/" + currentTenant + "/"
				+ EncryptKeyUtils.getEncryptedKey(hrEmail);
		return url;
	}

	/**
	 * Get all the marked for delete user and delete their entry from the
	 * position
	 *
	 * @throws RecruizException
	 */
	@Transactional
	public void getAllMarkedForDeleteUser(String newOwner) throws RecruizException {
		List<User> allMarkedUser = userService.getAllMarkedForDeleteUser();
		logger.debug("Current tenant : " + TenantContextHolder.getTenant());
		if (allMarkedUser != null && !allMarkedUser.isEmpty()) {
			for (User user : allMarkedUser) {
				List<Position> position = getPositionListByUser(user);
				if (position != null && !position.isEmpty()) {
					for (Position pos : position) {
						pos.getHrExecutives().remove(user);
						if (pos.getOwner().equalsIgnoreCase(user.getEmail())) {
							pos.setOwner(newOwner);
						}
						positionRepository.save(pos);
						logger.debug("Position is " + pos.getTitle());
					}
				}
			}
		}
	}

	
	public Set<PositionClientNameDTO> getPositionCodeNameMapForLoggedInUser() throws RecruizException {

		Set<PositionClientNameDTO> map = new LinkedHashSet<PositionClientNameDTO>();
		List<Position> posList = null;

		if (userService.isLoggedInUserVendor()) {
			posList = vendorService.getLoggedInVendorPosition();
		} else if (checkPermissionService.isSuperAdmin()) {
			posList = positionRepository.findByStatus(Status.Active.getDisplayName());
			calculateFinalStatusForPositions(posList);
		} else {
//			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
//			if (teams != null && !teams.isEmpty()) {
//				posList = positionRepository.findDistinctByStatusAndOwnerOrHrExecutivesInOrTeamIn(Status.Active.getDisplayName(),
//						userService.getLoggedInUserEmail(), userService.loggedInUserSet(), teams);
//			} else {
				posList = positionRepository.findDistinctByStatusAndOwnerOrHrExecutivesIn(Status.Active.getDisplayName(),
						userService.getLoggedInUserEmail(), userService.loggedInUserSet());
//			}
			calculateFinalStatusForPositions(posList);
		}

		if (posList != null && !posList.isEmpty()) {
			for (Position position : posList) {
				if (position.getFinalStatus().equalsIgnoreCase(Status.Active.getDisplayName())) {
					PositionClientNameDTO positionClientNameDTO = new PositionClientNameDTO();
					positionClientNameDTO.setPositionCode(position.getPositionCode());
					positionClientNameDTO.setPositionName(position.getTitle());
					positionClientNameDTO.setClientName(position.getClient().getClientName());
					positionClientNameDTO.setLocation(position.getLocation());
					map.add(positionClientNameDTO);
				}
			}
		}
		return map;
	}

	
	public Page<Position> getVendorPosition(Vendor vendor, Pageable pageable) {
		Set<Vendor> vendors = new HashSet<Vendor>();
		vendors.add(vendor);

		Page<Position> positionList = positionRepository.findByVendorsIsIn(vendors, pageable);
		calculateFinalStatusForPositions(positionList.getContent());
		return positionList;
	}

	
	public List<Position> getVendorPosition(Vendor vendor) {
		Set<Vendor> vendors = new HashSet<Vendor>();
		vendors.add(vendor);

		List<Position> positionList = positionRepository.findByVendorsIsIn(vendors);
		calculateFinalStatusForPositions(positionList);
		return positionList;
	}

	
	public Position findByPositionCodeAndVendorsIsIn(String positionCode, Vendor vendor) {
		Set<Vendor> vendors = new HashSet<Vendor>();
		vendors.add(vendor);

		Position position = positionRepository.findByPositionCodeAndVendorsIsIn(positionCode, vendors);
		calculateFinalStatusForPosition(position);

		return position;
	}

	@Transactional(readOnly = true)
	public Page<Position> findByLocationOrIndustryAndStatusAndPublishCareerSiteIsTrue(byte[] location, String industry, String status,
			Pageable pageable) {
		Page<Position> positions = positionRepository.findByLocationOrIndustryAndStatusAndPublishCareerSiteIsTrueOrderByCreationDateDesc(location, industry,
				status, pageable);
		return positions;
	}

	
	public List<Position> getPositionsByCandidate(String candidate_cid) {
		List<Position> positions = positionRepository.getPositionsByCandidate(candidate_cid);
		calculateFinalStatusForPositions(positions);
		return positions;
	}

	/**
	 * to get list of all Hr working under a position which belongs to HR
	 * manager
	 *
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public List<BigInteger> getAllPositionHrListForHrManager() throws RecruizException {

		List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
		List<Long> teamIds = new ArrayList<>();
		if (teams != null && !teams.isEmpty()) {
			for (Team team : teams) {
				teamIds.add(team.getId());
			}
		} else {
			teamIds.add(0L);
		}

		List<BigInteger> hrList = positionRepository.getPositionHRIdsForHrManager(userService.getLoggedInUserObject().getEmail(),
				userService.getLoggedInUserObject().getUserId() + "");
		return hrList;
	}

	/**
	 * to get list of all hr ids who is working on some position
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<BigInteger> getAllPositionHrList() {
		List<BigInteger> hrList = positionRepository.getPositionHRIds();
		return hrList;
	}

	/**
	 * this method will do OR operation for position status provided closed will
	 * have priority 0,onhold will have 1 and Active will have 2
	 */
	
	public void calculateFinalStatusForPositions(List<Position> positions) {
		if (positions != null && !positions.isEmpty()) {
			for (Position position : positions) {
				calculateFinalStatusForPosition(position);
			}
		}
	}

	/**
	 * this method will do OR operation for position status provided closed will
	 * have priority 0,onhold will have 1 and Active will have 2
	 */
	/**
	 * this method will do OR operation for position status provided closed will
	 * have priority 0,onhold will have 1 and Active will have 2
	 */
	
	public void calculateFinalStatusForPosition(Position position) {
		if (position != null) {

			// if client status 'Active' then position status would be in
			// priority
			if (position.getClientStatus().equalsIgnoreCase(Status.Active.name())) {
				if (position.getStatus().equalsIgnoreCase(Status.Closed.name())) {
					position.setFinalStatus(Status.Closed.name());
				} else if (position.getStatus().equalsIgnoreCase(Status.OnHold.name())) {
					position.setFinalStatus(Status.OnHold.name());
				} else if (position.getStatus().equalsIgnoreCase(Status.StopSourcing.name())) {
					position.setFinalStatus(Status.StopSourcing.name());
				} else {
					position.setFinalStatus(Status.Active.name());
				}
			} else {
				if (position.getClientStatus().equalsIgnoreCase(Status.Closed.name())) {
					position.setFinalStatus(Status.Closed.name());
				} else if (position.getClientStatus().equalsIgnoreCase(Status.OnHold.name())) {
					position.setFinalStatus(Status.OnHold.name());
				} else if (position.getClientStatus().equalsIgnoreCase(Status.StopSourcing.name())) {
					position.setFinalStatus(Status.StopSourcing.name());
				} else {
					position.setFinalStatus(Status.Active.name());
				}
			}
		}
	}

	/**
	 * this method to add display name of employment type for position
	 */
	
	public void getEmploymentTypeDisplayName(Position position) {
		if (EmploymentType.FullTime.toString().equalsIgnoreCase(position.getType()))
			position.setTypeDisplayName(EmploymentType.FullTime.getDisplayName());
		else if (EmploymentType.PartTime.toString().equalsIgnoreCase(position.getType()))
			position.setTypeDisplayName(EmploymentType.PartTime.getDisplayName());
		else
			position.setTypeDisplayName(position.getType());
	}

	/**
	 * Returns Contents required for social share
	 *
	 * @param positionCode
	 * @param user
	 * @return
	 * @throws RecruizWarnException
	 * @throws RecruizException
	 */
	public Map<String, String> getPositionDetailsForSocilShare(String positionCode, User user)
			throws RecruizWarnException, RecruizException {
		Map<String, String> positionDetailsMap = new HashMap<String, String>();

		if (positionCode == null || positionCode.isEmpty()) {
			return null;
		}
		Position position = getPositionByCode(positionCode);
		if (position == null) {
			throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		}
		Source[] sourceList = Source.values();
		for (Source sourceMode : sourceList) {
			String source = sourceMode.getDisplayName();
			if (source == null || source.isEmpty()) {
				continue;
			}
			if (!sourceMode.getIsSocial()) {
				continue;
			}
			Map<String, Object> varValues = new HashMap<String, Object>();
			varValues.put(GlobalConstants.POSITION_NAME, position.getTitle());
			varValues.put(GlobalConstants.NO_OF_OPENINGS, position.getTotalPosition());
			if (Source.Facebook.getDisplayName().equalsIgnoreCase(source)) {
				varValues.put(GlobalConstants.FB_APP_ID, fbAppId);
			}
			if (Source.Twitter.getDisplayName().equalsIgnoreCase(source)) {
				varValues.put(GlobalConstants.TWITTER_USERNAME, twitterUsername);
			}
			String orgType = user.getOrganization().getOrgType();
			if (GlobalConstants.SIGNUP_MODE_CORPORATE.equalsIgnoreCase(orgType)) {
				varValues.put(GlobalConstants.IS_CORPORATE, true);
				String orgName = user.getOrganization().getOrgName();
				varValues.put(GlobalConstants.ORG_NAME, orgName);
			} else if (GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(orgType)) {
				varValues.put(GlobalConstants.IS_AGENCY, true);
				String orgName = user.getOrganization().getOrgName();
				varValues.put(GlobalConstants.CLIENT_NAME, orgName);
			}
			String shareUrl = getPositionUrl(positionCode, source);
			varValues.put(GlobalConstants.SHARE_URL, shareUrl);

			String positionDetails = populatePositionDetails(GlobalConstants.SOCIAL_SHARE_DETAILS_TEMPLATE, varValues);
			positionDetailsMap.put(source, positionDetails);
		}

		return positionDetailsMap;
	}

	public String populatePositionDetails(String template, Map<String, Object> model) {
		String posDetails = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, "mail-templates/" + template, "UTF-8", model);
		return posDetails;
	}

	/**
	 * this will return only active position code and whose status is also
	 * active
	 *
	 * @return
	 * @throws RecruizException
	 */
	
	public Set<String> getPositionCodeForLoggedInUser() throws RecruizException {

		List<Position> posList = null;
		Set<String> positionCodes = new HashSet<>();

		if (userService.isLoggedInUserVendor()) {
			posList = vendorService.getLoggedInVendorPosition();
		} else {
			if (checkPermissionService.isSuperAdmin()) {
				posList = positionRepository.findAll();
			} else {
				List<Team> teams = teamService.getAllTeamsForCurrentUser();
				if (teams == null || teams.isEmpty()) {
					posList = positionRepository.findDistinctByStatusAndOwnerOrHrExecutivesIn(Status.Active.getDisplayName(),
							userService.getLoggedInUserEmail(), userService.loggedInUserSet());
				} else {
					posList = positionRepository.findDistinctByStatusAndOwnerOrHrExecutivesInOrTeamIn(Status.Active.getDisplayName(),
							userService.getLoggedInUserEmail(), userService.loggedInUserSet(), teams);
				}

			}

			calculateFinalStatusForPositions(posList);
		}

		if (posList != null && !posList.isEmpty()) {
			for (Position position : posList) {
				if (position.getFinalStatus().equalsIgnoreCase(Status.Active.getDisplayName())) {
					positionCodes.add(position.getPositionCode());
				}
			}
		}
		return positionCodes;
	}

	@Transactional(readOnly = true)
	public List<Position> getDummyPositions() {
		return positionRepository.findByDummy(true);
	}

	@Transactional(readOnly = true)
	public void sendNearClosureReminder() {
		java.sql.Date startDate = new java.sql.Date(new Date().getTime());
		java.sql.Date lastDate = new java.sql.Date((new DateTime().plusDays(2).toDate()).getTime());

		List<Position> nearClosurePositions = positionRepository.findByStatusNotAndCloseByDateBetween(Status.Closed.getDisplayName(),
				startDate, lastDate);

		if (null != nearClosurePositions && !nearClosurePositions.isEmpty()) {
			for (Position position : nearClosurePositions) {

				try {
					Set<User> positionHrs = position.getHrExecutives();
					List<String> emailListToBeNotified = new ArrayList<>();

					if (null != positionHrs && !positionHrs.isEmpty()) {
						for (User positionUser : positionHrs) {
							emailListToBeNotified.add(positionUser.getEmail());
						}
					}

					// adding position owner to list of users to be notified
					if (null != emailListToBeNotified && !emailListToBeNotified.isEmpty()
							&& !emailListToBeNotified.contains(position.getOwner())) {
						emailListToBeNotified.add(position.getOwner());
					}

					String templateName = "email-template-position-closure-reminder.html";
					Map<String, Object> emailBodyVariableMap = new HashMap<>();
					emailBodyVariableMap.put("positionName", position.getTitle());
					emailBodyVariableMap.put("clientName", position.getClient().getClientName());
					emailBodyVariableMap.put("closeByDate", position.getCloseByDate());
					emailBodyVariableMap.put("currentStatus", Status.valueOf(position.getStatus()).getDisplayName());

					emailTemplateDataService.initEmailBodyDefaultVariables(emailBodyVariableMap);

					String template = emailTemplateDataService.getHtmlContentFromFile(emailBodyVariableMap, templateName);
					String buttonLink = baseUrl + "/web/position-details?pid=" + position.getId();
					String renderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(template, buttonLink, "View Position");
					String subject = "Gentle Reminder for " + position.getTitle() + " position closure";

					emailService.sendEmail(emailListToBeNotified, renderedTemplate, subject);
				} catch (RecruizException e) {
					logger.warn(e.getMessage(), e);
				}

			}
		}
	}

	public String getRenderedJDTemplateStringForPosition(String positionCode) {
		String jdContent = null;

		Position position = positionRepository.findOneByPositionCode(positionCode);
		if (null != position) {
			jdContent = emailTemplateDataService.getRenderedTemplateForJD(position);
		}
		return jdContent;
	}

	// to remove interviewer from position
	public Set<ClientInterviewerPanel> removeInterviewer(Long pid, Long id) {
		Position position = positionRepository.findOne(pid);
		ClientInterviewerPanel clientInterviewerPanelToRemove = interviewPanelService.findOne(id);
		Set<ClientInterviewerPanel> existingInterviewPanel = position.getInterviewers();

		if (existingInterviewPanel.contains(clientInterviewerPanelToRemove)) {
			existingInterviewPanel.remove(clientInterviewerPanelToRemove);
			position.setInterviewers(existingInterviewPanel);
			save(position);

			// adding in position activity
			PositionActivity positionActivity;
			try {
				positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
						NotificationEvent.POSITION_MODIFIED.getDisplayName(),
						"Interviewer " + clientInterviewerPanelToRemove.getName() + " removed from position. ", new Date(),
						position.getPositionCode(), position.getTeam());

				positionActivityService.addActivity(positionActivity);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return existingInterviewPanel;
	}

	/**
	 * To add HRs to a position
	 *
	 * @param hrids
	 * @param pid
	 * @return
	 */
	public Set<User> addHrToPosition(List<Long> hrids, Long pid) {
		Position position = positionRepository.findOne(pid);
		String hrNames = "";
		List<String> hrEmails = new ArrayList<>();
		Set<User> existingHrs = position.getHrExecutives();
		for (Long hrId : hrids) {
			User hrToAdd = userService.findOne(hrId);
			hrEmails.add(hrToAdd.getEmail());
			existingHrs.add(hrToAdd);
			if (hrNames.isEmpty()) {
				hrNames = hrToAdd.getName();
			} else {
				hrNames = hrNames + ", " + hrToAdd.getName();
			}
		}

		position.setHrExecutives(existingHrs);
		positionRepository.save(position);
		
		try {
			
			String commaAdd = String.join(",", hrEmails);
			// adding in position activity
			PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					NotificationEvent.POSITION_MODIFIED.getDisplayName(),
					"The following email id's have been ("+commaAdd+")"+ActivityMessageConstants.getHRAddedToPositionMsg(userService.getLoggedInUserObject()), new Date(),
					position.getPositionCode(), position.getTeam());
			positionActivityService.addActivity(positionActivity);

			// send email here
			final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
			String emailSubject = "You have been added to '" + position.getTitle() + "'";
			Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(position);
			String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + position.getId();
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(hrEmails, masterRenderedTemplate, emailSubject, true);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return existingHrs;
	}

	@Transactional
	public Set<User> removeHrFromPosition(Long hrId, Long pid) {
		Position position = positionRepository.findOne(pid);
		Set<User> existingHrs = position.getHrExecutives();

		User hrToRemove = userService.findOne(hrId);
		position.getHrExecutives().remove(hrToRemove);
		position.setHrExecutives(position.getHrExecutives());
		positionRepository.save(position);

		// adding in position activity
		PositionActivity positionActivity;
		try {
			positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					NotificationEvent.POSITION_MODIFIED.getDisplayName(),
					"HR Executive " + hrToRemove.getName() + " removed from position "
							+ notificationService.getFormattedPositionClientData(position.getId()),
					new Date(), position.getPositionCode(), position.getTeam());

			positionActivityService.addActivity(positionActivity);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}

		return existingHrs;
	}

	/**
	 *
	 * To add vendors to a position
	 *
	 * @param vids
	 * @param pid
	 * @return
	 */
	public Set<Vendor> addVendorToPosition(List<Long> vids, Long pid) {
		String vendorNames = "";
		Position position = positionRepository.findOne(pid);
		Set<Vendor> vendors = position.getVendors();
		List<String> vendorEmails = new ArrayList<>();
		for (Long vid : vids) {
			Vendor vendorToAdd = vendorService.findOne(vid);
			vendorEmails.add(vendorToAdd.getEmail());
			vendors.add(vendorToAdd);
			if (vendorNames.isEmpty()) {
				vendorNames = vendorToAdd.getName();
			} else {
				vendorNames = vendorNames + ", " + vendorToAdd.getName();
			}
		}
		position.setVendors(vendors);
		positionRepository.save(position);

		// adding in position activity
		PositionActivity positionActivity;
		try {
			positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					NotificationEvent.POSITION_MODIFIED.getDisplayName(), vendorNames + " added to position as Vendor(s). ", new Date(),
					position.getPositionCode(), position.getTeam());
			positionActivityService.addActivity(positionActivity);

			// send email to vendor users
			final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
			String emailSubject = "You have been added to '" + position.getTitle() + "'";
			Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(position);
			String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + position.getId();
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(vendorEmails, masterRenderedTemplate, emailSubject, true);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return vendors;
	}

	/**
	 * TO remove vendors from a position
	 *
	 * @param vid
	 * @param pid
	 * @return
	 */
	public Set<Vendor> removeVendorFromPosition(Long vid, Long pid) {
		Position position = positionRepository.findOne(pid);
		Set<Vendor> existingVendors = position.getVendors();

		Vendor vendorToRemove = vendorService.findOne(vid);
		existingVendors.remove(vendorToRemove);

		position.setVendors(existingVendors);
		positionRepository.save(position);

		// adding in position activity
		PositionActivity positionActivity;
		try {
			positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					NotificationEvent.POSITION_MODIFIED.getDisplayName(), "Vendor " + vendorToRemove.getName() + " removed from position. ",
					new Date(), position.getPositionCode(), position.getTeam());

			positionActivityService.addActivity(positionActivity);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return existingVendors;
	}

	/**
	 * To add position added activity in client and position
	 *
	 * @param newPosition
	 * @throws RecruizException
	 */
	public void addPositionCreateActivity(Position newPosition) throws RecruizException {

		// adding in client activity
		ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.POSITION_CREATED.getDisplayName(),
				ActivityMessageConstants.getPositionCreatedByMsg(userService.getLoggedInUserObject()), new Date(),
				newPosition.getClient().getId());
		clientActivityService.addActivity(activity);

		// adding in position activity
		PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				NotificationEvent.POSITION_CREATED.getDisplayName(),
				ActivityMessageConstants.getCreatedByMsg(userService.getLoggedInUserObject()), new Date(), newPosition.getPositionCode(),
				newPosition.getTeam());

		positionActivityService.addActivity(positionActivity);
	}

	public Page<Position> getAllPositionByStatusAndHrNotIn(String status, Set<User> existingHrs, Pageable pageable) {
		Page<Position> positions = positionRepository.findByStatusAndHrExecutivesNotIn(status, existingHrs, pageable);
		return positions;
	}

	// to add HR executive to List of positions
	@Transactional
	public void assignHrToPositions(Long hrId, List<Long> positionIds) {
		List<Long> userId = new ArrayList<>();
		userId.add(hrId);
		for (Long pid : positionIds) {
			addHrToPosition(userId, pid);
		}
	}

	public List<Position> getAllPositionByClientAndHrIn(Client client, Set<User> positionHrs, String status) {
		if (null != status && !status.trim().isEmpty()) {
			return positionRepository.findByClientAndStatusAndHrExecutivesIn(client, status, positionHrs);
		}
		return positionRepository.findByClientAndHrExecutivesIn(client, positionHrs);
	}

	public List<String> getDistinctIndustryForCareerSite() {
		return positionRepository.getDistinctIndustryForCareerSite(Status.Active.toString());
	}

	public List<String> getDistinctLocationForCareerSite() {
		return positionRepository.getDistinctLocationForCareerSite(Status.Active.toString());
	}

	public int getTotalPositionCount() {
		Integer count = positionRepository.getTotalOpenedPosition();
		if (null == count) {
			count = 0;
		}
		return count.intValue();
	}

	public long getTotalPositionCountForLoggedInUser() throws RecruizException {
		Long count = 0L;
//		List<Team> teams = teamService.getAllTeamsForCurrentUser();
		Set<User> hrList = new HashSet<>();
		hrList.add(userService.getLoggedInUserObject());
//		if (teams == null || teams.isEmpty())
			count = positionRepository.countOwnerOrHrExecutivesInAndStatus(userService.getLoggedInUserEmail(), hrList,
					Status.Active.toString());
//		else
//			count = positionRepository.countOwnerOrHrExecutivesInOrTeamInAndStatus(userService.getLoggedInUserEmail(), hrList, teams,
//					Status.Active.toString());

		if (count == null)
			return 0L;

		return count;

	}

	public long getTotalPositionCountForLoggedInUserManager() throws RecruizException {
		Long count = 0L;
		List<Team> teams = teamService.getAllTeamsForCurrentUser();
		Set<User> hrList = new HashSet<>();
		hrList.add(userService.getLoggedInUserObject());
		if (teams == null || teams.isEmpty())
			count = positionRepository.countOwnerOrHrExecutivesInAndStatus(userService.getLoggedInUserEmail(), hrList,
					Status.Active.toString());
		else
			count = positionRepository.countOwnerOrHrExecutivesInOrTeamInAndStatus(userService.getLoggedInUserEmail(), hrList, teams,
					Status.Active.toString());

		if (count == null)
			return 0L;

		return count;

	}
	
	// to get sum of active position count
	public int getTotalPositionCountForHR(List<Long> ids) {
		Integer count = positionRepository.getTotalOpenedPositionHr(ids);
		if (null == count) {
			count = 0;
		}
		return count.intValue();
	}

	public void sendEmailToTeam(String emailSubject, final String emailTemplate, Long teamId, Position position) throws RecruizException {
		// = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
		// String emailSubject = "You have been added to '" +
		// position.getTitle() + "'";

		Team team = teamService.findOne(teamId);

		for (TeamMember member : team.getMembers()) {
			List<String> memberEmails = new ArrayList<>();
			memberEmails.add(member.getUser().getEmail());

			Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForPositionAddedEmail(position);
			String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + position.getId();
			String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link, "View");
			emailService.sendEmail(memberEmails, masterRenderedTemplate, emailSubject, true);
		}
	}

	public Integer getHrPositionCount(String userEmail, List<Long> teamId, Long userId) {
		Integer cnt = positionRepository.getCountOfPositionForHr(userEmail, teamId, userId);
		if (null == cnt) {
			return 0;
		}
		return cnt;
	}
	
	public Integer getHrPositionCount(String userEmail, Long userId) {
		Integer cnt = positionRepository.getCountOfPositionForHr(userEmail, userId);
		if (null == cnt) {
			return 0;
		}
		return cnt;
	}

	// to get position count by client and date interval
	public String getPositionCountByClientAndDateInterval(Long clientId, Integer intervalInMonths) {
		return positionRepository.getPositionCountByClientForDateRange(clientId, intervalInMonths);
	}

	// to get position total opening c by client and date interval
	public String getTotalPositionsOpeiningByClientAndDateInterval(Long clientId, Integer intervalInMonths) {
		return positionRepository.getTotalOpeningByClientForDateRange(clientId, intervalInMonths);
	}

	// to get position total opening c by client and date interval
	public String getTotalPositionsByStatusClientAndDateInterval(Long clientId, String status, Integer intervalInMonths) {
		return positionRepository.getStatusCountByClientForDateRange(clientId, status, intervalInMonths);
	}

	// to get position total opening c by client and date interval
	public String getTotalPositionsByStatusesClientAndDateInterval(Long clientId, List<String> statuses, Integer intervalInMonths) {
		return positionRepository.getStatusCountByClientForDateRange(clientId, statuses, intervalInMonths);
	}

	// to get position total opening c by client and date interval
	public String getCountForLessThan1MonthClosureDate(Long clientId) {
		return positionRepository.getCountForLessThan1MonthClosureDate(clientId);
	}

	// to get position total opening c by client and date interval
	public String getCountFor1To2MonthClosureDate(Long clientId) {
		return positionRepository.getCountFor1To2MonthClosureDate(clientId);
	}

	// to get position total opening c by client and date interval
	public String getCountForMoreThan2MonthClosureDate(Long clientId) {
		return positionRepository.getCountForMoreThan2MonthClosureDate(clientId);
	}

	public List<Position> getPositionByClientAndDateInterval(Client client, Date startDate, Date endDate) {
		return positionRepository.findByClientAndCreationDateBetween(client, startDate, endDate);
	}
	
	public List<Position> getPositionByClientAndPositionNamesInAndDateInterval(Client client,List<String> posNames, Date startDate, Date endDate) {
		return positionRepository.findByClientAndTitleInAndCreationDateBetween(client,posNames, startDate, endDate);
	}
	public List<Position> getPositionByClientAndPositionCodeInAndDateInterval(Client client,List<String> posCodes, Date startDate, Date endDate) {
		return positionRepository.findByClientAndPositionCodeInAndCreationDateBetween(client,posCodes, startDate, endDate);
	}
	
	
	

	public void deleteCustomFieldWithName(String name) {
		positionRepository.deleteCustomFieldWithName(name);
	}

	/**
	 * @param teamIds
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Position> getAllPositionByTeamsInAndModifiationDateBetween(List<Long> teamIds, Date startDate, Date endDate) {
		return positionRepository.getPositionByTeams(teamIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
	}

	public List<Object> getDashboardPositionGraphData(List<Long> teamIds, Date startDate, Date endDate) {
		return positionRepository.getDashboardPositionGraphData(teamIds, userService.getLoggedInUserObject().getUserId(),
				new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
	}

	public List<Object> getDashboardClientGraphData(List<Long> teamIds, Date startDate, Date endDate) {
		return positionRepository.getDashboardClientGraphData(userService.getLoggedInUserEmail(), teamIds,
				userService.getLoggedInUserObject().getUserId(), new java.sql.Date(startDate.getTime()),
				new java.sql.Date(endDate.getTime()));
	}

	public List<Position> getPositionByTeamAndStatusActive(Team team) {
		return positionRepository.findByTeamAndStatus(team, Status.Active.getDisplayName());
	}

	public List<Position> getPositionByTeamAndStatusNotActive(Team team) {
		Set<String> statusNotIn = new HashSet<>();
		statusNotIn.add(Status.Active.getDisplayName());
		return positionRepository.findByTeamAndStatusIsNotIn(team, statusNotIn);
	}

	public List<Position> getPositionByTeamAndStatusNotActiveAndDateRange(Team team, Date startDate, Date endDate) {
		Set<String> statusNotIn = new HashSet<>();
		statusNotIn.add(Status.Active.getDisplayName());
		return positionRepository.findByTeamAndStatusIsNotInAndModificationDateBetween(team, statusNotIn, startDate, endDate);
	}
	
	public List<Position> getPositionByTeamAndStatusClosedAndDateRange(Team team, Date startDate, Date endDate) {
		Set<String> statuses = new HashSet<>();
		statuses.add(Status.Closed.getDisplayName());
		return positionRepository.findByTeamAndStatusAndClosedDateBetween(team, statuses, startDate, endDate);
	}
	
	public List<String> getPositionByTeamAndStatusClosedAndDateRangeNativeQuery(Team team, Date startDate, Date endDate) {
		Set<String> statuses = new HashSet<>();
		statuses.add(Status.Closed.getDisplayName());
		return positionRepository.findByTeamAndStatusAndClosedDateBetweenNative(team, statuses, startDate, endDate);
	}
	
	//Modified by Sajin
	public List<Long> getPositionIdsByTeamAndStatusClosedAndDateRange(Team team, Date startDate, Date endDate) {
		
		Set<String> statuses = new HashSet<>();
		statuses.add(Status.Closed.getDisplayName());
		return positionRepository.findPositionIdByTeamAndStatusAndClosedDateBetweenNative(team, statuses);
	}
	
	
	
	
	//@author - Sajin
	public List<String> getPositionNamesfromPositionCodes(List<String> positionCodes) {
		Set<String> statuses = new HashSet<>();
		statuses.add(Status.Closed.getDisplayName());
		return positionRepository.getPositionNamesfromPositionCodes(positionCodes);
	}
	
	//@author - Sajin
	public List<String> getPositionNamesfromPositionIDs(List<Long> positionIds) {
		Set<String> statuses = new HashSet<>();
		statuses.add(Status.Closed.getDisplayName());
		return positionRepository.getPositionNamesfromPositionIDs(positionIds);
	}
	
	
	

	public List<Position> getPositionByTeamAndVerticalAndLocationAndClients(Team team, String vertical, String location,
			List<Long> clientIds) {
		String verticalOrginalText = vertical;
		if (null == vertical || vertical.trim().isEmpty()) {
			vertical = "%";
		}

		if (null != clientIds && !clientIds.isEmpty()) {
			return positionRepository.getPositionsByClientsAndLocationAndTeamAndVerticals(location, verticalOrginalText, vertical,
					team.getId(), clientIds);
		}
		return positionRepository.getPositionsByLocationAndTeamAndVerticals(location, verticalOrginalText, vertical, team.getId());
	}

	public List<String> getPositionCodeByTeamAndVerticalAndLocationAndClients(Team team, String vertical, String location,
			List<Long> clientIds, Date startDate, Date endDate) {
		String verticalOrginalText = vertical;
		if (null == vertical || vertical.trim().isEmpty()) {
			vertical = "%";
		}

		if (null != clientIds && !clientIds.isEmpty()) {
			return positionRepository.getPositionsCodeByClientsAndLocationAndTeamAndVerticalsAndDateRange(location, verticalOrginalText,
					vertical, team.getId(), clientIds, startDate, endDate);
		}
		return positionRepository.getPositionsCodeByLocationAndTeamAndVerticalsAndDateRange(location, verticalOrginalText, vertical,
				team.getId(), startDate, endDate);
	}
	
	//Added by Sajin for Teamware custom reports
	public List<BigInteger> getPositionIDsByTeamAndVerticalAndLocationAndClients(Team team, String vertical, String location,
			List<Long> clientIds, Date startDate, Date endDate) {
		String verticalOrginalText = vertical;
		if (null == vertical || vertical.trim().isEmpty()) {
			vertical = "%";
		}

		if (null != clientIds && !clientIds.isEmpty()) {
			return positionRepository.getPositionIdsByClientsAndLocationAndTeamAndVerticalsAndDateRange(location, verticalOrginalText,
					vertical, team.getId(), clientIds, startDate, endDate);
		}
		return positionRepository.getPositionIdsByLocationAndTeamAndVerticalsAndDateRange(location, verticalOrginalText, vertical,
				team.getId(), startDate, endDate);
	}
	

	public List<Position> getPositionsByTeamIds(List<Long> teamIds) {
		String ids = Joiner.on(",").join(teamIds.toArray());
		return positionRepository.getPositionByTeams(ids);
	}

	public List<String> getPositionsCodeByTeamIds(List<Long> teamIds) {
		return positionRepository.getPositionCodeByTeams(teamIds);
	}

	public List<String> getPositionsCodeByTeamIdsAndStatus(List<Long> teamIds, String status) {
		return positionRepository.getPositionCodeByTeamsAndStatus(teamIds, status);
	}

	public List<String> getPositionsCodeByTeamIdsAndStatusAndDateRange(List<Long> teamIds, String status, Date startDate, Date endDate) {
		return positionRepository.getPositionCodeByTeamsAndStatusAndDateRange(teamIds, status, startDate, endDate);
	}
	
	//Added by Sajin - Teamware Reports
	public List<Long> getIDsByTeamIdsAndStatusAndDateRange(List<Long> teamIds, String status, Date startDate, Date endDate) {
		return positionRepository.getIdsByTeamsAndStatusAndDateRange(teamIds, status);
	}
	

	public List<Position> getPositionsByTeamIdsAndVertical(List<Long> teamIds, String vertical) {
		String ids = Joiner.on(",").join(teamIds.toArray());
		return positionRepository.getPositionByTeamsAndVertical(ids, vertical);
	}

	public List<String> getPositionsCodeByTeamIdsAndVertical(List<Long> teamIds, String vertical) {
		String ids = Joiner.on(",").join(teamIds.toArray());
		return positionRepository.getPositionCodeByTeamsAndVertical(ids, vertical);
	}

	public List<Object> getPrefReportObjectForTeamware(Date start, Date end) {
		return positionRepository.getPositionForteamwarePrefTrends(new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
	}

	public List<Long> getPositionIdsForUser(List<Long> teamIds) {
		return positionRepository.getPositionIdsForUserWithTeam(teamIds, userService.getLoggedInUserEmail(),
				userService.getLoggedInUserObject().getUserId());
	}

	public List<InterviewSchedule> getInterviewScheduleForUser(List<Long> teamIds, Date start, Date end) {
		return positionRepository.getAllSchedulesForUser(userService.getLoggedInUserEmail(), teamIds, userService.getLoggedInUserEmail(),
				userService.getLoggedInUserObject().getUserId(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
	}

	public Long getTotalPositionOpenedForUser(List<Long> teamIds) {
		return positionRepository.getTotalOpenedPositionForUser(teamIds, userService.getLoggedInUserEmail(),
				userService.getLoggedInUserObject().getUserId());
	}

	public String getClientNameFromPositioncode(String pcode) {
		return positionRepository.getClientNameForPosition(pcode);
	}
	
	//Added by Sajin
	public String getClientNameFromPositionId(Long pid) {
		return positionRepository.getClientNameForPositionID(pid);
	}
	
	//Added by Sajin
	public String getClientNameFromPositionId(BigInteger pid) {
		return positionRepository.getClientNameForPositionID(pid);
	}

	public Long getPositionCountForLoggedInUser() throws RecruizException {
//		List<Team> teams = teamService.getAllTeamsForCurrentUser();
		Set<User> hrList = new HashSet<>();
		hrList.add(userService.getLoggedInUserObject());
//		if (teams == null || teams.isEmpty())
			return positionRepository.countOwnerOrHrExecutivesIn(userService.getLoggedInUserEmail(), hrList);

//		return positionRepository.countOwnerOrHrExecutivesInOrTeamIn(userService.getLoggedInUserEmail(), hrList, teams);
	}
	
	public Long getPositionCountForLoggedInUserManager() throws RecruizException {
		List<Team> teams = teamService.getAllTeamsForCurrentUser();
		Set<User> hrList = new HashSet<>();
		hrList.add(userService.getLoggedInUserObject());
		if (teams == null || teams.isEmpty())
			return positionRepository.countOwnerOrHrExecutivesIn(userService.getLoggedInUserEmail(), hrList);

		return positionRepository.countOwnerOrHrExecutivesInOrTeamIn(userService.getLoggedInUserEmail(), hrList, teams);
	}

	public Long getTotalOpenPositionByPositionCodes(List<String> positioCodes) {
		return positionRepository.getTotalPositionsByPositionCodes(positioCodes);
	}
	
	//Added by Sajin
	public Long getTotalOpenPositionByPositionIDs(List<Long> positionIds) {
		return positionRepository.getTotalPositionsByPositionIDs(positionIds);
	}
	
	//Added by Sajin
	public Long getTotalOpenPositionByPositionIDsBigInt(List<BigInteger> positionIds) {
		return positionRepository.getTotalPositionsByPositionIDsBigInteger(positionIds);
	}

	List<Integer> getWeeksForGicenDateRange(java.sql.Date startDate, java.sql.Date endDate) {
		return positionRepository.getWeeksForGivenDateRangeBasedOnCreationDate(startDate, endDate);
	}

	List<String> getPositionCodesForGivenWeek(Integer weekNo) {
		return positionRepository.getPositionCodeFromWeeks(weekNo);
	}

	public List<String> getPositionCodesByClientAndVerticalAndLocationAndWeek(List<Long> clientIds, List<String> vertical, String location,
			Integer weekNo, Integer year, List<String> statuses, String loggedInUserEmail, List<Long> teamIds, Long userId) {

		if (clientIds == null || clientIds.isEmpty()) {
			clientIds = positionRepository.getDistinctClientIds();
		}
		if (null == vertical || vertical.isEmpty()) {
			vertical = positionRepository.getDistinctVertical();
		}

		if (null != location && !location.isEmpty()) {
			return positionRepository.getPositionCodesByClientVerticalAndLocation(weekNo, year, clientIds, vertical, location, statuses,
					loggedInUserEmail, teamIds, userId);
		}
		return positionRepository.getPositionCodesByClientVertical(weekNo, year, clientIds, vertical, statuses, loggedInUserEmail, teamIds,
				userId);
	}

	public List<Integer> getWeeksForGivenYearAndMonth(String month, Integer year, Date startDate, Date endDate) {
		return positionRepository.getWeekForGivenYearAndMonth(month, year, new java.sql.Date(startDate.getTime()),
				new java.sql.Date(endDate.getTime()));
	}

	public List<String> getMonthsForGiveYear(Integer year, Date startDate, Date endDate) {
		return positionRepository.getMonthForGivenYear(year, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
	}

	public List<Integer> getYearsForGivenDateRange(Date startDate, Date endDate) {
		return positionRepository.getYearForGivenDate(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
	}
	
	public List<Position> getAllPositionsByModifiationDateBetween(Date startDate, Date endDate) {
		return positionRepository.getPositionsBetweenModificationDates(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
	}

	public int getJoinedOREmployeeCount(String positionCode) throws RecruizException {
		
		List<RoundCandidate> countJoined = roundCandidateService.getByPostionCodeAndStatus(positionCode, "Joined");
		List<RoundCandidate> countEmployee = roundCandidateService.getByPostionCodeAndStatus(positionCode, "Employee");
		
		return countJoined.size()+countEmployee.size();
	}
	
	//Added by Sajin
	public List <String> getPositionCodesforPostionIds (List<Long> positionIds) {	
		
		return positionRepository.getPositionCodesforPostionIds(positionIds);
	}

	public RestResponse addPositionOfferCost(PositionOfferCostDTO costDTO) throws RecruizException {

		return positionOfferCostService.addPositionOfferCost(costDTO);
	}

	public RestResponse updatePositionOfferCost(PositionOfferCostDTO costDTO) throws RecruizException {

		return positionOfferCostService.updatePositionOfferCost(costDTO);
	}

	public RestResponse getPositionOfferCostById(String id) {

		try{
		PositionOfferCost data = positionOfferCostService.findOne(Long.valueOf(id));

		if(data!=null)
			return new RestResponse(RestResponse.SUCCESS, data, "get positionOfferCost details !");
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, "", "Internal server error !");
		}
		return new RestResponse(RestResponse.FAILED, "", "Not Found any result !");

	}

	public RestResponse deletePositionOfferCostById(String id) {

		try{
			PositionOfferCost data = positionOfferCostService.findOne(Long.valueOf(id));

			if(data!=null){
				positionOfferCostService.delete(Long.valueOf(id));
				return new RestResponse(RestResponse.SUCCESS,"delete positionOfferCost details !");
			}
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, "", "Internal server error !");
		}
		return new RestResponse(RestResponse.FAILED, "", "Not Found any result !");
	}

	public RestResponse getAllPositionOfferCost(String positionId) {
		
		if(positionId==null || positionId.isEmpty())
			return new RestResponse(RestResponse.FAILED, "positionId parameter required !");
		
		try{
		    	List<PositionOfferCost> data = positionOfferCostService.findAllByPosition(positionId);
				return new RestResponse(RestResponse.SUCCESS, data, "get positionOfferCost details by positionId !");
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, "", "Internal server error !");
		}
		
	}

	public String getCountForPastCloseByDate(Long id) {
		// TODO Auto-generated method stub
		return positionRepository.getCountForPastCloseByDate(id,new Date());
	}

	public Position getPositionByNameAndClientName(long clientId, String positionName) {
		return positionRepository.getPositionByNameAndClientName(clientId,positionName);
	}

}
