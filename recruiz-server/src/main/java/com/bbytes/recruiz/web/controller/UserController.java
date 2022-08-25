package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.AuthenticationFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.EmailValidator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.database.UpdateDBTemplateService;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.enums.EmailClientType;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.enums.UserType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.integration.servetel.ServetelService;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseSearchService;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseUpdateSearchValuesService;
import com.bbytes.recruiz.mail.service.EmailClientSession;
import com.bbytes.recruiz.mail.service.imap.ImapClient;
import com.bbytes.recruiz.rest.dto.models.ExternalUserDTO;
import com.bbytes.recruiz.rest.dto.models.ImportFileDTO;
import com.bbytes.recruiz.rest.dto.models.InviteUser;
import com.bbytes.recruiz.rest.dto.models.InviteUserDTO;
import com.bbytes.recruiz.rest.dto.models.ProfileDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.DummyValueService;
import com.bbytes.recruiz.service.EmailAccountDetailService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.EmailedFileItemService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.ImportExportService;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.service.NotificationService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionRequestService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ValidateEmailDomain;

@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private UserService userService;
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private ServetelService servetelService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	@Autowired
	private DummyValueService addDummyValueService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private UpdateDBTemplateService updateDBTemplateService;

	@Autowired
	private PositionRequestService positionRequestService;

	@Autowired
	private EmailAccountDetailService emailClientDetailService;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private FileService fileService;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Autowired
	private SixthSenseUpdateSearchValuesService senseUpdateSearchValuesService;

	@Autowired
	private SixthSenseSearchService sixthSenseSearchService;

	@Autowired
	private ImapClient client;

	@Autowired
	private ClientService clientService;

	@Autowired
	private EmailedFileItemService emailedFileItemService;

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

	@Value("${gmail.imap.url}")
	private String gmailImapUrl;

	@Value("${gmail.imap.port}")
	private String gmailImapPort;

	@Value("${gmail.smtp.url}")
	private String gmailSmtpUrl;

	@Value("${gmail.smtp.port}")
	private String gmailSmtpPort;

	@Value("${yahoo.imap.url}")
	private String yahooImapUrl;

	@Value("${yahoo.imap.port}")
	private String yahooImapPort;

	@Value("${yahoo.smtp.url}")
	private String yahooSmtpUrl;

	@Value("${yahoo.smtp.port}")
	private String yahooSmtpPort;

	@Value("${outlook.imap.url}")
	private String outllookImapUrl;

	@Value("${outlook.imap.port}")
	private String outlookImapPort;

	@Value("${outlook.smtp.url}")
	private String outlookSmtpUrl;

	@Value("${outlook.smtp.port}")
	private String outlookSmtpPort;

	@RequestMapping(value = "/api/v1/user/invite", method = RequestMethod.GET)
	public RestResponse inviteUser(@RequestParam String email, @RequestParam(value = "userType", required = false) String userType)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserInvite.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (email == null || email.isEmpty())
			return null;
		final String template = GlobalConstants.INVITE_USER_TO_SIGNUP_EMAIL_TEMPLATE;

		if (!(EmailValidator.getInstance().isValid(email))) {
			throw new RecruizException("Email Id '" + email + "' not valid", "email_invalid");
		}

		if (ValidateEmailDomain.isEmailDomainNotValid(email))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.DISPOSABLE_EMAIL_DOMAIN, ErrorHandler.INVALID_DOMAIN);

		inviteToAccept(email, template, userType);
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userService.getUserByEmail(email), SuccessHandler.USER_INVITED);

		return userReponse;
	}

	/**
	 * User is already part of recruiz Invite user to accpet the invition
	 * 
	 * @param email
	 * @param template
	 * @throws RecruizException
	 */
	private void inviteToAccept(String email, final String template, String userType) throws RecruizException {

		int index = email.indexOf("@");
		String name = email.substring(0, index);
		User user = userService.getLoggedInUserObject();
		userService.create(email, name, GlobalConstants.DEFAULT_PASSWORD, user.getOrganization(), null, user.getTimezone(),
				user.getLocale(), userType, null);

		String encryptedEmail = EncryptKeyUtils.getEncryptedKey(email);

		List<String> emailList = new ArrayList<String>();
		emailList.add(email);

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, name);
		emailBody.put(GlobalConstants.USER_EMAIL, email);
		emailBody.put(GlobalConstants.FIRST_PASSWORD, GlobalConstants.DEFAULT_PASSWORD);
		emailBody.put(GlobalConstants.ACTIVATION_LINK, baseUrl + GlobalConstants.PASSKEY + encryptedEmail);

		emailService.sendEmail(template, emailList, inviteToJoinSubject, emailBody);
	}

	@RequestMapping(value = "/api/v1/user/reInviteUser", method = RequestMethod.GET)
	public RestResponse reInviteUser(@RequestParam String email) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserReInvite.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		notificationService.sendMsg(userService.getLoggedInUserEmail(), email + " re-invited");

		if (email == null || email.isEmpty())
			return null;
		final String reInviteToJoinTemplate = GlobalConstants.RE_INVITE_USER_TO_JOIN_EMAIL_TEMPLATE;
		final String reInviteToSignup = GlobalConstants.RE_INVITE_USER_TO_SIGNUP_EMAIL_TEMPLATE;

		if (!(EmailValidator.getInstance().isValid(email))) {
			throw new RecruizException("Email Id '" + email + "' not valid", "email_invalid");
		}

		User existingUser = tenantResolverService.getUserByEmail(email);
		if (existingUser != null) {
			String name;
			if (existingUser.getName() == null) {
				int index = email.indexOf("@");
				name = email.substring(0, index);
			} else
				name = existingUser.getName();
			userService.inviteUser(reInviteToJoinTemplate, reInviteToJoinSubject, null, email, name);
		} else {
			int index = email.indexOf("@");
			String name = email.substring(0, index);
			userService.inviteUserToSignup(reInviteToSignup, reInviteToSignupSubject, email, name,
					userService.getLoggedInUserObject().getOrganization().getOrgId());
		}
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userService.getUserByEmail(email), SuccessHandler.USER_INVITED);
		return userReponse;
	}

	/**
	 * Invite list of user, if they are part of recruiz then join template will
	 * be sent other wise link to signup will be sent
	 * 
	 * @param inviteUsers
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/user/inviteUserList", method = RequestMethod.POST)
	public RestResponse inviteUserList(@RequestBody InviteUserDTO inviteUserDTO,
			@RequestParam(value = "userType", required = false) String userType)
			throws RecruizException, UnknownHostException, IOException, ParseException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserInvite.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkAppSettingsService.isValidityExpired()) {
			return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
		} else if (userType.equalsIgnoreCase(UserType.APP.getDisplayName()) && checkAppSettingsService.isUserLimitExceeded(userType)) {
			return new RestResponse(false, ErrorHandler.USER_LIMIT_EXCEEDED, ErrorHandler.USER_LIMIT_REACHED);
		} else if ((userType.equalsIgnoreCase(UserType.Vendor.getDisplayName()) && checkAppSettingsService.isUserLimitExceeded(userType))) {
			return new RestResponse(false, ErrorHandler.USER_LIMIT_EXCEEDED, ErrorHandler.USER_LIMIT_REACHED);
		}

		// ******* Delete this role creation when in prod, this role will be
		// created during signup process *******/
		// **** Department Head Role created here *******/
		List<Permission> deptartmentHeadPermissions = new ArrayList<Permission>();
		deptartmentHeadPermissions.add(new Permission(PermissionConstant.ADD_EDIT_POSITION));
		UserRole departmentHead = new UserRole();
		departmentHead.getPermissions().addAll(deptartmentHeadPermissions);
		departmentHead.setRoleName(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE);
		if (userRoleService.getRolesByName(departmentHead.getRoleName()) == null)
			userRoleService.save(departmentHead);
		/******** Department Head Role created here Ends *************/

		final String inviteToSignup = GlobalConstants.INVITE_USER_TO_SIGNUP_EMAIL_TEMPLATE;
		final String inviteToJoinTemplate = GlobalConstants.INVITE_USER_TO_JOIN_EMAIL_TEMPLATE;

		if (userType == null || userType.isEmpty()) {
			userType = UserType.APP.getDisplayName();
		}

		List<String> invalidEmails = new ArrayList<String>();
		List<InviteUser> validUserList = new ArrayList<InviteUser>();
		List<String> invitedEmails = new ArrayList<String>();
		List<User> successfullyInvitedEmails = new ArrayList<User>();

		for (InviteUser user : inviteUserDTO.getInviteUsers()) {

			if (!(EmailValidator.getInstance().isValid(user.getEmail()))) {
				invalidEmails.add(user.getEmail());
			} else if (ValidateEmailDomain.isEmailDomainNotValid(user.getEmail())) {
				invalidEmails.add(user.getEmail());
			} else {
				validUserList.add(user);
			}
		}

		User loggedInUser = userService.getLoggedInUserObject();

		for (InviteUser validUser : validUserList) {

			// checking user limit
			if (userType.equalsIgnoreCase(UserType.APP.getDisplayName()) && checkAppSettingsService.isUserLimitExceeded(userType)) {
				return new RestResponse(false, ErrorHandler.USER_LIMIT_EXCEEDED, ErrorHandler.USER_LIMIT_REACHED);
			} else if ((userType.equalsIgnoreCase(UserType.Vendor.getDisplayName())
					&& checkAppSettingsService.isUserLimitExceeded(userType))) {
				return new RestResponse(false, ErrorHandler.USER_LIMIT_EXCEEDED, ErrorHandler.USER_LIMIT_REACHED);
			}

			User validemail = userService.getUserByEmail(validUser.getEmail());
			boolean isUserRegisteredWithRecruiz = tenantResolverService.emailExist(validUser.getEmail());

			UserRole userRole = null;
			// assigning department head by user type
			if ("department_head".equalsIgnoreCase(userType)) {
				userRole = userRoleService.getRolesByName(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE);
			} else
				userRole = userRoleService.getRoleById(Long.valueOf(validUser.getRoleId()));
			// if no role present for id, assigning to normal user
			if (userRole == null)
				userRole = userRoleService.getRolesByName(GlobalConstants.NORMAL_USER_ROLE);
			if (validemail != null) {
				invitedEmails.add(validUser.getEmail());
			} else if (isUserRegisteredWithRecruiz) {
				String name = validUser.getUserName();
				User createdUser = userService.create(validUser.getEmail(), name, GlobalConstants.DEFAULT_PASSWORD,
						loggedInUser.getOrganization(), userRole, loggedInUser.getTimezone(), loggedInUser.getLocale(), userType, null);
				tenantResolverService.saveTenantResolverForUser(createdUser);
				userService.inviteUser(inviteToJoinTemplate, inviteToJoinSubject, successfullyInvitedEmails, validUser.getEmail(), name);
			} else {
				userService.create(validUser.getEmail(), validUser.getUserName(), GlobalConstants.DEFAULT_PASSWORD,
						loggedInUser.getOrganization(), userRole, loggedInUser.getTimezone(), loggedInUser.getLocale(), userType, null);
				userService.inviteUserToSignup(inviteToSignup, inviteToSignupSubject, validUser.getEmail(), validUser.getUserName(),
						loggedInUser.getOrganization().getOrgId());
			}
		}
		Map<String, List<?>> resp = new HashMap<String, List<?>>();
		resp.put("SuccessfullyInvitedEmails", successfullyInvitedEmails);
		resp.put("ExistingEmail", invitedEmails);
		resp.put("InvalidEmails", invalidEmails);

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, resp, null);
		return userReponse;
	}

	/**
	 * Upload import file and return header map
	 * 
	 * @param file
	 * @return
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/user/bulkupload/import", method = RequestMethod.POST)
	public RestResponse uploadImportFile(@RequestParam("file") MultipartFile file)
			throws RecruizException, IllegalStateException, IOException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserBulkUpload.name());*/

		if (!checkUserPermissionService.isUserTypeApp() && (!checkUserPermissionService.hasOrgAdminPermission()
				|| !checkUserPermissionService.hasITAdminPermission() || !checkUserPermissionService.hasAdminSettingPermission()))
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// checking uploaded file is excel file
		if (!"xlsx".equals(FilenameUtils.getExtension(file.getOriginalFilename()))
				&& !"xls".equals(FilenameUtils.getExtension(file.getOriginalFilename())))
			return new RestResponse(RestResponse.FAILED, "Invalid file format", ErrorHandler.INVALID_FILE_FORMAT);

		File importFile = fileService.multipartToFileForBulkUpload(file);
		Map<String, Object> headerMap = importExportService.getUploadedFileHeaderMap(importFile, GlobalConstants.USERS);

		return new RestResponse(RestResponse.SUCCESS, headerMap);

	}

	/**
	 * The importData method is used to import user data from excel file and
	 * update into db
	 * 
	 * @param importFileDTO
	 * @return
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/user/bulkupload", method = RequestMethod.POST)
	public RestResponse importUserData(@RequestBody ImportFileDTO importFileDTO)
			throws RecruizException, IllegalStateException, IOException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserBulkUpload.name());*/

		RestResponse response;
		if (importFileDTO.getFilePath() == null || importFileDTO.getFilePath().isEmpty())
			return new RestResponse(RestResponse.FAILED, "File Path must not be null");

		File importFile = new File(importFileDTO.getFilePath());
		if (!importFile.exists())
			return new RestResponse(RestResponse.FAILED, "File does not exist");
		if (importFileDTO.getHeaderMapList() == null || importFileDTO.getHeaderMapList().isEmpty())
			return new RestResponse(RestResponse.FAILED, "Header map list must not be null");

		Map<String, String> headerMap = new HashMap<String, String>();
		for (Map<String, String> map : importFileDTO.getHeaderMapList()) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				headerMap.put(entry.getKey(), entry.getValue());
			}
		}

		User loggedInUser = userService.getLoggedInUserObject();
		List<UserDTO> responseDTO = null;
		if (loggedInUser != null) {
			List<User> successfulUploadedUserList = userService.bulkUploadUsers(importFile, headerMap, loggedInUser);
			responseDTO = dataModelToDTOConversionService.convertUsers(successfulUploadedUserList);
			logger.debug(responseDTO.size() + "' User are uploaded successfully");
		}

		response = new RestResponse(RestResponse.SUCCESS, responseDTO);

		return response;
	}

	@RequestMapping(value = "/api/v1/user/account/activate", method = RequestMethod.GET)
	public RestResponse updateJoinedStatus() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateJoinedStatus.name());*/

		User loggedInUser = userService.getLoggedInUserObject();
		if (loggedInUser == null) {
			return new RestResponse(false, ErrorHandler.ACTIVATION_ERROR, ErrorHandler.USER_NOT_FOUND);
		}
		if (null != loggedInUser.getJoinedStatus() && loggedInUser.getJoinedStatus()) {
			return new RestResponse(false, ErrorHandler.ACCOUNT_IS_ACTIVATED, ErrorHandler.ALREADY_ACTIVATED);
		}
		Map<String, String> userTenantMap = tenantResolverService
				.getOrgIdToOrgNameMap(tenantResolverService.findAllTenantsForUserId(loggedInUser.getEmail()));
		User user = userService.updateJoinedStatus(loggedInUser.getEmail());

		UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put(GlobalConstants.USER_DETAILS, userDTO);
		responseMap.put(GlobalConstants.TENANT_LIST, userTenantMap);

		// after activating joined status updating the email templates from the
		// file -- Recruiz RECZ-1010
		try {
			// after activating account updating sixth sense search values
			senseUpdateSearchValuesService.updateSixthSenseSearchValue(TenantContextHolder.getTenant());
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
			responseMap.put(ErrorHandler.FAILED_UPDATING_TEMPLATES, ErrorHandler.ACTIAVTED_FAILED_TO_UPDATE_TEMPLATE);
		}

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, responseMap, SuccessHandler.STATUS_UPDATED);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/account/status", method = RequestMethod.PUT)
	public RestResponse updateAccountStatus(@RequestParam("email") String email, @RequestParam("status") boolean status)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateUserAccountStatus.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (email == null || email.isEmpty())
			return null;
		User user = userService.updateUserAccountStatus(email, status);

		UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userDTO, SuccessHandler.ACCOUNT_STATUS);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/changeUserRole", method = RequestMethod.PUT)
	public RestResponse changeUserRole(@RequestParam String email, @RequestParam String id) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ChangeUserRole.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (email == null || email.isEmpty() || id == null || id.isEmpty())
			return null;
		long roleId = Long.parseLong(id);
		String roleName = userRoleService.getRoleById(roleId).getRoleName();
		userService.assignUserRole(email, roleName);
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userService.getUserByEmail(email), SuccessHandler.Role_ASSIGNED);
		return userReponse;
	}

	
	@RequestMapping(value = "/api/v1/user/resetUserPasswordByAdmin", method = RequestMethod.GET)
	public RestResponse resetUserPasswordByAdmin(@RequestParam String email, @RequestParam String password) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), "Reset User password by Admin");*/

		if (email == null || email.isEmpty() || password == null || password.isEmpty())
			return new RestResponse(false, "Cannot Reset User Password", "Please put correct password");
		try{
		  userService.resetUserPasswordByAdmin(email,password);
		  return  new RestResponse(RestResponse.SUCCESS, userService.getUserByEmail(email), SuccessHandler.Role_ASSIGNED);
		}catch(Exception e){
			return new RestResponse(false, "Cannot Reset User Password", "Reset User Password Failed");
		}
		
	}
	
	@RequestMapping(value = "/api/v1/user/getAllUser", method = RequestMethod.GET)
	public RestResponse getAllUsers() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllUser.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		List<User> users = (List<User>) userService.getAllUsers();
		Map<String, Object> usersMap = dataModelToDTOConversionService.getResponseMapWithGridDataAndUserStatusCount(users);

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, usersMap, SuccessHandler.GET_USER_SUCCESS);

		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/getAllUserByOrgID", method = RequestMethod.POST)
	public RestResponse getAllUsersByOrgID(@RequestParam String orgID) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllUser.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (orgID == null || orgID.isEmpty())
			return null;
		List<User> users = userService.getAllUsersForOrganization(orgID);
		Map<String, Object> usersMap = dataModelToDTOConversionService.getResponseMapWithGridDataAndUserStatusCount(users);

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, usersMap, SuccessHandler.GET_USER_SUCCESS);

		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/getAllUserByJoinedStatus", method = RequestMethod.POST)
	public RestResponse getAllUsersByJoinedStatus(@RequestParam boolean status) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllJoinedUser.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		List<User> users = userService.getAllUserByJoinedStatus(status);
		Map<String, Object> usersMap = dataModelToDTOConversionService.getAllUser(users);
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, usersMap, SuccessHandler.GET_USER_SUCCESS);

		return userReponse;
	}

	/**
	 * This method is used to fetch all HR list.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/hr", method = RequestMethod.GET)
	public RestResponse getHrLIST() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllHr.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		List<User> hrList = userService.getHrList();
		RestResponse allHrList = new RestResponse(RestResponse.SUCCESS, hrList, null);
		return allHrList;
	}

	@RequestMapping(value = "/api/v1/user/{email:.+}", method = RequestMethod.DELETE)
	public RestResponse deleteUser(@PathVariable("email") String email, @RequestParam("newOwner") String newOwner)

			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteUser.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (email == null || email.isEmpty() || newOwner == null || newOwner.isEmpty())
			return null;
		User userToDelete = userService.getUserByEmail(email);
		if (!userToDelete.getJoinedStatus()) {
			userService.delete(userToDelete);
			// checking user has multiple accounts,
			// return count = 1 means single org account
			if (tenantResolverService.getTenantsCount(email) == 1) {
				// deleting entry from user table -
				// tenant_management db
				tenantResolverService.deleteUserFromTenantResolver(email);
			}
			// deleting from tenant resolver also as per ticket RCZ-827
			tenantResolverService.deleteTenantResolver(email, TenantContextHolder.getTenant());
		} else if (userToDelete.getEmail().equalsIgnoreCase(newOwner)) {
			return new RestResponse(false, ErrorHandler.CAN_NOT_DELETE_LOOGEDIN_USER, ErrorHandler.USER_DELETE_FAILED);
		} else {
			try {
				// deleting from tenant DB
				userService.delete(userToDelete, newOwner);
				// deleting from tenant resolver also
				tenantResolverService.deleteTenantResolver(email, TenantContextHolder.getTenant());

				// checking if there is no entry in tenant resolver then delete
				// user from user table in tenant mgmt database
				if (!(tenantResolverService.getTenantsCount(email) > 0)) {
					tenantResolverService.deleteUserFromTenantResolver(email);
				}

				// sending email to new owner of the previous user's data
				User newUser = userService.getUserByEmail(newOwner);
				sendOwnershipEmailOnUserDelete(newUser.getUsername(), newUser.getEmail());

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				return new RestResponse(false, ErrorHandler.CAN_NOT_DELETE_USER, ErrorHandler.USER_DELETE_FAILED);
			}
		}
		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, userToDelete, null);
		return deleteResponse;
	}

	/**
	 * to send ownership email on user delete to newly added user
	 * 
	 * @param newUserName
	 * @param newUserEmail
	 * @throws RecruizException
	 */
	private void sendOwnershipEmailOnUserDelete(String newUserName, String newUserEmail) throws RecruizException {

		String templateName = "email-template-new-ownership-assigned.html";

		Map<String, Object> emailVariabeMap = new HashMap<>();

		if (null == newUserName || newUserName.isEmpty()) {
			newUserName = "";
		}

		emailVariabeMap.put(GlobalConstants.USER_NAME, newUserName);
		emailTemplateDataService.initEmailBodyDefaultVariables(emailVariabeMap);

		String template = emailTemplateDataService.getHtmlContentFromFile(emailVariabeMap, templateName);
		String renderedTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(template);
		List<String> emailList = new ArrayList<>();
		emailList.add(newUserEmail);
		emailService.sendEmail(emailList, renderedTemplate, "New ownership assinged");
	}

	@RequestMapping(value = "/api/v1/user/role", method = RequestMethod.GET)
	public RestResponse getAllUsersWithAllRoles(@RequestParam(value = "userType", required = true) String userType)

			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetUserRole.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		List<User> users = userService.getAllUsersByType(userType);
		Map<String, Object> usersMap = dataModelToDTOConversionService.getResponseMapWithGridDataAndUserStatusCount(users);

		List<UserRole> userRoles = userService.getAllRoles();
		Map<String, Object> roleMap = dataModelToDTOConversionService.getAllRoles(userRoles);

		Map<String, Object> allUserWithAllRoles = new HashMap<String, Object>();
		allUserWithAllRoles.put("All_User", usersMap);
		allUserWithAllRoles.put("All_Roles", roleMap);

		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, allUserWithAllRoles, SuccessHandler.GET_USER_SUCCESS);

		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/getAllUserRoleWithUserCount", method = RequestMethod.GET)
	public RestResponse getAllUserRoleWithUserCount() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllUser.name());
