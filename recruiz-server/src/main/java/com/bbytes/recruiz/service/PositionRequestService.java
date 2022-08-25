package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.enums.UserType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.PositionRequestRepository;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.utils.ActivityMessageConstants;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;

@Service
public class PositionRequestService extends AbstractService<PositionRequest, Long> {

	private PositionRequestRepository positionRequestRepository;

	@Autowired
	public PositionRequestService(PositionRequestRepository positionRequestRepository) {
		super(positionRequestRepository);
		this.positionRequestRepository = positionRequestRepository;
	}

	@Autowired
	private UserService userService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private PositionActivityService positionActivityService; 
	
	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Transactional(readOnly = true)
	public Page<PositionRequest> getAllPositionRequest(Pageable pageable) {
		Page<PositionRequest> allRequestedPosition = positionRequestRepository
				.findByRequestedByEmail(userService.getLoggedInUserEmail(), pageable);
		return allRequestedPosition;
	}

	@Transactional(readOnly = true)
	public Page<PositionRequest> getAllPositionRequestForAppUser(Pageable pageable) {
	    Set<String> statusIn = new HashSet<>();
	    statusIn.add(PositionRequestStatus.Pending.name());
	    statusIn.add(PositionRequestStatus.Closed.name());
	    statusIn.add(PositionRequestStatus.Rejected.name());
	    statusIn.add(PositionRequestStatus.OnHold.name());
	    statusIn.add(PositionRequestStatus.InProcess.name());
	    
		Page<PositionRequest> allRequestedPosition = positionRequestRepository.findByStatusIn(statusIn,pageable);
		return allRequestedPosition;
	}

	@Transactional(readOnly = true)
	public Page<PositionRequest> getAllPositionRequestForAppUserByClient(String clientName, Pageable pageable) {
	    Set<String> statusIn = new HashSet<>();
	    statusIn.add(PositionRequestStatus.Pending.name());
	    statusIn.add(PositionRequestStatus.Closed.name());
	    statusIn.add(PositionRequestStatus.Rejected.name());
	    statusIn.add(PositionRequestStatus.OnHold.name());
	    
		Page<PositionRequest> allRequestedPosition = positionRequestRepository.findByClientNameAndStatusIn(clientName,statusIn, pageable);
		return allRequestedPosition;
	}

	@Transactional
	public PositionRequest requestPosition(PositionDTO positionDTO, String fileName, MultipartFile multipartFile,
			String clientName) throws RecruizException, IOException {
		if (userService.getLoggedInUserObject().getUserType() != null && !userService.getLoggedInUserObject()
				.getUserType().equalsIgnoreCase(UserType.DepartmentHead.getDisplayName())) {
			return null;
		}
		PositionRequest positionRequest = new PositionRequest();
		positionRequest.setClientName(clientName);
		positionRequest.setTitle(positionDTO.getTitle());
		positionRequest.setLocation(positionDTO.getLocation());
		positionRequest.setTotalPosition(positionDTO.getTotalPosition());
		positionRequest.setCloseByDate(positionDTO.getCloseByDate());
		positionRequest.setPositionUrl(positionDTO.getPositionUrl());
		positionRequest.setGoodSkillSet(positionDTO.getGoodSkillSet());
		positionRequest.setReqSkillSet(positionDTO.getReqSkillSet());
		positionRequest.setEducationalQualification(positionDTO.getEducationalQualification());
		positionRequest.setExperienceRange(positionDTO.getExperienceRange());
		positionRequest.setRemoteWork(Boolean.parseBoolean(positionDTO.getRemoteWork()));
		positionRequest.setType(positionDTO.getType());
		positionRequest.setMaxSal(positionDTO.getMaxSal());
		positionRequest.setMinSal(positionDTO.getMinSal());
		positionRequest.setSalUnit(positionDTO.getSalUnit());
		positionRequest.setIndustry(positionDTO.getIndustry());
		positionRequest.setFunctionalArea(positionDTO.getFunctionalArea());
		positionRequest.setNotes(positionDTO.getNotes());
		positionRequest.setDescription(positionDTO.getDescription());
		positionRequest.setRequestedByEmail(userService.getLoggedInUserEmail());
		positionRequest.setRequestedByName(userService.getLoggedInUserObject().getName());
		positionRequest.setRequestedByPhone(userService.getLoggedInUserObject().getMobile());
		positionRequest = positionRequestRepository.save(positionRequest);

		// storing the JD file here
		if (multipartFile != null && !multipartFile.isEmpty()) {
			uploadFileService.createFolderStructureForRequestedPosition(positionRequest.getId() + "");
			File jdFile = fileService.multipartToFile(multipartFile);
			String jdPath = uploadFileService.uploadFileToLocalServer(jdFile, fileName, "requested position",
					positionRequest.getId() + "");
			String pdfFilePath = fileService.convert(jdPath);
			positionRequest.setJdPath(pdfFilePath);
			positionRequestRepository.save(positionRequest);
		}

		// sending email to all the hr managers -- Recruiz-QA REZQA-26
		sendEmailToSAAndManagers(positionDTO.getTitle(), clientName, positionRequest, "new");

		return positionRequest;
	}

