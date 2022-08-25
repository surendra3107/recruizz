package com.bbytes.recruiz.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.NotificationRepository;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class NotificationService extends AbstractService<Notification, Long> {

	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	SimpMessagingTemplate template;

	@Autowired
	private UserService userService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;
	
	private NotificationRepository notificationRepository;
	

	@Autowired
	public NotificationService(NotificationRepository notificationRepository) {
		super(notificationRepository);
		this.notificationRepository = notificationRepository;
	}

	// @MessageMapping("/ping")
	// @SendTo("/queue/greetings")
	// public void sendMsg() {
	// this.template.convertAndSendToUser("sourav@beyondbytes.co",
	// "/queue/greetings", "Form notifications");
	// }

	@MessageMapping("/ping")
	@SendTo("/queue/greetings")
	public void sendMsg(String userEmail, String msg) {
		try {
			this.template.convertAndSendToUser(userEmail, "/queue/greetings", msg);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@MessageMapping("/ping")
	@SendTo("/queue/greetings")
	public void sendNotification(Notification notification) {
		if (!userService.getLoggedInUserEmail().equalsIgnoreCase(notification.getUser())) {
			try {

				this.template.convertAndSendToUser(notification.getUser(), "/queue/greetings",
						notification.getMessage());

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	@Transactional(readOnly = true)
	public Page<Notification> getNotificationsForLoggedInUser(Pageable pageable) {
		return notificationRepository.findByUser(userService.getLoggedInUserEmail(), pageable);
	}

	@Transactional(readOnly = true)
	public Page<Notification> getUnviewdNotificationsForLoggedInUser(Pageable pageable) {
		return notificationRepository.findByUserAndViewStateIsFalse(userService.getLoggedInUserEmail(), pageable);
	}

	@Transactional(readOnly = true)
	public Long getUnreadCountForLoggedInUser() {
		return notificationRepository.countByViewStateAndUser(false, userService.getLoggedInUserEmail());
	}

	@Transactional
	public Notification getNotificationDetails(String id) {
		Notification notification = notificationRepository.findOne(Long.parseLong(id));
		if (notification == null) {
			return null;
		}
		notification.setReadState(true);
		notification = notificationRepository.save(notification);
		return notification;
	}

	@Transactional(readOnly = true)
	public String getFormattedPositionClientData(Long positionId) throws RecruizException {
		Position position = positionService.getPositionById(positionId);
		String formattedPositionClientData = "";
		formattedPositionClientData = " $@" + position.getClient().getId() + "_" + position.getClient().getClientName()
				+ " $#" + position.getId() + "_" + position.getTitle() + "$#";

		return formattedPositionClientData;
	}

	@Transactional(readOnly = true)
	public String getFormattedClientData(Long clientId) throws RecruizException {
		Client client = clientService.getClient(clientId).getClient();
		String formattedPositionClientData = "";
		formattedPositionClientData = " $@" + clientId + "_" + client.getClientName() + " $#" + " _ " + "$#";

		return formattedPositionClientData;
	}

	@Transactional
	public User updateFetchNotificationStatus(Boolean isNotificationEnabled) {
		User loggedInUser = userService.getLoggedInUserObject();
		loggedInUser.setIsNotificationOn(isNotificationEnabled);
		userService.save(loggedInUser);
		return loggedInUser;
	}

	@Transactional
	public void markNotificationAsRead(List<Long> notificationIds, boolean status) {
		if (notificationIds != null && !notificationIds.isEmpty()) {
			for (Long id : notificationIds) {
				try {
					Notification notification = notificationRepository.findById(id);
					notification.setReadState(status);
					notification.setViewState(status);
					notificationRepository.save(notification);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	@Transactional(readOnly = true)
	public String getClientNameCodePattern(Long clientId) throws RecruizException {
		Client client = clientService.getClient(clientId).getClient();
		String clientCodeNamePattern = "";
		clientCodeNamePattern = " $@" + clientId + "_" + client.getClientName() + " $@";

		return clientCodeNamePattern;
	}

	@Transactional(readOnly = true)
	public String getClientNameCodePatternByPosition(Long positionId) throws RecruizException {
		Client client = positionService.getPositionById(positionId).getClient();
		String clientCodeNamePattern = "";
		clientCodeNamePattern = " $@" + client.getId() + "_" + client.getClientName() + " $@";

		return clientCodeNamePattern;
	}

	@Transactional(readOnly = true)
	public String getPositionNameCodePattern(Long positionId) throws RecruizException {
		Position position = positionService.getPositionById(positionId);
		String positionCodeNamePattern = "";
		positionCodeNamePattern = " $#" + position.getId() + "_" + position.getTitle() + "$#";

		return positionCodeNamePattern;
	}

	public String getMessageForPositionDetailsUpdated(long positionId) {
		String message = "";
		try {
			message = "Position details of " + getPositionNameCodePattern(positionId)
					+ " has been updated.<br />Client: " + getClientNameCodePatternByPosition(positionId);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageClientDetailsUpdated(long clientId) {
		String message = "";
		try {
			message = "Client details of " + getFormattedClientData(clientId) + "has been updated.";
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForPositionAdded(long positionId) {
		String message = "";
		try {
			message = "New position by name " + getPositionNameCodePattern(positionId)
					+ " has been created.You have been invited to collaborate.<br /> Client: "
					+ getClientNameCodePatternByPosition(positionId);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	String message = "";

	public String getMessageForFeedbackRecieved(long positionId, String candidateName) {
		try {
			message = "Feedback for " + candidateName + " for " + getPositionNameCodePattern(positionId)
					+ " has been received.<br />Client: " + getClientNameCodePatternByPosition(positionId);

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForCandidateMoveInBoard(long positionId, String candidateName, String oldRoundName,
			String newRoundName) {
		String message = "";
		try {
			message = candidateName + " in pipeline of " + getPositionNameCodePattern(positionId)
					+ " has been moved from " + oldRoundName + " to " + newRoundName + " Client: "
					+ getClientNameCodePatternByPosition(positionId);

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForInterviewScheduled(long positionId, String candidateName, String roundName) {
		String message = "";
		try {
			message = "Interview with " + candidateName + " has been scheduled.<br /> Position: "
					+ getPositionNameCodePattern(positionId) + " (Pipeline Stage: " + roundName + " ) Client: "
					+ getClientNameCodePatternByPosition(positionId);
			;

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForInterviewReScheduled(long positionId, String candidateName, String roundName) {
		String message = "";
		try {
			message = "Interview with " + candidateName + " has been re-scheduled.<br /> Position: "
					+ getPositionNameCodePattern(positionId) + " (Pipeline Stage: " + roundName + " ) Client: "
					+ getClientNameCodePatternByPosition(positionId);
			;

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForInterviewCancelled(long positionId, String candidateName, String roundName) {
		String message = "";
		try {
			message = "Interview with " + candidateName + " has been cancelled.<br /> Position: "
					+ getPositionNameCodePattern(positionId) + " (Pipeline Stage: " + roundName + " ) Client: "
					+ getClientNameCodePatternByPosition(positionId);
			;

		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForRemovedFromBoard(long positionId, String candidateName) {
		String message = "";
		try {
			message = candidateName + "‘s profile has been removed from " + getPositionNameCodePattern(positionId)
					+ " <br />Client: " + getClientNameCodePatternByPosition(positionId);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForForwardProfile(String candidateName) {
		String message = candidateName + "‘sprofile has been forwarded for feedback";
		return message;
	}

	public String getMessageForCandidateProfileUpdated(String candidateName) {
		String message = candidateName + " profile details have been updated";
		return message;
	}

	public String getMessageForCandidateAdded(String candidateName) {
		String message = candidateName + " has been added to the common candidate pool";
		return message;
	}

	public String getMessageForSourcedToBoard(long positionId, String candidateName) {
		String message = "";
		try {
			message = candidateName + " has been sourced for " + getPositionNameCodePattern(positionId)
					+ " <br />Client: " + getClientNameCodePatternByPosition(positionId);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForCandidateStatusChangedInBoard(long positionId, String candidateName, String oldStatus,
			String newStatus) {
		String message = "";
		try {
		    String oStatus = oldStatus,nStatus = newStatus; 
		    if (boardCustomStatusService.getBoardCustomStatusByKey(oldStatus) == null) {
			oStatus = BoardStatus.valueOf(oldStatus).getDisplayName();
			}else {
			    oStatus = boardCustomStatusService.getBoardCustomStatusByKey(oldStatus).getStatusKey();
			}
		    
		    if (boardCustomStatusService.getBoardCustomStatusByKey(newStatus) == null) {
			nStatus = BoardStatus.valueOf(newStatus).getDisplayName();
			}else {
			    nStatus = boardCustomStatusService.getBoardCustomStatusByKey(newStatus).getStatusKey();
			}
		    
			message = "Status of " + candidateName + " has been changed from "
					+  oStatus + " to "
					+ nStatus + "  <br />Position : "
					+ getPositionNameCodePattern(positionId) + " Client: "
					+ getClientNameCodePatternByPosition(positionId);
		}catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	public String getMessageForCandidateEmailSent(String candidateName) {
		String message = "An email has been sent to " + candidateName;
		return message;
	}

	@Transactional
	public void markUserAllNotificationAsRead() {
		List<Notification> notifications = notificationRepository.findByViewStateAndReadStateAndUser(false, false,
				userService.getLoggedInUserEmail());
		if (notifications != null && !notifications.isEmpty()) {
			for (Notification notification : notifications) {
				notification.setReadState(true);
				notification.setViewState(true);
			}
			notificationRepository.save(notifications);
		}
	}

	@Transactional(readOnly = true)
	public List<Notification> getClientActivityForNotification(Long clientId) {
		List<Long> clientIds = clientService.getClientIdsForHrExecutive(userService.getLoggedInUserObject());
		Set<String> eventType = new HashSet<>();
		eventType.add(NotificationEvent.CLIENT_MODIFIED.getDisplayName());
		eventType.add(NotificationEvent.POSITION_CREATED.getDisplayName());
		eventType.add(NotificationEvent.POSITION_MODIFIED.getDisplayName());
		eventType.add(NotificationEvent.POSITION_STATUS_CHANEGD.getDisplayName());
		List<Notification> notifications = notificationRepository.getClientActivity(clientId, eventType,
				userService.getLoggedInUserEmail());
		return notifications;
	}

	@Transactional(readOnly = true)
	public LinkedList<Notification> getPositionActivityForLoggedInUser(String positioncode) throws RecruizException {
		Set<String> eventType = new HashSet<>();
		eventType.add(NotificationEvent.POSITION_CREATED.getDisplayName());
		eventType.add(NotificationEvent.POSITION_MODIFIED.getDisplayName());
		eventType.add(NotificationEvent.POSITION_STATUS_CHANEGD.getDisplayName());
		eventType.add(NotificationEvent.HR_REMOVED_FROM_POSITION.getDisplayName());
		eventType.add(NotificationEvent.HR_ADDED_TO_POSITION.getDisplayName());
		eventType.add(CandidateActivityType.SourcedToBoard.getDisplayName());

		LinkedList<Notification> notifications = notificationRepository.getPositionActivity(positioncode, eventType);
		return notifications;
	}

	@Transactional
	public void deleteByPositionCode(String positionCode) {
		List<Notification> notifications = notificationRepository.findByPositionCode(positionCode);
		if (notifications != null && !notifications.isEmpty()) {
			notificationRepository.delete(notifications);
		}
	}

	@Transactional
	public void deleteByClientId(Long id) {
		List<Notification> notifications = notificationRepository.findByClientId(id);
		if (notifications != null && !notifications.isEmpty()) {
			notificationRepository.delete(notifications);
		}
	}

	@Transactional
	public void deleteByCandidateId(Long id) {
		List<Notification> notifications = notificationRepository.findByCandidateId(id);
		if (notifications != null && !notifications.isEmpty()) {
			notificationRepository.delete(notifications);
		}
	}

	public String getMessageForClientAdded(Client client) throws RecruizException {
		String message = "";
		if(userService.getLoggedInUserObject().getOrganization().getOrgType().equalsIgnoreCase("agency")){
			message = "Client created.";
		}else{
			message = "Department created.";
		}
		return message;
	}

}
