package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientFile;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.ClientNotes;
import com.bbytes.recruiz.domain.PositionFile;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceInfoDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceInfoRateDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientFileService;
import com.bbytes.recruiz.service.ClientNotesService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DecisionMakerService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.InterviewPanelService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Client Controller
 *
 */
@RestController
public class ClientController {

	private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

	@Autowired
	private ClientService clientService;

	@Autowired
	private DecisionMakerService decisionMakerService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private ClientNotesService clientNotesService;

	@Autowired
	private UserService userService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private FileService fileService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private ClientFileService clientFileService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;

	@Value("${candidate.aws.bucketname}")
	private String bucketName;
	/**
	 * This method is used to add client along with decision maker and interview
	 * panel.
	 * 
	 * @param clientDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client", method = RequestMethod.POST)
	public RestResponse addClient(@RequestBody ClientDTO clientDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddClient.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Client client = clientService.addClient(clientDTO);
		RestResponse clientAddResponse = new RestResponse(RestResponse.SUCCESS, client);
		return clientAddResponse;
	}

	/**
	 * This method is used to update client along with decision maker and
	 * interview panel.
	 * 
	 * @param clientDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	@RequestMapping(value = "/api/v1/client/{clientId}", method = RequestMethod.PUT)
	public RestResponse updateClient(@PathVariable("clientId") String clientId, @RequestBody ClientDTO clientDTO) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateClient.name());

		Client client = clientService.updateClient(clientId, clientDTO);

		RestResponse clientUpdateResponse = new RestResponse(RestResponse.SUCCESS, client);
		return clientUpdateResponse;
	}

	/**
	 * This method is used to check decision makers email exists.
	 * 
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/decisionmaker/check", method = RequestMethod.GET)
	public RestResponse isDecisionMakerEmailExist(@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		if (email == null || email.isEmpty())
			return null;
		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && id != "") {
			ClientDecisionMaker decisionMaker = decisionMakerService.findOne(Long.parseLong(id));
			if (decisionMaker.getEmail().equalsIgnoreCase(email))
				isExist = false;
			else
				isExist = decisionMakerService.decisionMakerExists(email);
		} else {
			isExist = decisionMakerService.decisionMakerExists(email);
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Decision Maker exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Decision Maker does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/client/decisionmaker/check/mobile", method = RequestMethod.GET)
	public RestResponse isDecisionMakerMobileExist(@RequestParam(value = "mobile", required = false) String mobile,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		if (mobile == null || mobile.isEmpty())
			return null;
		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && !id.trim().isEmpty()) {
			ClientDecisionMaker decisionMaker = decisionMakerService.findOne(Long.parseLong(id));
			if (mobile == null || mobile.trim().isEmpty() || decisionMaker.getMobile().equalsIgnoreCase(mobile))
				isExist = false;
			else
				isExist = decisionMakerService.decisionMakerMobileExists(mobile);
		} else {
			isExist = decisionMakerService.decisionMakerMobileExists(mobile);
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Decision Maker exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Decision Maker does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	/**
	 * This method is used to check interviewers email exists.
	 * 
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/interviewers/check", method = RequestMethod.GET)
	public RestResponse isInterviewersEmailExist(@RequestParam(value = "email", required = false) String email) throws RecruizException {

		if (email == null || email.isEmpty())
			return null;
		Map<String, String> response = new HashMap<String, String>();
		boolean isExist = interviewPanelService.interviewerExists(email);
		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Interviewer exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Interviewer does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/client/interviewers/check/mobile", method = RequestMethod.GET)
	public RestResponse isInterviewersMobileExist(@RequestParam(value = "mobile", required = false) String mobile,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		if (mobile == null || mobile.isEmpty())
			return null;
		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && !id.trim().isEmpty()) {
			ClientInterviewerPanel interviewerPanel = interviewPanelService.findOne(Long.parseLong(id));
			if (mobile == null || mobile.trim().isEmpty() || interviewerPanel.getMobile().equalsIgnoreCase(mobile))
				isExist = false;
			else
				isExist = interviewPanelService.interviewerMobileExists(mobile);
		} else {
			isExist = interviewPanelService.interviewerMobileExists(mobile);
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Interviewer exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Interviewer does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	/**
	 * This method is used to check client name exists.
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/check", method = RequestMethod.GET)
	public RestResponse isClientNameExist(@RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		if (clientName == null || clientName.isEmpty())
			return null;
		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && !id.trim().isEmpty()) {
			Client client = clientService.findOne(Long.parseLong(id));
			if (client.getClientName().equalsIgnoreCase(clientName))
				isExist = false;
			else
				isExist = clientService.clientExist(clientName);
		} else {
			isExist = clientService.clientExist(clientName);
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Client exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Client does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/client/getClientByName", method = RequestMethod.GET)
	public RestResponse getClientByName(@RequestParam(value = "name", required = false) String name) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetClientDetails.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (name == null || name.isEmpty())
			return null;
		Client client = clientService.getClientByName(name);
		if (client == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.CLIENT_NOT_FOUND);
		ClientOpeningCountDTO clientDTO = clientService.getClient(client.getId());

		// Need for lazy loading
		clientDTO.getClient().getClientInterviewerPanel().size();
		clientDTO.getClient().getClientDecisionMaker().size();
		clientDTO.getClient().getCustomeField().size();
		for (ClientInterviewerPanel interviewPanel : clientDTO.getClient().getClientInterviewerPanel()) {
			interviewPanel.getInterviewerTimeSlots().size();
		}

		RestResponse clientListResponse = new RestResponse(RestResponse.SUCCESS, client);
		return clientListResponse;
	}

	/**
	 * This Method is used to change the status of client. Note : if you change
	 * the status of client, all position's status for particular client will be
	 * change.
	 * 
	 * @param clientId
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}/status", method = RequestMethod.PUT)
	public RestResponse changeClientStatus(@PathVariable("clientId") String clientId,
			@RequestParam(value = "status", required = false) String status) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ChangeClientStatus.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (status == null || status.isEmpty())
			return null;

		Client client = clientService.updateClientStatus(Long.valueOf(clientId), status);

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, client);
		return positionResponse;
	}

	/**
	 * getAllClient Method is used to fetch all client.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client", method = RequestMethod.GET)
	public RestResponse getAllClient(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllClient.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (permissionService.hasNormalRole()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}
		Page<ClientOpeningCountDTO> clientList = clientService.getAllClient(pageableService.getPageRequestObject(pageNo, sortField),
				sortField, sortOrder);

		RestResponse clientListResponse = new RestResponse(RestResponse.SUCCESS, clientList);
		return clientListResponse;
	}

	/**
	 * get all client for HR (logged in user)
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/client", method = RequestMethod.GET)
	public RestResponse getAllClientForHr(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllClient.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Page<ClientOpeningCountDTO> clientList = clientService.getAllClient(pageableService.getPageRequestObject(pageNo, sortField),
				sortField, sortOrder);

		// if (clientList.getContent() != null &&
		// !clientList.getContent().isEmpty()) {
		// for (ClientOpeningCountDTO clientOpeningCountDTO : clientList) {
		// String status = clientOpeningCountDTO.getClient().getStatus();
		// String enumStatus = Status.valueOf(status).getDisplayName();
		// clientOpeningCountDTO.getClient().setStatus(enumStatus);
		// }
		// }

		logger.info("All clients are fetch successfully.");
		RestResponse clientListResponse = new RestResponse(RestResponse.SUCCESS, clientList);
		return clientListResponse;
	}

	/**
	 * This method is used to get client object by clientId.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}", method = RequestMethod.GET)
	public RestResponse getClient(@PathVariable("clientId") String clientId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetClientDetails.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		ClientOpeningCountDTO client = clientService.getClient(Long.parseLong(clientId));

		if (client.getClient() == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.CLIENT_NOT_FOUND);

		// Need for lazy loading
		client.getClient().getClientInterviewerPanel().size();
		client.getClient().getClientDecisionMaker().size();
		client.getClient().getCustomeField().size();

		for (ClientInterviewerPanel interviewPanel : client.getClient().getClientInterviewerPanel()) {
			interviewPanel.getInterviewerTimeSlots().size();
		}

		logger.info("Client - " + client.getClient().getClientName() + " is fetch successfully.");
		RestResponse clientListResponse = new RestResponse(RestResponse.SUCCESS, client);
		return clientListResponse;
	}

	/**
	 * This Method is used to get interview panel by client
	 * 
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/interviewer", method = RequestMethod.GET)
	public RestResponse getClientInterviewerList(@RequestParam(value = "clientName", required = false) String clientName)
			throws RecruizException {

		/*if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);*/