	/**
	 * Send email to Super admin and all hr managers
	 * 
	 * @param positionDTO
	 * @param clientName
	 * @param positionRequest
	 * @throws RecruizException
	 */
	private void sendEmailToSAAndManagers(String reuestedPositionName, String clientName,
			PositionRequest positionRequest, String notificaionType) throws RecruizException {
		Set<User> hrManagerUsers = userService.getAllUserByPermissionName(PermissionConstant.MANAGER_SETTING);
		List<String> userEmails = new ArrayList<>();

		for (User user : hrManagerUsers) {
			userEmails.add(user.getEmail());
		}

		Map<String, Object> emailBodyVariableMap = new HashMap<>();
		emailBodyVariableMap.put(GlobalConstants.CLIENT_NAME, clientName);
		emailBodyVariableMap.put(GlobalConstants.POSITION_NAME, reuestedPositionName);

		String clientLabel = "Department";

		emailBodyVariableMap.put(GlobalConstants.CLIENT_LABEL, clientLabel);
		String templateFileName = "";

		if (notificaionType.equalsIgnoreCase("new")) {
			templateFileName = "email-template-position-requested.html";
		} else {
			templateFileName = "email-template-position-request-modified.html";
		}

		String emailLink = fileService.getBaseUrl() + "/web/department-head-position-details?pid="
				+ positionRequest.getId();

		emailTemplateDataService.initEmailBodyDefaultVariables(emailBodyVariableMap);
		String templateFromFile = emailTemplateDataService.getHtmlContentFromFile(emailBodyVariableMap,
				templateFileName);
		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateFromFile,
				emailLink, "View Requested Position");

