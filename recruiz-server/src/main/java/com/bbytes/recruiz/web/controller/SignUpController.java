package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.validator.EmailValidator;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.bbytes.recruiz.auth.jwt.AuthUserDetailsService;
import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.database.TenantDBService;
import com.bbytes.recruiz.database.UpdateDBTemplateService;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.TenantResolverRepository;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.DummyValueService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.RecruizPlutusClientService;
import com.bbytes.recruiz.service.RegistrationService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.MultiTenantUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ValidateEmailDomain;

@RestController
public class SignUpController {

    private static Logger logger = LoggerFactory.getLogger(SignUpController.class);

    @Autowired
    private TenantResolverService tenantResolverService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private SpringProfileService profileService;

    @Autowired
    protected TokenAuthenticationProvider tokenAuthenticationProvider;

    @Autowired
    private AuthUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private TenantDBService tenantDbService;

    @Autowired
    private DataModelToDTOConversionService dataModelToDTOConversionService;

    @Autowired
    private TenantResolverRepository tenantResolverRepository;

    @Autowired
    private RecruizPlutusClientService recruizPlutusClientService;

    @Autowired
    private DummyValueService addDummyValueService;

    @Autowired
    private UpdateDBTemplateService updateDBTemplateService;

    @Autowired
    private SpringProfileService springProfileService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${email.inviteToJoin.subject}")
    private String signupSubject;

    private final ProviderSignInUtils signInUtils;

