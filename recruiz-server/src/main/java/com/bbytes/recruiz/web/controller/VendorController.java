package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.BoardDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.InviteUserDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateFileService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.VendorService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ValidateEmailDomain;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    @Autowired
    protected TokenAuthenticationProvider tokenAuthenticationProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private DataModelToDTOConversionService dataModelToDTOConversionService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CheckAppSettingsService checkAppSettingsService;

    @Autowired
    private CheckUserPermissionService checkUserPermissionService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private CandidateFileService candidateFileService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${email.inviteToJoin.subject}")
    private String inviteToJoinSubject;

    @Value("${email.inviteToSignup.subject}")
    private String inviteToSignupSubject;

    @Value("${email.reinviteToJoin.subject}")
    private String reInviteToJoinSubject;

    @Value("${email.reinviteToSignup.subject}")
    private String reInviteToSignupSubject;

    @Value("${candidate.folderPath.path}")
    private String candidateFolderPath;

    @RequestMapping(value = "/api/v1/vendor/new", method = RequestMethod.POST)
    public RestResponse addVendor(@RequestBody Vendor vendor)
	    throws RecruizException, UnknownHostException, IOException, ParseException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddVendor.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (checkAppSettingsService.isValidityExpired()) {
	    return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
	}
	// checking vendor feature
	if (!checkAppSettingsService.isVendorFeatureEnabled()) {
	    return new RestResponse(false, ErrorHandler.VENDOR_FEATURE_NOT_ALLOWED, ErrorHandler.FEATURE_NOT_ALLOWED);
	}
	if (checkAppSettingsService.isVendorLimitExceeded()) {
	    return new RestResponse(false, ErrorHandler.VENDOR_LIMIT_EXCEEDED, ErrorHandler.FEATURE_NOT_ALLOWED);
	}

	if (!(EmailValidator.getInstance().isValid(vendor.getEmail()))) {
	    throw new RecruizWarnException("Email Id '" + vendor.getEmail() + "' not valid", "email_invalid");
	}

	if (ValidateEmailDomain.isEmailDomainNotValid(vendor.getEmail()))
	    throw new RecruizWarnException(ErrorHandler.DISPOSABLE_EMAIL_DOMAIN, ErrorHandler.INVALID_DOMAIN);

	vendor = vendorService.inviteVendor(vendor);
	vendorService.calculateVendorStatus(vendor);
	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, vendor, null);

	return userReponse;
    }

    /**
     * get all vendor list (not all vendor user)
     * 
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/all", method = RequestMethod.GET)
    public RestResponse getAllVendor() throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllVendor.name());*/

	/*if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);*/

	List<Vendor> vendorList = vendorService.getAllVendor();
	vendorService.calculateVendorStatus(vendorList);

	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, vendorList, null);
	return userReponse;
    }

    /**
     * get all position list for vendor
     * 
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/position", method = RequestMethod.GET)
    public RestResponse getAllPositionForLoggedInVendor(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetPositionForVendor.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	Page<Position> positionList = vendorService.getLoggedInVendorPosition(
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, positionList, null);
	return userReponse;
    }

    /**
     * adding candidate for vendor
     * 
     * @param candidate
     * @param file
     * @param fileName
     * @param roundId
     * @return
     * @throws RecruizException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/vendor/candidate", method = RequestMethod.POST)
    public RestResponse addVendorCandidate(@RequestPart("json") @Valid Candidate candidate,
	    @RequestPart(value = "file", required = true) MultipartFile file, @RequestParam("fileName") String fileName,
	    @RequestParam(value = "positionCode", required = true) String positionCode)
	    throws RecruizException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddCandidateByVendor.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (checkAppSettingsService.isCandidateCountExceeded())
	    return new RestResponse(RestResponse.SUCCESS, ErrorHandler.MAX_CANDIDATE_LIMIT_REACHED,
		    ErrorHandler.MAX_CANDIDATE_REACHED);

	if (file == null || file.isEmpty()) {
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.RESUME_MISSING,
		    ErrorHandler.INVALID_SERVER_REQUEST);
	}

	if (positionCode == null || positionCode.isEmpty()) {
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXIST,
		    ErrorHandler.INVALID_SERVER_REQUEST);
	}
	Candidate existingCandidate = candidateService.getCandidateByEmail(candidate.getEmail());
	if (null != existingCandidate) {
	    Long allowedModificationLimit = organizationService.getCurrentOrganization().getCandidateModificationDays();
	    Long difference = 0L;
	    if (null != allowedModificationLimit) {
		long differenceInMilliSec = new Date().getTime() - existingCandidate.getSourcedOnDate().getTime();
		difference = differenceInMilliSec / (24 * 60 * 60 * 1000);
	    } else {
		allowedModificationLimit = -1L;
	    }
	    if (allowedModificationLimit < 0L || difference < allowedModificationLimit) {
		return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_EXISTS,
			ErrorHandler.DUPLICATE_CANDIDATE);
	    }
	    // setting id to candidate object
	    candidate.setCid(existingCandidate.getCid());
	}

	File resumeFile = null;
	try {

	    resumeFile = fileService.multipartToFile(file);
	    candidate = vendorService.sourceVendorCandidate(candidate, resumeFile, fileName, positionCode);
	    if (candidate.getCid() > 0) {
		if (resumeFile.exists()) {
		    candidateService.uploadCandidateFile(candidate, resumeFile);
		}
	    }
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	} finally {
	    if (resumeFile.exists())
		resumeFile.delete();
	}

	RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidate);
	return candidateAddResponse;
    }

    @RequestMapping(value = "/api/v1/vendor/candidate", method = RequestMethod.GET)
    public RestResponse getVendorCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder)
	    throws RecruizException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorsCandidate.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	Page<Candidate> candidate = vendorService.getVendorCandidate(
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));

	RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidate);
	return candidateAddResponse;
    }

    @RequestMapping(value = "/api/v1/vendor/candidate/details", method = RequestMethod.GET)
    public RestResponse getVendorCandidate(@RequestParam(value = "email", required = true) String email)
	    throws RecruizException, IOException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorCandidateDetails.name());

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (email == null || email.isEmpty())
	    return null;
	Candidate candidate = vendorService.getVendorCandidateDetails(email);

	if (candidate == null)
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS,
		    ErrorHandler.CANDIDATE_NOT_FOUND);

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

	List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(candidate.getCid() + "");
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

	RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidateMap);
	return candidateAddResponse;
    }

    /**
     * Get vendor board
     * 
     * @param positionCode
     * @return
     * @throws RecruizException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/vendor/board", method = RequestMethod.GET)
    public RestResponse getVendorBoard(@RequestParam(value = "positionCode", required = true) String positionCode)
	    throws RecruizException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorBoard.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (positionCode == null || positionCode.isEmpty())
	    return null;
	BoardDTO vendorBoard = vendorService.getVendorBoard(positionCode);
	RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, vendorBoard);
	return candidateAddResponse;
    }

    /**
     * Update vendor candidate
     * 
     * @param candidate
     * @param file
     * @param fileName
     * @param candidateId
     * @return
     * @throws RecruizException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/vendor/candidate/{candidateId}", method = RequestMethod.POST)
    public RestResponse updateCandidate(@RequestPart("json") @Valid Candidate candidate,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam("fileName") String fileName, @PathVariable("candidateId") String candidateId)
	    throws RecruizException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.UpdateVendorCandidate.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
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

	Candidate updatedCandidate = vendorService.updateVendorCandidate(candidate, file, fileName, candidateId);

	RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, updatedCandidate);
	return candidateResponse;
    }

    /**
     * Invite users for vendor
     * 
     * @param emailLists
     * @param id
     * @return
     * @throws RecruizException
     * @throws IOException
     * @throws UnknownHostException
     * @throws ParseException
     */
    @RequestMapping(value = "/api/v1/vendor/user/invite", method = RequestMethod.POST)
    public RestResponse inviteUserList(@RequestBody InviteUserDTO inviteUserDTO, @RequestParam("id") String id)
	    throws RecruizException, UnknownHostException, IOException, ParseException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.InviteVendorUser.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (id == null || id.isEmpty())
	    return null;

	if (checkAppSettingsService.isValidityExpired()) {
	    return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
	}

	Vendor vendor = vendorService.findOne(Long.parseLong(id));
	if (vendor == null) {
	    return new RestResponse(false, ErrorHandler.VENDOR_NOT_FOUND, ErrorHandler.INVALID_VENDOR);
	}

	if (checkAppSettingsService.isVendorUserLimitExceeded(id)) {
	    return new RestResponse(false, ErrorHandler.USER_LIMIT_EXCEEDED, ErrorHandler.USER_LIMIT_REACHED);
	}

	if (!vendor.getStatus()) {
	    return new RestResponse(false, ErrorHandler.VENDOR_DISABLED, ErrorHandler.VENDOR_DISABLED);
	}
	Map<String, List<?>> resp = vendorService.inviteVendorUserList(inviteUserDTO.getInviteUsers(), vendor);

	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, resp, null);
	return userReponse;
    }

    /**
     * 
     * @param emailLists
     * @param id
     * @return
     * @throws RecruizException
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/v1/vendor/user/{id}", method = RequestMethod.GET)
    public RestResponse getVendorUsers(@PathVariable String id) throws RecruizException, JsonProcessingException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorUserList.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	List<User> users = userService.getAllUserByVendor(id);
	Map<String, Object> usersMap = dataModelToDTOConversionService
		.getResponseMapWithGridDataAndUserStatusCount(users);

	Map<String, Object> allUserWithAllRoles = new HashMap<String, Object>();
	allUserWithAllRoles.put("All_User", usersMap);

	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, allUserWithAllRoles,
		SuccessHandler.GET_USER_SUCCESS);

	return userReponse;
    }

    /**
     * get all position list for vendor
     * 
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/position/details/{positionCode}", method = RequestMethod.GET)
    public RestResponse getAllPositionDetailsForLoggedInVendor(@PathVariable("positionCode") String positionCode)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorsPositionDetails.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	Position position = vendorService.getPositionDetails(positionCode);
	if (position == null)
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS,
		    ErrorHandler.POSITION_NOT_FOUND);
	Map<String, Object> positionMap = new HashMap<String, Object>();
	positionMap.put("clientName", position.getClient().getClientName());
	positionMap.put("position", position);

	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, positionMap, null);
	return userReponse;
    }

    @RequestMapping(value = "/api/v1/vendor/board/candidate/source", method = RequestMethod.GET)
    public RestResponse getCandidatesToSource(@RequestParam("boardId") String boardId) throws RecruizException {

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (boardId == null || boardId.isEmpty())
	    return null;
	List<Candidate> candidateList = vendorService.getCandidateToSource(boardId);
	RestResponse response = new RestResponse(RestResponse.SUCCESS, candidateList, null);
	return response;
    }

    /**
     * Source Candidate to board by vendor
     * 
     * @param roundCandidateDTO
     * @param sourceMode
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/candidate/source/board", method = RequestMethod.POST)
    public RestResponse sourceCandidate(@RequestBody CandidateToRoundDTO roundCandidateDTO,
	    @RequestParam(value = "sourceMode", required = false) String sourceMode) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.SourceCandidateByVendor.name());*/

	if (!checkUserPermissionService.isUserTypeVendor())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (roundCandidateDTO.getPositionCode() == null || roundCandidateDTO.getPositionCode().isEmpty()) {
	    return new RestResponse(RestResponse.FAILED, "Position code not present",
		    ErrorHandler.INVALID_SERVER_REQUEST);
	}

	Round round = roundService.getRoundByBoardAndType(
		positionService.getPositionBoard(roundCandidateDTO.getPositionCode()), RoundType.Source.toString());

	vendorService.sourceCandidateToBoard(roundCandidateDTO, round.getId() + "");

	RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CANDIDATE_ADDED,
		null);
	return addRoundResponse;
    }

    /**
     * Enabling disabling vendor
     * 
     * @param vendorId
     * @param status
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/status", method = RequestMethod.PUT)
    public RestResponse changeVendorStatus(@RequestParam(value = "vendorId", required = true) String vendorId,
	    @RequestParam(value = "status", required = true) Boolean status) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ChangeVendorStatus.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (vendorId == null || vendorId.isEmpty())
	    return null;
	Vendor vendor = vendorService.findOne(Long.parseLong(vendorId));
	vendor.setStatus(status);
	vendorService.save(vendor);

	RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.STATUS_CHANGED, null);
	return response;
    }

    @RequestMapping(value = "/api/v1/vendor/update", method = RequestMethod.POST)
    public RestResponse updateVendor(@RequestBody Vendor vendor) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.UpdateVendor.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	RestResponse response;
	if (vendor.getId() == 0) {
	    response = new RestResponse(false, ErrorHandler.VENDOR_NOT_FOUND, ErrorHandler.INVALID_VENDOR);
	    return response;
	}
	Vendor existingVendor = vendorService.findOne(vendor.getId());
	if (!existingVendor.getEmail().equalsIgnoreCase(vendor.getEmail())) {
	    response = new RestResponse(false, ErrorHandler.EMAIL_MODIFICATION_NOT_ALLOWED,
		    ErrorHandler.EMAIL_CHANGE_NOT_ALLLOWED);
	    return response;
	} else {
	    vendor.setOrganization(existingVendor.getOrganization());
	    vendorService.save(vendor);
	    response = new RestResponse(RestResponse.SUCCESS, vendor, null);
	    return response;
	}
    }

    @RequestMapping(value = "/api/v1/vendor", method = RequestMethod.GET)
    public RestResponse getVendorDetails(@RequestParam(value = "vendorId", required = true) String vendorId)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetVendorDetails.name());*/