		String emailSubject = "A position titled " + reuestedPositionName + " is requested under " + clientName;
		emailService.sendEmail(userEmails, renderedMasterTemplate, emailSubject, true);
	}

	@Transactional(readOnly = true)
	public Page<PositionRequest> getAllInProcessPositionRequest(Pageable pageable) {
		Page<PositionRequest> allRequestedPosition = positionRequestRepository
				.findByStatus(PositionRequestStatus.InProcess.toString(), pageable);
		return allRequestedPosition;
	}

	@Transactional
	public PositionRequest getPositionRequestDetails(String id) {
		PositionRequest requestedPosition = positionRequestRepository.findOne(Long.parseLong(id));
		return requestedPosition;
	}

	/**
	 * this method to add display name of employment type for position
	 */
	@Transactional(readOnly = true)
	public void getEmploymentTypeDisplayName(PositionRequest requestedPosition) {
		if (EmploymentType.FullTime.toString().equalsIgnoreCase(requestedPosition.getType()))
			requestedPosition.setTypeDisplayName(EmploymentType.FullTime.getDisplayName());
		else if (EmploymentType.PartTime.toString().equalsIgnoreCase(requestedPosition.getType()))
			requestedPosition.setTypeDisplayName(EmploymentType.PartTime.getDisplayName());
		else
			requestedPosition.setTypeDisplayName(requestedPosition.getType());
	}

	@Transactional
	public Page<PositionRequest> getAllNewRequest(Pageable pageable) {
		Page<PositionRequest> requestedPositions = positionRequestRepository
				.findByStatus(PositionRequestStatus.Pending.toString(), pageable);
		return requestedPositions;
	}

	@Transactional
	public Page<PositionRequest> getAllNewRequestByClient(Pageable pageable, String clientName) {
		Page<PositionRequest> requestedPositions = positionRequestRepository
				.findByStatusAndClientName(PositionRequestStatus.Pending.toString(), clientName, pageable);
		return requestedPositions;
	}

	@Transactional
	public PositionRequest updateRequestStatus(Long id, String status, String comment) throws RecruizException {
		PositionRequest requestedPosition = positionRequestRepository.findOne(id);
		String oldStatus = requestedPosition.getStatus();
		requestedPosition.setStatus(status);
		requestedPosition.getPositionRequestNotes().add(comment);
		requestedPosition = positionRequestRepository.save(requestedPosition);

		// sending email to deptHead on status change.
		String requestOldStatus = PositionRequestStatus.valueOf(oldStatus).getDisplayName();
		String requestNewStatus = PositionRequestStatus.valueOf(status).getDisplayName();
		sendEmailToDeptHeadOnPositionStatusChange(requestNewStatus, requestedPosition, requestOldStatus);
		return requestedPosition;
	}

	/**
	 * sendemail to department head on requested position status change
	 * 
	 * @param status
	 * @param requestedPosition
	 * @param oldStatus
	 * @throws RecruizException
	 */
	public void sendEmailToDeptHeadOnPositionStatusChange(String status, PositionRequest requestedPosition,
			String oldStatus) throws RecruizException {
		String templateName = "requested-position-status-change.html";
		Map<String, Object> emailVariabeMap = new HashMap<String, Object>();

		emailVariabeMap.put("loggedInUserEmail", userService.getLoggedInUserEmail());
		emailVariabeMap.put("logggedInUserName", userService.getLoggedInUserName());
		if (userService.getLoggedInUserObject().getProfileSignature() != null
				&& !userService.getLoggedInUserObject().getProfileSignature().isEmpty()) {
			emailVariabeMap.put("signature", userService.getLoggedInUserObject().getProfileSignature());
		} else {
			emailVariabeMap.put("signature", "");
		}

		emailVariabeMap.put("requstedPositionName", requestedPosition.getTitle());
		emailVariabeMap.put("oldStatus", oldStatus);
		emailVariabeMap.put("newStatus", status);
		emailVariabeMap.put("deptHead", requestedPosition.getRequestedByName());

		String template = emailTemplateDataService.getHtmlContentFromFile(emailVariabeMap, templateName);
		String renderedTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(template);
		List<String> emailList = new ArrayList<>();
		emailList.add(requestedPosition.getRequestedByEmail());
		emailService.sendEmail(emailList, renderedTemplate,
				"Requested postion " + requestedPosition.getTitle() + " status changed");
	}

	@Transactional
	public PositionRequest updatePositionRequest(PositionRequest positionRequest, String fileName,
			MultipartFile multiPartFile) throws RecruizException, IOException {
		if (userService.getLoggedInUserObject().getUserType() != null && !userService.getLoggedInUserObject()
				.getUserType().equalsIgnoreCase(UserType.DepartmentHead.getDisplayName())) {
			return null;
		}
		// storing the JD file here
		String pdfFilePath = "";
		if (multiPartFile != null && !multiPartFile.isEmpty()) {
			uploadFileService.createFolderStructureForRequestedPosition(positionRequest.getId() + "");
			File jdFile = fileService.multipartToFile(multiPartFile);
			String jdPath = uploadFileService.uploadFileToLocalServer(jdFile, fileName, "requested position",
					positionRequest.getId() + "");
			pdfFilePath = fileService.convert(jdPath);
			positionRequest.setJdPath(pdfFilePath);
		}
		positionRequest.setExperienceRange(positionRequest.getExperienceRange());
		if (positionRequest.getStatus().equalsIgnoreCase(PositionRequestStatus.OnHold.toString())
				|| positionRequest.getStatus().equalsIgnoreCase(PositionRequestStatus.Rejected.toString())) {
			positionRequest.setStatus(PositionRequestStatus.Pending.toString());
		}

		positionRequest = positionRequestRepository.save(positionRequest);

		if (positionRequest.getPositionId() > 0) {

			Set<String> goodSkills = new HashSet<String>();
			Set<String> reqSkills = new HashSet<String>();
			Set<String> educationalQual = new HashSet<String>();

			if (positionRequest.getGoodSkillSet() != null && !positionRequest.getGoodSkillSet().isEmpty()) {
				for (String skill : positionRequest.getGoodSkillSet()) {
					goodSkills.add(skill);
				}
			}

			if (positionRequest.getReqSkillSet() != null && !positionRequest.getReqSkillSet().isEmpty()) {
				for (String skill : positionRequest.getReqSkillSet()) {
					reqSkills.add(skill);
				}
			}

			if (positionRequest.getEducationalQualification() != null
					&& !positionRequest.getEducationalQualification().isEmpty()) {
				for (String skill : positionRequest.getEducationalQualification()) {
					educationalQual.add(skill);
				}
			}

			Position existingPosition = positionService.findOne(positionRequest.getPositionId());
			existingPosition.setTitle(positionRequest.getTitle());
			existingPosition.setTotalPosition(positionRequest.getTotalPosition());
			existingPosition.setCloseByDate(positionRequest.getCloseByDate());
			existingPosition.setLocation(positionRequest.getLocation());
			existingPosition.setPositionUrl(positionRequest.getPositionUrl());
			existingPosition.setEducationalQualification(educationalQual);
			existingPosition.setReqSkillSet(reqSkills);
			existingPosition.setGoodSkillSet(goodSkills);
			existingPosition.setExperienceRange(positionRequest.getExperienceRange());
			existingPosition.setType(positionRequest.getType());
			existingPosition.setRemoteWork(positionRequest.isRemoteWork());
			existingPosition.setDescription(positionRequest.getDescription());
			existingPosition.setFunctionalArea(positionRequest.getFunctionalArea());
			existingPosition.setIndustry(positionRequest.getIndustry());
			existingPosition.setMaxSal(positionRequest.getMaxSal());
			existingPosition.setMinSal(positionRequest.getMinSal());
			existingPosition.setJdPath(positionRequest.getJdPath());
			existingPosition.setNotes(positionRequest.getNotes());
			existingPosition.setStatus(positionRequest.getStatus());
			if (pdfFilePath != null && !pdfFilePath.isEmpty()) {
				existingPosition.setJdPath(pdfFilePath);
			}
			positionService.save(existingPosition);
			
			// adding in position activity
			PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
				ActivityMessageConstants.getUpdatedByMsg(userService.getLoggedInUserObject()), new Date(),
				existingPosition.getPositionCode(),existingPosition.getTeam());
			positionActivityService.addActivity(positionActivity);

			// sending notification to position hr on request update by dept
			// head.
			positionService.notificationOnPositionUpdate(existingPosition, null, null);
		} else {
			// send email to all the SA on update of a request -- Recruiz-QA
			// REZQA-79
			sendEmailToSAAndManagers(positionRequest.getTitle(), positionRequest.getClientName(), positionRequest,
					"modification");
		}
		return positionRequest;
	}

	@Transactional
	public PositionRequest getPositionRequestByPositionCode(String positionCode) {
		PositionRequest requestedPosition = positionRequestRepository
				.findByPositionCodeAndRequestedByEmail(positionCode, userService.getLoggedInUserEmail());
		return requestedPosition;
	}

	@Transactional
	public PositionRequest getPositionRequestBypositionId(long positionId) {
		PositionRequest requestedPosition = positionRequestRepository.findByPositionId(positionId);
		return requestedPosition;
	}

	@Transactional(readOnly = true)
	public List<PositionRequest> getAllPositionRequestForSearchIndex(Pageable pageable) {
		List<PositionRequest> result = new ArrayList<>();
		Page<PositionRequest> positionRequests = positionRequestRepository.findAll(pageable);

		if (positionRequests == null)
			return result;

		for (PositionRequest positionRequest : positionRequests) {
			positionRequest.getGoodSkillSet().size();
			positionRequest.getReqSkillSet().size();
			result.add(positionRequest);
		}
		return result;
	}

	@Transactional
	public void assignAndDeleteUser(String userEmailToRemove, User newOwner) {
		List<PositionRequest> requestedPositions = positionRequestRepository.findByRequestedByEmail(userEmailToRemove);

		if (null != requestedPositions && !requestedPositions.isEmpty()) {
			for (PositionRequest positionRequest : requestedPositions) {
				positionRequest.setRequestedByEmail(newOwner.getEmail());
				positionRequest.setRequestedByName(newOwner.getUsername());
				positionRequest.setRequestedByPhone(newOwner.getMobile());
			}

			positionRequestRepository.save(requestedPositions);
		}

	}

}