    @Autowired
    public SignUpController(ConnectionFactoryLocator connectionFactoryLocator,
	    UsersConnectionRepository connectionRepository) {
	signInUtils = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);
    }

    @RequestMapping(value = "/auth/signup", method = RequestMethod.POST)
    public RestResponse signUp(@RequestBody OrganizationUserDTO signUpRequestDTO, HttpServletResponse response)
	    throws RecruizException, InterruptedException, IOException, PlutusClientException {

    	
    logger.error("Step 1 ..............Sign up for "+signUpRequestDTO.getEmail());	
	String mode = signUpRequestDTO.getSignUpMode().trim();
	signUpRequestDTO.setSignUpMode(mode);

	// as discussed removing null heck for mobile number, if it is nulll it
	// will be processed. if it is not null then it will be verified in
	// existing record

	// disabling this check as we have decided the check it at UI only
	/*
	 * if (signUpRequestDTO.getMobile() != null &&
	 * !signUpRequestDTO.getMobile().isEmpty() &&
	 * tenantResolverService.isMobileExists(signUpRequestDTO.getMobile())) {
	 * return new RestResponse(RestResponse.FAILED,
	 * "Provided mobile number exists", "number_exists"); }
	 */

	// making email in lower case
	String email = signUpRequestDTO.getEmail().toLowerCase();
	signUpRequestDTO.setEmail(email);
	checkOrgAndUserExist(signUpRequestDTO.getOrgID(), signUpRequestDTO.getEmail());
	checkHybridModeOrEnterpriseModeAndTenantCount(signUpRequestDTO.getSignUpMode());

	logger.error("Step 2 ..............Sign up for "+signUpRequestDTO.getEmail());
	
	String orgID = createOrgId(signUpRequestDTO.getOrgName());
	signUpRequestDTO.setOrgID(orgID);

	logger.error("Step 3 ..............Sign up for "+signUpRequestDTO.getEmail());
	
	validateEmail(signUpRequestDTO);

	logger.error("Step 4 ..............Sign up for "+signUpRequestDTO.getEmail());
	
	createDBSchema(signUpRequestDTO.getOrgID(), signUpRequestDTO.getSignUpMode());

	
	logger.error("Step 5 ..............Sign up for "+signUpRequestDTO.getEmail());
	
	if (signUpRequestDTO.getSignUpMode() != null
		&& signUpRequestDTO.getSignUpMode().equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_AGENCY)) {

		logger.error("Step 6 ..............Sign up for "+signUpRequestDTO.getEmail());
		
	    if (tenantResolverService.organizationExist(orgID))
		throw new RecruizException(ErrorHandler.ORG_NOT_UNIQUE, ErrorHandler.INVALID_REQUEST);

	    if (profileService.isEnterpriseMode() && tenantResolverService.organizationCount() > 0) {
		logger.error("User trying to create more than one organization in enterprise mode");
		// if org already exist in enterprise mode then jus sign up the
		// user as SIGNUP_MODE_INDIVIDUAL and ignore the org value sent
		// from ui
		registrationService.savePreSignUpUser(signUpRequestDTO);
	    } else {
		registrationService.signUp(signUpRequestDTO, GlobalConstants.SIGNUP_MODE_AGENCY);

		// this will make a rest template call to plutus server and
		// update the info on plutus and recruiz database
		registerOnPlutus(signUpRequestDTO);

		// TODO
		// add dummy values here

	    }
	} else if (signUpRequestDTO.getSignUpMode() != null
		&& signUpRequestDTO.getSignUpMode().equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_CORPORATE)) {

		
		logger.error("Step 7 ..............Sign up for "+signUpRequestDTO.getEmail());
		
	    if (tenantResolverService.organizationExist(orgID))
		throw new RecruizException(ErrorHandler.ORG_NOT_UNIQUE, ErrorHandler.INVALID_REQUEST);

	    if (profileService.isEnterpriseMode() && tenantResolverService.organizationCount() > 0) {
		logger.error("User trying to create more than one organization in enterprise mode");
		// if org already exist in enterprise mode then jus sign up the
		// user as SIGNUP_MODE_INDIVIDUAL and ignore the org value sent
		// from ui
		registrationService.savePreSignUpUser(signUpRequestDTO);
	    } else {
		registrationService.signUp(signUpRequestDTO, GlobalConstants.SIGNUP_MODE_CORPORATE);
		// this will make a rest template call to plutus server and
		// update the info on plutus and recruiz database
		registerOnPlutus(signUpRequestDTO);
	    }

	} else if (signUpRequestDTO.getSignUpMode() != null
		&& signUpRequestDTO.getSignUpMode().equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_INVITED)) {

		
		logger.error("Step 8 ..............Sign up for "+signUpRequestDTO.getEmail());
		
	    registrationService.invitedSignup(signUpRequestDTO);

	    User authenticatedUser = userDetailsService.loadUserByUsername(signUpRequestDTO.getEmail());
	    com.bbytes.recruiz.domain.User user = userService.getUserByEmail(signUpRequestDTO.getEmail());
	    final MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(
		    signUpRequestDTO.getOrgID(), authenticatedUser, WebMode.DASHBOARD, WebRequestMode.WEB_APP,
		    DateTimeZone.forID(user.getTimezone()), new Locale(user.getLocale()));
	    tokenAuthenticationProvider.addAuthentication(response, userAuthentication);
	    // Add the authentication to the Security context
	    SecurityContextHolder.getContext().setAuthentication(userAuthentication);

	    /**
	     * Getting user details to return after successful authentication
	     */
	    UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);
	    /*
	     * getting tenant list for the user to add in the response
	     */
	    Map<String, String> userTenantMap = tenantResolverService
		    .getOrgIdToOrgNameMap(tenantResolverService.findAllTenantsForUserId(userDTO.getEmail()));
	    /*
	     * Writing user information to response after successful
	     * authentication
	     */
	    Map<String, Object> responseMap = new HashMap<String, Object>();
	    responseMap.put("userDetails", userDTO);
	    responseMap.put("userTenantList", userTenantMap);

	    RestResponse authStatus = new RestResponse(true, responseMap);
	    return authStatus;
	} else if (signUpRequestDTO.getSignUpMode() != null
		&& signUpRequestDTO.getSignUpMode().equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_VENDOR)) {

		logger.error("Step 9 ..............Sign up for "+signUpRequestDTO.getEmail());
		
	    registrationService.invitedSignup(signUpRequestDTO);

	    User authenticatedUser = userDetailsService.loadUserByUsername(signUpRequestDTO.getEmail());
	    com.bbytes.recruiz.domain.User user = userService.getUserByEmail(signUpRequestDTO.getEmail());
	    final MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(
		    signUpRequestDTO.getOrgID(), authenticatedUser, WebMode.DASHBOARD, WebRequestMode.WEB_APP,
		    DateTimeZone.forID(user.getTimezone()), new Locale(user.getLocale()));
	    tokenAuthenticationProvider.addAuthentication(response, userAuthentication);
	    // Add the authentication to the Security context
	    SecurityContextHolder.getContext().setAuthentication(userAuthentication);

	    /**
	     * Getting user details to return after successful authentication
	     */
	    UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);

	    /*
	     * getting tenant list for the user to add in the response
	     */
	    Map<String, String> userTenantMap = tenantResolverService
		    .getOrgIdToOrgNameMap(tenantResolverService.findAllTenantsForUserId(userDTO.getEmail()));
	    /*
	     * Writing user information to response after successful
	     * authentication
	     */
	    Map<String, Object> responseMap = new HashMap<String, Object>();
	    responseMap.put("userDetails", userDTO);
	    responseMap.put("userTenantList", userTenantMap);

	    RestResponse authStatus = new RestResponse(true, responseMap);
	    // ObjectMapper mapper = new ObjectMapper();
	    // String responseObject = mapper.writeValueAsString(authStatus);

	    return authStatus;
	} else {
		
		logger.error("Step 10 ..............Sign up for "+signUpRequestDTO.getEmail());
		
	    throw new RecruizException(ErrorHandler.INVALID_REQUEST_TO_SERVER, ErrorHandler.INVALID_REQUEST);
	}

	logger.debug("Org created sucessfully and about to send response to user ");
	RestResponse signUpResponse = new RestResponse(RestResponse.SUCCESS, signUpRequestDTO,
		SuccessHandler.SIGN_UP_SUCCESS);
	logger.debug("Org created sucessfully with response " + signUpResponse);
	
	logger.error("Step 11 ..............Sign up for "+signUpRequestDTO.getEmail());
	
	return signUpResponse;
    }

    private void registerOnPlutus(OrganizationUserDTO signUpRequestDTO) throws PlutusClientException {
	TenantContextHolder.setTenant(signUpRequestDTO.getOrgID());

	updateOrganizationMap(signUpRequestDTO);
    }

    @Transactional
    public void updateOrganizationMap(OrganizationUserDTO signUpRequestDTO) throws PlutusClientException {

	Organization org = organizationService.findByOrgId(signUpRequestDTO.getOrgID());
	org.setOrgName(signUpRequestDTO.getOrgName());
	org.setOrganizationEmail(signUpRequestDTO.getEmail());
	org.setOrgId(signUpRequestDTO.getOrgID());

	org.setRegisteredMobile(signUpRequestDTO.getMobile());

	recruizPlutusClientService.registerOnPlutus(org, signUpRequestDTO.getRecruizPlanId());
    }

    private void validateEmail(OrganizationUserDTO signUpRequestDTO) throws RecruizException {
	if (!(EmailValidator.getInstance().isValid(signUpRequestDTO.getEmail()))) {
	    throw new RecruizException("Email Id '" + signUpRequestDTO.getEmail() + "' not valid", "email_invalid");
	}

	if (ValidateEmailDomain.isEmailDomainNotValid(signUpRequestDTO.getEmail())) {
	    logger.error("Email domain is not valid , email : " + signUpRequestDTO.getEmail());
	    throw new RecruizException(ErrorHandler.DISPOSABLE_EMAIL_DOMAIN, ErrorHandler.INVALID_DOMAIN);
	}
    }

    private void checkOrgAndUserExist(String orgId, String email) throws RecruizException {
	// checking whether user exists with same email and organization or not.
	// If exists it will throw exception
	if (tenantResolverService.userExistsForOrg(email, orgId)) {
	    throw new RecruizWarnException(ErrorHandler.USER_EXISTS, ErrorHandler.EMAIL_NOT_UNIQUE);
	}
    }

    /**
     * In hybrid mode or enterprise mode not more then one org allowed
     * 
     * @throws RecruizException
     */
    private void checkHybridModeOrEnterpriseModeAndTenantCount(String signupMode) throws RecruizException {
	if ((springProfileService.isHybridMode() || springProfileService.isEnterpriseMode())
		&& tenantResolverService.organizationCount() >= 1
		&& (signupMode.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_CORPORATE)
			|| signupMode.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_AGENCY)))
	    throw new RecruizException(ErrorHandler.ONLY_ONE_ACCN_ALLOWED, ErrorHandler.MULTIPLE_ORG_ERROR);
    }

    private String createOrgId(String orgName) throws RecruizException {
	if (orgName == null)
	    throw new RecruizWarnException("Organization name missing", ErrorHandler.ORG_REGISTRATION_FAILED);

	String orgID = orgName.trim().replace(" ", "_");
	orgID = orgID.replace("-", "_");
	orgID = orgID.replace(".", "_");
	orgID = orgID.replaceAll("[^a-zA-Z0-9]", "_");
	return orgID;
    }

    @RequestMapping(value = "/api/v1/org/info", method = RequestMethod.POST)
    public RestResponse updateOrganization(@RequestPart("json") @Valid OrganizationUserDTO orgDTO,
	    @RequestPart(value = "file", required = false) MultipartFile file) throws RecruizException {

	/*if (orgDTO.getOrgName() == null || orgDTO.getOrgName().isEmpty()) {
	    return new RestResponse(false, "Organization Name Can not be empty !", "org_name_null");
	}*/

	try {
	    organizationService.updateOrganization(TenantContextHolder.getTenant(), orgDTO, file);
	    // updating organization name in tenant mgmt db here
	    tenantResolverService.changeOrgName(TenantContextHolder.getTenant(), orgDTO.getOrgName());
	} catch (IOException e) {
	    new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
	}

	Organization org = organizationService.findByOrgId(TenantContextHolder.getTenant());
	if (org.getLogoUrlPath() != null && !org.getLogoUrlPath().isEmpty()) {
	    String url = baseUrl + "/pubset/" + org.getLogoUrlPath();
	    org.setOrgLogoUrl(url);
	}
	RestResponse signUpResponse = new RestResponse(RestResponse.SUCCESS, org,
		SuccessHandler.ORGANIZATION_NAME_UPDATE);
	return signUpResponse;
    }

    @RequestMapping(value = "/auth/social/signup", method = { RequestMethod.POST, RequestMethod.GET })
    public RedirectView socialSignup(RedirectAttributes redirectAttributes, WebRequest request,
	    HttpServletResponse response) throws RecruizException, IOException {
	String orgIdOrTenantId = MultiTenantUtils.getTenantId(request);
	String orgName = MultiTenantUtils.getTenantName(request);
	String inviteEmailId = MultiTenantUtils.getInviteEmailId(request);
	String signUpMode = MultiTenantUtils.getSignUpMode(request);

	Connection<?> connection = signInUtils.getConnectionFromSession(request);
	if (signUpMode == null)
	    throw new RecruizException(
		    String.format("User with email id '%s'not registered", connection.fetchUserProfile().getEmail()),
		    ErrorHandler.USER_NOT_FOUND);

	OrganizationUserDTO orgUser = new OrganizationUserDTO();
	orgUser.setEmail(connection.fetchUserProfile().getEmail());
	orgUser.setOrgID(orgIdOrTenantId);
	orgUser.setOrgName(orgName);
	orgUser.setSignUpMode(signUpMode);

	logger.info("saving social user connection to tenant management db ");
	signInUtils.doPostSignUp(connection.fetchUserProfile().getEmail(), request);

	if (connection != null) {
	    if (inviteEmailId != null && !inviteEmailId.equals(connection.fetchUserProfile().getEmail())
		    && signUpMode.contains(GlobalConstants.SIGNUP_MODE_INVITED)) {
		logger.warn(String.format("Invite email id '%s' doesnt match with signin email id '%s'", inviteEmailId,
			connection.fetchUserProfile().getEmail()));
		redirectAttributes.addAttribute(GlobalConstants.ERROR_REASON,
			ErrorHandler.SIGNUP_INVITE_EMAIL_MISMATCH);
		return new RedirectView(GlobalConstants.SOCIAL_SIGNUP_ERROR_URL);
	    }
	}

	if (orgIdOrTenantId != null) {
	    createDBSchema(orgIdOrTenantId, orgUser.getSignUpMode());
	}

	if (signUpMode.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_INDIVIDUAL)) {
	    registrationService.savePreSignUpUser(orgUser);
	} else if (signUpMode.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_FREELANCER)) {

	    if (orgIdOrTenantId == null)
		throw new RecruizException("User name is missing", ErrorHandler.INVALID_REQUEST);

	    if (tenantResolverService.organizationExist(orgIdOrTenantId))
		throw new RecruizException(ErrorHandler.INVALID_REQUEST, ErrorHandler.FREELANCER_EXISTS);

	    registrationService.signUpSocialUser(orgUser, GlobalConstants.SIGNUP_MODE_FREELANCER);

	} else if (signUpMode.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_ORGANIZATION)) {

	    if (orgIdOrTenantId == null)
		throw new RecruizException("Organization information missing", ErrorHandler.INVALID_REQUEST);

	    if (tenantResolverService.organizationExist(orgIdOrTenantId))
		throw new RecruizException(ErrorHandler.ORG_NOT_UNIQUE, ErrorHandler.INVALID_REQUEST);

	    registrationService.signUpSocialUser(orgUser, GlobalConstants.SIGNUP_MODE_ORGANIZATION);
	}

	if (TenantContextHolder.getTenant() != null) {
	    logger.info("saving social user connection to tenant db : " + TenantContextHolder.getTenant());
	    signInUtils.doPostSignUp(connection.fetchUserProfile().getEmail(), request);
	}

	User user = userDetailsService.loadUserBySocialConnect(connection);
	tokenAuthenticationProvider.addAuthentication(response, user, orgIdOrTenantId, WebMode.DASHBOARD);
	String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(user, orgIdOrTenantId, WebMode.DASHBOARD,
		1);

	// remove attributes in request after signup
	MultiTenantUtils.clearTenantIdInRequest(request);
	MultiTenantUtils.clearInviteEmailIdInRequest(request);
	MultiTenantUtils.clearSignUpModeInRequest(request);

	redirectAttributes.addAttribute(GlobalConstants.SOCIAL_PASSKEY, xauthToken);
	return new RedirectView(GlobalConstants.POST_SIGNUP_SUCCESS_URL);

    }

    private void createDBSchema(String orgID, String orgType) throws RecruizException {
	try {
	    // create new db for new org and then set the new org as current
	    // using flyway script
	    tenantDbService.createOrUpdateDBSchema(orgID);
	    TenantContextHolder.setTenant(orgID);

	    // updating templates form folder
	    if (orgType != null && !orgType.isEmpty() && (orgType.equalsIgnoreCase(GlobalConstants.ORG_TYPE_AGENCY)
		    || orgType.equalsIgnoreCase(GlobalConstants.SIGNUP_MODE_CORPORATE))) {
		updateDBTemplateService.updateDatabaseTemplateFromFile(orgID, orgType);
		tenantUsageStatService.createUsageTableForTenant(orgID);
	    }

	} catch (SQLException e) {
	    throw new RecruizException(e.getMessage(), e);
	} catch (Exception e) {
	    throw new RecruizException(e.getMessage(), e);
	}
    }

    public RedirectView redirectWithUsingRedirectView(RedirectAttributes redirectAttributes) {
	redirectAttributes.addAttribute("attribute", "redirectWithRedirectView");
	return new RedirectView("redirectedUrl");
    }

    @RequestMapping(value = "/auth/signup/validate/{email:.+}", method = RequestMethod.GET)
    public RestResponse validateUserBeforSignUp(@PathVariable("email") String email) throws RecruizException {
	if (tenantResolverService.emailExist(email))
	    throw new RecruizWarnException(ErrorHandler.USER_EXISTS, ErrorHandler.EMAIL_NOT_UNIQUE);

	RestResponse signUpResponse = new RestResponse(RestResponse.SUCCESS, ErrorHandler.USER_IS_UNIQUE, null);
	return signUpResponse;

    }

    @RequestMapping(value = "/auth/signup/resend", method = RequestMethod.POST)
    public RestResponse resendSignupEmail(@RequestParam String orgName, @RequestParam String orgID,
	    @RequestParam String email) throws RecruizException, InterruptedException, IOException {

	if (orgName == null || orgName.isEmpty() || orgID == null || orgID.isEmpty() || email == null
		|| email.isEmpty())
	    return null;
	TenantContextHolder.setTenant(orgID);
	OrganizationUserDTO signUpRequestDTO = new OrganizationUserDTO();
	signUpRequestDTO.setEmail(email);
	signUpRequestDTO.setOrgID(orgID);
	signUpRequestDTO.setOrgName(orgName);

	registrationService.sendSignupEmail(signUpRequestDTO);

	RestResponse signUpResponse = new RestResponse(RestResponse.SUCCESS, signUpRequestDTO,
		SuccessHandler.SIGN_UP_SUCCESS);
	logger.debug("Email resend \n " + signUpResponse);
	return signUpResponse;
    }

    /**
     * to check if the mobile number is already registered
     * 
     * @param mobileNumber
     * @return
     * @throws RecruizException
     * @throws InterruptedException
     * @throws IOException
     */
    @RequestMapping(value = "/auth/signup/mobile/exists", method = RequestMethod.GET)
    public RestResponse resendSignupEmail(@RequestParam String mobileNumber)
	    throws RecruizException, InterruptedException, IOException {

	RestResponse checkMobileNumberResponse = null;
	try {
	    checkMobileNumberResponse = new RestResponse(RestResponse.SUCCESS,
		    tenantResolverService.isMobileExists(mobileNumber));
	} catch (Exception e) {
	    checkMobileNumberResponse = new RestResponse(RestResponse.FAILED, "Couldn't check", "no_response");
	}
	return checkMobileNumberResponse;
    }

    @RequestMapping(value = "/api/v1/org/address", method = RequestMethod.PUT)
    public RestResponse updateOrganizationAddress(@RequestBody OrganizationUserDTO orgDTO)
	    throws RecruizException, IOException {
	organizationService.updateOrganizationAddress(TenantContextHolder.getTenant(), orgDTO);
	Organization org = organizationService.findByOrgId(TenantContextHolder.getTenant());
	RestResponse organizationAddressUpdateResponse = new RestResponse(RestResponse.SUCCESS, org,
		SuccessHandler.ORGANIZATION_ADDRESS_UPDATE);
	return organizationAddressUpdateResponse;

    }

    @RequestMapping(value = "/auth/enterprise/mode", method = RequestMethod.GET)
    public RestResponse isEnterpriseMode() {
	RestResponse response = new RestResponse(RestResponse.SUCCESS, springProfileService.isEnterpriseMode());
	return response;
    }

    @RequestMapping(value = "/auth/hybrid/mode", method = RequestMethod.GET)
    public RestResponse isHybridMode() {
	RestResponse response = new RestResponse(RestResponse.SUCCESS, springProfileService.isHybridMode());
	return response;
    }

    @RequestMapping(value = "/auth/saas/mode", method = RequestMethod.GET)
    public RestResponse isSaasMode() {
	RestResponse response = new RestResponse(RestResponse.SUCCESS, springProfileService.isSaasMode());
	return response;
    }

    @RequestMapping(value = "/auth/enterpriseOrHybrid/mode", method = RequestMethod.GET)
    public RestResponse isEnterpriseOrHybridMode() {
	RestResponse response = new RestResponse(RestResponse.SUCCESS,
		(springProfileService.isHybridMode() || springProfileService.isEnterpriseMode()));
	return response;
    }
}