*/
		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Map<String, Object> result = userService.getRoleWithUserCount();
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, result, null);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/getAllUserToAssignRole", method = RequestMethod.GET)
	public RestResponse getAllUserToAssignRole() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllUser.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
				&& !checkUserPermissionService.hasAdminSettingPermission())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Map<String, Object> userList = userService.getUserListToChangeRole();
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userList, null);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/logout", method = RequestMethod.GET)
	public RestResponse logoutPage() {

		IntegrationProfileDetails sixthSenseIntegrationDetails = null;
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		sixthSenseIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.SIXTH_SENSE_APP_ID);

		// on logout closing sixthsense session
		if (sixthSenseIntegrationDetails != null && sixthSenseIntegrationDetails.getIntegrationDetails() != null
				&& sixthSenseIntegrationDetails.getIntegrationDetails().get(IntegrationConstants.SIXTH_SENSE_BASE_URL) != null) {
			sixthSenseSearchService.closeSixthSenseSession(false);

		}
		String tenant = TenantContextHolder.getTenant();
		String userEmail = userService.getLoggedInUserEmail();
		String userName = userService.getLoggedInUserName();
		TenantContextHolder.clearContext();
		tenantUsageStatService.insertInToUsageStatTable(tenant, userEmail, userName, UsageActionType.LoggedOut.name());

		return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.LOGOUT_SUCCESS, null);

	}

	@RequestMapping(value = "/api/v1/user/generate/apiToken", method = RequestMethod.GET)
	public RestResponse generateApiTokenForCurrentUser() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GenerateApiToken.name());*/

		User loggedInUser = userService.getLoggedInUserObject();
		String apiToken = userService.generateNewApiToken(loggedInUser.getEmail(), loggedInUser.getOrganization().getOrgId());
		return new RestResponse(RestResponse.SUCCESS, apiToken);
	}

	@RequestMapping(value = "/api/v1/user/apiToken", method = RequestMethod.GET)
	public RestResponse getApiTokenForCurrentUser() {
/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAPIToken.name());
*/
		User loggedInUser = userService.getLoggedInUserObject();
		String apiToken = userService.getApiToken(loggedInUser);
		return new RestResponse(RestResponse.SUCCESS, apiToken);
	}

	/**
	 * This API is just for updating new auth token to maintain single session
	 * and its been taking taken in TokenAuthenticationProvider class and
	 * overriding new token in redis cache.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/api/v1/update/auth/token", method = RequestMethod.GET)
	public RestResponse updateToken() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateToken.name());*/

		return new RestResponse(RestResponse.SUCCESS, SuccessHandler.AUTH_TOKEN_UPDATED);

	}

	@RequestMapping(value = "/api/v1/user/tenant/list", method = RequestMethod.GET)
	public RestResponse getTenantList() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllTenantList.name());*/

		List<String> tenantIds = tenantResolverService.findAllTenantsForUserId(userService.getLoggedInUserEmail());

		Map<String, String> tenantMap = tenantResolverService.getOrgIdToOrgNameMap(tenantIds);
		return new RestResponse(RestResponse.SUCCESS, tenantMap, null);

	}

	@RequestMapping(value = "/api/v1/current/user", method = RequestMethod.GET)
	public RestResponse getCurrentUser() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetCurrentUser.name());*/

		User user = userService.getLoggedInUserObject();
		UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);
		logger.info("Getting current user info");
		return new RestResponse(RestResponse.SUCCESS, userDTO);

	}

	@RequestMapping(value = "/api/v1/external/user", method = RequestMethod.GET)
	public RestResponse getExternalUserInfo() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetExternalUser.name());*/

		ExternalUserAccessDataHolder externalUser = userService.getExternalUserObject();
		ExternalUserDTO externalUserDTO = dataModelToDTOConversionService.convertExternalUser(externalUser);
		logger.info("Getting external user info");
		return new RestResponse(RestResponse.SUCCESS, externalUserDTO);
	}

	@RequestMapping(value = "/auth/user/forget/password", method = RequestMethod.PUT)
	public RestResponse forgetPassword(@RequestParam String email) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ForgetPassword.name());*/

		if (email == null || email.isEmpty())
			return null;
		if (tenantResolverService.emailExist(email)) {
			userService.forgotPassword(email);
		} else {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.USER_NOT_FOUND, ErrorHandler.USER_NOT_FOUND);
		}
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.PASSWORD_RESET,
				RestResponseConstant.RESET_PASSWORD);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/password/reset", method = RequestMethod.PUT)
	public RestResponse resetPassword(@RequestParam("newPassword") String newPassword) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ResetPassword.name());*/

		if (newPassword == null || newPassword.isEmpty())
			return null;
		userService.updatePassword(newPassword, userService.getLoggedInUserEmail());
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.PASSWORD_CHANGED,
				SuccessHandler.PASSWORD_UPDATED);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/password/change", method = RequestMethod.PUT)
	public RestResponse changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserPasswordChange.name());
