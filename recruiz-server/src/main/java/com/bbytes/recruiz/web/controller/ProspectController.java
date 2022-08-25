package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectActivity;
import com.bbytes.recruiz.domain.ProspectContactInfo;
import com.bbytes.recruiz.domain.ProspectNotes;
import com.bbytes.recruiz.domain.ProspectPosition;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectContactInfoDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectPostionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.ProspectActivityService;
import com.bbytes.recruiz.service.ProspectContactInfoService;
import com.bbytes.recruiz.service.ProspectNotesService;
import com.bbytes.recruiz.service.ProspectPositionService;
import com.bbytes.recruiz.service.ProspectService;
import com.bbytes.recruiz.service.TaskItemService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class ProspectController {

	private static final Logger logger = LoggerFactory.getLogger(ProspectController.class);

	public static final String TITLE = "title";

	public static final String CLOSEDBYDATE = "closeByDate";

	@Autowired
	private ProspectService prospectService;

	@Autowired
	private ProspectNotesService prospectNotesService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private ProspectContactInfoService prospectContactInfoService;

	@Autowired
	private ProspectActivityService prospectActivityService;

	@Autowired
	private UserService userService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private TaskItemService taskItemService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	@Autowired
	private ProspectPositionService prospectPositionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	/************************
	 * To add a new prospect*
	 ************************
	 * @param prospectDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect", method = RequestMethod.POST)
	public RestResponse addProspect(@RequestBody ProspectDTO prospectDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddProspect.name());

		Prospect prospect = prospectService.addProspect(prospectDTO);
		ProspectDTO convertedProspect = dataModelToDTOConversionService.convertProspect(prospect);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, convertedProspect,
				SuccessHandler.PROSPECT_CREATION_SUCCESS);
		return prospectResponse;
	}

	/*********************************
	 * To get all prospect(pageable)**
	 *********************************
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect", method = RequestMethod.GET)
	public RestResponse getAllProspect(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetAllProspect.name());*/

		Page<Prospect> prospects = null;
		RestResponse prospectResponse = null;

		if(sortOrder.equalsIgnoreCase("min") || sortOrder.equalsIgnoreCase("max")){
		
			List<Prospect> data = prospectService.findAll();

			if(sortOrder.equalsIgnoreCase("max"))
				Collections.sort(data, (o1, o2) -> o2.getProspectRating() - o1.getProspectRating());

			if(sortOrder.equalsIgnoreCase("min"))
				Collections.sort(data, (o1, o2) -> o1.getProspectRating() - o2.getProspectRating());

			Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField,
					pageableService.getSortDirection(sortOrder));
			int start = pageable.getOffset();
			int end = (start + pageable.getPageSize()) > data.size() ? data.size()
					: (start + pageable.getPageSize());
			final Page<Prospect> page = new PageImpl<Prospect>(data.subList(start, end),
					pageable, data.size());

			return new RestResponse(RestResponse.SUCCESS, page);
		}else{
			// checking user has view all candidate permission
			if (checkUserPermission.isSuperAdmin() || checkUserPermission.hasViewAllProspectPermission()) {
				prospects = prospectService.getAllProspect(pageableService.getPageRequestObject(pageNo, sortField,
						pageableService.getSortDirection(sortOrder)));
			} else {
				String loggedInUserEmail = userService.getLoggedInUserEmail();
				prospects = prospectService.getAllProspectByOwner(pageableService.getPageRequestObject(pageNo, sortField,
						pageableService.getSortDirection(sortOrder)), loggedInUserEmail);
			}

		}

		prospectResponse = new RestResponse(RestResponse.SUCCESS, prospects);

		return prospectResponse;
	}

	/******************
	 * To get prospect*
	 ******************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}", method = RequestMethod.GET)
	public RestResponse getProspect(@PathVariable("prospectId") String prospectId) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetProspectDetails.name());*/

		Prospect prospect = prospectService.getProspectById(Long.parseLong(prospectId));
		ProspectDTO convertedProspect = dataModelToDTOConversionService.convertProspect(prospect);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, convertedProspect);
		return prospectResponse;
	}

	/*********************************
	 * To Update a existing Prospect*
	 *********************************
	 * @param prospectId
	 * @param prospectDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}", method = RequestMethod.PUT)
	public RestResponse updateProspect(@PathVariable("prospectId") String prospectId,
			@RequestBody ProspectDTO prospectDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateProspect.name());

		Prospect prospect = prospectService.updateProspect(Long.parseLong(prospectId), prospectDTO);
		ProspectDTO convertedProspect = dataModelToDTOConversionService.convertProspect(prospect);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, convertedProspect,
				SuccessHandler.PROSPECT_UPDATE_SUCCESS);
		return prospectResponse;
	}

	/*********************
	 * To delete Prospect*
	 *********************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}", method = RequestMethod.DELETE)
	public RestResponse deleteProspect(@PathVariable("prospectId") String prospectId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteProspect.name());

		Boolean isDeleted = prospectService.deleteProspect(Long.parseLong(prospectId));
		RestResponse prospectResponse = null;
		if (isDeleted)
			prospectResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROSPECT_DELETED,
					SuccessHandler.PROSPECT_DELETED);
		else
			prospectResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETE_FAILED,
					ErrorHandler.DELETE_FAILED);
		return prospectResponse;
	}

	/***********************************************
	 * To add new prospect contact info in prospect*
	 ***********************************************
	 * @param prospectId
	 * @param prospectContactInfoDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/prospectcontactinfo", method = RequestMethod.POST)
	public RestResponse addProspectContactInfoInProspect(@PathVariable("prospectId") String prospectId,
			@RequestBody ProspectContactInfoDTO prospectContactInfoDTO) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.AddProspectContactInfo.name());*/

		ProspectContactInfo prospectContactInfo = prospectContactInfoService
				.addProspectContactInfo(Long.parseLong(prospectId), prospectContactInfoDTO);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectContactInfo);
		return prospectResponse;
	}

	/*******************************************
	 * To update existing prospect contact info*
	 *******************************************
	 * @param Id
	 * @param prospectContactInfoDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{Id}/prospectcontactinfo", method = RequestMethod.PUT)
	public RestResponse updateProspectContactInfo(@PathVariable("Id") String Id,
			@RequestBody ProspectContactInfoDTO prospectContactInfoDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateProspectContactInfo.name());

		ProspectContactInfo prospectContactInfo = prospectContactInfoService
				.updateProspectContactInfo(Long.parseLong(Id), prospectContactInfoDTO);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectContactInfo);
		return prospectResponse;
	}

	/********************************
	 * To get prospect contact info**
	 ********************************
	 * @param Id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/prospectcontactinfo/{Id}", method = RequestMethod.GET)
	public RestResponse getProspectContactInfo(@PathVariable("Id") String Id) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetProspectContactInfo.name());*/

		ProspectContactInfo prospectContactInfo = prospectContactInfoService
				.getProspectContactInfoById(Long.parseLong(Id));
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectContactInfo);
		return prospectResponse;
	}

	/***********************************************
	 * To get All prospect contact info in Prospect*
	 ***********************************************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/prospectcontactinfo", method = RequestMethod.GET)
	public RestResponse getAllProspectContactInfoInProspect(@PathVariable("prospectId") String prospectId)
			throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetProspectContactInfo.name());*/

		List<ProspectContactInfo> prospectContactInfos = prospectContactInfoService
				.getProspectContactInfo(Long.parseLong(prospectId));
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectContactInfos);
		return prospectResponse;
	}

	/**********************************
	 * To delete prospect contact info*
	 **********************************
	 * @param Id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/prospectcontactinfo/{Id}", method = RequestMethod.DELETE)
	public RestResponse deleteProspectContactInfo(@PathVariable("Id") String Id) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteProspectContactInfo.name());

		Boolean isDeleted = prospectContactInfoService.deleteProspectContactInfo(Long.parseLong(Id));
		RestResponse prospectResponse = null;
		if (isDeleted)
			prospectResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROSPECT_CONTACT_INFO_DELETED,
					SuccessHandler.PROSPECT_CONTACT_INFO_DELETED);
		else
			prospectResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETE_FAILED,
					ErrorHandler.DELETE_FAILED);
		return prospectResponse;
	}

	/********************************
	 * To add a new note to Prospect*
	 ********************************
	 * @param prospectId
	 * @param prospectNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/notes", method = RequestMethod.POST)
	public RestResponse addProspectNotes(@PathVariable("prospectId") String prospectId,
			@RequestBody ProspectNotes prospectNotes) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.AddProspectNotes.name());*/

		try {
			Prospect prospect = prospectService.getProspectById(Long.parseLong(prospectId));
			if (prospect == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.PROSPECT_NOT_EXIST,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			prospectNotes.setProspect(prospect);
			if (prospectNotes.getAddedBy() == null) {
				prospectNotes.setAddedBy(userService.getLoggedInUserEmail());
			}
			prospectNotes = prospectNotesService.save(prospectNotes);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectNotes);
		return prospectResponse;
	}

	/*************************************
	 * To Update a existing Prospect note*
	 *************************************
	 * @param prospectId
	 * @param notesId
	 * @param prospectNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/notes/{notesId}", method = RequestMethod.POST)
	public RestResponse updateProspectNotes(@PathVariable("prospectId") String prospectId,
			@PathVariable("notesId") String notesId, @RequestBody ProspectNotes prospectNotes) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.UpdateProspectNotes.name());*/

		try {
			Prospect prospect = prospectService.getProspectById(Long.parseLong(prospectId));
			ProspectNotes notes = prospectNotesService.findOne(Long.parseLong(notesId));
			if (prospect == null || notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_UPDATE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			notes.setNotes(prospectNotes.getNotes());
			prospectNotes = prospectNotesService.save(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectNotes,
				SuccessHandler.UPDATE_SUCCESS);
		return prospectResponse;
	}

	/**********************************
	 * To delete existig Prospect note*
	 **********************************
	 * @param notesId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/notes/{notesId}", method = RequestMethod.DELETE)
	public RestResponse deleteProspectNote(@PathVariable("notesId") String notesId) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.DeleteProspectNotes.name());*/

		try {
			ProspectNotes notes = prospectNotesService.findOne(Long.parseLong(notesId));
			if (notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_DELETE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			prospectNotesService.delete(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTES_DELETED,
				SuccessHandler.DELETE_SUCCESS);
		return prospectResponse;
	}

	/***********************************************
	 * To get list of all Prospect notes (Pageable)*
	 ***********************************************
	 * @param prospectId
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/notes", method = RequestMethod.GET)
	public RestResponse getProspectNotes(@PathVariable("prospectId") String prospectId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetProspectNotes.name());*/

		Page<ProspectNotes> prospectNotes = null;
		try {
			Prospect prospect = prospectService.getProspectById(Long.parseLong(prospectId));
			prospectNotes = prospectNotesService.getProspectNotes(prospect, pageableService.getPageRequestObject(pageNo,
					sortField, pageableService.getSortDirection(sortOrder)));
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, prospectNotes,
				SuccessHandler.GET_SUCCESS);
		return prospectResponse;
	}

	/********************************
	 * To change prospect to client**
	 ********************************
	 * @param status
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/changetoclient/status/{status}", method = RequestMethod.PUT)
	public RestResponse changetoclient(@PathVariable("status") String status,
			@PathVariable("prospectId") String prospectId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ConvertProspectToClient.name());

		Prospect prospect = prospectService.convertProspectToClient(Long.parseLong(prospectId), status);
		ProspectDTO convertedProspect = dataModelToDTOConversionService.convertProspect(prospect);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, convertedProspect,
				SuccessHandler.PROSPECT_TO_CLIENT_CHANGE_SUCCESS);
		return prospectResponse;
	}

	/*********************************
	 * To change the prospect status**
	 *********************************
	 * @param status
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/status/{status}", method = RequestMethod.PUT)
	public RestResponse chageStatus(@PathVariable("status") String status,
			@PathVariable("prospectId") String prospectId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangeProspectStatus.name());

		Prospect prospect = prospectService.changeProspectStatus(Long.parseLong(prospectId), status);
		ProspectDTO convertedProspect = dataModelToDTOConversionService.convertProspect(prospect);
		RestResponse prospectResponse = new RestResponse(RestResponse.SUCCESS, convertedProspect,
				SuccessHandler.STATUS_CHANGE_SUCCESS);
		return prospectResponse;
	}

	/*******************
	 * To add Reminder**
	 *******************
	 * @param taskItem
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/reminder", method = RequestMethod.POST)
	public RestResponse addReminder(@RequestBody TaskItem taskItem) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddTaskReminder.name());

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// checking for back date
		if (taskItem.getDueDateTime().before(new Date())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.BACK_DATE_NOT_ALLOWED,
					ErrorHandler.BACK_DATE_SELECTED);
		}

		try {
			taskItem = taskItemService.saveTaskItem(GlobalConstants.PROSPECT_TASK_FOLDER_NAME, taskItem);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_SCHEDULE_FAILED,
					RestResponseConstant.CAN_NOT_SCHEDULE_TASK);
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_ADDED,
				RestResponseConstant.TASK_ITEM_ADDED_SUCCESSFULLY);
		return response;

	}

	/*******************************************
	 * To get all prospect activity in prospect*
	 *******************************************
	 * @param prospectId
	 * @param pageNo
	 * @param sortField
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/activity/prospect/{prospectId}", method = RequestMethod.GET)
	public RestResponse getProspectActivity(@PathVariable("prospectId") String prospectId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetProspectActivity.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Page<ProspectActivity> allActivity = prospectActivityService.getProspectActivityById(prospectId,
				pageableService.getPageRequestObject(pageNo, sortField));
		RestResponse prospectActivityResponse = new RestResponse(RestResponse.SUCCESS, allActivity,
				SuccessHandler.GET_SUCCESS);
		return prospectActivityResponse;
	}

	/********************************
	 * To check company exist or not*
	 ********************************
	 * @param company
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/check/company", method = RequestMethod.GET)
	public RestResponse checkCompany(@RequestParam(value = "company", required = false) String company,
			@RequestParam(value = "prospectId", required = false) String prospectId) throws RecruizException {

		if (prospectId == null) {
			prospectId = "0";
		}

		Boolean isExist = prospectService.checkCompanyExist(Long.parseLong(prospectId), company);
		RestResponse checkResponse = null;
		if (isExist) {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist,
					SuccessHandler.COMPANY_EXIST_BUT_NOT_IN_SAME_ID);
		} else {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist, ErrorHandler.COMPANY_EXIST_IN_SAME_ID);
		}
		return checkResponse;

	}

	/*******************************************
	 * To check mobile exist or not in prospect*
	 *******************************************
	 * @param mobile
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/check/mobile", method = RequestMethod.GET)
	public RestResponse checkMobile(@RequestParam(value = "mobile", required = false) String mobile,
			@RequestParam(value = "prospectId", required = false) String prospectId) throws RecruizException {

		if (prospectId == null) {
			prospectId = "0";
		}
		Boolean isExist = prospectService.checkMobileExist(Long.parseLong(prospectId), mobile);
		RestResponse checkResponse = null;
		if (isExist) {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist,
					SuccessHandler.MOBILE_EXIST_BUT_NOT_IN_SAME_ID);
		} else {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist, ErrorHandler.MOBILE_EXIST_IN_SAME_ID);
		}
		return checkResponse;

	}

	/******************************************************************
	 * To check mobile exist or not in prospect contact info mobile no*
	 ******************************************************************
	 * @param mobile
	 * @param prospectContactInfoId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/check/prospectcontactinfo/mobile", method = RequestMethod.GET)
	public RestResponse checkProspectContactInfoMobile(@RequestParam(value = "mobile", required = false) String mobile,
			@RequestParam(value = "prospectContactInfoId", required = false) String prospectContactInfoId)
					throws RecruizException {

		if (prospectContactInfoId == null) {
			prospectContactInfoId = "0";
		}

		Boolean isExist = prospectService.checkProspectContactInfoMobileExist(Long.parseLong(prospectContactInfoId),
				mobile);
		RestResponse checkResponse = null;
		if (isExist) {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist,
					SuccessHandler.MOBILE_EXIST_BUT_NOT_IN_SAME_ID);
		} else {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist, ErrorHandler.MOBILE_EXIST_IN_SAME_ID);
		}
		return checkResponse;

	}

	/******************************************
	 * To check email exist or not in prospect*
	 ******************************************
	 * @param email
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/check/email", method = RequestMethod.GET)
	public RestResponse checkEmail(@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "prospectId", required = false) String prospectId) throws RecruizException {

		if (prospectId == null) {
			prospectId = "0";
		}

		Boolean isExist = prospectService.checkEmailExist(Long.parseLong(prospectId), email);
		RestResponse checkResponse = null;
		if (isExist) {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist,
					SuccessHandler.EMAIL_MOBILE_EXIST_BUT_NOT_IN_SAME_ID);
		} else {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist, ErrorHandler.EMAIL_EXIST_IN_SAME_ID);
		}
		return checkResponse;

	}

	/******************************************************
	 * To check email exist or not in prospect contact info*
	 ******************************************************
	 * @param email
	 * @param prospectContactInfoId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/check/prospectcontactinfo/email", method = RequestMethod.GET)
	public RestResponse checkProspectContactInfoEmail(@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "prospectContactInfoId", required = false) String prospectContactInfoId)
					throws RecruizException {

		if (prospectContactInfoId == null) {
			prospectContactInfoId = "0";
		}

		Boolean isExist = prospectService.checkProspectContactInfoEmailExist(Long.parseLong(prospectContactInfoId),
				email);
		RestResponse checkResponse = null;
		if (isExist) {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist,
					SuccessHandler.EMAIL_MOBILE_EXIST_BUT_NOT_IN_SAME_ID);
		} else {
			checkResponse = new RestResponse(RestResponse.SUCCESS, isExist, ErrorHandler.EMAIL_EXIST_IN_SAME_ID);
		}
		return checkResponse;

	}

	/**************************************************
	 * When status is lost then it will capture reason*
	 **************************************************
	 * @param prospectId
	 * @param reason
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/reason", method = RequestMethod.PUT)
	public RestResponse prospectReason(@PathVariable("prospectId") String prospectId,
			@RequestParam(value = "reason") String reason) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.UpdateProspectReason.name());*/

		Boolean updatedreason = prospectService.prospectReason(Long.parseLong(prospectId), reason);
		RestResponse restResponse = null;
		if (updatedreason)
			restResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.STATUS_CHANGE_SUCCESS,
					SuccessHandler.REASON_UPDATED);
		else
			restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.STATUS_CHANGE_FAILED,
					ErrorHandler.REASON_UPDATE_FAILED);
		return restResponse;
	}

	/******************************
	 * To Add Position in Prospect*
	 ******************************
	 * @param prospectId
	 * @param prospectPostionDTOs
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/add/position", method = RequestMethod.POST)
	public RestResponse addPositionInProspect(@PathVariable("prospectId") long prospectId,
			@RequestBody List<ProspectPostionDTO> prospectPostionDTOs) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddProspectPosition.name());

		Boolean isSuccess = prospectPositionService.addPositionInProspect(prospectId, prospectPostionDTOs);
		RestResponse restResponse = null;
		if (isSuccess)
			restResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.POSITION_ADDED_SUCCESS,
					SuccessHandler.SUCCESS);
		else
			restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_ADDED_FAILED,
					ErrorHandler.POSITION_ADDED_FAILURE);
		return restResponse;
	}

	/**********************************
	 * To get All position in prospect*
	 **********************************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/position", method = RequestMethod.GET)
	public RestResponse getAllPositionInProspect(@PathVariable("prospectId") long prospectId) throws RecruizException {
		List<ProspectPosition> prospectPositions = prospectPositionService.getAllPostion(prospectId);
		List<ProspectPostionDTO> prospectPostionDTOs = dataModelToDTOConversionService
				.convertProspectPosition(prospectPositions);

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetAllProspectPosition.name());*/

		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, prospectPostionDTOs, SuccessHandler.SUCCESS);
		return restResponse;
	}

	/*********************************
	 * To delete position in prospect*
	 *********************************
	 * @param prospectPositionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/position/{prospectPositionId}", method = RequestMethod.DELETE)
	public RestResponse deletePositionInProspect(@PathVariable("prospectPositionId") long prospectPositionId)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteProspectPosition.name());

		Boolean isSuccess = prospectPositionService.deletePositionInProspect(prospectPositionId);
		RestResponse restResponse = null;
		if (isSuccess)
			restResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROSPECT_POSITION_DELETED_SUCCESS,
					SuccessHandler.SUCCESS);
		else
			restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.PROSPECT_POSITION_DELETE_FAILED,
					ErrorHandler.PROSPECT_POSITION_DELETE_FAILURE);
		return restResponse;
	}

	/*************************************
	 * To delete All position in prospect*
	 *************************************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/{prospectId}/position", method = RequestMethod.DELETE)
	public RestResponse deleteAllPositionInProspect(@PathVariable("prospectId") long prospectId)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteProspectPosition.name());

		Boolean isSuccess = prospectPositionService.deleteAllPositionInProspect(prospectId);
		RestResponse restResponse = null;
		if (isSuccess)
			restResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROSPECT_POSITION_DELETED_SUCCESS,
					SuccessHandler.SUCCESS);
		else
			restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.PROSPECT_POSITION_DELETE_FAILED,
					ErrorHandler.PROSPECT_POSITION_DELETE_FAILURE);
		return restResponse;
	}

	/*********************************
	 * To update position in prospect*
	 *********************************
	 * @param prospectPositionId
	 * @param prospectPostionDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/position/{prospectPositionId}", method = RequestMethod.PUT)
	public RestResponse updatePositionInProspect(@PathVariable("prospectPositionId") long prospectPositionId,
			@RequestBody ProspectPostionDTO prospectPostionDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateProspectPosition.name());

		Boolean isSuccess = prospectPositionService.updatePosition(prospectPositionId, prospectPostionDTO);
		RestResponse restResponse = null;
		if (isSuccess)
			restResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROSPECT_POSITION_UPDATE_SUCCESS,
					SuccessHandler.SUCCESS);
		else
			restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.PROSPECT_POSITION_UPDATE_FAILED,
					ErrorHandler.PROSPECT_POSITION_UPDATE_FAILURE);
		return restResponse;
	}

	/*************************************************
	 * To Get All Position after Converting to Client*
	 *************************************************
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 */
	/*
	 * @RequestMapping(value =
	 * "/api/v1/prospect/requested/position/{clientName}", method =
	 * RequestMethod.GET) public RestResponse
	 * getAllRequestedPosition(@PathVariable("clientName") String clientName,
	 * 
	 * @RequestParam(value = "pageNo", required = false) String pageNo,
	 * 
	 * @RequestParam(value = "sortField", required = false) String sortField,
	 * 
	 * @RequestParam(value = "sortOrder", required = false) String sortOrder)
	 * throws RecruizException {
	 * 
	 * Page<ProspectPosition> prospectPositions =
	 * prospectPositionService.getByClientName(clientName,
	 * pageableService.getPageRequestObject(pageNo, sortField,
	 * pageableService.getSortDirection(sortOrder))); RestResponse restResponse
	 * = new RestResponse(RestResponse.SUCCESS, prospectPositions,
	 * SuccessHandler.SUCCESS); return restResponse; }
	 */

	/**********************************
	 * To Get Requested Position By Id*
	 **********************************
	 * @param prospectPositionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/position/{prospectPositionId}", method = RequestMethod.GET)
	public RestResponse getRequestedPosition(@PathVariable("prospectPositionId") long prospectPositionId)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetProspectPositionDetails.name());

		ProspectPosition prospectPosition = prospectPositionService.getById(prospectPositionId);
		ProspectPostionDTO prospectPostionDTO = dataModelToDTOConversionService
				.convertProspectPosition(prospectPosition);
		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, prospectPostionDTO, SuccessHandler.SUCCESS);
		return restResponse;
	}

	/*************************************************
	 * To Get All pending Requested Prospect Position*
	 *************************************************
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/position", method = RequestMethod.GET)
	public RestResponse getAllRequestedPosition(@RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetClientsProspectPosition.name());

		Page<ProspectPosition> prospectPositions = null;

		if (sortField.equals(TITLE))
			sortField = "positionName";
		if (sortField.equals(CLOSEDBYDATE))
			sortField = "closureDate";

		if (clientName != null && !clientName.isEmpty()) {
			prospectPositions = prospectPositionService.getByClientName(clientName, pageableService
					.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
		} else {
			prospectPositions = prospectPositionService.getByStatus(pageableService.getPageRequestObject(pageNo,
					sortField, pageableService.getSortDirection(sortOrder)));
		}

		for (ProspectPosition prospectPosition : prospectPositions) {
			Client client = clientService.getClientByName(prospectPosition.getClientName());
			if(client !=null){
				prospectPosition.setClientStatus(client.getStatus());
			}
		}

		// Page<ProspectPosition> prospectPositions =
		// prospectPositionService.getByStatus(
		// pageableService.getPageRequestObject(pageNo, sortField,
		// pageableService.getSortDirection(sortOrder)));
		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, prospectPositions, SuccessHandler.SUCCESS);
		return restResponse;
	}

	/************************************************************************
	 * To change the Requested Prospect Position status pending to processed*
	 ************************************************************************
	 * @param prospectPositionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/prospect/position/change/status/{prospectPositionId}", method = RequestMethod.GET)
	public RestResponse changeStatusOfRequestedProspectPosition(
			@PathVariable("prospectPositionId") long prospectPositionId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangeStatusOfProspectPosition.name());

		prospectPositionService.changeStatusOfRequestedProspectPosition(prospectPositionId);
		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, "Status Changed Successfully",
				SuccessHandler.SUCCESS);
		return restResponse;
	}
}
