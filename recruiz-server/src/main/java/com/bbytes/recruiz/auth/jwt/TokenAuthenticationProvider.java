package com.bbytes.recruiz.auth.jwt;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceResolver;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.mobile.device.LiteDeviceResolver;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.ApplicationConstant;
import com.bbytes.recruiz.auth.storage.RecruizSecuritySessionTokenStore;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.DeviceType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.jwt.ExternalAppJWTAuthTokenData;
import com.bbytes.recruiz.jwt.IExternalAppJWTAuthClientSecretRepository;
import com.bbytes.recruiz.jwt.IExternalAppJWTAuthTokenParser;
import com.bbytes.recruiz.jwt.impl.ExternalAppJWTAuthTokenParserImpl;
import com.bbytes.recruiz.repository.OrganizationRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.repository.UserRolesRepository;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.web.config.WebMvcConfig;

import io.jsonwebtoken.JwtException;

@Component
public class TokenAuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

	@Value("${token.handler.secret}")
	private String secret;

	@Value("${api.token.handler.secret}")
	private String apiSecret;
	
	@Value("${jwt.login.token.validity.hrs:24}")
	private int tokenValidity;

	@Autowired
	private UserDetailsService authUserDetailsService;

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private RecruizSecuritySessionTokenStore recruizSecuritySessionTokenStore;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private IExternalAppJWTAuthClientSecretRepository externalAppJWTAuthClientSecretRepository;

	private TokenHandler tokenHandler;

	private ExternalUserAccessDataHolder externalUserAccessDataHolder;

	private RequestMatcher logoutRequestMatcher;

	private RequestMatcher updateTokenRequestMatcher;

	private RequestMatcher resetPasswordRequestMatcher;

	private RequestMatcher activateAccountRequestMatcher;

	private DeviceResolver deviceResolver;

	private IExternalAppJWTAuthTokenParser externalAppJWTAuthTokenParser;

	@PostConstruct
	public void setupTokenHandler() {
		tokenHandler = new TokenHandler(secret, apiSecret, springProfileService.isSaasMode(),tokenValidity);
		updateTokenRequestMatcher = new AntPathRequestMatcher(GlobalConstants.UPDATE_TOKEN__API_URL);
		resetPasswordRequestMatcher = new AntPathRequestMatcher(GlobalConstants.RESET_PASSOWRD_API_URL);
		logoutRequestMatcher = new AntPathRequestMatcher(GlobalConstants.LOGOUT_URL);
		activateAccountRequestMatcher = new AntPathRequestMatcher(GlobalConstants.ACTIVATE_ACCOUNT_URL);
		deviceResolver = new LiteDeviceResolver();
		externalAppJWTAuthTokenParser = new ExternalAppJWTAuthTokenParserImpl(externalAppJWTAuthClientSecretRepository);

	}

	/**
	 * The method is called after successful login to add the jwt token header
	 * 
	 * @param response
	 * @param authentication
	 * @return
	 */
	public String addAuthentication(HttpServletResponse response, MultiTenantAuthenticationToken authentication) {
		final UserDetails user = authentication.getDetails();
		final String userTenantId = authentication.getTenantId();
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, authentication.getWebMode(),
				authentication.getWebRequestMode(), authentication.getUserTimeZone(), authentication.getUserLocale());
		String jwtStringToken = tokenHandler.createJWTStringTokenForUser(tokenDataHolder);
		logger.debug("Auth token for user :-  " + user.getUsername());
		logger.debug("Auth token :-  " + jwtStringToken);
		response.addHeader(GlobalConstants.HEADER_AUTH_TOKEN, jwtStringToken);
		response.addHeader(ApplicationConstant.HEADER_AUTH_TOKEN, jwtStringToken);
		return jwtStringToken;
	}

	/**
	 * The method is called after successful login to add the jwt token header
	 * 
	 * @param response
	 * @param authentication
	 * @return
	 */
	public String addAuthentication(HttpServletResponse response, User user, String tenantId, WebMode webMode) {
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, tenantId, webMode);
		String jwtStringToken = tokenHandler.createJWTStringTokenForUser(tokenDataHolder);
		logger.debug("Auth token for user :-  " + user.getUsername());
		logger.debug("Auth token :-  " + jwtStringToken);
		response.addHeader(GlobalConstants.HEADER_AUTH_TOKEN, jwtStringToken);
		response.addHeader(ApplicationConstant.HEADER_AUTH_TOKEN, jwtStringToken);
		return jwtStringToken;
	}

	/**
	 * Method called from filter to verify every request in the app for jwt auth
	 * token or api token
	 * 
	 * @param request
	 * @return
	 */
	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationServiceException {

		String jwtWebAuthToken = request.getHeader(GlobalConstants.HEADER_AUTH_TOKEN);

		// sometimes the auth token is sent as part of request url param using
		// GlobalConstants.URL_AUTH_TOKEN as param name so we need ot parse
		if (jwtWebAuthToken == null) {
			jwtWebAuthToken = getAuthTokenFromURL(request);
		}

		// can be a api call and not a call from web or mobile app so
		// X-AUTH-TOKEN might be missing and might have X-API-TOKEN
		String jwtAPIToken = request.getHeader(GlobalConstants.HEADER_API_TOKEN);

		if (jwtAPIToken == null)
			jwtAPIToken = request.getHeader(GlobalConstants.HEADER_ORG_API_TOKEN);

		// extAccInfoToken contains the access info if the link is clicked by
		// external user else we will not have this access token part of the url
		// it contain three parts . 1. email 2.name 3. mobile_no
		String extAccInfoToken = request.getHeader(GlobalConstants.HEADER_EXT_ACC_INFO_TOKEN);

		TokenDataHolder tokenDataHolder = null;
		MultiTenantAuthenticationToken multiTenantAuthenticationToken = null;

		if (jwtWebAuthToken != null || jwtAPIToken != null) {

			if (jwtWebAuthToken != null) {
				tokenDataHolder = tokenHandler.parseJWTStringTokenForUser(jwtWebAuthToken);
				// only one session per user allowed check , we do in web
				// version only
				singleSessionLoginCheck(request, tokenDataHolder, jwtWebAuthToken);
			} else {
				tokenDataHolder = tokenHandler.parseAPITokenForUser(jwtAPIToken);
			}
		} else if (externalAppJWTAuthTokenParser.hasValidHeaders(request)) {
			// third party app request comes here like career site , plutus etc 
			ExternalAppJWTAuthTokenData externalAppJWTAuthTokenData = externalAppJWTAuthTokenParser.parseToken(request);

			UserDetails user = null;
			try {
				user = authUserDetailsService.loadUserByUsername(externalAppJWTAuthTokenData.getEmail());
			} catch (Exception e) {
				// do nothing if user is not there as it can be third party app
				// call not specific to any user , might be general call like
				// department sync or app user sync
			}

//			tokenDataHolder = new TokenDataHolder(user, externalAppJWTAuthTokenData.getClientId(), WebMode.STANDALONE,
//					WebRequestMode.API);
			tokenDataHolder = new TokenDataHolder(user, externalAppJWTAuthTokenData.getClientId(), WebMode.EXTERNAL_APP,
					WebRequestMode.API);
		}

		if (tokenDataHolder != null) {

			multiTenantAuthenticationToken = new MultiTenantAuthenticationToken(tokenDataHolder.getTenantId(),
					tokenDataHolder.getUser(), tokenDataHolder.getWebMode(), tokenDataHolder.getWebRequestMode(),
					tokenDataHolder.getUserTimeZone(), tokenDataHolder.getUserLocale());

			if (extAccInfoToken != null) {

				String decryptedExternaluserInfo = EncryptKeyUtils.getDecryptedKey(extAccInfoToken);

				if (tokenDataHolder != null && decryptedExternaluserInfo != null) {
					List<String> externalUserData = com.bbytes.recruiz.utils.StringUtils
							.commaSeparateStringToList(decryptedExternaluserInfo);
					ExternalUserAccessDataHolder externalUserAccessDataHolder = new ExternalUserAccessDataHolder();
					externalUserAccessDataHolder.setExtenalUserEmail(externalUserData.get(0));
					externalUserAccessDataHolder.setExtenalUserName(externalUserData.get(1));
					// this check because mobile number for external user is
					// not mandatory
					if (externalUserData.size() > 2)
						externalUserAccessDataHolder.setExtenalUserMobile(externalUserData.get(2));

					multiTenantAuthenticationToken.setExternalUserAccessDataHolder(externalUserAccessDataHolder);
				}
			}

			return multiTenantAuthenticationToken;
		}

		throw new AuthenticationServiceException("Auth token header missing");
	}

	/**
	 * At any point of time we allow only one login and since jwt token is
	 * stateless we store that in redis cache and check for single session for a
	 * user at any point of time
	 * 
	 * Notes : 1. The key should be the combination of Email + Tenant Id +
	 * Web/Mobile mode. 2. The use case is , at once we have to restrict user to
	 * login only from one browser/machine. 3. The user should also be allowed
	 * to login from web and mobile at the same time. 4. We also need to check
	 * if the token is valid or not, in case if the token is expired we need to
	 * store new token in cache.
	 * 
	 * {@link DeviceResolverHandlerInterceptor} in {@link WebMvcConfig} takes
	 * care of adding the device type resolution to request
	 * 
	 * @param request
	 * 
	 * @param tokenDataHolder
	 * @param jwtStringTokenNew
	 */
	private void singleSessionLoginCheck(HttpServletRequest request, TokenDataHolder tokenDataHolder,
			String jwtStringTokenNew) throws AuthenticationServiceException {

		if (tokenDataHolder == null || jwtStringTokenNew == null)
			throw new AuthenticationServiceException("Invalid auth token");

		// dont do duplicate check for external user links
		if (tokenDataHolder.getWebMode().equals(WebMode.STANDALONE)
				|| tokenDataHolder.getWebMode().equals(WebMode.EXTERNAL_APP))
			return;

		// if reset password url then we dont need to check for duplicate
		// session
		if (resetPasswordRequestMatcher.matches(request))
			return;

		// if activate account url then we dont need to check for duplicate
		// session
		if (activateAccountRequestMatcher.matches(request))
			return;

		// dont do duplicate check for external api model calls , we check for
		// web access only
		if (tokenDataHolder.getWebRequestMode().equals(WebRequestMode.API))
			return;
		
		// dont do duplicate check for external browser plugin calls , we check fory
		if (tokenDataHolder.getWebRequestMode().equals(WebRequestMode.PLUGIN))
			return;

		String email = tokenDataHolder.getUser().getUsername();
		String tenantId = tokenDataHolder.getTenantId();
		String deviceType = getDeviceType(request);

		String jwtStringTokenFromKeyStorage = recruizSecuritySessionTokenStore.getSecurityToken(email, tenantId,
				deviceType);

		TokenDataHolder tokenDataHolderFromKeyStorage = null;
		if (jwtStringTokenFromKeyStorage == null || updateTokenRequestMatcher.matches(request)) {
			// no token in storage so add and return else if the url says update
			// token then update new token in storage and return
			recruizSecuritySessionTokenStore.addSecurityToken(email, tenantId, deviceType, jwtStringTokenNew);
			return;
		} else if (jwtStringTokenFromKeyStorage.equals(jwtStringTokenNew)) {
			// if logout url then clear and return
			if (logoutRequestMatcher.matches(request)) {
				recruizSecuritySessionTokenStore.deleteSecurityToken(email, tenantId, deviceType);
			}
			// new token is same token from storage so return
			return;

		} else {

			try {
				tokenDataHolderFromKeyStorage = tokenHandler.parseJWTStringTokenForUser(jwtStringTokenFromKeyStorage);
			} catch (AuthenticationServiceException | JwtException exp) {
				// the token in storage expired so store new token and return
				recruizSecuritySessionTokenStore.addSecurityToken(email, tenantId, deviceType, jwtStringTokenNew);
				return;
			}

			if (tokenDataHolderFromKeyStorage != null && tokenDataHolderFromKeyStorage.getUser().getUsername()
					.equals(tokenDataHolder.getUser().getUsername()))
				throw new AuthenticationServiceException(ErrorHandler.DUPLICATE_SESSION_NOT_SUPPORTED);
		}
	}

	private String getDeviceType(HttpServletRequest request) {
		String deviceType = DeviceType.WEB.toString();

		Device currentDevice = deviceResolver.resolveDevice(request);
		if (currentDevice != null && (currentDevice.isTablet() || currentDevice.isMobile()))
			deviceType = DeviceType.MOBILE_OR_TABLET.toString();
		return deviceType;
	}

	public String getAuthTokenForUser(String email, String userTenantId, WebMode webMode, Integer tokenValidityInHrs) {
		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		logger.debug(String.format("User with %s is loaded from database", email));
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, webMode);
		return tokenHandler.createJWTStringTokenForUser(tokenDataHolder, tokenValidityInHrs);
	}

	public String getAuthTokenForUser(String email, String userTenantId, WebMode webMode, Integer tokenValidityInHrs,
			String timeZone, String language) {
		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		logger.debug(String.format("User with %s is loaded from database", email));
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, webMode,
				DateTimeZone.forID(timeZone), new Locale(language));
		return tokenHandler.createJWTStringTokenForUser(tokenDataHolder, tokenValidityInHrs);
	}

	public String getAuthTokenForUser(User user, String userTenantId, WebMode webMode, Integer tokenValidityInHrs) {
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, webMode);
		return tokenHandler.createJWTStringTokenForUser(tokenDataHolder, tokenValidityInHrs);
	}

	public TokenDataHolder getAuthTokenHolderForUser(String email, String userTenantId) {
		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, WebMode.DASHBOARD);
		return tokenDataHolder;
	}

	public String getTempPassKeyTokenForUser(String email, WebMode webMode, WebRequestMode webRequestMode,
			Integer tokenValidityInHrs) {
		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, null, webMode);
		return tokenHandler.createJWTStringTokenForUser(tokenDataHolder, tokenValidityInHrs);
	}

	public boolean isTempPassKeyTokenValid(String tempPassKey, String email) {
		return tokenHandler.isJWTPassKeyTokenValid(tempPassKey, email);
	}

	public String getNewApiToken(String email, String userTenantId) {
		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, WebMode.DASHBOARD,
				WebRequestMode.API);
		String newApiToken = tokenHandler.createApiTokenForUser(tokenDataHolder);
		return saveApiToken(email, newApiToken);
	}

	public String getOrganizationApiToken() throws RecruizException {
		UserRole superAdminRole = userRolesRepository.findOneByRoleName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
		Set<com.bbytes.recruiz.domain.User> superAdminUsers = userRepository.findByUserRole(superAdminRole);
		if (superAdminUsers != null && !superAdminUsers.isEmpty()) {
			com.bbytes.recruiz.domain.User userfromDB = superAdminUsers.iterator().next();
			Organization organization = organizationRepository.findByOrgId(userfromDB.getOrganization().getOrgId());
			String orgApiToken = organization.getOrganizationAPIToken();
			// return old api token if there else create a new one
			if (orgApiToken != null && !orgApiToken.isEmpty()) {
				return orgApiToken;
			}

			UserDetails user = authUserDetailsService.loadUserByUsername(userfromDB.getEmail());
			final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userfromDB.getOrganization().getOrgId(),
					WebMode.DASHBOARD, WebRequestMode.API);
			orgApiToken = tokenHandler.createApiTokenForUser(tokenDataHolder);
			return saveOrganizationApiToken(userfromDB.getOrganization().getOrgId(), orgApiToken);
		}

		throw new RecruizException("No super admin user to create organization level api token",
				ErrorHandler.USER_NOT_FOUND);

	}

	/**
	 * This method creates web app auth token, adds it to token store cache to
	 * activate new token. Adds it to security context
	 * 
	 * @param email
	 * @param userTenantId
	 * @param userTimeZone
	 * @param userLocale
	 * @param request
	 * @return
	 */
	public String getNewCachedWebAppAuthToken(String email, String userTenantId, DateTimeZone userTimeZone,
			Locale userLocale, HttpServletRequest request) {

		UserDetails user = authUserDetailsService.loadUserByUsername(email);
		final TokenDataHolder tokenDataHolder = new TokenDataHolder(user, userTenantId, WebMode.DASHBOARD,
				WebRequestMode.WEB_APP, userTimeZone, userLocale);

		MultiTenantAuthenticationToken newMultiTenantAuthenticationToken = new MultiTenantAuthenticationToken(
				tokenDataHolder.getTenantId(), tokenDataHolder.getUser(), tokenDataHolder.getWebMode(),
				tokenDataHolder.getWebRequestMode(), tokenDataHolder.getUserTimeZone(),
				tokenDataHolder.getUserLocale());

		SecurityContextHolder.getContext().setAuthentication(newMultiTenantAuthenticationToken);
		String newAuthToken = tokenHandler.createJWTStringTokenForUser(tokenDataHolder);

		String deviceType = getDeviceType(request);
		updateTokenInCache(email, userTenantId, deviceType, newAuthToken);
		return newAuthToken;
	}

	public void updateTokenInCache(String email, String tenantId, String deviceType, String jwtStringTokenNew) {
		recruizSecuritySessionTokenStore.addSecurityToken(email, tenantId, deviceType, jwtStringTokenNew);
	}

	public String getApiToken(String username) {
		com.bbytes.recruiz.domain.User user = userRepository.findOneByEmail(username);
		return user.getApiToken();
	}

	@Transactional(readOnly = true)
	public boolean validApiTokenInDB(String username, String apiToken) {
		com.bbytes.recruiz.domain.User user = userRepository.findOneByEmail(username);
		if (user != null && user.getApiToken() != null)
			return user.getApiToken().equals(apiToken);

		return false;
	}

	@Transactional(readOnly = true)
	public boolean validOrganizationApiTokenInDB(String orgId, String apiToken) {
		Organization organization = organizationRepository.findByOrgId(orgId);
		if (organization != null && organization.getOrganizationAPIToken() != null) {
			return organization.getOrganizationAPIToken().equals(apiToken);
		}

		return false;
	}

	@Transactional
	public String saveApiToken(String username, String apiToken) {
		com.bbytes.recruiz.domain.User user = userRepository.findOneByEmail(username);
		if (user != null) {
			user.setApiToken(apiToken);
			userRepository.save(user);
		}

		return apiToken;
	}

	@Transactional
	public String saveOrganizationApiToken(String orgId, String orgApiToken) {
		Organization organization = organizationRepository.findByOrgId(orgId);

		if (organization != null) {
			organization.setOrganizationAPIToken(orgApiToken);
			organization = organizationRepository.save(organization);
		}
		return organization.getOrganizationAPIToken();
	}

	public ExternalUserAccessDataHolder getExternalUserAccessDataHolder() {
		return externalUserAccessDataHolder;
	}

	public void setExternalUserAccessDataHolder(ExternalUserAccessDataHolder externalUserAccessDataHolder) {
		this.externalUserAccessDataHolder = externalUserAccessDataHolder;
	}

	private String getAuthTokenFromURL(ServletRequest request) {
		return request.getParameter(GlobalConstants.URL_AUTH_TOKEN);
	}
}