*/
		if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty())
			return null;
		userService.change(newPassword, oldPassword, userService.getLoggedInUserEmail());
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.PASSWORD_CHANGED,
				SuccessHandler.PASSWORD_UPDATED);
		return userReponse;
	}

	@RequestMapping(value = "/api/v1/user/update", method = RequestMethod.PUT)
	public RestResponse updateUser(@RequestBody ProfileDTO profileDTO, HttpServletRequest request) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserUpdate.name());*/

		if (profileDTO.getUserName() == null && profileDTO.getUserName().isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.USER_NAME_NOT_VALID, ErrorHandler.INVALID_NAME);
		}
		User user = updateUserProfile(profileDTO);
		UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);
		
		/*Organization org = organizationService.getOrgInfo();
		if(org.getIvrCallingIntegration()!=null && org.getIvrCallingIntegration().equalsIgnoreCase("servetel")){
			if(user.getMobile()!=null && !user.getMobile().trim().equalsIgnoreCase("")){	
				servetelService.updateServetelAgentMobile(user,org);
			}
		}*/
		
		RestResponse userReponse = new RestResponse(RestResponse.SUCCESS, userDTO);
		return userReponse;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private User updateUserProfile(ProfileDTO profileDTO) throws RecruizException {
		User user = userService.update(profileDTO.getUserName(), profileDTO.getTimeZone(), profileDTO.getMobile(),
				profileDTO.getDesignation(), profileDTO.getProfileSignature());
		return user;
	}

	/*
	 * @Transactional(propagation = Propagation.REQUIRES_NEW) private String
	 * getUpdatedAuhToken(HttpServletRequest request) { User loggedInUser =
	 * userService.getLoggedInUserObject(); String newToken =
	 * tokenAuthenticationProvider.getNewCachedWebAppAuthToken(loggedInUser.
	 * getEmail(), loggedInUser.getOrganization().getOrgId(),
	 * DateTimeZone.forID(loggedInUser.getTimezone()), new
	 * Locale(loggedInUser.getLocale()), request); return newToken; }
	 */

	/**
	 * mark for delete user
	 * 
	 * @param email
	 * @param markForDelete
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user", method = RequestMethod.PUT)
	public RestResponse markForDeleteUser(@RequestParam("email") String email, @RequestParam("markForDelete") String markForDelete)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.MarkForDeleteUser.name());*/

		if (email == null || email.isEmpty() || markForDelete == null || markForDelete.isEmpty())
			return null;
		User userToMark = userService.getUserByEmail(email);
		userToMark.setMarkForDelete(Boolean.parseBoolean(markForDelete));
		userService.save(userToMark);
		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.USER_MARKED_TO_DELETE, null);
		return deleteResponse;
	}

	/**
	 * Get sample excel file for bulk upload users
	 * 
	 * @param response
	 * @throws RecruizException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	@RequestMapping(value = "/api/v1/bulkupload/sample/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadSampleBulkUserFile(HttpServletResponse response) throws RecruizException, IOException, InvalidFormatException {

		// exporting excel report file
		Object[] metaData = { "Name", "Email", "Role Name" };
		Object[][] resultsetData = new Object[100][5];
		List<UserRole> userRoles = userService.getAllRoles();
		int count = 1;
		for (UserRole role : userRoles) {
			if (!GlobalConstants.DEPARTMENT_HEAD_USER_ROLE.equals(role.getRoleName())
					&& !GlobalConstants.VENDOR_ROLE.equals(role.getRoleName())) {
				resultsetData[count][0] = "Recruiz_User" + count;
				resultsetData[count][1] = "sampleemail" + count + "@recruiz.com";
				resultsetData[count][2] = role.getRoleName();
				count++;
			}
		}
		File exceltFile = importExportService.resultSetToExcelExport(metaData, resultsetData, "Sample User bulk format", null);

		Path getPathFromServer = exceltFile.toPath();
		// checking if file exists in path
		if (getPathFromServer.toFile() == null || !getPathFromServer.toFile().exists()) {
			return;
		}

		String mimeType = URLConnection.guessContentTypeFromName(getPathFromServer.getFileName().toString());
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + getPathFromServer.getFileName().toString() + "\""));

		response.setContentLength((int) getPathFromServer.toFile().length());
		response.setHeader("recruiz-file-name", getPathFromServer.getFileName().toString());
		Files.copy(getPathFromServer, response.getOutputStream());

	}

	@RequestMapping(value = "/api/v1/user/exists", method = RequestMethod.GET)
	public RestResponse isUserExists(@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "userType", required = false) String userType) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UserExistsCheck.name());
*/
		if (email == null || email.isEmpty() || userType == null || userType.isEmpty()) {
			return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.PARAM_MISSING, ErrorHandler.REQUIRED_PARAM_MISSING);
		}
		Map<String, String> userExists = new HashMap<>();
		userExists.put("userExits", userService.isUserExists(email, userType) + "");

		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, userExists, null);
		return deleteResponse;
	}

	@RequestMapping(value = "/api/v1/dummy/data/delete", method = RequestMethod.DELETE)
	public RestResponse deleteDummyData() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteDummyData.name());*/

		addDummyValueService.deleteDummyData();
		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, "dummy data deleted", null);
		return deleteResponse;
	}

	@RequestMapping(value = "/api/v1/dummy/data/populate", method = RequestMethod.PUT)
	public RestResponse populateDummyData() throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.PopulateDummyData.name());*/

		if (!checkUserPermissionService.isUserTypeApp()) {
			return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
		}
		if (!checkUserPermissionService.isSuperAdmin()) {
			return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
		}

		// check if dummy data exists then don't allow it to be added
		List<Client> clients = clientService.getDummyClient();
		if (null != clients && !clients.isEmpty()) {
			return new RestResponse(false, ErrorHandler.DUMMY_DATA_ALREADY_ADDED, ErrorHandler.DUMMY_DATA_EXISTS);
		}

		addDummyValueService.addDummyData(userService.getLoggedInUserEmail());
		addDummyValueService.addDummyInvoice();
		addDummyValueService.addDummyProspect();
		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, "populated", null);
		return deleteResponse;
	}

	/**
	 * to get list of active dept head user
	 * 
	 * @param userEmailToRemove
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/dept/head/get", method = RequestMethod.GET)
	public RestResponse getDeptHeadToAssignOwnerShip(@RequestParam String userEmailToRemove) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetDeptHead.name());*/

		UserRole deptHeadRole = userRoleService.getRolesByName(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE);
		Set<User> deptHeadUsers = userService.getAllActiveUserByRole(deptHeadRole);
		User userToRemove = userService.getUserByEmail(userEmailToRemove);

		if (deptHeadUsers != null && !deptHeadUsers.isEmpty() && deptHeadUsers.contains(userToRemove)) {
			deptHeadUsers.remove(userToRemove);
		}

		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, deptHeadUsers, null);
		return deleteResponse;
	}

	/**
	 * to delete dept head user
	 * 
	 * @param userEmailToRemove
	 * @param newOwnerEmail
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/user/dept/head/delete", method = RequestMethod.DELETE)
	public RestResponse deleteDeptHead(@RequestParam(required = false) String userEmailToRemove,
			@RequestParam(required = false) String newOwnerEmail) throws RecruizException {
/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteDeptHead.name());*/

		if (null == userEmailToRemove || userEmailToRemove.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING, ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		if (null == newOwnerEmail || newOwnerEmail.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING, ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		User newOwner = userService.getUserByEmail(newOwnerEmail);
		if (newOwner == null) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NEW_OWNER_NOT_EXISTS_IN_SYSTEM, ErrorHandler.USER_NOT_FOUND);
		}

		positionRequestService.assignAndDeleteUser(userEmailToRemove, newOwner);

		User userToDelete = userService.getUserByEmail(userEmailToRemove);
		userService.delete(userToDelete);

		RestResponse deleteResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.USER_DELETED, null);
		return deleteResponse;
	}

	@RequestMapping(value = "/api/v1/user/email/client/details/add", method = RequestMethod.POST)
	public RestResponse addEmailClientDetails(@RequestBody EmailClientDetails emailClientDetails) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddEmailClient.name());
