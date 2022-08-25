package com.bbytes.recruiz.auth.jwt;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.mail.service.imap.ImapClient;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.EmailAccountDetailService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PostTenantSelectionFilter extends GenericFilterBean {

	private RequestMatcher requestMatcher;
	private final UserService userService;
	private final DataModelToDTOConversionService dataModelToDTOConversionService;
	private final TokenAuthenticationProvider tokenAuthenticationService;
	private final AuthUserDetailsService userDetailsService;
	private final TenantResolverService tenantResolverService;
	private final MultiRequestSecurityContextHolder multiRequestSecurityContextHolder;
	private final EmailAccountDetailService emailClientDetailService;
	private final ImapClient imapClient;
	private TenantUsageStatService tenantUsageStatService;

	public PostTenantSelectionFilter(String urlMappingToConsider, UserService userService,
			DataModelToDTOConversionService dataModelToDTOConversionService, AuthUserDetailsService userDetailsService,
			TokenAuthenticationProvider tokenAuthenticationService, TenantResolverService tenantResolverService,
			AuthenticationManager authManager, MultiRequestSecurityContextHolder multiRequestSecurityContextHolder,
			EmailAccountDetailService emailClientDetailService, ImapClient imapClient,
			TenantUsageStatService tenantUsageStatService) {
		requestMatcher = new AntPathRequestMatcher(urlMappingToConsider);
		this.userService = userService;
		this.dataModelToDTOConversionService = dataModelToDTOConversionService;
		this.userDetailsService = userDetailsService;
		this.tokenAuthenticationService = tokenAuthenticationService;
		this.tenantResolverService = tenantResolverService;
		this.multiRequestSecurityContextHolder = multiRequestSecurityContextHolder;
		this.imapClient = imapClient;
		this.emailClientDetailService = emailClientDetailService;
		this.tenantUsageStatService = tenantUsageStatService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		if (!requestMatcher.matches((HttpServletRequest) request)) {
			filterChain.doFilter(request, response); // skip auth and continue
			return;
		}
		try {

			String tenantId = getTenantIdSelected(request);
			String emailId = getEmailIdSelected(request);
			String tempPassKey = getTempPassKey(request);
			WebMode webMode = WebMode.DASHBOARD;
			WebRequestMode webRequestMode = WebRequestMode.WEB_APP;

			if (request.getParameter(GlobalConstants.WEB_REQUEEST_MODE) != null && WebRequestMode.PLUGIN.toString()
					.equals(request.getParameter(GlobalConstants.WEB_REQUEEST_MODE))) {
				webRequestMode = WebRequestMode.PLUGIN;
			}

			if (!tokenAuthenticationService.isTempPassKeyTokenValid(tempPassKey, emailId)) {
				throw new RecruizAuthException("Key passed invalid", ErrorHandler.AUTH_FAILURE);
			}

			boolean valid = tenantResolverService.isTenantValid(tenantId);
			if (!valid)
				throw new AuthenticationServiceException(ErrorHandler.TENANT_NOT_PRESENT);

			// if the url is not part of url pattern to skip then we check auth

			// Add the authentication to the Security context
			MultiTenantAuthenticationToken userAuthToken = (MultiTenantAuthenticationToken) multiRequestSecurityContextHolder
					.getAuthentication(emailId);
			TenantContextHolder.setTenant(tenantResolverService.getTenant(tenantId));
			if (userAuthToken != null) {
				emailId = userAuthToken.getPrincipal().toString();
				webMode = userAuthToken.getWebMode();
				webRequestMode = userAuthToken.getWebRequestMode();
			}
			final User authenticatedUser = userDetailsService.loadUserByUsername(emailId);

			com.bbytes.recruiz.domain.User user = userService.getUserByEmail(emailId);

			DateTimeZone dateTimeZone = null;
			Locale userLocale = null;
			if (user == null || user.getTimezone() == null || user.getTimezone().trim().equals("null")) {
				dateTimeZone = DateTimeZone.forID("Asia/Kolkata");
			} else {
				dateTimeZone = DateTimeZone.forID(user.getTimezone());
			}
			if (user == null || user.getLocale() == null || user.getLocale().trim().equals("null")) {
				userLocale = new Locale("en");
			} else {
				userLocale = new Locale(user.getLocale());
			}

			MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(tenantId,
					authenticatedUser, webMode, webRequestMode, dateTimeZone, userLocale);

			// Add the custom token as HTTP header to the response
			tokenAuthenticationService.addAuthentication((HttpServletResponse) response, userAuthentication);

			// Add the authentication to the Security context
			SecurityContextHolder.getContext().setAuthentication(userAuthentication);

			// we dont need this any more as we set it in default security
			// context after the second request i.e post tenant selection
			// request
			multiRequestSecurityContextHolder.clearAuthentication(emailId);

			/**
			 * Getting user details to return after successful authentication
			 */

			if (userService.isLicenceExpired()) {
				onLicenceExpired(request, response,
						new RecruizAuthException(ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED));
				return;
			}

			UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);

			/**
			 * checking user joined status and account status and throwing up the error
			 * accordingly
			 */
			if (!userDTO.getJoinedStatus()) {
				onNotJoined(request, response,
						new RecruizAuthException(ErrorHandler.NOT_JOINED, ErrorHandler.ACCOUNT_INACTIVE));
				return;
			}

			if (user.getOrganization().getDisableStatus()) {
				onAccountDisabled(request, response, new RecruizAuthException(ErrorHandler.SUSPENDED_ORGANIZATION_MSG,
						ErrorHandler.ACCOUNT_DISABLED));
				return;
			}

			if (!userDTO.getAccountStatus()) {
				onNotJoined(request, response,
						new RecruizAuthException(ErrorHandler.USER_DISABLED, ErrorHandler.ACCOUNT_DISABLED));
				return;
			}

			// calling email store service to fetch and update emails in mongodb
			// for the default email account

			// feature removed, code commented
			/*
			 * try { EmailClientDetails emailClientDetails =
			 * emailClientDetailService.getDefaultEmailAccount(user);
			 * 
			 * if (null != emailClientDetails && null !=
			 * emailClientDetailService.getConnectEmailAccountSessionMap() &&
			 * emailClientDetailService.getConnectEmailAccountSessionMap()
			 * .containsKey(emailClientDetails.getEmailId())) { EmailClientSession
			 * connectedClientSession =
			 * emailClientDetailService.getConnectEmailAccountSessionMap()
			 * .get(emailClientDetails.getEmailId()); // implementing runnable in java 8
			 * style Runnable r = () ->{ try {
			 * imapClient.readAndStoreInRedis(connectedClientSession,
			 * emailClientDetails.getEmailId()); } catch (MessagingException e) {
			 * logger.warn(e.getMessage(), e); } }; Thread emailThread = new Thread(r);
			 * emailThread.setName("RECZ - Login time email sync for " +
			 * emailClientDetails.getEmailId()); emailThread.start();
			 * 
			 * } else { EmailClientSession connectedClientSession =
			 * imapClient.getConnectedClient(emailClientDetails); if(connectedClientSession
			 * != null){ emailClientDetailService.getConnectEmailAccountSessionMap().put(
			 * emailClientDetails.getEmailId(), connectedClientSession); // implementing
			 * runnable in java 8 style Runnable r = () ->{ try {
			 * imapClient.readAndStoreInRedis(connectedClientSession,
			 * emailClientDetails.getEmailId()); } catch (MessagingException e) {
			 * logger.warn(e.getMessage(), e); } };
			 * 
			 * Thread emailThread = new Thread(r);
			 * emailThread.setName("RECZ - Login time email sync for " +
			 * emailClientDetails.getEmailId()); emailThread.start(); }else{ logger.
			 * error("\n\n----------------------Couldn't connect to  mail server for " +
			 * emailClientDetails.getEmailId()); }
			 * 
			 * } } catch (Exception ex) {
			 * 
			 * }
			 */

			/*
			 * getting tenant list for the user to add in the response
			 */
			Map<String, String> userTenantMap = tenantResolverService
					.getOrgIdToOrgNameMap(tenantResolverService.findAllTenantsForUserId(userDTO.getEmail()));

			// updating last login time
			userService.updateLoggedOnTime(userDTO.getEmail());

			/*
			 * // making entry to usage stat after successful authentication
			 * tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant
			 * (), userDTO.getEmail(), userDTO.getUserName(),
			 * UsageActionType.LoggedIn.name());
			 */
			/*
			 * Writing user information to response after successful authentication
			 */
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(GlobalConstants.USER_DETAILS, userDTO);
			responseMap.put(GlobalConstants.TENANT_LIST, userTenantMap);
			responseMap.put(GlobalConstants.TENANT_TYPE, GlobalConstants.SINGLE);

			RestResponse authStatus = new RestResponse(true, responseMap);
			ObjectMapper mapper = new ObjectMapper();
			String responseObject = mapper.writeValueAsString(authStatus);
			((HttpServletResponse) response).setContentType(MediaType.APPLICATION_JSON_VALUE);
			((HttpServletResponse) response).getWriter().append(responseObject);

		} catch (RecruizAuthException recruizAuthException) {
			SecurityContextHolder.clearContext();
			String erroMsg = ErrorHandler.resolveAuthError(recruizAuthException);
			((HttpServletResponse) response).setContentType("application/json");
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((HttpServletResponse) response).getOutputStream().println(erroMsg);
		} catch (AuthenticationServiceException authenticationException) {
			SecurityContextHolder.clearContext();
			String erroMsg = ErrorHandler.resolveAuthError(authenticationException);
			((HttpServletResponse) response).setContentType("application/json");
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((HttpServletResponse) response).getOutputStream().println(erroMsg);
		} catch (ParseException e) {
			SecurityContextHolder.clearContext();
			String erroMsg = e.getMessage();
			((HttpServletResponse) response).setContentType("application/json");
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((HttpServletResponse) response).getOutputStream().println(erroMsg);
		}

	}

	private String getTenantIdSelected(ServletRequest request) {
		return request.getParameter("tenant");
	}

	private String getTempPassKey(ServletRequest request) {
		return request.getParameter(GlobalConstants.TEMP_PASS_KEY);
	}

	/**
	 * Needs to be implemented . The request should have email.
	 * 
	 * @param request
	 * @return
	 */
	// @Deprecated
	private String getEmailIdSelected(ServletRequest request) {
		// return "orgadmin@acc.in";
		return request.getParameter("email");
	}

	public void onNotJoined(ServletRequest request, ServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {

		authFailureResponse(response, exception);
	}

	public void onAccountDisabled(ServletRequest request, ServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {

		authFailureResponse(response, exception);
	}

	public void onLicenceExpired(ServletRequest request, ServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {

		authFailureResponse(response, exception);
	}

	private void authFailureResponse(ServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException {
		SecurityContextHolder.clearContext();

		String erroMsg = ErrorHandler.resolveAuthError(exception);
		((HttpServletResponse) response).setContentType("application/json");
		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
		((HttpServletResponse) response).getOutputStream().println(erroMsg);
	}
}
