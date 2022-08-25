package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class RegistrationService {

	private static Logger logger = LoggerFactory.getLogger(RegistrationService.class);

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private PasswordHashService passwordHashService;

	@Autowired
	private UserService userService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private DummyValueService addDummyValueService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${email.signup.subject}")
	private String signupSubject;

	@Value("${org.signup.notify:#{null}}")
	private String signupNotify;

	@Value("${org.signup.notify.email.list:#{null}}")
	private String signupNotifyList;

	final String signupTemplate = GlobalConstants.SIGNUP_EMAIL_TEMPLATE;

	final String signupNotifyTemplate = GlobalConstants.SIGNUP_NOTIFY_TEMPLATE;

	public void savePreSignUpUser(OrganizationUserDTO orgDTO) throws RecruizException {
		if (tenantResolverService.emailExist(orgDTO.getEmail()))
			throw new RecruizWarnException(ErrorHandler.ALREADY_SIGNED_UP, ErrorHandler.SIGN_UP_FAILED);

		User user = new User();
		user.setEmail(orgDTO.getEmail());
		boolean savedStatus = tenantResolverService.savePreSignUpUser(user);
		if (savedStatus) {
			user.setName(orgDTO.getOrgName());
			user.setPassword(passwordHashService.encodePassword(orgDTO.getPassword()));
			user.setMobile(orgDTO.getMobile());
			tenantResolverService.saveUserToTenantMgmtDatabase(user);
		} else
			throw new RecruizException(ErrorHandler.INDIVIDUAL_SIGN_UP_FAILED, ErrorHandler.SIGN_UP_FAILED);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void signUp(OrganizationUserDTO signUpRequestDTO, String orgType)
			throws RecruizException, InterruptedException {
		createOrgAndSendInvite(signUpRequestDTO, orgType);
	}

	@Transactional
	private void createOrgAndSendInvite(OrganizationUserDTO signUpRequestDTO, String orgType) throws RecruizException {
		organizationService.createOrganizationWithUser(signUpRequestDTO.getOrgID(), signUpRequestDTO.getOrgName(),
				signUpRequestDTO.getEmail(), signUpRequestDTO.getPassword(), orgType, signUpRequestDTO.getTimezone(),
				signUpRequestDTO.getLocale(), signUpRequestDTO.getMobile());

		logger.debug(String.format("Created org '%s' successfully", signUpRequestDTO.getOrgName()));

		sendSignupEmail(signUpRequestDTO);

		sendNewOrgSignupEmailToManagementTeam(signUpRequestDTO);
		
		logger.debug(String.format("Email for create org '%s' sent successfully", signUpRequestDTO.getOrgName()));
	}

	public void sendSignupEmail(OrganizationUserDTO signUpRequestDTO) throws RecruizException {
		List<String> emailList = new ArrayList<String>();
		emailList.add(signUpRequestDTO.getEmail());

		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(signUpRequestDTO.getEmail(),
				signUpRequestDTO.getOrgID(), WebMode.DASHBOARD, 48, signUpRequestDTO.getTimezone(),
				signUpRequestDTO.getLocale());

		logger.debug(String.format("AuthToken for org '%s' admin user created successfully with authtoken %s",
				signUpRequestDTO.getOrgName(), xauthToken));

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, signUpRequestDTO.getOrgName());
		emailBody.put(GlobalConstants.ACTIVATION_LINK,
				baseUrl + GlobalConstants.ACTIVATION_URL + GlobalConstants.PASSKEY + xauthToken);

		logger.debug(String.format("Email to be sent to org '%s' admin and email list is %s",
				signUpRequestDTO.getOrgName(), emailList));

		emailBody.put(GlobalConstants.USER_NAME, signUpRequestDTO.getOrgName());
		emailBody.put(GlobalConstants.ORG_NAME, signUpRequestDTO.getOrgName());
		emailBody.put(GlobalConstants.USER_EMAIL, signUpRequestDTO.getEmail());
		emailBody.put(GlobalConstants.FIRST_PASSWORD, GlobalConstants.DEFAULT_PASSWORD);

		TenantContextHolder.setTenant(signUpRequestDTO.getOrgID());

		emailService.sendEmail(signupTemplate, emailList, signupSubject, emailBody);
		// emailService.sendEmail(emailList, emailBody, signupSubject,
		// signupTemplate);
	}

	public void sendNewOrgSignupEmailToManagementTeam(OrganizationUserDTO signUpRequestDTO) throws RecruizException {

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.ORG_NAME, signUpRequestDTO.getOrgName());
		emailBody.put(GlobalConstants.USER_EMAIL, signUpRequestDTO.getEmail());
		emailBody.put(GlobalConstants.USER_MOBILE, signUpRequestDTO.getMobile());

		if (signupNotify != null && signupNotify.trim().equalsIgnoreCase("true") && signupNotifyList != null) {
			List<String> emailList = Arrays.asList(signupNotifyList.split("\\s*,\\s*"));
			emailService.sendEmail(signupNotifyTemplate, emailList, "CONGRATS !! New recruiz sign up", emailBody);
		}

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void signUpSocialUser(OrganizationUserDTO signUpRequestDTO, String orgType) throws RecruizException {
		createOrgWithSocialUser(signUpRequestDTO, orgType);
	}

	@Transactional
	private void createOrgWithSocialUser(OrganizationUserDTO signUpRequestDTO, String orgType) throws RecruizException {
		organizationService.createOrganizationWithSocialUser(signUpRequestDTO.getOrgID(), signUpRequestDTO.getOrgName(),
				signUpRequestDTO.getEmail(), orgType);
	}

	public User invitedSignup(OrganizationUserDTO invitedUserDTO) throws RecruizException {
		TenantContextHolder.setTenant(invitedUserDTO.getOrgID());
		return checkAndUpdateInvitedUser(invitedUserDTO);

	}

	@Transactional
	private User checkAndUpdateInvitedUser(OrganizationUserDTO invitedUserDTO) throws RecruizException {

		if (tenantResolverService.findByEmailAndOrgID(invitedUserDTO.getEmail(), invitedUserDTO.getOrgID()) != null)
			throw new RecruizWarnException(ErrorHandler.USER_ALREADY_REGISTERED, ErrorHandler.ALREADY_REGISTERED);

		User user = userService.getUserByEmail(invitedUserDTO.getEmail());
		if (user != null) {
			int index = invitedUserDTO.getEmail().indexOf("@");
			String name = invitedUserDTO.getEmail().substring(0, index);

			user.setJoinedStatus(true);
			user.setName(name);
			user.setPassword(passwordHashService.encodePassword(invitedUserDTO.getPassword()));
			user.setTimezone(invitedUserDTO.getTimezone());
			user.setLocale(invitedUserDTO.getLocale());
			userService.save(user);
			tenantResolverService.saveTenantResolverForUser(user);
		} else {
			throw new RecruizException(ErrorHandler.USER_NOT_FOUND, ErrorHandler.USER_NOT_FOUND);
		}
		return user;
	}
}
