package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateAssesment;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.domain.CandidateRating;
import com.bbytes.recruiz.domain.CandidateRatingQuestion;
import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.ParserCount;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.enums.UserType;
import com.bbytes.recruiz.exception.RecruizCandidateExistException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateCurrentPositionDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateExistsResponseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.FileUploadRequestDTO;
import com.bbytes.recruiz.rest.dto.models.FileUploadResponseDTO;
import com.bbytes.recruiz.rest.dto.models.PluginFileUploadDTO;
import com.bbytes.recruiz.rest.dto.models.PluginUploadDTO;
import com.bbytes.recruiz.rest.dto.models.PositionMatchResult;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.RestResponseEntity;
import com.bbytes.recruiz.service.BoardCustomStatusService;
import com.bbytes.recruiz.service.BulkUploadResumeService;
import com.bbytes.recruiz.service.CandidateAssesmentService;
import com.bbytes.recruiz.service.CandidateFileService;
import com.bbytes.recruiz.service.CandidateNotesService;
import com.bbytes.recruiz.service.CandidateRatingService;
import com.bbytes.recruiz.service.CandidateResumeBulkUploadBatchService;
import com.bbytes.recruiz.service.CandidateResumeUploadItemService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.CustomFieldService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IResumeParserService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.OfferLetterApprovalsService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.ParserCountService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.SearchService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.VendorService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

import edu.emory.mathcs.backport.java.util.Arrays;

@RestController
public class CandidateController {

	private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CustomFieldService customFieldService;
	
	@Autowired
	private BulkUploadResumeService resumeBulkUploadService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private CandidateRatingService candidateRatingService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private FileService fileService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private ParserCountService parserCountService;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private PositionService positionService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private CandidateNotesService candidateNotesService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private CandidateAssesmentService candidateAssesmentService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private CandidateResumeUploadItemService candidateResumeUploadItemService;

	@Autowired
	private CandidateResumeBulkUploadBatchService candidateResumeBulkUploadBatchService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Value("${levelbar.server.url}")
	protected String levelbarBaseUrl;

	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;

	/**
	 * This API is used to add candidates along with file-Resume.
	 *
	 * @param candidate
	 * @param file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate", method = RequestMethod.POST)
	public RestResponse addCandidate(@RequestPart("json") @Valid Candidate candidate,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam("fileName") String fileName,
			@RequestParam(value = "roundId", required = false) String roundId,
			@RequestParam(value = "positionCode", required = false) String positionCode) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkAppSettingsService.isCandidateCountExceeded())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.MAX_CANDIDATE_LIMIT_REACHED,
					ErrorHandler.MAX_CANDIDATE_REACHED);

		// if file is not present then adding dummy file
		if (file == null || file.isEmpty()) {
			fileName = GlobalConstants.DUMMY_RESUME_DOC_FILE_PATH;
			File existingFile = new File(fileName);
			if (existingFile.exists()) {
				FileInputStream input = new FileInputStream(existingFile);
				String contentType = Files.probeContentType(existingFile.toPath());
				file = new MockMultipartFile(existingFile.getName(), existingFile.getName(), contentType,
						IOUtils.toByteArray(input));
				fileName = existingFile.getName();
			}

		} else {
			fileName = GlobalConstants.DUMMY_RESUME_DOC_FILE_PATH;
		}

		if (candidateService.isCandidateExists(candidate.getEmail()))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_EXISTS,
					ErrorHandler.DUPLICATE_CANDIDATE);

		candidate = candidateService.sourceCandidate(candidate, file, fileName, roundId, positionCode);

		Position position = positionService.getPositionByCode(positionCode);
		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}
		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidate);
		return candidateAddResponse;
	}

	/**
	 * to upload candidate resume
	 *
	 * @param file
	 * @param fileName
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */

