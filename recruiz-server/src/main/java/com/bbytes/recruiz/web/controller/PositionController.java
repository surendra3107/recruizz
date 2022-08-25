package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionFile;
import com.bbytes.recruiz.domain.PositionNotes;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.PositionFileRepository;
import com.bbytes.recruiz.rest.dto.models.BoardDTO;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.PositionClientNameDTO;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.rest.dto.models.PositionOfferCostDTO;
import com.bbytes.recruiz.rest.dto.models.ReportDropdownDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.BoardService;
import com.bbytes.recruiz.service.CandidateStatusService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.DecisionMakerService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.ExternalUserService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.GenericInterviewerService;
import com.bbytes.recruiz.service.InterviewPanelService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionNotesService;
import com.bbytes.recruiz.service.PositionRequestService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.ProspectPositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.TeamService;
import com.bbytes.recruiz.service.UniqueIdentifierGeneratorService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.VendorService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Position Controller
 */
@RestController
public class PositionController {

	Logger logger = LoggerFactory.getLogger(PositionController.class);

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	RoundCandidateService roundCandidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private DecisionMakerService decisionMakerService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private PositionRequestService positionRequestService;

	@Autowired
	private ExternalUserService externalUserService;

	@Autowired
	private UniqueIdentifierGeneratorService uniqueIdentifierGeneratorService;

	@Autowired
	private CandidateStatusService candidateStatusService;

	@Autowired
	private ProspectPositionService prospectPositionService;

	@Autowired
	private PositionNotesService positionNotesService;

	@Autowired
	private GenericInterviewerService genericInterviewerService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private PositionFileRepository positionFileRepository;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;

	@Value("${candidate.aws.bucketname}")
	private String bucketName;