*/
		RestResponse response = null;
		try {
			String encryptedEmail = EncryptKeyUtils.getEncryptedKey(emailClientDetails.getEmailId());
			List<EmailClientDetails> existingClient = emailClientDetailService.getClientByEmail(encryptedEmail);
			if (null != existingClient && !existingClient.isEmpty()) {
				return new RestResponse(false, ErrorHandler.EMAIL_EXIST, ErrorHandler.EMAIL_EXIST_IN_SAME_ID);
			}

			fillImapAndSmtpDetails(emailClientDetails);
			emailClientDetails.setUser(userService.getLoggedInUserObject());
			emailClientDetails.setRecruizEmail(userService.getLoggedInUserEmail());

			EmailClientSession ssn = client.getConnectedClient(emailClientDetails);

			if (null == ssn || null == ssn.getImapStore()) {
				return new RestResponse(false, ErrorHandler.FAILED_TO_CONNECT_TO_MAIL_SERVER, ErrorHandler.MAIL_SERVER_CONNECTION_ERROR);
			}

			boolean conenctionStatus = client.testEmailConnection(emailClientDetails.getImapServerUrl(), emailClientDetails.getEmailId(),
					emailClientDetails.getPassword());

			if (conenctionStatus) {
				emailClientDetails = emailClientDetailService.saveDetails(emailClientDetails);
			} else {
				return new RestResponse(false, ErrorHandler.FAILED_TO_CONNECT_TO_MAIL_SERVER, ErrorHandler.MAIL_SERVER_CONNECTION_ERROR);
			}

			// emailClientDetails =
			// emailClientDetailService.saveDetails(emailClientDetails);
			response = new RestResponse(true, emailClientDetails);
		} catch (AuthenticationFailedException afe) {
			response = new RestResponse(false, afe.getMessage(), ErrorHandler.AUTHENTICATION_FAILED);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_ADD_EMAIL_CLIENT_DETAILS, ErrorHandler.FAILED_TO_ADD_EMAIL_CLIENT);
		}
		return response;
	}

	// to set the imap and smtp server url and port depending upon the
	// client type
	public void fillImapAndSmtpDetails(EmailClientDetails emailClientDetails) {
		if (emailClientDetails.getEmailClientName().equalsIgnoreCase(EmailClientType.Gmail.getDisplayName())) {
			emailClientDetails.setImapServerPort(gmailImapPort);
			emailClientDetails.setImapServerUrl(gmailImapUrl);
			emailClientDetails.setSmtpServerPort(gmailSmtpPort);
			emailClientDetails.setSmtpServerUrl(gmailSmtpUrl);
		} else if (emailClientDetails.getEmailClientName().equalsIgnoreCase(EmailClientType.Yahoo.getDisplayName())) {
			emailClientDetails.setImapServerPort(yahooImapPort);
			emailClientDetails.setImapServerUrl(yahooImapUrl);
			emailClientDetails.setSmtpServerPort(yahooSmtpPort);
			emailClientDetails.setSmtpServerUrl(yahooSmtpUrl);
		} else if (emailClientDetails.getEmailClientName().equalsIgnoreCase(EmailClientType.Outlook.name())) {
			emailClientDetails.setImapServerPort(outlookImapPort);
			emailClientDetails.setImapServerUrl(outllookImapUrl);
			emailClientDetails.setSmtpServerPort(outlookSmtpPort);
			emailClientDetails.setSmtpServerUrl(outlookSmtpUrl);
		}
	}

	@RequestMapping(value = "/api/v1/user/email/client/details/edit/{id}", method = RequestMethod.PUT)
	public RestResponse updateEmailClientDetails(@PathVariable long id, @RequestBody EmailClientDetails emailClientDetails)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.EditEmailClient.name());*/

		RestResponse response = null;
		try {
			if (null != emailClientDetailService.findOne(id)) {
				emailClientDetails.setId(id);
			} else {
				response = new RestResponse(false, ErrorHandler.EMAIL_CLIENT_DETAILS_NOT_FOUND, ErrorHandler.FAILED_TO_UPDATE_EMAIL_CLIENT);
				return response;
			}
			fillImapAndSmtpDetails(emailClientDetails);
			emailClientDetails.setUser(userService.getLoggedInUserObject());
			emailClientDetails.setRecruizEmail(userService.getLoggedInUserEmail());

			// validating email account details here

			EmailClientSession ssn = client.getConnectedClient(emailClientDetails);

			if (null == ssn || null == ssn.getImapStore()) {
				return new RestResponse(false, ErrorHandler.FAILED_TO_CONNECT_TO_MAIL_SERVER, ErrorHandler.MAIL_SERVER_CONNECTION_ERROR);
			}
			boolean conenctionStatus = client.testEmailConnection(emailClientDetails.getImapServerUrl(), emailClientDetails.getEmailId(),
					emailClientDetails.getPassword());
			if (conenctionStatus) {
				emailClientDetails = emailClientDetailService.saveDetails(emailClientDetails);
			} else {
				return new RestResponse(false, ErrorHandler.FAILED_TO_CONNECT_TO_MAIL_SERVER, ErrorHandler.MAIL_SERVER_CONNECTION_ERROR);
			}

			response = new RestResponse(true, emailClientDetails);
		} catch (AuthenticationFailedException me) {
			logger.warn(me.getMessage(), me);
			response = new RestResponse(false, me.getMessage(), ErrorHandler.FAILED_TO_UPDATE_EMAIL_CLIENT);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_UPDATE_EMAIL_CLIENT_DETAILS,
					ErrorHandler.FAILED_TO_UPDATE_EMAIL_CLIENT);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/details/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteEmailClientDetails(@PathVariable long id) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteEmailClient.name());*/

		RestResponse response = null;
		try {
			if (null != emailClientDetailService.findOne(id)) {
				emailClientDetailService.delete(id);
			} else {
				response = new RestResponse(false, ErrorHandler.EMAIL_CLIENT_DETAILS_NOT_FOUND, ErrorHandler.FAILED_TO_DELETE_EMAIL_CLIENT);
				return response;
			}
			response = new RestResponse(true, SuccessHandler.EMAIL_CLIENT_DELETE_SUCCESS);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_EMAIL_CLIENT_DETAILS,
					ErrorHandler.FAILED_TO_DELETE_EMAIL_CLIENT);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/details/all", method = RequestMethod.GET)
	public RestResponse getAllEmailClientDetails() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllEmailClient.name());*/

		RestResponse response = null;
		try {
			response = new RestResponse(true, emailClientDetailService.getAllEmailDetailsForLoggedInUser());
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_GET_EMAIL_CLIENT_DETAILS, ErrorHandler.FAILED_TO_GET_EMAIL_CLIENT);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/mark/default/{id}", method = RequestMethod.PUT)
	public RestResponse markDefault(@PathVariable Long id) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.EmailAccountMarkedDeafult.name());*/

		RestResponse response = null;
		try {
			emailClientDetailService.markDefault(id);
			response = new RestResponse(true, SuccessHandler.CLIENT_MARKED_DEFAULT_SUCCESS);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_MARK_DEFAULT_EMAIL_CLIENT_DETAILS,
					ErrorHandler.FAILED_TO_MARK_DEFAULT_EMAIL_CLIENT);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/sync/{id}", method = RequestMethod.PUT)
	public RestResponse syncAccount(@PathVariable Long id, @RequestParam Long syncStartDate, @RequestParam Long syncEndDate)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.SyncEmailAccount.name());*/

		RestResponse response = null;
		try {
			emailClientDetailService.updateSyncInfo(id, new Date(syncStartDate), new Date(syncEndDate));
			response = new RestResponse(true, SuccessHandler.CLIENT_MARKED_DEFAULT_SUCCESS);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_CONFIGURE_SYNC_INFO, ErrorHandler.FAILED_TO_SYNC_UPDATE);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/sync/result/get", method = RequestMethod.GET)
	public RestResponse syncAccountResult() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.SyncEmailResult.name());*/

		RestResponse response = null;
		try {

			List<EmailClientDetails> allClientsForUser = emailClientDetailService.getAllEmailDetailsForLoggedInUser();

			if (null != allClientsForUser && !allClientsForUser.isEmpty()) {
				for (EmailClientDetails emailClientDetails : allClientsForUser) {
					Long failedCount = emailedFileItemService.findItemCountByEmailAndStatus(ResumeUploadFileStatus.FAILED.name(),
							emailClientDetails.getEmailId(), emailClientDetails.getRecruizEmail());
					Long successCount = emailedFileItemService.findItemCountByEmailAndStatus(ResumeUploadFileStatus.SUCCESS.name(),
							emailClientDetails.getEmailId(), emailClientDetails.getRecruizEmail());
					Long yetToProcessCount = emailedFileItemService.findItemCountByEmailAndStatus(
							ResumeBulkBatchUploadStatus.UPLOADING.getDisplayName(), emailClientDetails.getEmailId(),
							emailClientDetails.getRecruizEmail());
					emailClientDetails.setFailedCount(failedCount);
					emailClientDetails.setSuccessCount(successCount);
					emailClientDetails.setYetToProcessCount(yetToProcessCount);

					Long total = 0L;
					if (failedCount != null) {
						total = total + failedCount;
					}
					if (successCount != null) {
						total = total + successCount;
					}
					if (yetToProcessCount != null) {
						total = total + yetToProcessCount;
					}

					emailClientDetails.setTotalCount(total);

					if (emailClientDetails.getEmailFetchEndDate() == null) {
						emailClientDetails.setStatus("Not Configured");
					} else if (emailClientDetails.getLastFetchedEndDate() == null) {
						emailClientDetails.setStatus(ResumeBulkBatchUploadStatus.IN_PROGRESS.getDisplayName());
					} else if (emailClientDetails.getLastFetchedEndDate().before(emailClientDetails.getEmailFetchEndDate())) {
						emailClientDetails.setStatus(ResumeBulkBatchUploadStatus.IN_PROGRESS.getDisplayName());
					} else if (0 == emailClientDetails.getLastFetchedEndDate().compareTo(emailClientDetails.getEmailFetchEndDate())
							&& yetToProcessCount != 0) {
						emailClientDetails.setStatus(ResumeBulkBatchUploadStatus.IN_PROGRESS.getDisplayName());
					} else if (0 == emailClientDetails.getLastFetchedEndDate().compareTo(emailClientDetails.getEmailFetchEndDate())
							&& yetToProcessCount == 0) {
						emailClientDetails.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.getDisplayName());
					}
				}
			}
			response = new RestResponse(true, allClientsForUser);

		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(false, ErrorHandler.FAILED_TO_CONFIGURE_SYNC_INFO, ErrorHandler.FAILED_TO_SYNC_UPDATE);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/user/email/client/sync/stop/{id}", method = RequestMethod.PUT)
	public RestResponse cancelSyncAction(@PathVariable Long id) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.CancelEmailSync.name());*/

		try {
			EmailClientDetails clientDetails = emailClientDetailService.findOne(id);
			clientDetails.setEmailFetchEndDate(null);
			clientDetails.setEmailFetchEndDate(null);
			clientDetails.setLastFetchedEndDate(null);
			clientDetails.setLastFetchedStartDate(null);

			emailClientDetailService.save(clientDetails);
			return new RestResponse(true, ErrorHandler.SYNC_STOPPED);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			return new RestResponse(false, ErrorHandler.FAILED_TO_STOP_SYNC, ErrorHandler.FAILED_STOP_SYNC);
		}

	}

}
