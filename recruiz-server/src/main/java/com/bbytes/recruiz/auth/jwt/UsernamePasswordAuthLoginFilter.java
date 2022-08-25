package com.bbytes.recruiz.auth.jwt;

import java.io.IOException;
import java.text.ParseException;

import javax.security.sasl.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.AuthUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class UsernamePasswordAuthLoginFilter extends AbstractAuthenticationProcessingFilter implements AuthenticationFailureHandler {

	private static Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthLoginFilter.class);

	private final UserService userService;
	private final DataModelToDTOConversionService dataModelToDTOConversionService;
	private final TokenAuthenticationProvider tokenAuthenticationService;
	private final AuthUserDetailsService userDetailsService;
	private final TenantResolverService tenantResolverService;
	private final MultiRequestSecurityContextHolder multiRequestSecurityContextHolder;

	protected UsernamePasswordAuthLoginFilter(String urlMapping, UserService userService,
			DataModelToDTOConversionService dataModelToDTOConversionService, AuthUserDetailsService userDetailsService,
			TokenAuthenticationProvider tokenAuthenticationService, TenantResolverService tenantResolverService,
			AuthenticationManager authManager, MultiRequestSecurityContextHolder multiRequestSecurityContextHolder) {
		super(new AntPathRequestMatcher(urlMapping));
		this.userService = userService;
		this.dataModelToDTOConversionService = dataModelToDTOConversionService;
		this.userDetailsService = userDetailsService;
		this.tokenAuthenticationService = tokenAuthenticationService;
		this.tenantResolverService = tenantResolverService;
		this.multiRequestSecurityContextHolder = multiRequestSecurityContextHolder;

		setAuthenticationManager(authManager);
		setAuthenticationFailureHandler(this);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		// this change needs to be confirm with TM
		logger.debug("Setting current tenant : " + TenantContextHolder.getTenant() + " to Null");
		TenantContextHolder.setTenant(null);

		if (request.getParameter(GlobalConstants.USERNAME_URL_PARAM) == null) {
			throw new AuthenticationServiceException("Username missing");
		}
		if (request.getParameter(GlobalConstants.PASSWORD_URL_PARAM) == null) {
			throw new AuthenticationServiceException("Password missing");
		}

		WebRequestMode webRequestMode = WebRequestMode.WEB_APP;
		// if web request mode is api then the request is not from dashboard but
		// from external or standalone
		if (request.getParameter(GlobalConstants.WEB_REQUEEST_MODE) != null
				&& WebRequestMode.API.toString().equals(request.getParameter(GlobalConstants.WEB_REQUEEST_MODE))) {
			webRequestMode = WebRequestMode.API;
		}else if (request.getParameter(GlobalConstants.WEB_REQUEEST_MODE) != null
				&& WebRequestMode.PLUGIN.toString().equals(request.getParameter(GlobalConstants.WEB_REQUEEST_MODE))) {
			webRequestMode = WebRequestMode.PLUGIN;
		}
		

		final MultiTenantAuthenticationToken loginToken = new MultiTenantAuthenticationToken(
				request.getParameter(GlobalConstants.USERNAME_URL_PARAM),
				request.getParameter(GlobalConstants.PASSWORD_URL_PARAM),
				request.getParameter(GlobalConstants.TENANT_URL_PARAM), WebMode.DASHBOARD, webRequestMode);

		return getAuthenticationManager().authenticate(loginToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {

		// Lookup the complete User object from the database and create an
		// Authentication for it
		MultiTenantAuthenticationToken authenticationToken = (MultiTenantAuthenticationToken) authentication;

		try {
			AuthUtils.onAuthSuccessPostProcessor(request, response, authenticationToken, tenantResolverService, userService,
					userDetailsService, tokenAuthenticationService, dataModelToDTOConversionService, multiRequestSecurityContextHolder,
					false);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {
		SecurityContextHolder.clearContext();

		String erroMsg = ErrorHandler.resolveAuthError(exception);
		((HttpServletResponse) response).setContentType(MediaType.APPLICATION_JSON_VALUE);
		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
		((HttpServletResponse) response).getOutputStream().println(erroMsg);

	}

	// public void onNotJoined(HttpServletRequest request, HttpServletResponse
	// response,
	// org.springframework.security.core.AuthenticationException exception)
	// throws IOException, ServletException {
	// SecurityContextHolder.clearContext();
	//
	// String erroMsg = ErrorHandler.resolveAuthError(exception);
	// ((HttpServletResponse) response).setContentType("application/json");
	// ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
	// ((HttpServletResponse) response).getOutputStream().println(erroMsg);
	//
	// }
	//
	// public void onAccountDisabled(HttpServletRequest request,
	// HttpServletResponse response,
	// org.springframework.security.core.AuthenticationException exception)
	// throws IOException, ServletException {
	// SecurityContextHolder.clearContext();
	//
	// String erroMsg = ErrorHandler.resolveAuthError(exception);
	// ((HttpServletResponse) response).setContentType("application/json");
	// ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
	// ((HttpServletResponse) response).getOutputStream().println(erroMsg);
	//
	// }

}