	/**
	 * This Method is used to add position for a client.
	 *
	 * @param position
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/position", method = RequestMethod.POST)
	public RestResponse addPosition(@RequestPart("json") @Valid PositionDTO position,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam("clientName") String clientName,
			@RequestParam(value = "fileName", required = false) String fileName)
					throws RecruizException, ParseException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddPosition.name());

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (clientName == null || clientName.isEmpty())
			return null;

		Client client = clientService.getClientByName(clientName);
		if (client.getStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| client.getStatus().equalsIgnoreCase(Status.OnHold.getDisplayName())
				|| client.getStatus().equalsIgnoreCase(Status.Closed.toString())
				|| client.getStatus().equalsIgnoreCase(Status.Closed.getDisplayName())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}

		List<String> hrId = position.getHrExexutivesId();
		List<String> interviewerIdList = position.getInterviewerPanelsId();
		List<String> roundIdList = position.getRoundListId();
		// getting list of HRExecutives
		Set<User> hrList = new LinkedHashSet<User>();
		if (hrId != null && !(hrId.isEmpty())) {
			for (String id : hrId) {
				long hid = Long.parseLong(id);
				User hr = userService.findOne(hid);
				hrList.add(hr);
			}
		}

		// getting list of InterviewerList
		Set<ClientInterviewerPanel> interviewerList = new LinkedHashSet<ClientInterviewerPanel>();
		if (interviewerIdList != null && !(interviewerIdList.isEmpty())) {
			for (String id : interviewerIdList) {
				long interviewId = Long.parseLong(id);
				ClientInterviewerPanel interviewPanel = interviewPanelService.findOne(interviewId);
				interviewerList.add(interviewPanel);
			}
		}

		// getting list of Vendors
		Set<Vendor> vendors = new LinkedHashSet<Vendor>();
		if (position.getVendorIds() != null && !(position.getVendorIds().isEmpty())) {
			for (String id : position.getVendorIds()) {
				long vendorId = Long.parseLong(id);
				Vendor vendor = vendorService.findOne(vendorId);
				vendors.add(vendor);
			}
		}

		// getting list of RoundList
		List<Round> roundList = new ArrayList<Round>();
		if (roundIdList != null && !(roundIdList.isEmpty())) {
			for (String id : roundIdList) {
				long roundId = Long.parseLong(id);
				Round round = roundService.findOne(roundId);
				roundList.add(round);
			}
		}

		Position newPosition = new Position();
		newPosition.setPositionCode(position.getPositionCode());
		newPosition.setTitle(position.getTitle());
		newPosition.setLocation(position.getLocation());
		newPosition.setTotalPosition(position.getTotalPosition());
		newPosition.setCloseByDate(position.getCloseByDate());
		newPosition.setPositionUrl(position.getPositionUrl());
		newPosition.setGoodSkillSet(position.getGoodSkillSet());
		newPosition.setReqSkillSet(position.getReqSkillSet());
		newPosition.setType(position.getType());
		newPosition.setStatus(Status.Active.toString());
		newPosition.setRemoteWork(Boolean.parseBoolean(position.getRemoteWork()));
		newPosition.setMaxSal(position.getMaxSal());
		newPosition.setNotes(position.getNotes());
		newPosition.setSalUnit(position.getSalUnit());
		newPosition.setDescription(position.getDescription());
		newPosition.setHrExecutives(hrList);
		newPosition.setClient(clientService.getClientByName(clientName));
		newPosition.setDecisionMakers(null);
		newPosition.addInterviewerPanel(interviewerList);
		newPosition.setExperienceRange(position.getExperienceRange());
		newPosition.setEducationalQualification(position.getEducationalQualification());
		newPosition.setFunctionalArea(position.getFunctionalArea());
		newPosition.setIndustry(position.getIndustry());
		newPosition.setMinSal(position.getMinSal());
		newPosition.setNationality(position.getNationality());
		newPosition.setVendors(vendors);
		if (null != position.getTeamId()) {
			newPosition.setTeam(teamService.findOne(position.getTeamId()));
		}

		if (null != position.getCustomField()) {
			newPosition.setCustomField(position.getCustomField());
		}

		newPosition.setVerticalCluster(position.getVerticalCluster());
		newPosition.setEndClient(position.getEndClient());
		newPosition.setHiringManager(position.getHiringManager());
		newPosition.setScreener(position.getScreener());
		newPosition.setRequisitionId(position.getRequisitionId());
		newPosition.setSpoc(position.getSpoc());

		positionService.addPosition(newPosition);

		// if entries are there in generic interviewer list then adding it to
		// ClientInterviewerPanel
		if (null != position.getGenericInterviewerList() && !position.getGenericInterviewerList().isEmpty()) {
			position.setGenericInterviewerList(
					genericInterviewerService.saveInterviewer(position.getGenericInterviewerList()));
			for (GenericInterviewer genericInterviewer : position.getGenericInterviewerList()) {
				genericInterviewerService.addGenericInterviewerToPosition(genericInterviewer, newPosition.getId());
			}
		}

		// storing the JD file here
		if (file != null && !file.isEmpty()) {
			uploadFileService.createFolderStructureForPosition(newPosition.getId() + "");
			File jdFile = fileService.multipartToFile(file);
			String jdPath = uploadFileService.uploadFileToLocalServer(jdFile, fileName, "jd", newPosition.getId() + "");
			String pdfFilePath = fileService.convert(jdPath);
			newPosition.setJdPath(pdfFilePath);
			positionService.save(newPosition);
		} else if (position.getJdLink() != null && !position.getJdLink().isEmpty()) {
			try {
				uploadFileService.createFolderStructureForPosition(newPosition.getId() + "");
				Path jdFilePath = Paths.get(fileName);
				String jdPath = uploadFileService.uploadFileToLocalServer(jdFilePath.toFile(), fileName, "jd",
						newPosition.getId() + "");
				if (!jdPath.toLowerCase().endsWith(".pdf")) {
					String pdfFilePath = fileService.convert(jdPath);
					newPosition.setJdPath(pdfFilePath);
				} else {
					newPosition.setJdPath(jdPath);
				}
				positionService.save(newPosition);
			} catch (NoSuchFileException ex) {
				return new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
			}
		}

		// Generating position Code
		String[] positionName = StringUtils.cleanFileName(position.getTitle().trim()).split(" ");
		String posCode = "";
		posCode = positionName[0].substring(0, 1) + "_" + newPosition.getId();

		newPosition.setPositionCode(posCode.toLowerCase());
		positionService.save(newPosition);

		// updating requested position here if any associated with this
		if (position.getId() >= 0) {
			PositionRequest requestedPosition = positionRequestService.findOne(position.getId());
			if (requestedPosition != null) {
				String oldStatus = requestedPosition.getStatus();
				requestedPosition.setStatus(PositionRequestStatus.InProcess.toString());
				requestedPosition.setPositionId(newPosition.getId());
				requestedPosition.setPositionCode(newPosition.getPositionCode());
				requestedPosition.setProcessedDate(new Date());
				positionRequestService.save(requestedPosition);
				positionRequestService.sendEmailToDeptHeadOnPositionStatusChange(
						PositionRequestStatus.InProcess.getDisplayName(), requestedPosition, oldStatus);
			}
		}

		// check here if propsectPositionId is available then change their
		// status
		if (position.getProspectPositionId() != null && !position.getProspectPositionId().isEmpty()) {
			prospectPositionService
			.changeStatusOfRequestedProspectPosition(Long.valueOf(position.getProspectPositionId()));
		}

		// adding position added activity
		positionService.addPositionCreateActivity(newPosition);

		//	// sending email to team members
		//	if(position.getTeamId() != null) {
		//	    final String template = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
		//		String subject = "You have been added to position " + position.getTitle();
		//		positionService.sendEmailToTeam(subject, template, position.getTeamId(), newPosition);
		//	}

		RestResponse addPositionResponse = new RestResponse(RestResponse.SUCCESS,
				positionService.getPositionByCode(newPosition.getPositionCode()));
		return addPositionResponse;
	}

	/**
	 * This method is used to update position.
	 *
	 * @param position
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}", method = RequestMethod.POST)
	public RestResponse updatePosition(@PathVariable("positionId") String positionId,
			@RequestPart("json") @Valid PositionDTO positionDTO, @RequestParam String clientName,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "fileName", required = false) String fileName)
					throws RecruizException, ParseException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdatePosition.name());

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (clientName == null || clientName.isEmpty())
			return null;

		Position position = positionService.findOne(Long.parseLong(positionId));

		// deleting file if it exists or marked for deletion
		if ((file != null && !file.isEmpty()) && positionDTO.getJdLink().equalsIgnoreCase("")) {
			fileService.deleteFile(positionDTO.getJdLink());
		}

		// storing the JD file here
		if (file != null && !file.isEmpty()) {
			uploadFileService.createFolderStructureForPosition(position.getId() + "");
			File jdFile = fileService.multipartToFile(file);
			String jdPath = uploadFileService.uploadFileToLocalServer(jdFile, fileName, "jd", position.getId() + "");
			String pdfFilePath = fileService.convert(jdPath);
			positionDTO.setJdLink(pdfFilePath);
		}

		if (position.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| position.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (position.getStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| position.getStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		Position updatedPosition = positionService.updatePosition(positionId, positionDTO, clientName);

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, updatedPosition);
		return positionResponse;
	}

	/**
	 * This Method is used to change the status of position.
	 *
	 * @param positionId
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/status", method = RequestMethod.PUT)
	public RestResponse changePositionStatus(@PathVariable("positionId") String positionId,
			@RequestParam(value = "status", required = false) String status) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangePositionStatus.name());

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (status == null || status.isEmpty())
			return null;

		Position position = positionService.updatePositionStatus(Long.valueOf(positionId), status);

		//Unpublish from career site if position is Closed or StopSourcing or OnHold
		if(status.equalsIgnoreCase(Status.StopSourcing.name()) || status.equalsIgnoreCase(Status.Closed.name()) || (status.equalsIgnoreCase(Status.OnHold.name())))
		{
			positionService.publishCareerSitePosition(Long.valueOf(positionId), Boolean.FALSE);

		}

		position.getGoodSkillSet().size();
		position.getReqSkillSet().size();
		position.getEducationalQualification().size();
		position.getClient().getClientName();
		position.getCustomField().size();
		position.getVendors().size();
		Map<String, String> boardCandidateCount = roundCandidateService
				.getBoardCandidateCount(position.getPositionCode());
		position.setBoardCandidateCount(boardCandidateCount);
		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, position);
		return positionResponse;
	}

	/**
	 * This Method is used to publish the position for career site
	 *
	 * @param positionId
	 * @param publish
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/publish", method = RequestMethod.PUT)
	public RestResponse publishCareerSitePosition(@PathVariable("positionId") String positionId,
			@RequestParam(value = "publish") boolean publish) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.PublishToCareerSite.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Position position = positionService.publishCareerSitePosition(Long.valueOf(positionId), publish);
		position.getGoodSkillSet().size();
		position.getReqSkillSet().size();
		position.getEducationalQualification().size();
		position.getClient().getClientName();
		position.getCustomField().size();
		position.getVendors().size();
		Map<String, String> boardCandidateCount = roundCandidateService
				.getBoardCandidateCount(position.getPositionCode());
		position.setBoardCandidateCount(boardCandidateCount);
		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, position);
		return positionResponse;
	}

	/**
	 * This Method is used to publish the position for recruiz connect
	 *
	 * @param positionId
	 * @param publish
	 * @return
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/publish/connect", method = RequestMethod.PUT)
	public RestResponse publishRecruizConnectPosition(@PathVariable("positionId") String positionId,
			@RequestParam(value = "publish") boolean publish) throws NumberFormatException, Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.PublishRecruizConnectPosition.name());
		 */
		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Position position = positionService.publishRecruizConnectPosition(Long.valueOf(positionId), publish);
		position.getGoodSkillSet().size();
		position.getReqSkillSet().size();
		position.getEducationalQualification().size();
		position.getClient().getClientName();
		position.getCustomField().size();
		position.getVendors().size();
		Map<String, String> boardCandidateCount = roundCandidateService
				.getBoardCandidateCount(position.getPositionCode());
		position.setBoardCandidateCount(boardCandidateCount);
		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, position);
		return positionResponse;
	}

	/**
	 * This method is used to get list of all positions irrespective of the logged
	 * in user . It fetches all in db that is of the given status
	 *
	 * @return
	 * @throws RecruizException
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/api/v1/position/status/{status}", method = RequestMethod.GET)
	public RestResponse getAllPosition(@PathVariable("status") String status) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllPosition.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!permissionService.hasOrgAdminPermission() && !permissionService.isSuperAdmin()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		List<Position> positionList = positionService.getPositionByStatus(status);
		RestResponse postionsResponse = new RestResponse(RestResponse.SUCCESS, positionList);
		return postionsResponse;
	}

	/**
	 * This Method is used to get list of all positions.
	 *
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/api/v1/position", method = RequestMethod.GET)
	public RestResponse getAllPosition(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
					throws RecruizException, ParseException, UnsupportedEncodingException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllPosition.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (permissionService.hasNormalRole()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}
		Page<Position> positionList = positionService.getAllPosition(
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));

		if (positionList == null || positionList.getContent().isEmpty()) {
			return new RestResponse(false, ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.NO_POSITION);
		}

		positionService.calculateFinalStatusForPositions(positionList.getContent());

		RestResponse postionsResponse = new RestResponse(RestResponse.SUCCESS, positionList);
		return postionsResponse;
	}

	@RequestMapping(value = "/api/v1/client/position", method = RequestMethod.GET)
	public RestResponse getAllPositionByClient(@RequestParam String clientId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllPositionByClient.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (clientId == null || clientId.isEmpty())
			return null;
		Client client = clientService.findOne(Long.parseLong(clientId));

		Page<Position> allPosition = positionService.getAllPositionByClient(client,
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				sortField);
		if (allPosition != null) {
			for (Position position : allPosition) {
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getCustomField().size();
			}
		}

		if (allPosition == null || allPosition.getContent().isEmpty()) {
			return new RestResponse(false, ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.NO_POSITION);
		}

		positionService.calculateFinalStatusForPositions(allPosition.getContent());

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, allPosition);
		return positionResponse;
	}

	@RequestMapping(value = "/api/v1/client/positionByClientName", method = RequestMethod.GET)
	public RestResponse getAllPositionByClientName(@RequestParam String clientName,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllPositionByClient.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (clientName == null || clientName.isEmpty())
			return null;
		Client client = clientService.getClientByName(clientName);

		Page<Position> allPosition = positionService.getAllPositionByClient(client,
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				sortField);
		if (allPosition != null) {
			for (Position position : allPosition) {
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getCustomField().size();
			}
		}

		if (allPosition == null || allPosition.getContent().isEmpty()) {
			return new RestResponse(false, ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.NO_POSITION);
		}

		positionService.calculateFinalStatusForPositions(allPosition.getContent());

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, allPosition);
		return positionResponse;
	}

	/**
	 * This method is used to delete position by positionId
	 *
	 * @param positionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}", method = RequestMethod.DELETE)
	public RestResponse deletePosition(@PathVariable("positionId") String positionId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeletePosition.name());

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		long pid = Long.parseLong(positionId);
		candidateStatusService.deleteByPositionId(pid);
		positionService.deletePosition(pid);

		List<PositionFile> positionFiles = positionFileRepository.getPositionFilesByPositionId(pid+"");

		for (PositionFile positionFile : positionFiles) {

			positionFileRepository.delete(positionFile.getId());
			try{
				uploadFileService.deleteCandidateFolderFromAWS(positionFile.getFilePath());
			}catch(Exception e){ 
				e.printStackTrace();
			}
			try{
				fileService.deleteFile(positionFile.getFilePath());
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		RestResponse deletePositionResponse = new RestResponse(RestResponse.SUCCESS,
				RestResponseConstant.POSITION_DELETED);
		return deletePositionResponse;
	}

	/**
	 * This method is used to check position code exists.
	 *
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/check", method = RequestMethod.GET)
	public RestResponse isPositionCodeExist(@RequestParam("positionCode") String positionCode) throws RecruizException {

		if (positionCode == null || positionCode.isEmpty())
			return null;
		boolean isExist = positionService.isPositionExists(positionCode);
		if (isExist)
			throw new RecruizWarnException(ErrorHandler.DUPLICATE_POSITION, ErrorHandler.POSITON_CODE_EXIST);
		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, isExist);
		return positionResponse;
	}

	@RequestMapping(value = "/api/v1/position/interviewer", method = RequestMethod.GET)
	public RestResponse getPositionInterviewerList(@RequestParam String positionCode) throws RecruizException {

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;
		Set<ClientInterviewerPanel> interviewerList = positionService.getPositionInterviewer(positionCode);
		RestResponse interviewers = new RestResponse(RestResponse.SUCCESS, interviewerList);
		return interviewers;
	}

	@RequestMapping(value = "/api/v1/position/interviewer/decisionmaker", method = RequestMethod.GET)
	public RestResponse getPositionDecisionMaker(@RequestParam String positionCode) throws RecruizException {

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;
		Position position = positionService.getPositionByCode(positionCode);

		List<ClientDecisionMaker> decisionMakerList = decisionMakerService
				.getDecisionMakerByClient(position.getClient());
		Set<ClientInterviewerPanel> interviewerList = positionService.getPositionInterviewer(positionCode);
		Map<String, Object> interviewerAndDecisionMakerMap = new HashMap<String, Object>();

		interviewerAndDecisionMakerMap.put("interviewer", interviewerList);
		interviewerAndDecisionMakerMap.put("decisionmaker", decisionMakerList);

		RestResponse decisionMakers = new RestResponse(RestResponse.SUCCESS, interviewerAndDecisionMakerMap, null);
		return decisionMakers;
	}

	@RequestMapping(value = "/api/v1/position/getPositionHRList", method = RequestMethod.GET)
	public RestResponse getPositionHRList(@RequestParam String positionCode) throws RecruizException {

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;
		Set<User> HRList = positionService.getPositionHRList(positionCode);
		RestResponse hrList = new RestResponse(RestResponse.SUCCESS, HRList, null);
		return hrList;
	}

	@RequestMapping(value = "/api/v1/position/getBoard", method = RequestMethod.POST)
	public RestResponse getBoard(@RequestParam String positionCode, @RequestParam(required = false) String status,
			@RequestParam(required = false) String sourcedBy, @RequestBody ReportDropdownDTO reportDropdownDTO)
					throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(), UsageActionType.GetBoard.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;
		Position position = positionService.getPositionByCode(positionCode);
		if (position == null) {
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		}

		if (!position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				&& !positionService.isLoggedInUserWorkingOn(position) && !permissionService.isSuperAdmin()) {

			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			if (null == teams || teams.isEmpty() || !teams.contains(position.getTeam())) {
				return new RestResponse(false, ErrorHandler.DOES_NOT_HAVE_PERMISSION,
						ErrorHandler.NOT_AUTHORISIED_TO_BOARD);
			}
		}

		positionService.calculateFinalStatusForPosition(position);

		// this condition only when user routes from performance report and
		// view board per user per position
		Date[] dateArray = DateUtil.calculateTimePeriod(reportDropdownDTO.getTimePeriod(),
				reportDropdownDTO.getStartDate(), reportDropdownDTO.getEndDate());

		Date startDate = dateArray[0];
		Date endDate = dateArray[1];
		String boardStatus = BoardStatus.getValueByDisplayName(status) != null
				? BoardStatus.getValueByDisplayName(status).name()
						: null;

				BoardDTO boardDTO = boardService.getBoard(positionCode, boardStatus, sourcedBy, startDate, endDate);
				boardDTO.setPositionStatus(position.getFinalStatus());
				RestResponse board = new RestResponse(RestResponse.SUCCESS, boardDTO, null);
				return board;
	}

	/**
	 * This method is used to get position object by positionId.
	 *
	 * @param positionId
	 * @return
	 * @throws RecruizException
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}", method = RequestMethod.GET)
	public RestResponse getPositionById(@PathVariable("positionId") String positionId) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionDetails.name());*/

		/*if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);*/

		long pid = Long.parseLong(positionId);
		Position position = positionService.getPositionById(pid);
		if (position == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS,
					ErrorHandler.POSITION_NOT_FOUND);

		positionService.calculateFinalStatusForPosition(position);

		// for UI to show the display name, adding in response
		positionService.getEmploymentTypeDisplayName(position);

		Map<String, Object> positionMap = new LinkedHashMap<String, Object>();
		position.getGoodSkillSet().size(); // Need to load lazy
		position.getReqSkillSet().size(); // need to load lazy
		position.getEducationalQualification().size(); // need to load lazy
		position.getVendors().size(); // need to load lazy
		position.getCustomField().size(); // need to load lazy
		if (null != position.getTeam() && position.getTeam().getMembers() != null) {
			position.getTeam().getMembers().size();
		}

		positionMap.put(RestResponseConstant.POSITION, position);
		positionMap.put(RestResponseConstant.CLIENT, position.getClient().getClientName());
		positionMap.put(RestResponseConstant.CLIENT_NOTES, position.getClient().getNotes());
		positionMap.put(RestResponseConstant.HRLIST,
				dataModelToDTOConversionService.convertHrExecutives(position.getHrExecutives()));
		positionMap.put(RestResponseConstant.DIRECT_HR_LIST,
				dataModelToDTOConversionService.convertHrExecutives(position.getDirectHrExecutives()));
		positionMap.put(RestResponseConstant.INTERVIEWERlIST,
				dataModelToDTOConversionService.convertInterviewers(position.getInterviewers()));
		positionMap.put(RestResponseConstant.POSITION_EMAIL,
				uniqueIdentifierGeneratorService.generateUniqueResumeEmailForPosition(position.getPositionCode()));

		int joinedOREmployeeCount = positionService.getJoinedOREmployeeCount(position.getPositionCode()); 
		positionMap.put("closures", joinedOREmployeeCount);
		positionMap.put("pending_openings", position.getTotalPosition()-joinedOREmployeeCount);

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, positionMap);
		return positionResponse;
	}

	/**
	 * This method is used to get position email Identifier
	 *
	 * @param positionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/email", method = RequestMethod.GET)
	public RestResponse getPositionEmail(@RequestParam("positionCode") String positionCode) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionEmail.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;

		if (positionService.getPositionByCode(positionCode) == null)
			throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		String currentTenant = TenantContextHolder.getTenant();
		String positionEmailCode = positionCode + "-" + currentTenant;

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, positionEmailCode);
		return positionResponse;
	}

	/**
	 * This method is used to get position url to be shared Identifier
	 *
	 * @param positionId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/url", method = RequestMethod.GET)
	public RestResponse getPositionUrl(@RequestParam("positionCode") String positionCode,
			@RequestParam("sourceMode") String sourceMode) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionURL.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty() || sourceMode == null || sourceMode.isEmpty())
			return null;

		if (positionService.getPositionByCode(positionCode) == null)
			throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		String positionUrl = positionService.getPositionUrl(positionCode, sourceMode);

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, positionUrl);
		return positionResponse;
	}

	@RequestMapping(value = "/api/v1/position/name/map", method = RequestMethod.GET)
	public RestResponse getPositionNameCodeMap() throws RecruizException {

		Set<PositionClientNameDTO> positionMap = positionService.getPositionCodeNameMapForLoggedInUser();

		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, positionMap);
		return positionResponse;
	}

	@RequestMapping(value = "/api/v1/depthead/position/getBoard", method = RequestMethod.GET)
	public RestResponse getBoardForDeptHead(@RequestParam String positionCode) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetBoardForDeptHead.name());*/

		if (positionCode == null || positionCode.isEmpty())
			return null;

		Position position = positionService.getPositionByCode(positionCode);
		if (position == null) {
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		}

		PositionRequest requestedPosition = positionRequestService.getPositionRequestByPositionCode(positionCode);
		if (requestedPosition == null) {
			return new RestResponse(false, ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.NO_POSITION);
		}

		BoardDTO boardDTO = boardService.getBoard(positionCode, null, null, null, null);
		RestResponse board = new RestResponse(RestResponse.SUCCESS, boardDTO, null);
		return board;
	}

	@RequestMapping(value = "/api/v1/position/board/permitted", method = RequestMethod.GET)
	public RestResponse isBoardViewPermitted(@RequestParam String positionCode) throws RecruizException {

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (positionCode == null || positionCode.isEmpty())
			return null;

		Position position = positionService.getPositionByCode(positionCode);
		if (position == null) {
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);
		}
		if (permissionService.isSuperAdmin()) {
			return new RestResponse(true, "yes");
		} else if (!position.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				&& !positionService.isLoggedInUserWorkingOn(position)) {
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			if (null != teams && !teams.isEmpty() && teams.contains(position.getTeam())) {
				return new RestResponse(true, "yes");
			}

			return new RestResponse(false, ErrorHandler.DOES_NOT_HAVE_PERMISSION,
					ErrorHandler.NOT_AUTHORISIED_TO_BOARD);
		}
		return new RestResponse(true, "yes");
	}

	/*
	 * as per discussion creating this API to pass all position information to
	 * interviewer
	 *
	 */
	@RequestMapping(value = "/api/v1/external/position/interviewer/{positionCode}", method = RequestMethod.GET)
	public RestResponse getPositionDetails(@PathVariable("positionCode") String positionCode)
			throws RecruizException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionDetails.name());

		Map<String, Object> positionDTO = externalUserService.getPosition(positionCode);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, positionDTO);
		return response;
	}

	/**
	 * This method is used to get position details title, qoute and descriptions to
	 * share on social sites i.e, FB, twitter etc
	 *
	 * @param (positionCode) shareprovider
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/share/details/{positionCode}", method = RequestMethod.GET)
	public RestResponse getPositionDetailsForSocialShare(@PathVariable("positionCode") String positionCode)
			throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionDetails.name());*/

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User user = userService.getLoggedInUserObject();
		Map<String, String> positionDetails = positionService.getPositionDetailsForSocilShare(positionCode, user);
		RestResponse positionResponse = new RestResponse(RestResponse.SUCCESS, positionDetails);
		return positionResponse;
	}

	@RequestMapping(value = "/api/v1/position/jd/candidate/email/{positionCode}", method = RequestMethod.GET)
	public RestResponse getJDTemplate(@PathVariable("positionCode") String positionCode) throws RecruizException {
		Position position = positionService.getPositionByCode(positionCode);

		Map<String, String> jdMap = new HashMap<>();
		if (null == position) {
			new RestResponse(false, "Position not found", ErrorHandler.NO_POSITION);
		}

		String jdContent = positionService.getRenderedJDTemplateStringForPosition(positionCode);
		String subject = "JD for " + position.getTitle() + " at " + position.getClient().getClientName();
		jdMap.put("subject", subject);
		jdMap.put("content", jdContent);

		RestResponse jdResponse = new RestResponse(RestResponse.SUCCESS, jdMap);
		return jdResponse;
	}

	/**
	 * @param positionId
	 * @param positionNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/notes", method = RequestMethod.POST)
	public RestResponse addPositionNotes(@PathVariable("positionId") Long positionId,
			@RequestBody PositionNotes positionNotes) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddPositionNotes.name());*/

		try {
			Position position = positionService.getPositionById(positionId);
			if (position == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXIST,
						ErrorHandler.POSITION_NOT_FOUND);
			}
			positionNotes.setPositionID(position);
			positionNotesService.save(positionNotes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, positionNotes);
		return candidatetResponse;
	}

	/**
	 *
	 * @param positionId
	 * @param notesId
	 * @param positionNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/notes/{notesId}", method = RequestMethod.POST)
	public RestResponse updatePositionNotes(@PathVariable("positionId") Long positionId,
			@PathVariable("notesId") Long notesId, @RequestBody PositionNotes positionNotes) throws RecruizException {
		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdatePositionNotes.name());*/

		try {
			Position position = positionService.getPositionById(positionId);
			PositionNotes notes = positionNotesService.findOne(notesId);
			if (position == null || notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_UPDATE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			notes.setNotes(positionNotes.getNotes());
			positionNotes = positionNotesService.save(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, positionNotes);
		return candidatetResponse;
	}

	/**
	 *
	 * @param notesId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/position/notes/{notesId}", method = RequestMethod.DELETE)
	public RestResponse deletePositionNote(@PathVariable("notesId") String notesId) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeletePositionNote.name());*/

		try {
			PositionNotes notes = positionNotesService.findOne(Long.parseLong(notesId));
			if (notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_DELETE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			positionNotesService.delete(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTES_DELETED);
		return candidatetResponse;
	}

	/**
	 *
	 * @param positionId
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/position/{positionId}/notes", method = RequestMethod.GET)
	public RestResponse getPositionNotes(@PathVariable("positionId") Long positionId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

		Page<PositionNotes> positionNotes = null;
		try {
			Position position = positionService.getPositionById(positionId);

			positionNotes = positionNotesService.getAllClienNotesByClient(position, pageableService
					.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}

		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, positionNotes);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/position/{pid}/interviewer/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteInterviewerFromPosition(@PathVariable("id") Long id, @PathVariable("pid") Long pid)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteInterviewerFromPosition.name());

		RestResponse response = null;
		try {
			Set<ClientInterviewerPanel> existingInterviewer = positionService.removeInterviewer(pid, id);
			response = new RestResponse(true, existingInterviewer);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_INTERVIEWER,
					ErrorHandler.FAILED_DELETING_INTERVIEWER);
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/position/{pid}/hr/add", method = RequestMethod.PUT)
	public RestResponse addHRToPosition(@RequestParam(required = false) Long teamId,
			@RequestParam(required = false) List<Long> hrids, @PathVariable("pid") Long pid) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddHRToPosition.name());

		RestResponse response = null;
		try {
			Set<User> existingHrs = new HashSet<>();
			if (hrids != null && !hrids.isEmpty()) {
				existingHrs = positionService.addHrToPosition(hrids, pid);
			}
			Long oldTeamId = null;
			Position position = positionService.findOne(pid);
			if (position.getTeam() != null) {
				oldTeamId = position.getTeam().getId();
			}

			if (teamId != null && teamId > 0) {
				position.setTeam(teamService.findOne(teamId));
			} else {
				position.setTeam(null);
			}
			positionService.save(position);

			//	    if(teamId != null && oldTeamId != teamId) {
			//	     	final String template = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
			//		String subject = "You have been added to position " + position.getTitle();
			//		positionService.sendEmailToTeam(subject, template, teamId, position);
			//	    }

			response = new RestResponse(true, existingHrs);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_ADD_HR_TO_POSITION,
					ErrorHandler.FAILED_ADDING_HR);
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/position/{pid}/hr/delete/{hrId}", method = RequestMethod.DELETE)
	public RestResponse deleteHrFromPosition(@PathVariable("hrId") Long hrId, @PathVariable("pid") Long pid)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteHRFromPosition.name());

		RestResponse response = null;
		try {
			Set<User> existingHrs = positionService.removeHrFromPosition(hrId, pid);
			response = new RestResponse(true, existingHrs);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_HR, ErrorHandler.FAILED_DELETING_HR);
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/position/{pid}/vendor/add", method = RequestMethod.PUT)
	public RestResponse addVendorToPosition(@RequestParam List<Long> vids, @PathVariable("pid") Long pid)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddVendorToPosition.name());

		RestResponse response = null;
		try {
			Set<Vendor> existingVendors = positionService.addVendorToPosition(vids, pid);
			response = new RestResponse(true, existingVendors);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_ADD_VENDOR_TO_POSITION,
					ErrorHandler.FAILED_ADDING_VENDOR);
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/position/{pid}/vendor/delete/{vid}", method = RequestMethod.DELETE)
	public RestResponse deleteVendorFromPosition(@PathVariable("vid") Long vid, @PathVariable("pid") Long pid)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.RemoveVendorFromPosition.name());

		RestResponse response = null;
		try {
			Set<Vendor> vendors = positionService.removeVendorFromPosition(vid, pid);
			response = new RestResponse(true, vendors);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_VENDOR,
					ErrorHandler.FAILED_DELETING_VENDOR);
		}

		return response;
	}

	/**
	 * To get the list of position which will be used to assign HR executives from
	 * admin page
	 *
	 * @param userId
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/api/v1/position/assign/hr/{userId}", method = RequestMethod.GET)
	public RestResponse getAllPositionToAssignToHrList(@PathVariable Long userId,
			@RequestParam(value = "pageNo", required = false, defaultValue = "0") String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
					throws RecruizException, ParseException, UnsupportedEncodingException {
		RestResponse response = null;

		User userToAssign = userService.findOne(userId);
		if (!permissionService.belongsToHrExecGroup(userToAssign.getUserRole())
				&& !permissionService.belongsToHrManagerGroup(userToAssign.getUserRole())) {
			return new RestResponse(false, ErrorHandler.CAN_NOT_ASSIGN_NON_HR_TO_POSITION,
					ErrorHandler.HR_ASSIGNMENT_FAILED);
		}

		Set<User> hrUserGroup = new HashSet<>();
		hrUserGroup.add(userToAssign);
		int page = Integer.parseInt(pageNo);
		Pageable pageable = pageableService.defaultPageRequest(page, 25);
		Page<Position> positions = positionService.getAllPositionByStatusAndHrNotIn(Status.Active.name(), hrUserGroup,
				pageable);

		// initialing position
		for (Position position : positions) {
			position.getClient();
		}

		response = new RestResponse(true, positions);
		return response;
	}

	@RequestMapping(value = "/api/v1/hr/position/assign/{hrId}", method = RequestMethod.PUT)
	public RestResponse getAllPositionToAssignToHrList(@PathVariable Long hrId, @RequestParam List<Long> positionIds) {
		RestResponse response = null;
		try {
			positionService.assignHrToPositions(hrId, positionIds);
			response = new RestResponse(true, SuccessHandler.HR_ASSIGNMENT_SUCCESSFUL);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = new RestResponse(false, ex.getMessage(), ErrorHandler.FAILED_TO_ASSIGN_HR_TO_POSITION);
		}

		return response;
	}


	@RequestMapping(value = "/api/v1/jd/email", method = RequestMethod.POST)
	public RestResponse sendJdTemplate(@RequestPart("json") @Valid EmailActivity emailActivity,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "emailList", required = false) List<String> emailList)
					throws RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.SendJdInEmail.name());

		// render email template with master template
		String tenant = TenantContextHolder.getTenant();
		String interestedResponse = "interested";
		String notInterestedResponse = "notinterested";
		String hrEmail = userService.getLoggedInUserEmail();
		String interestedButtonText = "I'm Interested";
		String notInterestedButtonText = "Not Interested";

		String emailBody = emailActivity.getBody();
		
		if(emailActivity.getToEmails()!=null && emailList==null){
		for (String emailTo : emailActivity.getToEmails()) {

			String interestedLink = baseUrl + GlobalConstants.JD_SHARE_URL + "resp=" + interestedResponse + "&cemail="
					+ emailTo + "&pid=" + emailActivity.getPcode() + "&tid=" + tenant + "&from="
					+ hrEmail + "&src=" + GlobalConstants.SHARE_MODE_JD;
			String notInterestedLink = baseUrl + GlobalConstants.JD_SHARE_URL + "resp=" + notInterestedResponse + "&cemail="
					+ emailTo + "&pid=" + emailActivity.getPcode() + "&tid=" + tenant + "&from="
					+ hrEmail + "&src=" + GlobalConstants.SHARE_MODE_JD;

			String renderedTemplate = emailTemplateDataService.getMasterTemplateWith2ButtonForJDShare(
					emailBody, interestedButtonText, interestedLink, notInterestedButtonText,
					notInterestedLink);
			emailActivity.setBody(renderedTemplate);

			emailActivity.setEmailTo(emailTo);
			emailActivityService.sendBulkEmailActivity(emailActivity, emailList, file, fileName, true, null);
			if(positionCode!=null && !positionCode.isEmpty()){
				Position position = positionService.getPositionByCode(positionCode);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}
			}

		}
		}else{
			String interestedLink = baseUrl + GlobalConstants.JD_SHARE_URL + "resp=" + interestedResponse + "&cemail="
					+ emailActivity.getEmailTo() + "&pid=" + emailActivity.getPcode() + "&tid=" + tenant + "&from="
					+ hrEmail + "&src=" + GlobalConstants.SHARE_MODE_JD;
				String notInterestedLink = baseUrl + GlobalConstants.JD_SHARE_URL + "resp=" + notInterestedResponse + "&cemail="
					+ emailActivity.getEmailTo() + "&pid=" + emailActivity.getPcode() + "&tid=" + tenant + "&from="
					+ hrEmail + "&src=" + GlobalConstants.SHARE_MODE_JD;

				String renderedTemplate = emailTemplateDataService.getMasterTemplateWith2ButtonForJDShare(
					emailActivity.getBody(), interestedButtonText, interestedLink, notInterestedButtonText,
					notInterestedLink);
				emailActivity.setBody(renderedTemplate);

				emailActivityService.sendBulkEmailActivity(emailActivity, emailList, file, fileName, true, null);
				if(positionCode!=null && !positionCode.isEmpty()){
					Position position = positionService.getPositionByCode(positionCode);
					if(position!=null){
						position.setModificationDate(new Date());
						positionService.save(position);
					}
				}
		}
		RestResponse emailActivityResponse = new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
		return emailActivityResponse;
	}




	@RequestMapping(value = "/api/v1/position/{pid}/upload/file", method = RequestMethod.POST)
	public RestResponse uploadPositionFiles(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName,
			@RequestParam("fileType") String fileType, @PathVariable("pid") Long pid) throws RecruizException, IOException {


		if (fileName == null || fileName.isEmpty() || fileType == null || fileType.isEmpty() || pid == null)
			return null;

		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		String positionFolder = uploadFileService.createFolderStructureForPosition(pid + "");
		File fileToUpload = new File(positionFolder + File.separator + fileName);
		if (fileToUpload.exists()) {
			return new RestResponse(false, ErrorHandler.FILE_UPLOAD_FAILED, ErrorHandler.FILE_EXISTS);
		}

		File tmpFile = fileService.multipartToFile(file);
		Files.copy(tmpFile.toPath(), fileToUpload.toPath());

		PositionFile positionFile = new PositionFile();
		positionFile.setPositionId(pid + "");
		positionFile.setFileName(fileName);
		positionFile.setFilePath(fileToUpload.getPath());
		positionFile.setFileType(fileType);

		positionFile = positionFileRepository.save(positionFile);

		if(fileStorageMode!=null && fileStorageMode.equalsIgnoreCase("aws")){

			String filePath = uploadFileService.createFolderAndUploadFileInAwsForPosition(pid,fileToUpload,fileName);
			positionFile.setFilePath(filePath);
			positionFile.setStorageMode("aws");
			positionFileRepository.save(positionFile);
		}

		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, null,"file upload successfully !");
		return candidateAddResponse;
	}


	// to get all position files
	@RequestMapping(value = "/api/v1/position/{pid}/files/all", method = RequestMethod.GET)
	public RestResponse getAllPositionFiles(@PathVariable("pid") String pid) throws RecruizException {

		RestResponse response = null;
		List<PositionFile> files = positionFileRepository.getPositionFilesByPositionId(pid);
		response = new RestResponse(true, files);
		return response;
	}


	// delete position file
	@RequestMapping(value = "/api/v1/position/file/{fileId}/delete", method = RequestMethod.DELETE)
	public RestResponse deleteClientFile(@PathVariable("fileId") Long fileId) throws RecruizException {

		RestResponse response = null;
		PositionFile file = positionFileRepository.findOne(fileId);
		if (null != file) {
			String filePath = file.getFilePath();
			File diskFile = new File(filePath);

			positionFileRepository.delete(fileId);
			if (diskFile.exists()) {
				diskFile.delete();
			}

			try{
				String s3CandidatePath = file.getFilePath();
				if(s3CandidatePath.contains(bucketName))
					uploadFileService.deleteCandidateFolderFromAWS(s3CandidatePath);
			}catch(Exception e){

			}

		}
		response = new RestResponse(true, SuccessHandler.FILE_DELETED);
		return response;
	}



	@RequestMapping(value = "/api/v1/offerletter/workflow/addPositionOfferCost", method = RequestMethod.POST)
	public RestResponse addPositionOfferCost(@RequestBody PositionOfferCostDTO costDTO)throws Exception{

		if(costDTO.getBillRate()!=null && !costDTO.getBillRate().equalsIgnoreCase("") && !costDTO.getBillRate().isEmpty()
				&& costDTO.getBillHours()!=null && !costDTO.getBillHours().equalsIgnoreCase("") && !costDTO.getBillHours().isEmpty()
				&&  costDTO.getProjectDuration()!=null && !costDTO.getProjectDuration().equalsIgnoreCase("") && !costDTO.getProjectDuration().isEmpty()
				&& costDTO.getOneTimeCost()!=null && !costDTO.getOneTimeCost().equalsIgnoreCase("") && !costDTO.getOneTimeCost().isEmpty()
				&& costDTO.getHeadHunting()!=null && !costDTO.getHeadHunting().equalsIgnoreCase("") && !costDTO.getHeadHunting().isEmpty()
				&& costDTO.getPositionId()==0l){
			return new RestResponse(RestResponse.FAILED, "", "Some field value missing !");
		}

		return positionService.addPositionOfferCost(costDTO);
	}



	@RequestMapping(value = "/api/v1/offerletter/workflow/updatePositionOfferCost", method = RequestMethod.POST)
	public RestResponse updatePositionOfferCost(@RequestBody PositionOfferCostDTO costDTO)throws Exception{

		if(costDTO.getBillRate()!=null && !costDTO.getBillRate().equalsIgnoreCase("") && !costDTO.getBillRate().isEmpty()
				&& costDTO.getBillHours()!=null && !costDTO.getBillHours().equalsIgnoreCase("") && !costDTO.getBillHours().isEmpty()
				&&  costDTO.getProjectDuration()!=null && !costDTO.getProjectDuration().equalsIgnoreCase("") && !costDTO.getProjectDuration().isEmpty()
				&& costDTO.getOneTimeCost()!=null && !costDTO.getOneTimeCost().equalsIgnoreCase("") && !costDTO.getOneTimeCost().isEmpty()
				&& costDTO.getHeadHunting()!=null && !costDTO.getHeadHunting().equalsIgnoreCase("") && !costDTO.getHeadHunting().isEmpty()
				&& costDTO.getPositionId()==0l){
			return new RestResponse(RestResponse.FAILED, "", "Some field value missing !");
		}

		return positionService.updatePositionOfferCost(costDTO);
	}



	@RequestMapping(value = "/api/v1/offerletter/workflow/getPositionOfferCostById", method = RequestMethod.GET)
	public RestResponse getPositionOfferCostById(@RequestParam(value = "id") String id)throws Exception{

		return positionService.getPositionOfferCostById(id);
	}


	@RequestMapping(value = "/api/v1/offerletter/workflow/deletePositionOfferCostById", method = RequestMethod.GET)
	public RestResponse deletePositionOfferCostById(@RequestParam(value = "id") String id)throws Exception{

		return positionService.deletePositionOfferCostById(id);
	}


	@RequestMapping(value = "/api/v1/offerletter/workflow/getAllPositionOfferCost", method = RequestMethod.GET)
	public RestResponse getAllPositionOfferCost(@RequestParam(value = "positionId") String positionId)throws Exception{

		return positionService.getAllPositionOfferCost(positionId);
	}

	
	/**
	 * This Method is used to add position for a outsider (Sixthsense user).
	 *
	 * @param position
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/position", method = RequestMethod.POST)
	public RestResponse addPositionFromOutside(@RequestBody PositionDTO position)
					throws RecruizException, ParseException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddPosition.name());
		
	try{
		
		Client client = clientService.getClientByName("VmsClients");
			
		if(client==null){
			ClientDTO clientDTO = new ClientDTO();
			
			clientDTO.setClientName("VmsClients");
			clientDTO.setClientLocation("Dummy Location");
			
			client = clientService.addClient(clientDTO);
		}

		List<String> hrId = position.getHrExexutivesId();
		List<String> interviewerIdList = position.getInterviewerPanelsId();
		List<String> roundIdList = position.getRoundListId();
		// getting list of HRExecutives
		Set<User> hrList = new LinkedHashSet<User>();
		if (hrId != null && !(hrId.isEmpty())) {
			for (String id : hrId) {
				long hid = Long.parseLong(id);
				User hr = userService.findOne(hid);
				hrList.add(hr);
			}
		}

		// getting list of InterviewerList
		Set<ClientInterviewerPanel> interviewerList = new LinkedHashSet<ClientInterviewerPanel>();
		if (interviewerIdList != null && !(interviewerIdList.isEmpty())) {
			for (String id : interviewerIdList) {
				long interviewId = Long.parseLong(id);
				ClientInterviewerPanel interviewPanel = interviewPanelService.findOne(interviewId);
				interviewerList.add(interviewPanel);
			}
		}

		// getting list of Vendors
		Set<Vendor> vendors = new LinkedHashSet<Vendor>();
		if (position.getVendorIds() != null && !(position.getVendorIds().isEmpty())) {
			for (String id : position.getVendorIds()) {
				long vendorId = Long.parseLong(id);
				Vendor vendor = vendorService.findOne(vendorId);
				vendors.add(vendor);
			}
		}

		// getting list of RoundList
		List<Round> roundList = new ArrayList<Round>();
		if (roundIdList != null && !(roundIdList.isEmpty())) {
			for (String id : roundIdList) {
				long roundId = Long.parseLong(id);
				Round round = roundService.findOne(roundId);
				roundList.add(round);
			}
		}

		Position newPosition = new Position();
		newPosition.setPositionCode(position.getPositionCode());
		newPosition.setTitle(position.getTitle());
		newPosition.setLocation(position.getLocation());
		newPosition.setTotalPosition(position.getTotalPosition());
		newPosition.setCloseByDate(position.getCloseByDate());
		newPosition.setPositionUrl(position.getPositionUrl());
		newPosition.setGoodSkillSet(position.getGoodSkillSet());
		newPosition.setReqSkillSet(position.getReqSkillSet());
		newPosition.setType(position.getType());
		newPosition.setStatus(Status.Active.toString());
		newPosition.setRemoteWork(Boolean.parseBoolean(position.getRemoteWork()));
		newPosition.setMaxSal(position.getMaxSal());
		newPosition.setNotes(position.getNotes());
		newPosition.setSalUnit(position.getSalUnit());
		newPosition.setDescription(position.getDescription());
		newPosition.setHrExecutives(hrList);
		newPosition.setClient(client);
		newPosition.setDecisionMakers(null);
		newPosition.addInterviewerPanel(interviewerList);
		newPosition.setExperienceRange(position.getExperienceRange());
		newPosition.setEducationalQualification(position.getEducationalQualification());
		newPosition.setFunctionalArea(position.getFunctionalArea());
		newPosition.setIndustry(position.getIndustry());
		newPosition.setMinSal(position.getMinSal());
		newPosition.setNationality(position.getNationality());
		newPosition.setVendors(vendors);
		if (null != position.getTeamId()) {
			newPosition.setTeam(teamService.findOne(position.getTeamId()));
		}

		if (null != position.getCustomField()) {
			newPosition.setCustomField(position.getCustomField());
		}

		newPosition.setVerticalCluster(position.getVerticalCluster());
		newPosition.setEndClient(position.getEndClient());
		newPosition.setHiringManager(position.getHiringManager());
		newPosition.setScreener(position.getScreener());
		newPosition.setRequisitionId(position.getRequisitionId());
		newPosition.setSpoc(position.getSpoc());

		positionService.addPosition(newPosition);

		// if entries are there in generic interviewer list then adding it to
		// ClientInterviewerPanel
		if (null != position.getGenericInterviewerList() && !position.getGenericInterviewerList().isEmpty()) {
			position.setGenericInterviewerList(
					genericInterviewerService.saveInterviewer(position.getGenericInterviewerList()));
			for (GenericInterviewer genericInterviewer : position.getGenericInterviewerList()) {
				genericInterviewerService.addGenericInterviewerToPosition(genericInterviewer, newPosition.getId());
			}
		}

		/*// storing the JD file here
		if (file != null && !file.isEmpty()) {
			uploadFileService.createFolderStructureForPosition(newPosition.getId() + "");
			File jdFile = fileService.multipartToFile(file);
			String jdPath = uploadFileService.uploadFileToLocalServer(jdFile, fileName, "jd", newPosition.getId() + "");
			String pdfFilePath = fileService.convert(jdPath);
			newPosition.setJdPath(pdfFilePath);
			positionService.save(newPosition);
		} else if (position.getJdLink() != null && !position.getJdLink().isEmpty()) {
			try {
				uploadFileService.createFolderStructureForPosition(newPosition.getId() + "");
				Path jdFilePath = Paths.get(fileName);
				String jdPath = uploadFileService.uploadFileToLocalServer(jdFilePath.toFile(), fileName, "jd",
						newPosition.getId() + "");
				if (!jdPath.toLowerCase().endsWith(".pdf")) {
					String pdfFilePath = fileService.convert(jdPath);
					newPosition.setJdPath(pdfFilePath);
				} else {
					newPosition.setJdPath(jdPath);
				}
				positionService.save(newPosition);
			} catch (NoSuchFileException ex) {
				return new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
			}
		}*/

		// Generating position Code
		String[] positionName = StringUtils.cleanFileName(position.getTitle().trim()).split(" ");
		String posCode = "";
		posCode = positionName[0].substring(0, 1) + "_" + newPosition.getId();

		newPosition.setPositionCode(posCode.toLowerCase());
		positionService.save(newPosition);

		// updating requested position here if any associated with this
		if (position.getId() >= 0) {
			PositionRequest requestedPosition = positionRequestService.findOne(position.getId());
			if (requestedPosition != null) {
				String oldStatus = requestedPosition.getStatus();
				requestedPosition.setStatus(PositionRequestStatus.InProcess.toString());
				requestedPosition.setPositionId(newPosition.getId());
				requestedPosition.setPositionCode(newPosition.getPositionCode());
				requestedPosition.setProcessedDate(new Date());
				positionRequestService.save(requestedPosition);
				positionRequestService.sendEmailToDeptHeadOnPositionStatusChange(
						PositionRequestStatus.InProcess.getDisplayName(), requestedPosition, oldStatus);
			}
		}

		// check here if propsectPositionId is available then change their
		// status
		if (position.getProspectPositionId() != null && !position.getProspectPositionId().isEmpty()) {
			prospectPositionService
			.changeStatusOfRequestedProspectPosition(Long.valueOf(position.getProspectPositionId()));
		}

		// adding position added activity
		positionService.addPositionCreateActivity(newPosition);

		//	// sending email to team members
		//	if(position.getTeamId() != null) {
		//	    final String template = GlobalConstants.EMAIL_TEMPLATE_POSITION_HR_VENDOR_ADDED;
		//		String subject = "You have been added to position " + position.getTitle();
		//		positionService.sendEmailToTeam(subject, template, position.getTeamId(), newPosition);
		//	}

		RestResponse addPositionResponse = new RestResponse(RestResponse.SUCCESS,null,"position added successfullly");
		return addPositionResponse;
		
	  }catch(Exception e){
		  logger.error(""+e);
		  return new RestResponse(RestResponse.FAILED,null, "Internal server error !");
	  }
	}

	

}