/*	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);
*/
	if (vendorId == null || vendorId.isEmpty())
	    return null;
	Vendor vendor = vendorService.findOne(Long.parseLong(vendorId));

	RestResponse response = new RestResponse(RestResponse.SUCCESS, vendor, null);
	return response;
    }

    /**
     * get all vendor list (not all vendor user)
     * 
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/position/all", method = RequestMethod.GET)
    public RestResponse getAllVendorForPosition() throws RecruizException {

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	List<Vendor> vendorList = vendorService.getAllActiveVendor();
	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, vendorList, null);
	return userReponse;
    }

    /**
     * To delete vendor, all the vendor user will also be deleted.
     * 
     * @param id
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/vendor/remove/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteVendor(@PathVariable("id") String id) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteVendor.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	Vendor vendor = vendorService.findOne(Long.parseLong(id));
	if (vendor == null) {
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.VENDOR_NOT_FOUND, ErrorHandler.VENDOR_NOT_EXISTS);
	}
	vendorService.deleteVendor(vendor);
	RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, GlobalConstants.VENDOR_DELETED, null);
	return userReponse;
    }

    @RequestMapping(value = "/api/v1/vendor/position/candidate/add", method = RequestMethod.PUT)
    public RestResponse addCandidateToPosition(@RequestParam List<Long> cids, @RequestParam String positionCode)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddVendorCandidateToPosition.name());*/

	List<Long> failedIds = new ArrayList<>();
	List<Long> successIds = new ArrayList<>();
	for (Long cid : cids) {
	    try {
		Candidate candidate = candidateService.findOne(cid);
		if (candidate != null && candidate.getSource() != null
			&& candidate.getSource().equalsIgnoreCase(GlobalConstants.SOURCE_INFO_VENDOR)) {
		    boolean added = vendorService.addCandidateToPosition(candidate, positionCode);
		    if (added) {
			successIds.add(cid);
		    } else {
			failedIds.add(cid);
		    }
		}
	    } catch (Exception ex) {
		failedIds.add(cid);
		logger.warn(ex.getMessage(), ex);
	    }
	}
	Map<String, Object> responseMap = new HashMap<>();
	responseMap.put("failedIds", failedIds);
	responseMap.put("successIds", successIds);

	RestResponse response = new RestResponse(true, responseMap);
	return response;
    }

    @RequestMapping(value = "/api/v1/vendor/position/map", method = RequestMethod.GET)
    public RestResponse getVendorPositionMap() throws RecruizException {
	List<Position> vendorPositions = vendorService.getLoggedInVendorPosition();
	List<BaseDTO> positionNameMap = new ArrayList<BaseDTO>();
	if (null != vendorPositions && !vendorPositions.isEmpty()) {
	    for (Position position : vendorPositions) {
		BaseDTO baseDTO = new BaseDTO();
		baseDTO.setId(position.getPositionCode());
		baseDTO.setValue(position.getTitle());
		positionNameMap.add(baseDTO);
	    }
	}

	RestResponse response = new RestResponse(true, positionNameMap);
	return response;
    }

    
    @RequestMapping(value = "/api/v1/vendor/permission/scheduleInterview", method = RequestMethod.GET)
    public RestResponse giveScheduleInterviewPermission(@RequestParam(value = "vendorId") String vendorId,@RequestParam(value = "permission") String permission) throws RecruizException {

    	if(vendorId==null || permission==null || vendorId.equals("") || permission.equals(""))
    		return new RestResponse(RestResponse.FAILED, "vendorId OR permission both are required");
    	
	return vendorService.giveScheduleInterviewPermission(vendorId,permission);
    }
    
    
    
}