	@RequestMapping(value = "/api/v1/candidate/upload/resume", method = RequestMethod.POST)
	public RestResponse uploadCandidateResume(@RequestParam("file") MultipartFile file,
			@RequestParam("fileName") String fileName, @RequestParam("candidateId") String candidateId)
					throws RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UploadCandidateResume.name());

		if (candidateId == null || candidateId.isEmpty())
			return null;

		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		Set<Permission> loggedInUserPermissionList = userService.getLoggedInUserObject().getUserRole().getPermissions();
		if (!userRoleService.hasPermission(loggedInUserPermissionList, PermissionConstant.SUPER_ADMIN)) {
			if (!userRoleService.hasPermission(loggedInUserPermissionList, PermissionConstant.ADD_EDIT_CANDIDATE))
				return new RestResponse(RestResponse.FAILED, ErrorHandler.PERMISSION_DENIED, ErrorHandler.INVALID_ROLE);
		}

		File resumeFile = fileService.multipartToFile(file);
		String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, fileName, "resume", candidateId);
		String pdfFilePath = fileService.convert(serverPath);
		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, pdfFilePath);
		return candidateAddResponse;

	}

	@RequestMapping(value = "/api/v1/candidate", method = RequestMethod.GET)
	public RestResponse getAllCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllCandidate.name());

		try {
			Page<Candidate> candidateList = candidateService.getCandidateListForCurrentUser(pageNo, sortField,
					sortOrder);
			candidateService.attachCurrentPosition(candidateList.getContent());
			RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidateList);
			return candidateAddResponse;
		} catch (RecruizPermissionDeniedException e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

	}

	/**
	 * This API is used to do the action for all candidates on all pages
	 *
	 * @param emailActivity
	 * @param file
	 * @param fileName
	 * @param positionCode
	 * @param sourceMode
	 * @param action
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate/overall/pageresult", method = RequestMethod.POST)
	public RestResponse selectAllPageCandidatesAction(@RequestPart("json") @Valid EmailActivity emailActivity,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "sourceMode", required = false) String sourceMode,
			@RequestParam(value = "action") String action) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.BulkOperationOnCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}
		Set<String> candidateEmailList = new HashSet<String>();
		List<Candidate> candidateList = candidateService.getAllCandidates();

		for (Candidate candidate : candidateList) {
			candidateEmailList.add(candidate.getEmail());
		}

		if (GlobalConstants.SEND_EMAIL.equals(action)) {

			emailActivityService.sendBulkEmailActivity(emailActivity, new ArrayList<String>(candidateEmailList), null,
					null, false, null);

			return new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
		} else if (GlobalConstants.ADD_TO_POSITION.equals(action)) {

			CandidateToRoundDTO candidateToRoundDTO = new CandidateToRoundDTO();
			candidateToRoundDTO.setCandidateEmailList(new ArrayList<String>(candidateEmailList));
			candidateToRoundDTO.setPositionCode(positionCode);

			roundCandidateService.addCandidateToPosition(candidateToRoundDTO, sourceMode);
			return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CANDIDATE_ADDED);
		}

		return null;
	}

	/**
	 * Get the list of top N candidate that match the position given .
	 *
	 * @param positionCode
	 * @param pageNo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate/match/position/{postionCode}", method = RequestMethod.GET)
	public RestResponse getPositionMatchCandidates(@PathVariable("postionCode") String positionCode,
			@RequestParam(value = "pageNo", required = false) String pageNo) throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionMatchCandidate.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Position position = positionService.getPositionByCode(positionCode);

		int pageNoValue = 0;

		if (pageNo != null && !pageNo.isEmpty())
			pageNoValue = Integer.parseInt(pageNo);

		Page<Candidate> candidateList = searchService.getTopCandidateForPostion(positionCode,
				pageableService.defaultPageRequest(pageNoValue));

		// Need for lazy loading
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
			candidate.getCustomField().size();
		}

		candidateService.attachCurrentPosition(candidateList.getContent());
		PositionMatchResult positionMatchResult = new PositionMatchResult();
		positionMatchResult.setPosition(position);
		positionMatchResult.setCandidateList(candidateList);
		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, positionMatchResult);
		return candidateAddResponse;
	}

	/**
	 * API to get candidate details and roundId MAP using roundCandidateString
	 *
	 * @param roundCandidateString
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate/round", method = RequestMethod.GET)
	public RestResponse getRoundCandidates(
			@RequestParam(value = "roundCandidateString", required = false) String roundCandidateString,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "mskd", required = false) String mskd,
			@RequestParam(value = "candidateLevelbarkey", required = false) String candidateLevelbarkey)
					throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetRoundCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (roundCandidateString == null || roundCandidateString.isEmpty() || positionCode == null
				|| positionCode.isEmpty())
			return null;
		List<Map<String, Object>> roundCandidateMap = new LinkedList<Map<String, Object>>();
		List<String> roundCandidateList = StringUtils.commaSeparateStringToList(roundCandidateString);
		List<String> candidateLevelbarList = StringUtils.commaSeparateStringToList(candidateLevelbarkey);
		int count = 0;
		for (String colonSeparatedString : roundCandidateList) {
			Map<String, Object> map = new HashMap<String, Object>();
			List<String> colonSeparatedValuesInList = StringUtils.colonSeparateStringToList(colonSeparatedString);
			String roundId = colonSeparatedValuesInList.get(0);
			String candidateId = colonSeparatedValuesInList.get(1);
			Candidate candidate = candidateService.getCandidateById(Long.valueOf(candidateId));
			// marked as masked candidate hiding the info
			if (mskd != null && !mskd.isEmpty() && mskd.equalsIgnoreCase("y")) {
				candidate.setFullName("Hidden");
				candidate.setEmail("Hidden");
				candidate.setAlternateEmail("Hidden");
				candidate.setMobile("XXXXXXXXXXXX");
				candidate.setAlternateMobile("XXXXXXXXXXXX");

				// why list ? -> bcoz all candidate related files belong to one
				// object called candidate files.
				List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
						FileType.Masked_Resume_Converted.getDisplayName(), candidateId + "");
				if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
					String maskedResumeFilePath = candidateMaskedResume.get(0).getFilePath();
					candidate.setResumeLink(maskedResumeFilePath);
				} else {
					candidate.setResumeLink("");
				}

			}

			// Need for lazy loading
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
			candidate.getCustomField().size();

			List<String> colonSeparatedLevelbarKeyList = StringUtils
					.colonSeparateStringToList(candidateLevelbarList.get(count));

			String link = null;
			if (!"null".equals(colonSeparatedLevelbarKeyList.get(1)))
				link = levelbarBaseUrl + GlobalConstants.LEVELBAR_FEEDBACK_URL + colonSeparatedLevelbarKeyList.get(1);

			map.put("roundId", roundId);
			map.put("positionCode", positionCode);
			map.put("candidateDetails", candidate);
			map.put("levelbarkey", link);
			roundCandidateMap.add(map);
			count++;
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, roundCandidateMap);
		return response;
	}



	/*@RequestMapping(value = "/api/v1/offerletter/approval/candidate/round", method = RequestMethod.GET)
	public RestResponse getApprovalRoundCandidates(
			@RequestParam(value = "candidateId", required = false) String candidateId,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "mskd", required = false) String mskd,
			@RequestParam(value = "candidateLevelbarkey", required = false) String candidateLevelbarkey,
			@RequestParam(value = "approvalId", required = false) String approvalId)
					throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetRoundCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (candidateId == null || candidateId.isEmpty() || positionCode == null
				|| positionCode.isEmpty())
			return null;
		List<Map<String, Object>> roundCandidateMap = new LinkedList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
			Candidate candidate = candidateService.getCandidateById(Long.valueOf(candidateId));
			// marked as masked candidate hiding the info
			if (mskd != null && !mskd.isEmpty() && mskd.equalsIgnoreCase("y")) {
				candidate.setFullName("Hidden");
				candidate.setEmail("Hidden");
				candidate.setAlternateEmail("Hidden");
				candidate.setMobile("XXXXXXXXXXXX");
				candidate.setAlternateMobile("XXXXXXXXXXXX");

				// why list ? -> bcoz all candidate related files belong to one
				// object called candidate files.
				List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
						FileType.Masked_Resume_Converted.getDisplayName(), candidateId + "");
				if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
					String maskedResumeFilePath = candidateMaskedResume.get(0).getFilePath();
					candidate.setResumeLink(maskedResumeFilePath);
				} else {
					candidate.setResumeLink("");
				}

			}

			// Need for lazy loading
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
			candidate.getCustomField().size();


			map.put("positionCode", positionCode);
			map.put("candidateDetails", candidate);
			map.put("levelbarkey", link);

			if(approvalId!=null && !approvalId.equalsIgnoreCase("") && !approvalId.isEmpty()){

				OfferLetterApprovals offerLetterApproval = offerLetterApprovalsService.findOne(Long.parseLong(approvalId));
				Position position = positionService.getPositionByCode(positionCode);

				Set<String> skills = candidate.getKeySkills();
				String candidateSkills = "";
				int y = 1;
				for (String skill : skills) {
					if(y==1)
						candidateSkills = skill;
					else
						candidateSkills = candidateSkills + "," + skill;
					y++;

					if(y==3)
						break;
				}

				if(offerLetterApproval!=null){
					map.put("clientName", position.getClient().getClientName());
					map.put("candidateName", candidate.getFullName());
					map.put("skills", candidateSkills);
					map.put("experience", candidate.getTotalExp());
					map.put("percentHike", offerLetterApproval.getPercentage_hike()+" %");
					map.put("ctcOffered", offerLetterApproval.getCtc_offered());
					map.put("marginPercent", offerLetterApproval.getProfit_margin()+" %");
				}

			}

			roundCandidateMap.add(map);


		RestResponse response = new RestResponse(RestResponse.SUCCESS, roundCandidateMap);
		return response;
	}*/


	/**
	 * API to get candidate detail using resume parser.
	 *
	 * @param file
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/resume", method = RequestMethod.POST)
	public RestResponse getCandidateByResumeParser(@RequestPart("file") MultipartFile file)
			throws IllegalStateException, RecruizException, IOException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ParseAndGetCandidate.name());*/

		// need not to check permission as it is being called by vendor and app
		// both, so commenting the check code below

		/*
		 * if (!checkUserPermission.isUserTypeApp()) throw new
		 * RecruizPermissionDeniedException(GlobalConstants.INVLID_USER);
		 */

		return getParsedResume(file);

	}

	public RestResponse getParsedResume(MultipartFile file) throws IOException, RecruizWarnException, RecruizException {
		File resumeFile = null;
		RestResponse candidatetResponse = null;
		try {
			resumeFile = fileService.multipartToFile(file);
			Candidate candidate = resumeParserService.parseResume(resumeFile);

			// adding entry to parser count
			ParserCount parserCount = new ParserCount();
			parserCount.setUsedBy(userService.getLoggedInUserEmail());
			parserCountService.save(parserCount);

			// Need to load lazy
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
			candidate.getFiles().size();
			candidate.getCustomField().size();
			// this field setting to set correct drop down values
			candidate.setEmploymentType(
					(candidate.getEmploymentType() != null && !candidate.getEmploymentType().isEmpty())
					? candidate.getEmploymentType() : "N/A");
			candidate.setGender((candidate.getGender() != null && !candidate.getGender().isEmpty())
					? candidate.getGender() : "N/A");
			candidate.setCommunication((candidate.getCommunication() != null && !candidate.getCommunication().isEmpty())
					? candidate.getCommunication() : "N/A");

			if (null != candidate.getEmail() && !candidate.getEmail().trim().isEmpty()
					&& candidateService.isCandidateExists(candidate.getEmail())) {
				candidatetResponse = new RestResponse(RestResponse.SUCCESS,
						getCandidateExistsResponseDTO(resumeFile, candidate));
			} else {
				candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidate);
			}

			return candidatetResponse;
		} finally {
			// can not delete as we are expecting the path to come if UI asks
			// for update the file

			/**
			 * if (resumeFile != null && resumeFile.exists())
			 * resumeFile.delete();
			 */
		}
	}

	private CandidateExistsResponseDTO getCandidateExistsResponseDTO(File resumeFile, Candidate candidate)
			throws RecruizException {

		Candidate oldCandidate = candidateService.getCandidateByEmail(candidate.getEmail());

		if (null != oldCandidate.getEducationDetails()) {
			oldCandidate.getEducationDetails().size();
			candidate.getCustomField().size();
		}

		boolean isExists = true;
		if (checkUserPermission.isUserTypeVendor()) {
			isExists = checkExistingCandidateAge(oldCandidate);
			if (isExists) {
				CandidateExistsResponseDTO responseDTO = new CandidateExistsResponseDTO();
				responseDTO.setCandidateExists(isExists);
				return responseDTO;
			}
		}

		CandidateExistsResponseDTO responseDTO = new CandidateExistsResponseDTO();
		responseDTO.setCandidateExists(isExists);
		responseDTO.setNewCandidate(candidate);

		responseDTO.setOldCandidate(oldCandidate);
		responseDTO.setCid(oldCandidate.getCid());
		responseDTO.setNewFilePath(resumeFile.getPath());

		return responseDTO;
	}

	@RequestMapping(value = "/api/v1/candidate/find/Candidate", method = RequestMethod.POST)
	public RestResponse getCandidateByEmail(@RequestParam(value = "email", required = false) String email)
			throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateDetails.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (email == null || email.isEmpty())
			return null;
		Candidate candidate = candidateService.getCandidateByEmail(email);
		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidate, null);
		return candidateAddResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/{candidateId}/status", method = RequestMethod.PUT)
	public RestResponse updateCandidateStatus(@PathVariable("candidateId") String candidateId,
			@RequestParam(value = "status", required = false) String status) throws RecruizException {


		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangeCandidateStatus.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (status == null || status.isEmpty())
			return null;
		Candidate candidate = candidateService.updateCandidateStatus(Long.valueOf(candidateId), status);

		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, candidate);
		return candidateResponse;
	}

	/**
	 * This API is used to update candidates along with file-Resume.
	 *
	 * @param candidate
	 * @param file
	 * @param fileName
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}", method = RequestMethod.POST)
	public RestResponse updateCandidate(@RequestPart("json") @Valid Candidate candidate,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(name = "fileName", required = false) String fileName,
			@PathVariable("candidateId") String candidateId) throws RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// converting previously passed file to multipart bcoz UI is not able to
		// pass the file
		if (file == null || file.isEmpty()) {
			File existingFile = new File(fileName);
			if (existingFile.exists()) {
				FileInputStream input = new FileInputStream(existingFile);
				String contentType = Files.probeContentType(existingFile.toPath());
				file = new MockMultipartFile(existingFile.getName(), existingFile.getName(), contentType,
						IOUtils.toByteArray(input));
				fileName = existingFile.getName();
			}
		}

		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		Candidate candidateToUpdate = candidateService.getCandidateById(Long.parseLong(candidateId));

		candidate.setCid(Long.parseLong(candidateId));

		boolean updatable = false;

		if (candidateToUpdate.getOwner() == null || candidateToUpdate.getOwner().isEmpty()) {
			updatable = true;
		} else if (candidateToUpdate.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				|| checkUserPermission.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkUserPermission.hasAddEditCandidatePermission())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PERMISSION_DENIED_NON_OWNER,
					ErrorHandler.NO_OWNERSHIP);

		uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidateId);
		// to delete file if file is passed to update or notified to delete file
		if ((file != null && !file.isEmpty()) || candidate.getResumeLink().equalsIgnoreCase("")) {
			if (candidate.getResumeLink() != null && candidate.getResumeLink().equalsIgnoreCase("")) {
				removeResumeCandidateFile(candidate, candidateId);
			}
		}
		if (file != null && !file.isEmpty()) {
			removeResumeCandidateFile(candidate, candidateId);
			File resumeFile = fileService.multipartToFile(file);
			String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, fileName, "resume", candidateId);
			String pdfFilePath = fileService.convert(serverPath);
			candidate.setResumeLink(pdfFilePath);

			candidateService.uploadCandidateFiles(serverPath, fileName, FileType.Original_Resume.getDisplayName(),
					"new", candidateId, pdfFilePath);
		}

		// deleting file if it is marked for delete (When profileUrl is passed
		// as null)
		if (candidate.getPublicProfileUrl() == null || candidate.getPublicProfileUrl().isEmpty()) {
			String filePath = publicFolder + "/" + candidateToUpdate.getProfileUrl();
			File profilrPic = new File(filePath);
			if (profilrPic.exists())
				profilrPic.delete();
			candidateToUpdate.setProfileUrl("");
		}

		// adding profile pic o public folder
		if ((candidate.getImageContent() != null && !candidate.getImageContent().isEmpty())
				&& (candidate.getImageName() != null && !candidate.getImageName().isEmpty())) {
			byte[] imageBytes = Base64.decode(candidate.getImageContent().getBytes());
			if (imageBytes != null && imageBytes.length > 0) {

				File publicProfilePath = new File(
						publicFolder + "/" + TenantContextHolder.getTenant() + "/candidate/" + candidateId);

				if (!publicProfilePath.exists())
					org.apache.commons.io.FileUtils.forceMkdir(publicProfilePath);

				File logoFile = new File(publicProfilePath + "/" + candidate.getImageName());

				org.apache.commons.io.FileUtils.writeByteArrayToFile(logoFile, imageBytes);
				String dpPath = logoFile.getAbsolutePath().replace(publicFolder, "");
				// String logoPath = logoFile.getAbsolutePath().substring(index,
				// logoFile.getAbsolutePath().length());
				if (dpPath != null && !dpPath.isEmpty()) {
					candidateToUpdate.setProfileUrl(dpPath);
					candidateService.save(candidateToUpdate);
				}
			}
		}

		candidate = candidateService.setDefaultValues(candidate);
		candidate.setOwner(candidateToUpdate.getOwner());

		// deleting cover letter if file if it is marked for delete
		if (candidate.getCoverLetterPath() == null || candidate.getCoverLetterPath().isEmpty()) {
			if (candidateToUpdate.getCoverLetterPath() != null && !candidateToUpdate.getCoverLetterPath().isEmpty()) {
				File coverLetter = new File(candidateToUpdate.getCoverLetterPath());
				if (coverLetter.exists())
					coverLetter.delete();
				candidateToUpdate.setCoverLetterPath("");
			}
		}
		// updating cover letter if any new file content is passed
		candidateToUpdate.setCoverFileContent(candidate.getCoverFileContent());
		candidateToUpdate.setCoverFileName(candidate.getCoverFileName());
		candidateFileService.uploadCandidateCoverLetter(candidateToUpdate);

		Candidate updatedCandidate = candidateService.updateCandidate(candidate, candidateId);

		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, updatedCandidate);
		return candidateResponse;
	}

	@SuppressWarnings("unused")
	private void removeResumeCandidateFile(Candidate candidate, String candidateId) throws RecruizException {
		List<CandidateFile> candidateFiles = candidateFileService
				.getCandidateFileByTypeAndId(FileType.Original_Resume.getDisplayName(), candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile candidateFile : candidateFiles) {
				boolean deleted = fileService.deleteFile(candidateFile.getFilePath());
				candidateFileService.delete(candidateFile);
			}
		}

		candidateFiles = candidateFileService
				.getCandidateFileByTypeAndId(FileType.Original_Converted_Resume.getDisplayName(), candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile candidateFile : candidateFiles) {
				boolean deleted = fileService.deleteFile(candidateFile.getFilePath());
				candidateFileService.delete(candidateFile);
			}
		}

	}

	/**
	 * This method is used to delete candidate using candidateId.
	 *
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}", method = RequestMethod.DELETE)
	public RestResponse deleteCandidate(@PathVariable("candidateId") String candidateId,
			@RequestParam(value = "removeInvoiceFlag", required = false) boolean removeInvoiceFlag)
					throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		try {
			candidateService.deleteCandidate(Long.parseLong(candidateId), removeInvoiceFlag);

			List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(candidateId);

			for (CandidateFile candidateFile : candidateFiles) {

				candidateFileService.delete(candidateFile.getId());
				try{
					uploadFileService.deleteCandidateFolderFromAWS(candidateFile.getFilePath());
				}catch(Exception e){ 
					e.printStackTrace();
				}
				try{
					fileService.deleteDirectory(candidateFile.getFilePath().split(candidateId)[0]+"/"+candidateId);
				}catch(Exception e){
					e.printStackTrace();
				}
			}

		} catch (NumberFormatException e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		} catch (RecruizException ex) {
			return new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
		}

		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS,
				RestResponseConstant.CANDIDATE_DELETED);
		return candidatetResponse;
	}

	/**
	 * This method is used to delete multiple candidate using list of
	 * candidateId.
	 *
	 * @author Akshay
	 * @param candidateIdList
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/bulkdelete", method = RequestMethod.DELETE)
	public RestResponse deleteMultipleCandidate(@RequestParam("candidateIdList") List<String> candidateIdList,
			@RequestParam(value = "removeInvoiceFlag", required = false) boolean removeInvoiceFlag)
					throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteCandidate.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!candidateIdList.isEmpty()) {
			try {
				candidateService.deleteBulkCandidates(candidateIdList, removeInvoiceFlag);
			} catch (NumberFormatException e) {
				return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
			} catch (RecruizException ex) {
				return new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
			}
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS,
				RestResponseConstant.CANDIDATE_DELETED);
		return candidatetResponse;
	}

	/**
	 * This method is used to check candidate exists.
	 *
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/check", method = RequestMethod.GET)
	public RestResponse isCandidateEmailExist(@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.IsCandidateExists.name());*/

		if (email == null || email.isEmpty()) {
			return new RestResponse(false, ErrorHandler.Email_NOT_PRESENT, ErrorHandler.NO_EMAIL);
		}

		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && !id.isEmpty()) {
			Candidate candidate = candidateService.getCandidateById(Long.parseLong(id));
			if (candidate.getEmail().equalsIgnoreCase(email)) {
				isExist = false;
			} else if (email != null && !email.trim().isEmpty()) {
				isExist = candidateService.isCandidateExists(email);
			}
		} else if (email != null && !email.trim().isEmpty()) {
			Candidate existingCandidate = candidateService.getCandidateByEmail(email);
			if (null == existingCandidate) {
				isExist = false;
			} else if (checkUserPermission.isUserTypeVendor()) {
				isExist = checkExistingCandidateAge(existingCandidate);
			} else {
				isExist = true;
			}
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Candidate exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Candidate does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	private boolean checkExistingCandidateAge(Candidate existingCandidate) {
		boolean isExist;
		Long allowedModificationLimit = organizationService.getCurrentOrganization().getCandidateModificationDays();
		Long difference = 0L;
		if (null != allowedModificationLimit) {
			long differenceInMilliSec = 0;
			if (existingCandidate.getSourcedOnDate() != null) {
				differenceInMilliSec = new Date().getTime() - existingCandidate.getSourcedOnDate().getTime();
				difference = differenceInMilliSec / (24 * 60 * 60 * 1000);
			}
		} else {
			allowedModificationLimit = -1L;
		}
		if (allowedModificationLimit == -1L) {
			isExist = true;
		} else if (allowedModificationLimit == 0L) {
			isExist = false;
		} else if (difference < allowedModificationLimit) {
			isExist = false;
		} else {
			isExist = false;
		}
		return isExist;
	}

	/**
	 * This method is used to get candidate object by candidateId.
	 *
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}", method = RequestMethod.GET)
	public RestResponse getCandidate(@PathVariable("candidateId") String candidateId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateDetails.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));

		if (candidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS,
					ErrorHandler.CANDIDATE_NOT_FOUND);

		// for UI to show the display name, adding in response
		candidateService.getEmploymentTypeDisplayName(candidate);
		candidateService.attachCurrentPosition(candidate);

		// Need to load lazy
		candidate.getKeySkills().size();
		candidate.getEducationDetails().size();
		candidate.getCustomField().size();

		Set<CandidateFile> salarySlipFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> exitLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> appointmentLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> otherDocsFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> offerInHandFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> newAppointmentLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> originalResume = new HashSet<CandidateFile>();
		Set<CandidateFile> originalResumeConverted = new HashSet<CandidateFile>();
		Set<CandidateFile> maskedResume = new HashSet<CandidateFile>();
		Set<CandidateFile> maskedResumeConverted = new HashSet<CandidateFile>();
		Set<CandidateFile> coverLetter = new HashSet<CandidateFile>();
		Set<CandidateFile> extraDoc = new HashSet<CandidateFile>();

		List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile filesFromDB : candidateFiles) {


				switch (filesFromDB.getFileType()) {
				case GlobalConstants.SALARY_SLIP:
					salarySlipFiles.add(filesFromDB);
					break;
				case GlobalConstants.EXIT_LETTER:
					exitLetterFiles.add(filesFromDB);
					break;
				case GlobalConstants.APPOINTMENT_LETTER:
					appointmentLetterFiles.add(filesFromDB);
					break;
				case GlobalConstants.Offer_In_Hand:
					offerInHandFiles.add(filesFromDB);
					break;
				case GlobalConstants.Other_Docs:
					otherDocsFiles.add(filesFromDB);
					break;
				case GlobalConstants.NEW_APPOINTMENT_LETTER:
					newAppointmentLetterFiles.add(filesFromDB);
					break;
				case GlobalConstants.Original_Converted_Resume:
					originalResumeConverted.add(filesFromDB);
					break;
				case GlobalConstants.Original_Resume:
					originalResume.add(filesFromDB);
					break;
				case GlobalConstants.Masked_Resume_Converted:
					maskedResumeConverted.add(filesFromDB);
					break;
				case GlobalConstants.Masked_Resume_Original:
					maskedResume.add(filesFromDB);
					break;
				case GlobalConstants.Cover_Letter:
					coverLetter.add(filesFromDB);
					break;
				default:
					break;
				}

				if(filesFromDB.getCompanyType()!=null){

					if(filesFromDB.getCompanyType().equalsIgnoreCase(GlobalConstants.Extra_Doc)){
						extraDoc.add(filesFromDB);
					}
				}

			}
		}

		Map<String, Object> fileMap = new LinkedHashMap<String, Object>();
		fileMap.put(RestResponseConstant.SALARY_SLIP, salarySlipFiles);
		fileMap.put(RestResponseConstant.EXIT_LETTER, exitLetterFiles);
		fileMap.put(RestResponseConstant.APPOINTMENT_LETTER, appointmentLetterFiles);

		Map<String, Object> newFileMap = new LinkedHashMap<String, Object>();
		newFileMap.put(RestResponseConstant.SALARY_SLIP, otherDocsFiles);
		newFileMap.put(RestResponseConstant.EXIT_LETTER, offerInHandFiles);
		newFileMap.put(RestResponseConstant.APPOINTMENT_LETTER, newAppointmentLetterFiles);

		Map<String, Integer> fileCountMap = new LinkedHashMap<String, Integer>();
		fileCountMap.put(RestResponseConstant.SALARY_SLIP, 3);
		fileCountMap.put(RestResponseConstant.EXIT_LETTER, 1);
		fileCountMap.put(RestResponseConstant.APPOINTMENT_LETTER, 1);

		Map<String, Object> candidateMap = new HashMap<String, Object>();
		candidateMap.put(RestResponseConstant.FILE_COUNT, fileCountMap);
		candidateMap.put(RestResponseConstant.CANDIDATE_DETAILS, candidate);
		candidateMap.put(RestResponseConstant.OLD_COMP_FILES, fileMap);
		candidateMap.put(RestResponseConstant.NEW_COMP_FILES, newFileMap);
		candidateMap.put(RestResponseConstant.ORIGINAL_RESUME, originalResume);
		candidateMap.put(RestResponseConstant.MASKED_RESUME, maskedResume);
		candidateMap.put(RestResponseConstant.ORIGINAL_RESUME_CONVERTED, originalResumeConverted);
		candidateMap.put(RestResponseConstant.MASKED_RESUME_CONVERTED, maskedResumeConverted);
		candidateMap.put(RestResponseConstant.COVER_LETTER, coverLetter);
		candidateMap.put(RestResponseConstant.Extra_Doc, extraDoc);

		
		Set<String> extraDocNameList = new HashSet<>();
		for (CandidateFile candidateFile : extraDoc) {
			extraDocNameList.add(candidateFile.getFileType());
		}
		
		candidateMap.put(RestResponseConstant.Extra_Doc_List,extraDocNameList);
		RestResponse offerLetterList = organizationService.getListOfOfferLetter(Long.parseLong(candidateId));

		/*		 SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
		 String lastWorkingDay= formatter.format(candidate.getLastWorkingDay());

	    	Calendar c = Calendar.getInstance();
	    	try {
	    		c.setTime(candidate.getLastWorkingDay());
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	c.add(Calendar.DATE, -1);  
	    	Date editLastWorkingDay = c.getTime();
	    candidate.setLastWorkingDay(editLastWorkingDay);*/
		candidateMap.put(RestResponseConstant.CANDIDATE_DETAILS, candidate);	
		candidateMap.put(RestResponseConstant.OFFER_LETTERS, offerLetterList.getData());
		
		Organization org = organizationService.getOrgInfo();
		Set<String> listFiles = new HashSet<>();
		if (org != null && org.getDocumentsCheck().equalsIgnoreCase("Yes")){

			String docList = org.getMandatoryDocs();
			if(docList==null){
				listFiles.add(FileType.PAN_CARD.getDisplayName());listFiles.add(FileType.AADHAR_CARD.getDisplayName());listFiles.add(FileType.UPDATED_RESUME.getDisplayName());listFiles.add(FileType.TENTH_EDU_DOC.getDisplayName());
				listFiles.add(FileType.TWELETH_EDU_DOC.getDisplayName());listFiles.add(FileType.DEGREE_DOC.getDisplayName());listFiles.add(FileType.APPOINTMENT_LETTER.getDisplayName());listFiles.add(FileType.SALARY_SLIPS.getDisplayName());
				listFiles.add(FileType.RELIEVING_LETTER.getDisplayName());listFiles.add(FileType.PASSPORT_PHOTOGRAPH.getDisplayName());listFiles.add(FileType.STATEMENT_CHEQUE.getDisplayName());listFiles.add(FileType.ADDRESS_PROOF.getDisplayName());
			}else{
				if(docList.contains(",")){
					
					String[] docs = docList.split(",");
					listFiles.addAll(new HashSet<>(Arrays.asList(docs)));
				}else{
					listFiles.add(docList);
				}
			}
			
		}
		
		candidateMap.put("mandatoryDocs", listFiles);
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateMap);
		return candidatetResponse;
	}



	@RequestMapping(value = "/api/v1/candidate/email/attachment/{candidateId}", method = RequestMethod.GET)
	public RestResponse getCandidateEmailAttachments(@PathVariable("candidateId") String candidateId) throws RecruizException {

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));

		if (candidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS,
					ErrorHandler.CANDIDATE_NOT_FOUND);

		Set<CandidateFile> extraDoc = new HashSet<CandidateFile>();

		List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile filesFromDB : candidateFiles) {
				if(filesFromDB.getCompanyType()!=null){
					if(filesFromDB.getCompanyType().equalsIgnoreCase(GlobalConstants.Extra_Doc)){
						extraDoc.add(filesFromDB);
					}
				}
			}
		}

		Map<String, Object> candidateMap = new HashMap<String, Object>();
		candidateMap.put(RestResponseConstant.Extra_Doc, extraDoc);
		RestResponse offerLetterList = organizationService.getListOfOfferLetter(Long.parseLong(candidateId));
		candidateMap.put(RestResponseConstant.CANDIDATE_DETAILS, candidate);	
		candidateMap.put(RestResponseConstant.OFFER_LETTERS, offerLetterList.getData());

		//Assign owner if null
		if(candidate.getOwner() == null || candidate.getOwner().equalsIgnoreCase("")) {
			candidate.setOwner(userService.getLoggedInUserEmail());
		}

		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateMap);
		return candidatetResponse;
	}





	@RequestMapping(value = "/api/v1/candidate/getAllFiles/{candidateId}", method = RequestMethod.GET)
	public RestResponse getAllFilesOfCandidate(@PathVariable("candidateId") String candidateId) throws RecruizException {


		Set<CandidateFile> salarySlipFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> exitLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> appointmentLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> otherDocsFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> offerInHandFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> newAppointmentLetterFiles = new HashSet<CandidateFile>();
		Set<CandidateFile> originalResume = new HashSet<CandidateFile>();
		Set<CandidateFile> originalResumeConverted = new HashSet<CandidateFile>();
		Set<CandidateFile> maskedResume = new HashSet<CandidateFile>();
		Set<CandidateFile> maskedResumeConverted = new HashSet<CandidateFile>();
		Set<CandidateFile> coverLetter = new HashSet<CandidateFile>();
		Set<CandidateFile> extraDoc = new HashSet<CandidateFile>();

		List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile filesFromDB : candidateFiles) {

				if(filesFromDB.getCompanyType()==null || !filesFromDB.getCompanyType().equalsIgnoreCase(GlobalConstants.Extra_Doc)){
					switch (filesFromDB.getFileType()) {
					case GlobalConstants.SALARY_SLIP:
						salarySlipFiles.add(filesFromDB);
						break;
					case GlobalConstants.EXIT_LETTER:
						exitLetterFiles.add(filesFromDB);
						break;
					case GlobalConstants.APPOINTMENT_LETTER:
						appointmentLetterFiles.add(filesFromDB);
						break;
					case GlobalConstants.Offer_In_Hand:
						offerInHandFiles.add(filesFromDB);
						break;
					case GlobalConstants.Other_Docs:
						otherDocsFiles.add(filesFromDB);
						break;
					case GlobalConstants.NEW_APPOINTMENT_LETTER:
						newAppointmentLetterFiles.add(filesFromDB);
						break;
					case GlobalConstants.Original_Converted_Resume:
						originalResumeConverted.add(filesFromDB);
						break;
					case GlobalConstants.Original_Resume:
						originalResume.add(filesFromDB);
						break;
					case GlobalConstants.Masked_Resume_Converted:
						maskedResumeConverted.add(filesFromDB);
						break;
					case GlobalConstants.Masked_Resume_Original:
						maskedResume.add(filesFromDB);
						break;
					case GlobalConstants.Cover_Letter:
						coverLetter.add(filesFromDB);
						break;
					default:
						break;
					}

				}else{
					extraDoc.add(filesFromDB);
				}
			}
		}

		Map<String, Object> fileMap = new LinkedHashMap<String, Object>();
		fileMap.put(RestResponseConstant.SALARY_SLIP, salarySlipFiles);
		fileMap.put(RestResponseConstant.EXIT_LETTER, exitLetterFiles);
		fileMap.put(RestResponseConstant.APPOINTMENT_LETTER, appointmentLetterFiles);

		Map<String, Object> newFileMap = new LinkedHashMap<String, Object>();
		newFileMap.put(RestResponseConstant.SALARY_SLIP, otherDocsFiles);
		newFileMap.put(RestResponseConstant.EXIT_LETTER, offerInHandFiles);
		newFileMap.put(RestResponseConstant.APPOINTMENT_LETTER, newAppointmentLetterFiles);

		Map<String, Integer> fileCountMap = new LinkedHashMap<String, Integer>();
		fileCountMap.put(RestResponseConstant.SALARY_SLIP, 3);
		fileCountMap.put(RestResponseConstant.EXIT_LETTER, 1);
		fileCountMap.put(RestResponseConstant.APPOINTMENT_LETTER, 1);

		Map<String, Object> candidateMap = new HashMap<String, Object>();
		candidateMap.put(RestResponseConstant.FILE_COUNT, fileCountMap);
		candidateMap.put(RestResponseConstant.OLD_COMP_FILES, fileMap);
		candidateMap.put(RestResponseConstant.NEW_COMP_FILES, newFileMap);
		candidateMap.put(RestResponseConstant.ORIGINAL_RESUME, originalResume);
		candidateMap.put(RestResponseConstant.MASKED_RESUME, maskedResume);
		candidateMap.put(RestResponseConstant.ORIGINAL_RESUME_CONVERTED, originalResumeConverted);
		candidateMap.put(RestResponseConstant.MASKED_RESUME_CONVERTED, maskedResumeConverted);
		candidateMap.put(RestResponseConstant.COVER_LETTER, coverLetter);
		candidateMap.put(RestResponseConstant.Extra_Doc, extraDoc);

		RestResponse offerLetterList = organizationService.getListOfOfferLetter(Long.parseLong(candidateId));

		/*		 SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
		 String lastWorkingDay= formatter.format(candidate.getLastWorkingDay());

	    	Calendar c = Calendar.getInstance();
	    	try {
	    		c.setTime(candidate.getLastWorkingDay());
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	c.add(Calendar.DATE, -1);  
	    	Date editLastWorkingDay = c.getTime();
	    candidate.setLastWorkingDay(editLastWorkingDay);*/	
		candidateMap.put(RestResponseConstant.OFFER_LETTERS, offerLetterList.getData());
		//	candidateMap.put("lastWorkingDay", lastWorkingDay);
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateMap);
		return candidatetResponse;
	}



	/**
	 * to upload files like salary slip, Exp letter, etc..
	 *
	 * @param file
	 * @param fileName
	 * @param fileType
	 * @param companyType
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/upload/files", method = RequestMethod.POST)
	public RestResponse uploadFiles(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName,
			@RequestParam("fileType") String fileType, @RequestParam("companyType") String companyType,
			@RequestParam("candidateId") String candidateId) throws RecruizException, IOException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UploadCandidateFile.name());*/

		if (!checkUserPermission.isUserTypeApp() && !checkUserPermission.isUserTypeVendor())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (fileName == null || fileName.isEmpty() || fileType == null || fileType.isEmpty() || companyType == null
				|| companyType.isEmpty() || candidateId == null || candidateId.isEmpty())
			return null;

		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		File fileToUpload = fileService.multipartToFile(file);
		String serverPath = uploadFileService.uploadFileToLocalServer(fileToUpload, fileName, fileType, candidateId);

		String convertedFilePath = "";

		if(fileStorageMode!=null && fileStorageMode.equalsIgnoreCase("aws")){
			String fileAwsPath = uploadFileService.uploadFileToAWSServer(fileToUpload, fileName, fileType, candidateId);
			candidateService.uploadCandidateFiles(fileAwsPath, fileName, fileType, companyType, candidateId,
					convertedFilePath);
		}

		if (fileType.toLowerCase().equalsIgnoreCase(FileType.Original_Resume.getDisplayName())
				|| fileType.toLowerCase().equalsIgnoreCase(FileType.Masked_Resume_Original.getDisplayName())) {
			convertedFilePath = fileService.convert(serverPath);

			if(fileStorageMode!=null && fileStorageMode.equalsIgnoreCase("aws")){
				File resumePdf = new File(convertedFilePath);
				String pdfAwsPath = uploadFileService.uploadFileToAWSServer(resumePdf, StringUtils.cleanFileName(resumePdf.getName()),FileType.Original_Resume.getDisplayName(), candidateId + "");
				candidateService.uploadCandidateFiles(pdfAwsPath+".docx", resumePdf.getName(), FileType.Original_Resume.getDisplayName(), "new",
						candidateId + "", pdfAwsPath+".pdf");
			}

		}
		candidateService.uploadCandidateFiles(serverPath, fileName, fileType, companyType, candidateId,
				convertedFilePath);

		Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));
		if (candidate != null) {
			candidate.setS3Enabled(false);
			candidateService.save(candidate);
		}

		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, GlobalConstants.FILE_UPLOADED, null);
		return candidateAddResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/file", method = RequestMethod.DELETE)
	public RestResponse deleteFile(@RequestParam("fileName") String fileName,
			@RequestParam("candidateId") String candidateId) throws RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteCandidateFile.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (fileName == null || fileName.isEmpty() || candidateId == null || candidateId.isEmpty())
			return null;

		// byte[] fileNameBytes = Base64.decode(fileName.getBytes());
		// String file = new String(fileNameBytes);

		candidateFileService.deleteCandidateFile(fileName, candidateId);

		RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, GlobalConstants.FILE_DELETED, null);
		return candidateAddResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/number/check", method = RequestMethod.GET)
	public RestResponse isCandidateNumberExists(@RequestParam(value = "number", required = false) String number,
			@RequestParam(value = "id", required = false) String id) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.IsCandidateNumberExists.name());*/

		boolean isExist = false;
		Map<String, String> response = new HashMap<String, String>();
		if (id != null && !id.trim().isEmpty()) {
			Candidate candidate = candidateService.getCandidateById(Long.parseLong(id));
			if (number == null || number.trim().isEmpty() || number.equalsIgnoreCase(candidate.getMobile()))
				isExist = false;
			else if (number != null && !number.isEmpty()) {
				isExist = candidateService.numberExists(number);
			}

		} else if (number != null && !number.trim().isEmpty()) {
			isExist = candidateService.numberExists(number);
		}

		if (isExist) {
			response.put("exists", true + "");
			response.put("result", "Candidate exists");
		} else {
			response.put("exists", false + "");
			response.put("result", "Candidate does not exists");
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, response);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/bulk", method = RequestMethod.POST)
	public RestResponse bulkUpload(MultipartHttpServletRequest request) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CandidateBulkUpload.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkAppSettingsService.isCandidateCountExceeded())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.MAX_CANDIDATE_LIMIT_REACHED,
					ErrorHandler.MAX_CANDIDATE_REACHED);

		Map<String, Object> response = new HashMap<>();
		String batchId = request.getParameter("batchId");
		String source = request.getParameter("source");
		String folderId = request.getParameter("folderId");
		List<File> fileList = new LinkedList<File>();
		try {
			Iterator<String> itr = request.getFileNames();

			while (itr.hasNext()) {
				String uploadedFile = itr.next();
				MultipartFile file = request.getFile(uploadedFile);
				File resumeFile = fileService.multipartToFileForBulkUpload(file);
				fileList.add(resumeFile);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (fileList != null && !fileList.isEmpty()) {
			// few milli secs sleep is required to sync the job with db else too
			// many threads hitting db at same time causes issues.
			//Thread.sleep(500);
			resumeBulkUploadService.resumeBulkUploadAsync(TenantContextHolder.getTenant(),
					SecurityContextHolder.getContext().getAuthentication(), batchId, fileList, source, folderId);
			logger.info("RESUME FILE SEND FOR JOB !!!");
			return new RestResponse(RestResponse.SUCCESS, response, null);
		} else {
			RestResponse candidateAddResponse = new RestResponse(false, ErrorHandler.NO_FILES_UPLOADED,
					ErrorHandler.NO_RESUME);
			return candidateAddResponse;
		}
	}

	@RequestMapping(value = "/api/v1/candidate/bulk/quick", method = RequestMethod.POST)
	public RestResponseEntity<List<FileUploadResponseDTO>> quickAddBulkUpload(
			@RequestBody List<FileUploadRequestDTO> bulkUploadDTO) throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.QuickAddBulkCandidateUpload.name());*/

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkAppSettingsService.isCandidateCountExceeded())
			throw new RecruizException(ErrorHandler.MAX_CANDIDATE_LIMIT_REACHED, ErrorHandler.MAX_CANDIDATE_REACHED);

		List<FileUploadResponseDTO> response = new ArrayList<>();
		if (null != bulkUploadDTO && bulkUploadDTO.size() > 0) {
			response = quickBulkUpload(bulkUploadDTO);
		}
		RestResponseEntity<List<FileUploadResponseDTO>> candidateAddResponse = new RestResponseEntity<List<FileUploadResponseDTO>>(
				RestResponse.SUCCESS, response, null);
		return candidateAddResponse;
	}

	private List<FileUploadResponseDTO> quickBulkUpload(List<FileUploadRequestDTO> uploadDTO)
			throws IOException, RecruizCandidateExistException {
		List<FileUploadResponseDTO> result = new ArrayList<>();
		for (FileUploadRequestDTO dto : uploadDTO) {
			dto.setSourceDetails("Bulk upload by '" + userService.getLoggedInUserName() + "'");
			FileUploadResponseDTO response = new FileUploadResponseDTO();
			try {
				saveCandidateViaResumeFile(dto);
				response.setResult(true, "File : " + dto.getFileName() + " processed");
			} catch (RecruizException e) {
				response.setResult(false, e.getMessage());
			}
			result.add(response);
		}
		return result;
	}

	@RequestMapping(value = "/api/v1/candidate/plugin/add", method = RequestMethod.POST)
	public RestResponseEntity<FileUploadResponseDTO> pluginAddUpload(@RequestPart("file") MultipartFile file,
			@RequestParam(GlobalConstants.SOURCE) String source,
			@RequestParam(name = GlobalConstants.POSITION_CODE_PARAM, required = false) String positionCode,
			@RequestParam(name = GlobalConstants.CANDIDATE_EMAIL, required = false) String candidateEmail,
			@RequestParam(name = GlobalConstants.CANDIDATE_MOBILE, required = false) String candidateMobile,
			@RequestParam(name = GlobalConstants.CANDIDATE_NAME, required = false) String candidateName) throws Exception {

		// making entry to usage stat table
		/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CandidatePluginAdd.name());*/

		Map<String, String> ohterCandidateDetails = new HashMap<>();
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_EMAIL, candidateEmail);
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_MOBILE, candidateMobile);
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_NAME, candidateName);

		String responseMessage = "Add candidate via plugin failed";
		FileUploadResponseDTO response = new FileUploadResponseDTO();
		response.setResult(false, responseMessage);

		if (file == null || file.isEmpty()) {
			response.setResult(false, "Resume is missing");
			return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.FAILED, response,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}


		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		logger.error("candidate duplicate or not  ====="+ candidate + " candidate emailId = "+candidateEmail);
		if(candidate!=null)
			return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.FAILED, null ,ErrorHandler.CONNECT_DUPLICATE_CANDIDATE);


		response = doPluginUpload(file, source, positionCode, candidateEmail, candidateMobile, candidateName,
				responseMessage, response, null, null);

		RestResponseEntity<FileUploadResponseDTO> candidateAddResponse = new RestResponseEntity<FileUploadResponseDTO>(
				response.isSuccess(), response, null);
		return candidateAddResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/plugin/upload", method = RequestMethod.POST)
	public RestResponseEntity<FileUploadResponseDTO> pluginCandidateUpload(@RequestBody PluginUploadDTO pluginInfo) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CandidatePluginUpload.name());

		MultipartFile file = null;


		String candidateEmail = pluginInfo.getCandidateEmail();
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		logger.error("candidate duplicate or not  ====="+ candidate + " candidate emailId = "+candidateEmail);
		if(candidate!=null)
			return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.FAILED, null ,ErrorHandler.CONNECT_DUPLICATE_CANDIDATE);

		Map<String, String> ohterCandidateDetails = new HashMap<>();
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_EMAIL, pluginInfo.getCandidateEmail());
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_MOBILE, pluginInfo.getCandidateMobile());
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_NAME, pluginInfo.getCandidateName());

		String responseMessage = "Add candidate via plugin failed";
		FileUploadResponseDTO response = new FileUploadResponseDTO();
		response.setResult(false, responseMessage);

		File diskFile = null;
		if (file == null || file.isEmpty()) {
			try {
				if (null == pluginInfo.getFileHtmlContent() || pluginInfo.getFileHtmlContent().isEmpty()) {
					response.setResult(false, "Resume is missing");
					return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.FAILED, response,
							ErrorHandler.REQUEST_PARAMETER_MISSING);
				} else {
					diskFile = new File(FileUtils.getTempFilePath() + File.separator + System.currentTimeMillis()
					+ pluginInfo.getCandidateName() + ".html");
					org.apache.commons.io.FileUtils.writeStringToFile(diskFile, pluginInfo.getFileHtmlContent());
				}
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		}

		response = doPluginUpload(file, pluginInfo.getSource(), pluginInfo.getPositionCode(),
				pluginInfo.getCandidateEmail(), pluginInfo.getCandidateMobile(), pluginInfo.getCandidateName(),
				responseMessage, response, diskFile, pluginInfo);

		RestResponseEntity<FileUploadResponseDTO> candidateAddResponse = new RestResponseEntity<FileUploadResponseDTO>(
				response.isSuccess(), response, null);
		return candidateAddResponse;
	}

	private FileUploadResponseDTO doPluginUpload(MultipartFile file, String source, String positionCode,
			String candidateEmail, String candidateMobile, String candidateName, String responseMessage,
			FileUploadResponseDTO response, File diskFile, PluginUploadDTO pluginInfoDto) throws Exception {
		boolean iscandidateUpdated = false;

		Candidate checkcandidate = candidateService.getCandidateByEmail(candidateEmail);

		if(checkcandidate!=null && positionCode!=null && !positionCode.equalsIgnoreCase("null")){

			if (positionCode != null && !positionCode.trim().isEmpty() && !"null".equals(positionCode.trim())
					&& checkcandidate != null) {
				Position position = positionService.getPositionByCode(positionCode);
				if (position == null) {
					response.setResult(false,
							"Candidate add via plugin failed as position not found with code " + positionCode);
				} else {
					Round round = roundService.getRoundByBoardAndType(position.getBoard(),
							RoundType.Source.getDisplayName());
					roundCandidateService.sourceCandidateToBoard(position, round, checkcandidate);
					if (iscandidateUpdated)
						responseMessage = "Candidate details updated and added to position successfully via plugin";
					else
						responseMessage = "Candidate added to database and to position successfully via plugin";
					response.setResult(true, responseMessage);
				}

				return response;
			}
		}

		logger.error("Step first ===================================================");
		try {
			File resumeFile = null;
			if (file == null) {
				resumeFile = diskFile;
			} else {
				resumeFile = fileService.multipartToFile(file);
			}
			logger.error("Step second ===================================================");
			if (resumeFile.exists()) {
				Candidate candidate = null;

				// if user type is vendor then source using vendor
				if (userService.isLoggedInUserVendor()) {
					candidate = resumeParserService.parseResume(resumeFile);
					logger.error("Step third ===================================================");
					if(candidate==null)
						candidate = new Candidate();

					if (candidate.getFullName() == null || candidate.getFullName().isEmpty()) {
						candidate.setFullName(candidateName);
					}

					if (candidate.getMobile() == null || candidate.getMobile().isEmpty()) {
						candidate.setMobile(candidateMobile);
					}
					logger.error("Step fourth ===================================================");
					// if candidate exists then it should not be processed so
					// putting this if,else if condition
					if (candidate.getEmail() == null || candidate.getEmail().isEmpty()) {
						if (null != candidateEmail && !candidateEmail.isEmpty()) {
							candidate.setEmail(candidateEmail);
						} else {
							responseMessage = "Looks like candidate email is missing";
						}
					} else if (candidateService.isCandidateExists(candidate.getEmail())) {
						// we have decided not to update candidate when it is
						// sourced by vendor
						responseMessage = candidate.getFullName() + "(" + candidate.getEmail() + ")"
								+ " already added.";
					} else {
						candidate = vendorService.sourceVendorCandidate(candidate, resumeFile,
								StringUtils.cleanFileName(file.getOriginalFilename()), positionCode);
						if (positionCode != null && !positionCode.isEmpty()) {
							responseMessage = "Candidate added to database and to position successfully via plugin";
						} else {
							responseMessage = "Candidate added to database successfully via plugin";
						}
					}
					response.setResult(true, responseMessage);
					logger.error("Step fifth ===================================================");
					// masking resume
					try {
						long cid = candidate.getCid();
						if (cid == 0) {
							cid = candidateService.getCandidateByEmail(candidate.getEmail()).getCid();
						}
						Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
						if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
							candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
						}
					} catch (Exception ex) {
						logger.warn("*******Failed to mask resume*********", ex);
					}
					logger.error("Step sixth ===================================================");
				} else {
					// if in else part means the plugin is for app user
					logger.error("Step seventh ===================================================");
					try {
						candidate = resumeParserService.parseResume(resumeFile);
						// returning if candidate exists in the database
						if (candidate!=null && candidateService.isCandidateExists(candidate.getEmail())) {
							logger.error("Step eighth ===================================================");
							CandidateExistsResponseDTO candidateExistsResponseDTO = getCandidateExistsResponseDTO(
									resumeFile, candidate);
							FileUploadResponseDTO pluginUploadResponse = new FileUploadResponseDTO();
							pluginUploadResponse.setCandidateExistsResponseDTO(candidateExistsResponseDTO);
							pluginUploadResponse.setMessage("Candidate Exists");
							pluginUploadResponse.setSuccess(true);
							pluginUploadResponse.setResult(false, "Candidate Exists");
							return pluginUploadResponse;
						}
						logger.error("Step nine ===================================================");
						if(candidate==null)
							candidate = new Candidate();



						Map<String, Object> candidateMap = pluginCandidateUpload(
								Files.readAllBytes(resumeFile.toPath()), resumeFile.getName(), source, pluginInfoDto);
						candidate = (Candidate) candidateMap.get("candidate");
						logger.error("Step tenth ===================================================");
						//	iscandidateUpdated = (boolean) candidateMap.get("isUpdated");
						responseMessage = "Candidate added successfully via plugin";
						if (iscandidateUpdated)
							responseMessage = "Candidate details updated successfully via plugin";

						response.setResult(true, responseMessage);

						// masking resume
						try {
							long cid = candidate.getCid();
							Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
							if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
								candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
							}
						} catch (Exception ex) {
							logger.warn("*******Failed to mask resume*********", ex);
						}

					} catch (RecruizCandidateExistException e) {
						candidate = candidateService.getCandidateByEmail(e.getCandidateEmail());
						response.setResult(false, e.getMessage());
					}

					if (positionCode != null && !positionCode.trim().isEmpty() && !"null".equals(positionCode.trim())
							&& candidate != null) {
						Position position = positionService.getPositionByCode(positionCode);
						if (position == null) {
							response.setResult(false,
									"Candidate add via plugin failed as position not found with code " + positionCode);
						} else {
							Round round = roundService.getRoundByBoardAndType(position.getBoard(),
									RoundType.Source.getDisplayName());
							roundCandidateService.sourceCandidateToBoard(position, round, candidate);
							if (iscandidateUpdated)
								responseMessage = "Candidate details updated and added to position successfully via plugin";
							else
								responseMessage = "Candidate added to database and to position successfully via plugin";
							response.setResult(true, responseMessage);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response.setResult(false, "Something went wrong, Please try again.");
		}

		return response;
	}

	private Map<String, Object> pluginCandidateUpload(byte[] fileBytes, String fileName, String source,
			PluginUploadDTO pluginInfoDto) throws IOException, RecruizException, RecruizCandidateExistException {
		FileUploadRequestDTO fileUploadRequestDTO = new FileUploadRequestDTO();
		fileUploadRequestDTO.setCreationDate(DateTime.now().toDate());
		fileUploadRequestDTO.setFileName(StringUtils.cleanFileName(fileName));
		fileUploadRequestDTO.setSource(source);
		fileUploadRequestDTO.setSourceDetails("Uploaded via plugin by '" + userService.getLoggedInUserName() + "'");
		fileUploadRequestDTO.setFilebytes(fileBytes);
		if (userService.getLoggedInUserObject().getUserType().equalsIgnoreCase(UserType.APP.getDisplayName()))
			fileUploadRequestDTO.setOverwrite(true);

		return saveCandidateViaResumeFile(fileUploadRequestDTO, pluginInfoDto);
	}

	private Map<String, Object> saveCandidateViaResumeFile(FileUploadRequestDTO fileUploadRequestDTO,
			PluginUploadDTO pluginInfoDto) throws RecruizException, RecruizCandidateExistException {

		Candidate candidate = null;
		Map<String, Object> response = new HashMap<>();

		try{
			response = saveCandidateViaResumeFileViaPlugin(fileUploadRequestDTO,pluginInfoDto);
			candidate = (Candidate) response.get("candidate");
			logger.error("candiate added or not check = "+candidate.toString());
		}catch(Exception e){
			candidate = new Candidate();
		}

		if (null == pluginInfoDto) {
			return response;
		}

		Candidate checkExistCandidate = candidateService.getCandidateByEmail(pluginInfoDto.getCandidateEmail());

		if(checkExistCandidate!=null)
			candidate.setCid(checkExistCandidate.getCid());

		if(pluginInfoDto.getKeySkills()!=null && !pluginInfoDto.getKeySkills().equalsIgnoreCase("") && !pluginInfoDto.getKeySkills().isEmpty()){

			List<String> skills = Arrays.asList(pluginInfoDto.getKeySkills().split("\\s*,\\s*"));
			Set<String> keySkills = new HashSet<>(skills);
			candidate.setKeySkills(keySkills);
		}

		// updating additional plugin info to candidate
		candidate.setSource(pluginInfoDto.getPluginName());
		candidate.setCurrentLocation(pluginInfoDto.getCurrentLoc());
		candidate.setPreferredLocation(pluginInfoDto.getPrefLoc());
		candidate.setIndustry(pluginInfoDto.getIndustry());
		candidate.setNoticePeriod(pluginInfoDto.getNoticePeriod());
		candidate.setCurrentCtc(pluginInfoDto.getCctc());
		candidate.setFullName(pluginInfoDto.getCandidateName());
		candidate.setMobile(pluginInfoDto.getCandidateMobile());
		candidate.setEmail(pluginInfoDto.getCandidateEmail());

		try{
			if (null != pluginInfoDto.getTotalExp() && !pluginInfoDto.getTotalExp().trim().isEmpty()) {
				String exp = pluginInfoDto.getTotalExp().split(" ")[0].replace("yr","").trim()+"."+pluginInfoDto.getTotalExp().split(" ")[1].replace("m","").trim();
				candidate.setTotalExp(NumberUtils.toDouble(exp));
			}
		}catch(Exception e){

		}
		candidate.setLanguages(pluginInfoDto.getLanguage());
		candidate.setAddress(pluginInfoDto.getAddress());
		candidate.setDob(DateUtil.parseDate(pluginInfoDto.getDob()));
		candidate.setGender(pluginInfoDto.getGender());
		candidate.setAlternateMobile(pluginInfoDto.getAlternateMobile());

		// candidate.setPreviousEmployment(pluginInfoDto.getPrevEmployment());
		candidate.setCurrentTitle(pluginInfoDto.getCurrentTitle());
		candidate.setSource(pluginInfoDto.getPluginName());

		// saving to db

		candidateService.save(candidate);

		// adding in map
		response.put("candidate", candidate);

		return response;

	}

	private Map<String, Object> saveCandidateViaResumeFile(FileUploadRequestDTO fileUploadRequestDTO)
			throws RecruizException, RecruizCandidateExistException {
		FileUploadResponseDTO result = new FileUploadResponseDTO();

		if (fileUploadRequestDTO.getFileName() != null && !fileUploadRequestDTO.getFileName().isEmpty()) {
			fileUploadRequestDTO.setFileName(StringUtils.cleanFileName(fileUploadRequestDTO.getFileName()));
		}

		result.setFileName(fileUploadRequestDTO.getFileName());
		File resumeFile = null;
		Candidate candidate = null;
		try {
			resumeFile = FileUtils.writeToFile(fileUploadRequestDTO.getFileName(), fileUploadRequestDTO.getFilebytes());

			if (resumeFile == null || resumeFile.length() == 0) {
				throw new RecruizException("Empty File");

			} else {
				logger.error("###########call  queueParseResume() method from CandidateController.java #############");

				try{
					candidate = candidateService.addResumeFileAsCandidate(resumeFile);
				}catch(Exception e){
					candidate = new Candidate();
				}

				candidate.setSourcedOnDate(fileUploadRequestDTO.getCreationDate());
				candidate.setSourceDetails(fileUploadRequestDTO.getSourceDetails());
				candidate.setOwner(userService.getLoggedInUserEmail());

				// fill N/A if no values or null
				candidate = candidateService.setDefaultValues(candidate);

				if (fileUploadRequestDTO.getSource() == null || fileUploadRequestDTO.getSource().isEmpty()) {
					candidate.setSource(GlobalConstants.SOURCED_BY_HR);
				} else {
					candidate.setSource(fileUploadRequestDTO.getSource());
				}

				candidate = candidateService.saveCandidateToDB(candidate, fileUploadRequestDTO);

				if (candidate.getCid() > 0) {
					uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");
					String originalResume = uploadFileService.uploadFileToLocalServer(resumeFile,
							StringUtils.cleanFileName(resumeFile.getName()), "resume", candidate.getCid() + "");
					String convertedResume = fileService.convert(originalResume);
					candidate.setResumeLink(convertedResume);
					candidateService.updateCandidateResume(candidate, convertedResume);

					// add to resume docs
					String resumePath = uploadFileService.uploadFileToLocalServer(resumeFile,
							StringUtils.cleanFileName(resumeFile.getName()), FileType.Original_Resume.getDisplayName(),
							candidate.getCid() + "");
					// convert file first then upload it.
					String convertedResumePath = fileService.convert(resumePath);
					candidateService.uploadCandidateFiles(resumePath, resumeFile.getName(),
							FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "",
							convertedResumePath);
				}

				Map<String, Object> candidateSaveMap = new HashMap<>();
				candidateSaveMap.put("candidate", candidate);
				candidateSaveMap.put("isUpdated", fileUploadRequestDTO.isCandidateUpdated());

				return candidateSaveMap;

			}
		} catch (RecruizCandidateExistException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RecruizException(ex.getMessage(), ex);
		} finally {
			if (resumeFile != null && resumeFile.exists()) {
				resumeFile.delete();
			}
		}

	}



	private Map<String, Object> saveCandidateViaResumeFileViaPlugin(FileUploadRequestDTO fileUploadRequestDTO,PluginUploadDTO pluginInfoDto)
			throws RecruizException, RecruizCandidateExistException {
		FileUploadResponseDTO result = new FileUploadResponseDTO();

		if (fileUploadRequestDTO.getFileName() != null && !fileUploadRequestDTO.getFileName().isEmpty()) {
			fileUploadRequestDTO.setFileName(StringUtils.cleanFileName(fileUploadRequestDTO.getFileName()));
		}

		result.setFileName(fileUploadRequestDTO.getFileName());
		File resumeFile = null;
		Candidate candidate = null;
		try {
			resumeFile = FileUtils.writeToFile(fileUploadRequestDTO.getFileName(), fileUploadRequestDTO.getFilebytes());

			if (resumeFile == null || resumeFile.length() == 0) {
				throw new RecruizException("Empty File");

			} else {
				logger.error("###########call  queueParseResume() method from CandidateController.java #############");

				try{
					candidate = candidateService.addResumeFileAsCandidate(resumeFile);
				}catch(Exception e){
					candidate = new Candidate();

					candidate.setFullName(pluginInfoDto.getCandidateName());
					candidate.setMobile(pluginInfoDto.getCandidateMobile());
					candidate.setEmail(pluginInfoDto.getCandidateEmail());

					candidate = candidateService.save(candidate);
				}

				candidate.setSourcedOnDate(fileUploadRequestDTO.getCreationDate());
				candidate.setSourceDetails(fileUploadRequestDTO.getSourceDetails());
				candidate.setOwner(userService.getLoggedInUserEmail());

				// fill N/A if no values or null
				candidate = candidateService.setDefaultValues(candidate);

				if (fileUploadRequestDTO.getSource() == null || fileUploadRequestDTO.getSource().isEmpty()) {
					candidate.setSource(GlobalConstants.SOURCED_BY_HR);
				} else {
					candidate.setSource(fileUploadRequestDTO.getSource());
				}

				candidate = candidateService.saveCandidateToDB(candidate, fileUploadRequestDTO);

				if (candidate.getCid() > 0) {
					uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");
					String originalResume = uploadFileService.uploadFileToLocalServer(resumeFile,
							StringUtils.cleanFileName(resumeFile.getName()), "resume", candidate.getCid() + "");
					String convertedResume = fileService.convert(originalResume);
					candidate.setResumeLink(convertedResume);
					candidateService.updateCandidateResume(candidate, convertedResume);

					// add to resume docs
					String resumePath = uploadFileService.uploadFileToLocalServer(resumeFile,
							StringUtils.cleanFileName(resumeFile.getName()), FileType.Original_Resume.getDisplayName(),
							candidate.getCid() + "");
					// convert file first then upload it.
					String convertedResumePath = fileService.convert(resumePath);
					candidateService.uploadCandidateFiles(resumePath, resumeFile.getName(),
							FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "",
							convertedResumePath);
				}

				Map<String, Object> candidateSaveMap = new HashMap<>();
				candidateSaveMap.put("candidate", candidate);
				candidateSaveMap.put("isUpdated", fileUploadRequestDTO.isCandidateUpdated());

				return candidateSaveMap;

			}
		} catch (RecruizCandidateExistException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RecruizException(ex.getMessage(), ex);
		} finally {
			if (resumeFile != null && resumeFile.exists()) {
				resumeFile.delete();
			}
		}

	}




	@RequestMapping(value = "/api/v1/candidate/quick/add", method = RequestMethod.POST)
	public RestResponseEntity<FileUploadResponseDTO> quickAddUpload(
			@RequestBody FileUploadRequestDTO fileUploadRequestDTO) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CandidateQuickAdd.name());

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkAppSettingsService.isCandidateCountExceeded())
			throw new RecruizException(ErrorHandler.MAX_CANDIDATE_LIMIT_REACHED, ErrorHandler.MAX_CANDIDATE_REACHED);

		FileUploadResponseDTO response = new FileUploadResponseDTO();
		response.setResult(false, "Add failed");
		if (fileUploadRequestDTO != null) {
			try {
				fileUploadRequestDTO.setSourceDetails("Quick add by '" + userService.getLoggedInUserName() + "'");
				saveCandidateViaResumeFile(fileUploadRequestDTO);
				response.setResult(true, "File : " + fileUploadRequestDTO.getFileName() + " processed");
			} catch (RecruizException e) {
				response.setResult(false, e.getMessage());
			}
		}
		RestResponseEntity<FileUploadResponseDTO> candidateAddResponse = new RestResponseEntity<FileUploadResponseDTO>(
				RestResponse.SUCCESS, response, null);
		return candidateAddResponse;
	}

	/**
	 * This method is used to get candidate object and position description for
	 * external user
	 *
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}/{positionCode}", method = RequestMethod.GET)
	public RestResponse getCandidateForExternalUser(@PathVariable("candidateId") String candidateId,
			@PathVariable("positionCode") String positionCode,
			@RequestParam(value = "mskd", required = false) String mskd) throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateForExternalUser.name());
		 */
		Map<String, Object> candidateInfoMapForExternalUser = new HashMap<>();
		try {
			candidateInfoMapForExternalUser = candidateService
					.getCandidateDetailsForExternalUser(Long.parseLong(candidateId), positionCode, mskd);
		} catch (NumberFormatException | IOException e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		if (candidateInfoMapForExternalUser == null)
			return new RestResponse(RestResponse.FAILED, "Candidate or position missing",
					ErrorHandler.INVALID_SERVER_REQUEST);

		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateInfoMapForExternalUser);
		return candidatetResponse;
	}

	@RequestMapping(value = "/api/v1/candidate/applied/position/{cid}", method = RequestMethod.GET)
	public RestResponse getCandidateCurrentAppliedPosition(@PathVariable("cid") String cid) throws Exception {

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// position Name,ClientName,Sourced on date, status, round name
		Candidate candidate = candidateService.findOne(Long.parseLong(cid));
		if (candidate == null)
			return new RestResponse(RestResponse.SUCCESS, ErrorHandler.CANDIDATE_NOT_EXISTS,
					ErrorHandler.CANDIDATE_NOT_FOUND);

		List<RoundCandidate> roundCandidates = roundCandidateService.getAllRoundCandidates(candidate);
		Set<CandidateCurrentPositionDTO> candidateTotalRecord = new HashSet<>();
		if (roundCandidates != null && !roundCandidates.isEmpty()) {
			for (RoundCandidate roundCandidate : roundCandidates) {
				Position position = positionService.getPositionByCode(roundCandidate.getPositionCode());
				if (position != null) {
					// as per discussion on 15 - Feb returning list of all the
					// positions instead of only active
					// positionService.OROPerationForPosition(position);
					// if
					// (position.getFinalStatus().equalsIgnoreCase(Status.Active.name()))
					// {
					CandidateCurrentPositionDTO currentRecord = new CandidateCurrentPositionDTO();
					currentRecord.setClientName(position.getClient().getClientName());
					currentRecord.setPositionName(position.getTitle());
					try {
						BoardCustomStatus customStatus = boardCustomStatusService.getBoardCustomStatusByKey(roundCandidate.getStatus()) ;
						if(null != customStatus) {
							currentRecord.setCurrentStatus(customStatus.getStatusName());
						}else {
							currentRecord.setCurrentStatus(BoardStatus.valueOf(roundCandidate.getStatus()).getDisplayName());
						}
					}catch(Exception ex) {
						// doing nothing if custom status is not present
						currentRecord.setCurrentStatus(roundCandidate.getStatus());
					}


					currentRecord.setRoundName(roundCandidate.getRound().getRoundName());
					currentRecord.setSourceOnDate(roundCandidate.getCreationDate());
					InterviewSchedule schedule = interviewScheduleService.getScheduleByPositionCodeRoundEmail(
							roundCandidate.getPositionCode(), roundCandidate.getRound().getId() + "",
							candidate.getEmail());
					if (schedule != null)
						currentRecord.setInterviewScheduled(true);

					currentRecord.setClientId(position.getClient().getId() + "");
					currentRecord.setPositionId(position.getId() + "");
					candidateTotalRecord.add(currentRecord);
					// }
				}
			}
		}
		if (candidateTotalRecord == null || candidateTotalRecord.isEmpty())
			return new RestResponse(RestResponse.SUCCESS, ErrorHandler.NO_RECORD_FOUND, ErrorHandler.NO_RECORD);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, candidateTotalRecord, null);
		return response;
	}

	/**
	 * To add a new note to candidate
	 *
	 * @param candidateId
	 * @param candidateNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}/notes", method = RequestMethod.POST)
	public RestResponse addCandidateNotes(@RequestParam(value ="positionCode", required = false) String positionCode,@PathVariable("candidateId") String candidateId,
			@RequestBody CandidateNotes candidateNotes) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AddCandidateNotes.name());

		Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));

		try {

			if (candidate == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			candidateNotes.setCandidateId(candidate);
			candidateNotesService.save(candidateNotes);
			if(positionCode!=null){
				Position position = positionService.getPositionByCode(positionCode);
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}
			}

		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}

		//Assign owner if null
		if(candidate.getOwner() == null || candidate.getOwner().equalsIgnoreCase("")) {
			candidate.setOwner(userService.getLoggedInUserEmail());
		}

		
		RoundCandidate existingCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, positionCode);
		
		if(existingCandidate!=null)
		if(userService.getUserByEmail(existingCandidate.getSourcedBy()).getUserRole().getRoleName().equalsIgnoreCase("vendor") 
				&& !userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())){
			String roundCandidateName = existingCandidate.getCandidate().getFullName();
			String roundCandidateSouredBy = userService.getUserByEmail(existingCandidate.getSourcedBy()).getName();
			String SouredByEmail = userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail();

			emailActivityService.sendMailToVendorForChangesInCandidateNotes(roundCandidateSouredBy,roundCandidateName,SouredByEmail);
		}
		
		
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateNotes);
		return candidatetResponse;
	}

	/**
	 * Update a existing candidate note
	 *
	 * @param candidateId
	 * @param notesId
	 * @param candidateNotes
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}/notes/{notesId}", method = RequestMethod.POST)
	public RestResponse updateCandidateNotes(@PathVariable("candidateId") String candidateId,
			@PathVariable("notesId") String notesId, @RequestBody CandidateNotes candidateNotes)
					throws RecruizException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateCandidateNotes.name());*/

		try {
			Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));
			CandidateNotes notes = candidateNotesService.findOne(Long.parseLong(notesId));
			if (candidate == null || notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_UPDATE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			notes.setNotes(candidateNotes.getNotes());
			candidateNotes = candidateNotesService.save(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateNotes);
		return candidatetResponse;
	}

	/**
	 * to delete existig candidate note
	 *
	 * @param notesId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/candidate/notes/{notesId}", method = RequestMethod.DELETE)
	public RestResponse deleteCandidateNote(@PathVariable("notesId") String notesId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteCandidateNotes.name());

		try {
			CandidateNotes notes = candidateNotesService.findOne(Long.parseLong(notesId));
			if (notes == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTES_DELETE_FAILED,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			if (!notes.getAddedBy().equals(userService.getLoggedInUserEmail())) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NOTE_NOT_ADDED_BY_YOU,
						ErrorHandler.INVALID_SERVER_REQUEST);
			}
			candidateNotesService.delete(notes);
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NOTES_DELETED);
		return candidatetResponse;
	}

	/**
	 * To get list of all candidate notes (Pageable)
	 *
	 * @param candidateId
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/candidate/{candidateId}/notes", method = RequestMethod.GET)
	public RestResponse getCandidateNotes(@PathVariable("candidateId") String candidateId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

		Page<CandidateNotes> candidateNotes = null;
		try {
			Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));
			candidateNotes = candidateNotesService.getCandidateNotes(candidate, pageableService
					.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidatetResponse = new RestResponse(RestResponse.SUCCESS, candidateNotes);
		return candidatetResponse;
	}

	/**
	 * get candidate assessment page wise
	 */
	@RequestMapping(value = "/api/v1/candidate/assesment", method = RequestMethod.GET)
	public RestResponse getCandidateAssesment(@RequestParam("candidateEmail") String candidateEmail,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateAssesment.name());*/

		Page<CandidateAssesment> candidateAssesments = null;
		try {
			candidateAssesments = candidateAssesmentService.getCandidateAssesment(candidateEmail, pageableService
					.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}
		RestResponse candidateAssesmentResponse = new RestResponse(RestResponse.SUCCESS, candidateAssesments);
		return candidateAssesmentResponse;
	}

	/**
	 * public api for accesing parser, count will be increased for the tenant it
	 * is used
	 *
	 * @param file
	 * @param tnnt
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/pub/candidate/resume/{tnnt:.+}", method = RequestMethod.POST)
	public RestResponse getCandidateByResumeParserPublic(@RequestPart("file") MultipartFile file,
			@PathVariable String tnnt) throws IllegalStateException, RecruizException, IOException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ParseAndGetCandidate.name());*/

		if (tenantResolverService.isTenantValid(tnnt)) {
			TenantContextHolder.setTenant(tnnt);
			return getParsedResume(file);
		}

		return new RestResponse(false, "Failed to get data from parser.", ErrorHandler.RESUME_PARSER_ERROR);
	}

	/**
	 * public api to get candidate information using email, tenat should be
	 * passed in request
	 *
	 * @param file
	 * @param tnnt
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/pub/candidate/detals/email/{tnnt:.+}", method = RequestMethod.GET)
	public RestResponse getCandidateByEmailPublic(@RequestParam("emailId") String emailId, @PathVariable String tnnt)
			throws IllegalStateException, RecruizException, IOException {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateDetails.name());*/

		if (tenantResolverService.isTenantValid(tnnt)) {
			TenantContextHolder.setTenant(tnnt);
			Candidate candidate = candidateService.getCandidateByEmail(emailId);
			if (candidate != null) {
				candidate.getKeySkills().size();
				candidate.getEducationDetails().size();
				candidate.getCustomField().size();
			}
			return new RestResponse(true, candidate);
		}

		return new RestResponse(false, "Failed to get candidate infromation.", ErrorHandler.CANDIDATE_NOT_FOUND);
	}

	/**
	 * to get candidate added in a position
	 *
	 * @param emailId
	 * @param tnnt
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/position", method = RequestMethod.GET)
	public RestResponse getPositionCandidate(@RequestParam("positionCode") String positionCode)
			throws IllegalStateException, RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionCandidate.name());

		List<Candidate> positionCandidates = roundCandidateService.getCandidateByPositionCode(positionCode);
		return new RestResponse(true, positionCandidates);
	}

	/**
	 * To mask candidate resume
	 *
	 * @param cid
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/resume/mask/{cid}", method = RequestMethod.PUT)
	public RestResponse maskResume(@PathVariable("cid") long cid)
			throws IllegalStateException, RecruizException, IOException {

		RestResponse response = null;

		try {
			Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
			if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
				candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
				// marking candidate S3 enabled as false so that in next
				// scheduler cycle files will be synchronized on s3 cloud
				Candidate candidate = candidateService.getCandidateById(cid);
				candidate.setS3Enabled(false);
				candidateService.save(candidate);
				response = new RestResponse(true, SuccessHandler.RESUME_MASKED);
			} else {
				response = new RestResponse(false, ErrorHandler.RESUME_MASKED_FAILED,
						ErrorHandler.RESUME_MASKING_ERROR);
			}
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.RESUME_MASKED_FAILED, ErrorHandler.RESUME_MASKING_ERROR);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/candidate/init/random/id", method = RequestMethod.PUT)
	public RestResponse initRandomId() throws IllegalStateException, RecruizException, IOException {

		RestResponse response = null;

		try {
			candidateService.updateRandomCandidateId();
			response = new RestResponse(true, "All candidate updated with random id");
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ex.getCause(), ex.getMessage());
		}

		return response;
	}

	/**
	 * To check if a candidate have masked resume or not
	 *
	 * @return
	 * @throws IllegalStateException
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/candidate/masked/check", method = RequestMethod.GET)
	public RestResponse checkCandidateHasMaskedResume(@RequestParam List<Long> cids)
			throws IllegalStateException, RecruizException, IOException {

		RestResponse response = null;

		try {
			Map<Object, Object> statusMap = new HashMap<>();
			List<String> failedCandidate = new ArrayList<>();
			for (Long cid : cids) {
				boolean hasMaskedResume = candidateFileService.hasMaskedResume(cid);
				if (hasMaskedResume) {
					statusMap.put(cid, "true");
				} else {
					statusMap.put(cid, "false");
					failedCandidate.add(candidateService.findOne(cid).getFullName());
				}
			}

			statusMap.put("failedCandidate", failedCandidate);

			response = new RestResponse(true, statusMap);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ex.getCause(), ErrorHandler.NO_MASKED_RESUME);
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/candidate/rating/{cid}", method = RequestMethod.POST)
	public RestResponse rateCandidate(@PathVariable("cid") long cid,
			@RequestBody Map<Long, Double> questionIdToRating) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.RateCandidate.name());

		RestResponse response = new RestResponse(true, "Candidate rating updated");

		Candidate candidate = candidateService.findOne(cid);

		if (candidate == null)
			return new RestResponse(false, "Candidate not found", ErrorHandler.CANDIDATE_NOT_EXISTS);

		for (Long questionId : questionIdToRating.keySet()) {
			CandidateRatingQuestion candidateRatingQuestion = candidateRatingService
					.getCandidateRatingQuestionById(questionId);

			if (candidateRatingQuestion != null) {
				CandidateRating candidateRating = candidateRatingService
						.findByCandidateAndCandidateRatingQuestion(candidate, candidateRatingQuestion);

				if (candidateRating == null)
					candidateRating = new CandidateRating();

				candidateRating.setCandidate(candidate);
				candidateRating.setCandidateRatingQuestion(candidateRatingQuestion);
				candidateRating.setRatingScore(questionIdToRating.get(questionId));
				candidateRatingService.save(candidateRating);
			}

		}

		return response;
	}

	@RequestMapping(value = "/api/v1/candidate/rating/{cid}", method = RequestMethod.GET)
	public RestResponse getCandidateRating(@PathVariable("cid") long cid) {
		List<CandidateRating> candidateRatings = candidateRatingService.findByCandidateId(cid);
		RestResponse response = new RestResponse(true,
				dataModelToDTOConversionService.convertCandidateRating(candidateRatings));
		return response;
	}

	@RequestMapping(value = "/api/v1/candidate/rating/question/list", method = RequestMethod.GET)
	public RestResponse candidateRatingQuestionList() {
		RestResponse response = new RestResponse(true, candidateRatingService.getAllCandidateRatingQuestions());
		return response;
	}

	// to delete bulk upload items
	@RequestMapping(value = "/api/v1/candidate/bulk/upload/stop/{batch}", method = RequestMethod.DELETE)
	public RestResponse stopAndDeleteBulkItems(@PathVariable String batch) {

		/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.StopBulkUpload.name());*/

		Long deletedItems = candidateResumeUploadItemService.deleteItemsByBatchIdAndStatus(batch,
				ResumeUploadFileStatus.PENDING.name());

		if (null != deletedItems && deletedItems > 0) {
			CandidateResumeBulkUploadBatch uploadItem = candidateResumeBulkUploadBatchService.findByBatchId(batch);
			uploadItem.setStatus(ResumeBulkBatchUploadStatus.STOPPED.toString());
			candidateResumeBulkUploadBatchService.save(uploadItem);
		}
		RestResponse response = new RestResponse(true, deletedItems);
		return response;
	}

	@RequestMapping(value = "/api/v1/plugin/upload", method = RequestMethod.POST)
	public RestResponseEntity<FileUploadResponseDTO> uploadPlugin(@ModelAttribute PluginFileUploadDTO pluginInfo
			) throws Exception {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.CandidatePluginUpload.name());*/

		Map<String, String> ohterCandidateDetails = new HashMap<>();
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_EMAIL, pluginInfo.getCandidateEmail());
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_MOBILE, pluginInfo.getCandidateMobile());
		ohterCandidateDetails.put(GlobalConstants.CANDIDATE_NAME, pluginInfo.getCandidateName());

		if (null == pluginInfo.getCandidateName() || pluginInfo.getCandidateName().trim().isEmpty()) {
			FileUploadResponseDTO response = new FileUploadResponseDTO();
			response.setResult(false, "Name is missing");
			return new RestResponseEntity<FileUploadResponseDTO>(false, response);
		} else if (null == pluginInfo.getCandidateEmail() || pluginInfo.getCandidateEmail().trim().isEmpty()) {
			FileUploadResponseDTO response = new FileUploadResponseDTO();
			response.setResult(false, "Email is missing");
			return new RestResponseEntity<FileUploadResponseDTO>(false, response);
		}

		String responseMessage = "Add candidate via plugin failed";
		FileUploadResponseDTO response = new FileUploadResponseDTO();
		response.setResult(false, responseMessage);

		File diskFile = null;
		MultipartFile file = pluginInfo.getFile();
		if (file == null || file.isEmpty()) {
			try {
				if (null == pluginInfo.getFileHtmlContent() || pluginInfo.getFileHtmlContent().isEmpty()) {
					response.setResult(false, "Resume is missing");
					return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.FAILED, response,
							ErrorHandler.REQUEST_PARAMETER_MISSING);
				} else {
					diskFile = new File(FileUtils.getTempFilePath() + File.separator + System.currentTimeMillis()
					+ pluginInfo.getCandidateName() + ".html");
					org.apache.commons.io.FileUtils.writeStringToFile(diskFile, pluginInfo.getFileHtmlContent());
				}
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		}

		String candidateEmail = pluginInfo.getCandidateEmail();
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		logger.error("candidate duplicate or not  ====="+ candidate + " candidate emailId = "+candidateEmail);
		if(candidate!=null && pluginInfo.getPositionCode().equalsIgnoreCase("null"))
			return new RestResponseEntity<FileUploadResponseDTO>(RestResponse.SUCCESS, null ,ErrorHandler.CONNECT_DUPLICATE_CANDIDATE);


		response = doPluginUpload(file, pluginInfo.getSource(), pluginInfo.getPositionCode(),
				pluginInfo.getCandidateEmail(), pluginInfo.getCandidateMobile(), pluginInfo.getCandidateName(),
				responseMessage, response, diskFile, pluginInfo);

		RestResponseEntity<FileUploadResponseDTO> candidateAddResponse = new RestResponseEntity<FileUploadResponseDTO>(
				response.isSuccess(), response, null);
		return candidateAddResponse;
	}
	
	
}
