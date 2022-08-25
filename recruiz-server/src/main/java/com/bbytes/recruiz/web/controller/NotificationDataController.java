package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.ClientActivityService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.NotificationService;
import com.bbytes.recruiz.service.OfferLetterApprovalsService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionActivityService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.QueryService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.RestResponseConstant;

@RestController
public class NotificationDataController {

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	NotificationService notificationService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private QueryService queryService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private ClientActivityService clientActivityService;
	
	@Autowired
	private  OfferLetterApprovalsService offerLetterApprovalsService;

	@Autowired
	private PositionActivityService positionActivityService;
	
	@Autowired
	PositionService positionService;

	@RequestMapping(value = "/api/v1/notification/user", method = RequestMethod.GET)
	public RestResponse getAllNotificationsForLoggedInUser(
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField)
			throws RecruizException, ParseException, IOException {

		if (sortField == null || sortField.isEmpty()) {
			sortField = "creationDate";
		}
		long unreadCount = notificationService.getUnreadCountForLoggedInUser();
		Page<Notification> allNotification = notificationService
				.getNotificationsForLoggedInUser(pageableService.getPageRequestObject(pageNo, sortField));
		// as decided do not mark notification as read so commenting below code

		// if (allNotification.getContent() != null &&
		// !allNotification.getContent().isEmpty()) {
		// for (Notification notification : allNotification) {
		// notification.setViewState(true);
		// notificationService.save(notification);
		// }
		// }

		Map<String, Object> notificationMap = new HashMap<String, Object>();
		notificationMap.put("notifications", allNotification);
		notificationMap.put("unread_count", unreadCount);

		return new RestResponse(RestResponse.SUCCESS, notificationMap, null);
	}

	@RequestMapping(value = "/api/v1/notification/user/unread", method = RequestMethod.GET)
	public RestResponse getAllUnReadNotificationsForLoggedInUser(
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField)
			throws RecruizException, ParseException, IOException {

		if (sortField == null || sortField.isEmpty()) {
			sortField = "creationDate";
		}
		long unreadCount = notificationService.getUnreadCountForLoggedInUser();
		Page<Notification> allNotification = notificationService
				.getUnviewdNotificationsForLoggedInUser(pageableService.getPageRequestObject(pageNo, sortField));

		Map<String, Object> notificationMap = new HashMap<String, Object>();
		notificationMap.put("notifications", allNotification);
		notificationMap.put("unread_count", unreadCount);

		return new RestResponse(RestResponse.SUCCESS, notificationMap, null);
	}