		if (clientName == null || clientName.isEmpty())
			return null;
		Client client = clientService.getClientByName(clientName);
		Set<ClientInterviewerPanel> interviewerList = new LinkedHashSet<ClientInterviewerPanel>();

		interviewerList = interviewPanelService.getAllInterviewerByClient(client);

		RestResponse interviewerListResponse = new RestResponse(RestResponse.SUCCESS, interviewerList);
		return interviewerListResponse;
	}

	@RequestMapping(value = "/api/v1/client/getDecisionMakerList", method = RequestMethod.POST)
	public RestResponse getDecisionMakerList(@RequestParam(value = "clientName", required = false) String clientName)
			throws RecruizException {

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (clientName == null || clientName.isEmpty())
			return null;
		Client client = clientService.getClientByName(clientName);
		List<ClientDecisionMaker> decisionMakerList = decisionMakerService.getDecisionMakerByClient(client);

		RestResponse clientDecisionMakerList = new RestResponse(RestResponse.SUCCESS, decisionMakerList);
		return clientDecisionMakerList;
	}

	/**
	 * This Method returns all Client Names.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/name", method = RequestMethod.GET)
	public RestResponse getAllClientNames() throws RecruizException {

		List<String> clientNameList = clientService.getAllClientName();
		RestResponse clientNameResponse = new RestResponse(RestResponse.SUCCESS, clientNameList);
		return clientNameResponse;
	}

	/**
	 * This Method returns all Client Names for current user .
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/client/name", method = RequestMethod.GET)
	public RestResponse getClientNamesForLoggedInUser() throws RecruizException {
		List<String> clientNameList = new ArrayList<>();
		if (permissionService.isSuperAdmin()) {
			clientNameList = clientService.getAllClientName();
		} else {
			clientNameList = clientService.getClientNamesForLoggedInUser();
		}

		RestResponse clientNameResponse = new RestResponse(RestResponse.SUCCESS, clientNameList);
		return clientNameResponse;
	}

	/**
	 * This method is used to delete client using clientId.
	 * 
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}", method = RequestMethod.DELETE)
	public RestResponse deleteClient(@PathVariable("clientId") String clientId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteClient.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		
		
	List<ClientFile> clientFiles = clientFileService.getClientFilesByClientId(clientId+"");
		
		for (ClientFile clientFile : clientFiles) {
			
			try{
			uploadFileService.deleteCandidateFolderFromAWS(clientFile.getFilePath());
			}catch(Exception e){ 
				e.printStackTrace();
			}
			try{
			fileService.deleteFile(clientFile.getFilePath());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		clientService.deleteClient(Long.parseLong(clientId));
		RestResponse clientResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CLIENT_DELETED);
		return clientResponse;
	}

	/***
	 * 
	 * @param clientId
	 * @param clientNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}/notes", method = RequestMethod.POST)
	public RestResponse addClientNotes(@PathVariable("clientId") Long clientId, @RequestBody ClientNotes clientNotes)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddClientNotes.name());

		try {
			Client client = clientService.findOne(clientId);
			if (client == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.INVALID_SERVER_REQUEST);
			}
			clientNotes.setClientId(client);
			clientNotesService.save(clientNotes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, clientNotes);
		return candidatetResponse;
	}

	/****
	 * 
	 * @param clientId
	 * @param notesId
	 * @param clientNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}/notes/{notesId}", method = RequestMethod.POST)
	public RestResponse updateClientNotes(@PathVariable("clientId") Long clientId, @PathVariable("notesId") Long notesId,
			@RequestBody ClientNotes clientNotes) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateClientNotes.name());*/

		try {
			Client client = clientService.findOne(clientId);
			ClientNotes notes = clientNotesService.findOne(notesId);
			if (client == null || notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_UPDATE_FAILED, ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU, ErrorHandler.INVALID_SERVER_REQUEST);
			}
			notes.setNotes(clientNotes.getNotes());
			clientNotes = clientNotesService.save(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, clientNotes);
		return candidatetResponse;
	}

	/**
	 * 
	 * @param notesId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/client/notes/{notesId}", method = RequestMethod.DELETE)
	public RestResponse deleteClientNote(@PathVariable("notesId") String notesId) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteClientNotes.name());*/

		try {
			ClientNotes notes = clientNotesService.findOne(Long.parseLong(notesId));
			if (notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_DELETE_FAILED, ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU, ErrorHandler.INVALID_SERVER_REQUEST);
			}
			clientNotesService.delete(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTES_DELETED);
		return candidatetResponse;
	}

	/**
	 * @param clientId
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/client/{clientId}/notes", method = RequestMethod.GET)
	public RestResponse getClientNotes(@PathVariable("clientId") Long clientId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetClientNotes.name());*/

		Page<ClientNotes> clientNotes = null;
		try {
			Client client = clientService.findOne(clientId);
			clientNotes = clientNotesService.getAllClienNotesByClient(client,
					pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, clientNotes);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/client/{cid}/interviewer/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteInterviewerFromClient(@PathVariable("id") Long id, @PathVariable("cid") Long cid) throws RecruizException {
/*
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteClientInterviewer.name());*/

		RestResponse response = null;
		try {
			Set<ClientInterviewerPanel> existingInterviewer = clientService.deleteInterviewer(cid, id);
			response = new RestResponse(true, existingInterviewer);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_INTERVIEWER, ErrorHandler.FAILED_DELETING_INTERVIEWER);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/client/{cid}/decisionmaker/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteDecisionMakerFromClient(@PathVariable("id") Long id, @PathVariable("cid") Long cid) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteClientDM.name());*/

		RestResponse response = null;
		try {
			Set<ClientDecisionMaker> decisionMakerList = clientService.removeDecisionMakerFromClient(id, cid);
			response = new RestResponse(true, decisionMakerList);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_INTERVIEWER, ErrorHandler.FAILED_DELETING_INTERVIEWER);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/client/name/id/map", method = RequestMethod.GET)
	public RestResponse getAllClientNameIdForLoggedInUser() throws RecruizException {

		Object onj = clientService.getClientNameIdsForHrExecutive(userService.getLoggedInUserObject());

		RestResponse clientListResponse = new RestResponse(RestResponse.SUCCESS, onj);
		return clientListResponse;
	}

	@RequestMapping(value = "/api/v1/client/location/list", method = RequestMethod.GET)
	public RestResponse getAllClientLocations() throws RecruizException {

		Set<String> locations = clientService.getClientLocations();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, locations);
		return response;
	}

	@RequestMapping(value = "/api/v1/client/{cid}/invoice/info", method = RequestMethod.PUT)
	public RestResponse addUpdateInvoiceInfo(@PathVariable("cid") Long cid, @RequestBody InvoiceInfoDTO invoiceInfoDto)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddInvoiceInfoForClient.name());*/

		if (!organizationService.isAgency()) {
			return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
		}

		RestResponse response = null;
		try {
			Client client = clientService.findOne(cid);
			List<InvoiceInfoRateDTO> infoList = invoiceInfoDto.getInfo();
			if (null != infoList) {
				for (InvoiceInfoRateDTO infoRateDTO : infoList) {
					client.getInvoiceInfo().put(infoRateDTO.getName(), infoRateDTO.getValue() + ":" + infoRateDTO.getType());
				}
			}
			if (null != invoiceInfoDto.getRemovedKey() && !invoiceInfoDto.getRemovedKey().isEmpty()) {
				for (String removedKey : invoiceInfoDto.getRemovedKey()) {
					if (client.getInvoiceInfo().containsKey(removedKey)) {
						client.getInvoiceInfo().remove(removedKey);
					}
				}
			}
			clientService.save(client);
			response = new RestResponse(true, client.getInvoiceInfo());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_INTERVIEWER, ErrorHandler.FAILED_DELETING_INTERVIEWER);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/client/{cid}/invoice/info/get", method = RequestMethod.GET)
	public RestResponse getClientInvoiceInfo(@PathVariable("cid") Long cid) throws RecruizException {
		RestResponse response = null;
		if (!organizationService.isAgency()) {
			return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
		}

		Client client = clientService.findOne(cid);
		if (null != client) {
			Map<String, String> responseMap = 	client.getInvoiceInfo();
			List<InvoiceInfoRateDTO> info = new ArrayList<>();
			
			if(responseMap.size()>0)
			for (Map.Entry<String, String> entry : responseMap.entrySet()) {
			    System.out.println(entry.getKey() + "/" + entry.getValue());
			    InvoiceInfoRateDTO dto = new InvoiceInfoRateDTO();
			    dto.setName(entry.getKey());
			    
			    String[] array = entry.getValue().split(":");
			    if(array.length>0)
			    dto.setValue(entry.getValue().split(":")[0]);
			    
			    if(array.length>1)
			    dto.setType(entry.getValue().split(":")[1]);
			    
			    info.add(dto);
			}
			
			response = new RestResponse(true, info);
		} else {
			response = new RestResponse(false, ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.CLIENT_NOT_FOUND);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/client/{cid}/upload/file", method = RequestMethod.POST)
	public RestResponse uploadClientFiles(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName,
			@RequestParam("fileType") String fileType, @PathVariable("cid") Long cid) throws RecruizException, IOException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddClientFiles.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (fileName == null || fileName.isEmpty() || fileType == null || fileType.isEmpty() || cid == null)
			return null;

		Client client = clientService.findOne(cid);

		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		String clientFolder = uploadFileService.createFolderStructureForClient(cid + "");
		File fileToUpload = new File(clientFolder + File.separator + fileName);
		if (fileToUpload.exists()) {
			return new RestResponse(false, ErrorHandler.FILE_UPLOAD_FAILED, ErrorHandler.FILE_EXISTS);
		}

		File tmpFile = fileService.multipartToFile(file);
		Files.copy(tmpFile.toPath(), fileToUpload.toPath());

		ClientFile clientFile = new ClientFile();
		clientFile.setClientId(cid + "");
		clientFile.setFileName(fileName);
		clientFile.setFilePath(fileToUpload.getPath());
		clientFile.setFileType(fileType);

		// clientFileService.save(clientFile);

		if(fileStorageMode!=null && fileStorageMode.equalsIgnoreCase("aws")){

			String filePath = uploadFileService.createFolderAndUploadFileInAwsForClient(cid,fileToUpload,fileName);
			clientFile.setFilePath(filePath);
			clientFile.setStorageMode("aws");

		}


		client.getFiles().add(clientFile);
		clientService.save(client);

		if (null != client.getFiles() && !client.getFiles().isEmpty()) {
			client.getFiles().size();
		}

		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, client.getFiles(), null);
		return candidateAddResponse;
	}

	// to get all client files
	@RequestMapping(value = "/api/v1/client/{cid}/files/all", method = RequestMethod.GET)
	public RestResponse getAllClientFiles(@PathVariable("cid") String cid) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetClientFiles.name());*/

		RestResponse response = null;
		List<ClientFile> files = clientFileService.getClientFilesByClientId(cid);
		response = new RestResponse(true, files);
		return response;
	}

	// to get all client files
	@RequestMapping(value = "/api/v1/client/file/{fileId}/delete", method = RequestMethod.DELETE)
	public RestResponse deleteClientFile(@PathVariable("fileId") Long fileId) throws RecruizException {
/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteClientFile.name());*/

		RestResponse response = null;
		ClientFile file = clientFileService.findOne(fileId);
		if (null != file) {
			String filePath = file.getFilePath();
			File diskFile = new File(filePath);

			if (null != file.getClientId() && !file.getClientId().trim().isEmpty()) {
				Client client = clientService.findOne(Long.parseLong(file.getClientId()));
				client.getFiles().remove(file);
				clientService.save(client);
				clientFileService.delete(file);
			} else {

			}

			if (diskFile.exists()) {
				diskFile.delete();
			}
			
			try{
				String s3CandidatePath = file.getFilePath();
				if(s3CandidatePath.contains(bucketName))
					uploadFileService.deleteCandidateFolderFromAWS(s3CandidatePath);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		response = new RestResponse(true, SuccessHandler.FILE_DELETED);
		return response;
	}

}
