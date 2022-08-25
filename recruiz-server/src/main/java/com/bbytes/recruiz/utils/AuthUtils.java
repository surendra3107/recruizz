package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.joda.time.DateTimeZone;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bbytes.recruiz.auth.jwt.AuthUserDetailsService;
import com.bbytes.recruiz.auth.jwt.MultiRequestSecurityContextHolder;
import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthUtils {

	public static void onSocialAuthSuccessPostProcessor(HttpServletRequest request, HttpServletResponse response,
			MultiTenantAuthenticationToken authenticationToken, TenantResolverService tenantResolverService, UserService userService,
			SocialUserDetailsService userDetailsService, TokenAuthenticationProvider tokenAuthenticationService,
			DataModelToDTOConversionService dataModelToDTOConversionService,
			MultiRequestSecurityContextHolder multiRequestSecurityContextHolder, boolean socialLogin)
			throws IOException, ServletException, JsonProcessingException, ParseException {

		List<String> tenantIds = tenantResolverService.findAllTenantsForUserId(authenticationToken.getPrincipal().toString());

		final SocialUser authenticatedUser = (SocialUser) userDetailsService.loadUserByUserId(authenticationToken.getName());

		com.bbytes.recruiz.domain.User user = userService.getUserByEmail(authenticationToken.getPrincipal().toString());

		// if only one tenant is returned mean the user belongs to only one org
		// so allow him to login like normal user and don't ask the user to pick
		// an org in ui
		if (tenantIds.size() == 1) {

			String tenantId = tenantIds.get(0);
			TenantContextHolder.setTenant(tenantId);
			DateTimeZone dateTimeZone = DateTimeZone.forID(user.getTimezone());
			Locale userLocale = new Locale(user.getLocale());
			MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(tenantId, authenticatedUser,
					authenticationToken.getWebMode(), authenticationToken.getWebRequestMode(), dateTimeZone, userLocale);

			processSingleTenantAuthProcess(request, response, tenantResolverService, userService, tokenAuthenticationService,
					dataModelToDTOConversionService, tenantIds, userAuthentication, socialLogin);
		} else {
			processMultiTenantAuthProcess(request, response, authenticationToken, tenantResolverService, tokenAuthenticationService,
					multiRequestSecurityContextHolder, tenantIds, socialLogin);
		}

	}

	public static void onAuthSuccessPostProcessor(HttpServletRequest request, HttpServletResponse response,
			MultiTenantAuthenticationToken authenticationToken, TenantResolverService tenantResolverService, UserService userService,
			AuthUserDetailsService userDetailsService, TokenAuthenticationProvider tokenAuthenticationService,
			DataModelToDTOConversionService dataModelToDTOConversionService,
			MultiRequestSecurityContextHolder multiRequestSecurityContextHolder, boolean socialLogin)
			throws IOException, ServletException, JsonProcessingException, ParseException {

		List<String> tenantIds = new ArrayList<>();

		String tenantIdInToken = authenticationToken.getTenantId();
		if (tenantIdInToken != null) {
			tenantIds.add(tenantIdInToken);
			if (!tenantResolverService.isTenantValid(tenantIdInToken)) {
				onInvalidTenant(request, response);
				return;
			}

		} else {
			tenantIds = tenantResolverService.findAllTenantsForUserId(authenticationToken.getPrincipal().toString());
		}

		final User authenticatedUser = userDetailsService.loadUserByUsername(authenticationToken.getName());

		// if only one tenant is returned mean the user belongs to only one org
		// so allow him to login like normal user and don't ask the user to pick
		// an org in ui
		if (tenantIds.size() == 1) {

			String tenantId = tenantIds.get(0);
			TenantContextHolder.setTenant(tenantId);

			com.bbytes.recruiz.domain.User user = userService.getUserByEmail(authenticationToken.getPrincipal().toString());

			DateTimeZone dateTimeZone = DateTimeZone.forID(user.getTimezone());
			Locale userLocale = new Locale(user.getLocale());
			MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(tenantId, authenticatedUser,
					authenticationToken.getWebMode(), authenticationToken.getWebRequestMode(), dateTimeZone, userLocale);

			/*
			 * MultiTenantAuthenticationToken userAuthentication = new
			 * MultiTenantAuthenticationToken(tenantId,
			 * authenticatedUser,authenticationToken.getWebMode());
			 */

			/**
			 * Getting user details to return after successful authentication
			 */

			processSingleTenantAuthProcess(request, response, tenantResolverService, userService, tokenAuthenticationService,
					dataModelToDTOConversionService, tenantIds, userAuthentication, socialLogin);
		} else {

			processMultiTenantAuthProcess(request, response, authenticationToken, tenantResolverService, tokenAuthenticationService,
					multiRequestSecurityContextHolder, tenantIds, socialLogin);
		}
	}

	private static void processMultiTenantAuthProcess(HttpServletRequest request, HttpServletResponse response,
			MultiTenantAuthenticationToken authenticationToken, TenantResolverService tenantResolverService,
			TokenAuthenticationProvider tokenAuthenticationService, MultiRequestSecurityContextHolder multiRequestSecurityContextHolder,
			List<String> tenantIds, boolean socialLogin) throws JsonProcessingException, IOException {
		// Add the authentication to the Security context
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		// this is added to make sure we have security context for second
		// request for multi org selection request after login request
		multiRequestSecurityContextHolder.setAuthentication(authenticationToken.getPrincipal().toString(), authenticationToken);

		Map<String, String> orgIdToNameMap = tenantResolverService.getOrgIdToOrgNameMap(tenantIds);
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put(GlobalConstants.EMAIL, authenticationToken.getPrincipal().toString());

		if (socialLogin) {
			// if social login then this block
			List<NameValuePair> pairs = new ArrayList<>();
			pairs.add(new NameValuePair(GlobalConstants.TEMP_PASS_KEY,
					tokenAuthenticationService.getTempPassKeyTokenForUser(authenticationToken.getPrincipal().toString(),
							authenticationToken.getWebMode(), authenticationToken.getWebRequestMode(), 1)));
			pairs.add(new NameValuePair(GlobalConstants.EMAIL, authenticationToken.getPrincipal().toString()));
			pairs.add(new NameValuePair(GlobalConstants.TENANT_LIST, convertMapToString(orgIdToNameMap)));
			pairs.add(new NameValuePair(GlobalConstants.TENANT_TYPE, GlobalConstants.MULTIPLE));

			response.sendRedirect(buildPostSuccessLoginUrl(GlobalConstants.POST_LOGIN_SUCCESS_URL, pairs, request));

		} else {
			// if normal app login then this block
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(GlobalConstants.TEMP_PASS_KEY,
					tokenAuthenticationService.getTempPassKeyTokenForUser(authenticationToken.getPrincipal().toString(),
							authenticationToken.getWebMode(), authenticationToken.getWebRequestMode(), 1));
			responseMap.put(GlobalConstants.TENANT_LIST, orgIdToNameMap);
			responseMap.put(GlobalConstants.EMAIL, authenticationToken.getPrincipal().toString());
			responseMap.put(GlobalConstants.TENANT_TYPE, GlobalConstants.MULTIPLE);

			// if user belongs to more than one org then send the list to ui
			// along with user email
			RestResponse responseWithEmailAndTenantList = new RestResponse(true, responseMap);
			ObjectMapper mapper = new ObjectMapper();
			String responseObject = mapper.writeValueAsString(responseWithEmailAndTenantList);
			((HttpServletResponse) response).setContentType(MediaType.APPLICATION_JSON_VALUE);
			((HttpServletResponse) response).getWriter().append(responseObject);

		}

	}

	private static String convertMapToString(Map<String, String> map) {
		List<String> pair = new ArrayList<>();

		if (map == null)
			return "";

		for (String key : map.keySet()) {
			String value = map.get(key);
			pair.add(key + ":" + value);
		}

		return org.apache.commons.lang3.StringUtils.join(pair, ',');

	}

	private static void processSingleTenantAuthProcess(HttpServletRequest request, HttpServletResponse response,
			TenantResolverService tenantResolverService, UserService userService, TokenAuthenticationProvider tokenAuthenticationService,
			DataModelToDTOConversionService dataModelToDTOConversionService, List<String> tenantIds,
			MultiTenantAuthenticationToken userAuthentication, boolean socialLogin)
			throws IOException, ServletException, JsonProcessingException, ParseException {
		// Add the custom token as HTTP header to the response
		tokenAuthenticationService.addAuthentication(response, userAuthentication);

		// Add the authentication to the Security context
		SecurityContextHolder.getContext().setAuthentication(userAuthentication);

		/**
		 * Getting user details to return after successful authentication
		 */
		com.bbytes.recruiz.domain.User user = userService.getUserByEmail(userAuthentication.getPrincipal().toString());

		if (socialLogin) {
			// if social login then this block
			String xauthToken = tokenAuthenticationService.getAuthTokenForUser(user.getEmail(), userAuthentication.getTenantId(),
					userAuthentication.getWebMode(), 1);

			List<NameValuePair> pairs = new ArrayList<>();
			pairs.add(new NameValuePair(GlobalConstants.TEMP_PASS_KEY, xauthToken));

			response.sendRedirect(buildPostSuccessLoginUrl(GlobalConstants.POST_LOGIN_SUCCESS_URL, pairs, request));
		} else {
			// if normal app login then this block
			UserDTO userDTO = dataModelToDTOConversionService.convertUser(user);
			// updating last login time
			userService.updateLoggedOnTime(userDTO.getEmail());

			/**
			 * checking user joined status and account status and throwing up
			 * the error accordingly
			 */

			if (userService.isLicenceExpired()) {
				onLicenceExpired(request, response);
				return;
			}

			if (user.getOrganization().getDisableStatus()) {
				onAccountDisabled(request, response);
				return;
			}

			if (!userDTO.getJoinedStatus()) {
				onNotJoined(request, response);
				return;
			}

			if (userDTO.getType().equalsIgnoreCase(GlobalConstants.USER_TYPE_VENDOR)) {
				if (!userService.isVendorAcitve(userDTO.getEmail())) {
					onVendorDisabled(request, response);
					return;
				}
			}
			
			if (!userDTO.getAccountStatus()) {
				onNotJoined(request, response);
				return;
			}

			/*
			 * getting tenant list for the user to add in the response
			 */
			Map<String, String> userTenantMap = tenantResolverService.getOrgIdToOrgNameMap(tenantIds);
			/*
			 * Writing user information to response after successful
			 * authentication
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
		}

	}

	public static String buildPostSuccessLoginUrl(String baseURL, List<NameValuePair> pairs, HttpServletRequest request)
			throws URIException {
		baseURL = getUrlWithParam(baseURL, pairs);
		if (baseURL.startsWith("http://") || baseURL.startsWith("https://")) {
			return baseURL;
		}
		if (!baseURL.startsWith("/")) {
			return ServletUriComponentsBuilder.fromContextPath(request).path("/" + baseURL).build().toUriString();
		}
		return ServletUriComponentsBuilder.fromContextPath(request).path(baseURL).build().toUriString();
	}

	private static String getUrlWithParam(String baseUrl, List<NameValuePair> pairs) throws URIException {
		HttpMethod method = new GetMethod(baseUrl);
		if (pairs != null && !pairs.isEmpty())
			method.setQueryString(pairs.toArray(new NameValuePair[] {}));
		return method.getURI().getEscapedURI();
	}

	public static String buildSignupUrl(String signupUrl, HttpServletRequest request) {
		if (signupUrl.startsWith("http://") || signupUrl.startsWith("https://")) {
			return signupUrl;
		}
		if (!signupUrl.startsWith("/")) {
			return ServletUriComponentsBuilder.fromContextPath(request).path("/" + signupUrl).build().toUriString();
		}
		return ServletUriComponentsBuilder.fromContextPath(request).path(signupUrl).build().toUriString();
	}

	public static void onNotJoined(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		org.springframework.security.core.AuthenticationException exception = new RecruizAuthException(ErrorHandler.NOT_JOINED,
				ErrorHandler.ACCOUNT_INACTIVE);
		onInvalid(request, response, exception);
	}

	public static void onAccountDisabled(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		org.springframework.security.core.AuthenticationException exception = new RecruizAuthException(
				ErrorHandler.SUSPENDED_ORGANIZATION_MSG, ErrorHandler.ACCOUNT_DISABLED);
		onInvalid(request, response, exception);

	}

	public static void onVendorDisabled(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		org.springframework.security.core.AuthenticationException exception = new RecruizAuthException(ErrorHandler.USER_DISABLED,
				ErrorHandler.VENDOR_DISABLED);
		onInvalid(request, response, exception);

	}

	public static void onLicenceExpired(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		org.springframework.security.core.AuthenticationException exception = new RecruizAuthException(ErrorHandler.RENEW_LICENCE,
				ErrorHandler.LICENCE_EXPIRED);
		onInvalid(request, response, exception);
	}

	public static void onInvalidTenant(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		org.springframework.security.core.AuthenticationException exception = new RecruizAuthException(ErrorHandler.TENANT_INVALID,
				ErrorHandler.TENANT_NOT_VALID);
		onInvalid(request, response, exception);

	}

	public static void onInvalid(ServletRequest request, ServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		String erroMsg = ErrorHandler.resolveAuthError(exception);
		((HttpServletResponse) response).setContentType("application/json");
		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
		((HttpServletResponse) response).getOutputStream().println(erroMsg);

	}
}