	@RequestMapping(value = "/api/v1/notification/details", method = RequestMethod.GET)
	public RestResponse getNotificationDetails(@RequestParam(value = "id", required = true) String id)
			throws RecruizException, ParseException, IOException {
		Notification notification = notificationService.getNotificationDetails(id);
		if (notification == null) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.MESSAGE_NOT_FOUND, ErrorHandler.NO_MSG);
		}
		return new RestResponse(RestResponse.SUCCESS, notification, null);
	}

	@RequestMapping(value = "/api/v1/notification/count", method = RequestMethod.GET)
	public RestResponse getNotificationCountForDashboard() throws RecruizException, ParseException, IOException {

		Map<String, Long> countMap = queryService.getNotificationCount();
		return new RestResponse(RestResponse.SUCCESS, countMap, null);
	}

	/**
	 * to get list of notification for clients
	 * 
	 * @param clientIds
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/notification/count/client", method = RequestMethod.GET)
	public RestResponse getNotificationCountForClients(@RequestParam List<Long> clientIds)
			throws RecruizException, ParseException, IOException {

		Map<Long, List<Notification>> countMap = queryService.getNotificationCountMapForClient(clientIds);
		return new RestResponse(RestResponse.SUCCESS, countMap, null);
	}

	@RequestMapping(value = "/api/v1/notification/count/position", method = RequestMethod.GET)
	public RestResponse getNotificationCountForPositions(@RequestParam List<String> positionCodes)
			throws RecruizException, ParseException, IOException {

		Map<String, List<Notification>> countMap = queryService.getNotificationCountMapForPosittion(positionCodes);
		return new RestResponse(RestResponse.SUCCESS, countMap, null);
	}

	@RequestMapping(value = "/api/v1/notification/count/candidate", method = RequestMethod.GET)
	public RestResponse getNotificationCountForCandidates(@RequestParam List<Long> candidateIds)
			throws RecruizException, ParseException, IOException {

		Map<Long, List<Notification>> countMap = queryService.getNotificationCountMapForCandidate(candidateIds);
		return new RestResponse(RestResponse.SUCCESS, countMap, null);
	}

	@RequestMapping(value = "/api/v1/notification/fetch/status", method = RequestMethod.PUT)
	public RestResponse changeStatusForFetchingNotification(@RequestParam Boolean isNotificationEnabled)
			throws RecruizException, ParseException, IOException {

		User user = notificationService.updateFetchNotificationStatus(isNotificationEnabled);

		UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);

		return new RestResponse(RestResponse.SUCCESS, userDTO, null);
	}

	@RequestMapping(value = "/api/v1/notification/mark/read", method = RequestMethod.PUT)
	public RestResponse markNotificationAsRead(@RequestParam List<Long> notificationIds)
			throws RecruizException, ParseException, IOException {

		notificationService.markNotificationAsRead(notificationIds, true);
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTIFICATION_MARKED_AS_READ, null);
	}

	@RequestMapping(value = "/api/v1/notification/mark/unread", method = RequestMethod.PUT)
	public RestResponse markNotificationAsUnRead(@RequestParam List<Long> notificationIds)
			throws RecruizException, ParseException, IOException {

		notificationService.markNotificationAsRead(notificationIds, false);
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTIFICATION_MARKED_AS_UNREAD, null);
	}

	@RequestMapping(value = "/api/v1/notification/mark/read/all", method = RequestMethod.PUT)
	public RestResponse markAllNotificationAsUnRead() throws RecruizException, ParseException, IOException {

		notificationService.markUserAllNotificationAsRead();
		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTIFICATION_MARKED_AS_UNREAD, null);
	}

	@RequestMapping(value = "/api/v1/activity/client/{id}", method = RequestMethod.GET)
	public RestResponse getClientActivity(@PathVariable Long id,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField)
			throws RecruizException, ParseException, IOException {

		if (sortField == null || sortField.isEmpty()) {
			sortField = "creationDate";
		}

		RestResponse response = null;
		try {
			Page<ClientActivity> activities = clientActivityService.getClientActivity(id,
					pageableService.getPageRequestObject(pageNo, sortField));
			response = new RestResponse(RestResponse.SUCCESS, activities, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ACTIVITY, ErrorHandler.NO_ACTIVITY_FOUND);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/activity/position/{code}", method = RequestMethod.GET)
	public RestResponse getPositionActivity(@PathVariable String code,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField)
			throws RecruizException, ParseException, IOException {
		RestResponse response = null;
		try {
			Page<PositionActivity> activities = positionActivityService.getPositionActivity(code,
					pageableService.getPageRequestObject(pageNo, sortField));
			response = new RestResponse(RestResponse.SUCCESS, activities, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ACTIVITY, ErrorHandler.NO_ACTIVITY_FOUND);
		}
		return response;
	}
	
	
	@RequestMapping(value = "/api/v1/activity/position/approval", method = RequestMethod.GET)
	public RestResponse getPositionActivityApprovalDetails(@RequestParam(value = "offerLetterApprovalId", required = false) String offerLetterApprovalId)throws RecruizException, ParseException, IOException {
		RestResponse response = null;
		try {
			OfferLetterApprovals approvals = offerLetterApprovalsService.findOne(Long.valueOf(offerLetterApprovalId));
			response = new RestResponse(RestResponse.SUCCESS, approvals, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ACTIVITY, ErrorHandler.NO_ACTIVITY_FOUND);
		}
		return response;
	}
	
	
	@RequestMapping(value = "/api/v1/activity/candidate/approval", method = RequestMethod.GET)
	public RestResponse getCandidateActivityApprovalDetails(@RequestParam(value = "offerLetterApprovalId", required = false) String offerLetterApprovalId)
			throws RecruizException, ParseException, IOException {
		RestResponse response = null;
		try {
			OfferLetterApprovals approvals = offerLetterApprovalsService.findOne(Long.valueOf(offerLetterApprovalId));
			
			Position position = positionService.findOne(approvals.getPosition_id());
			
			if(position!=null){
				approvals.setField1(position.getTitle());
			}
			response = new RestResponse(RestResponse.SUCCESS, approvals, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ACTIVITY, ErrorHandler.NO_ACTIVITY_FOUND);
		}
		return response;
	}
	
}
