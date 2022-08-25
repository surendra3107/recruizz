package com.bbytes.recruiz.auth.social;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SocialAuthenticationRedirectException;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.util.Assert;
import org.springframework.web.context.request.ServletWebRequest;

import com.bbytes.recruiz.auth.jwt.MultiRequestSecurityContextHolder;
import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.AuthUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.MultiTenantUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

import scala.deprecated;

/**
 * Filter for handling the provider sign-in flow within the Spring Security
 * filter chain. Should be injected into the chain at or before the
 * PRE_AUTH_FILTER location.
 * 
 */
public class SocialMultiTenantAuthenticationFilter extends SocialAuthenticationFilter implements AuthenticationFailureHandler {

	private static Logger logger = LoggerFactory.getLogger(SocialMultiTenantAuthenticationFilter.class);

	private UserIdSource userIdSource;

	private String signupUrl = "/signup";

	private String connectionAddedRedirectUrl = "/";

	private static final String DEFAULT_FILTER_PROCESSES_URL = "/auth";

	private String filterProcessesUrl = DEFAULT_FILTER_PROCESSES_URL;

	private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

	private final SocialUserDetailsService socialUsersDetailsService;

	private final UserService userService;

	private final DataModelToDTOConversionService dataModelToDTOConversionService;

	private final TokenAuthenticationProvider tokenAuthenticationProvider;

	private final TenantResolverService tenantResolverService;

	private final MultiRequestSecurityContextHolder multiRequestSecurityContextHolder;

	public SocialMultiTenantAuthenticationFilter(AuthenticationManager authManager, UserIdSource userIdSource,
			UsersConnectionRepository usersConnectionRepository, SocialAuthenticationServiceLocator authServiceLocator,
			SocialUserDetailsService socialUsersDetailsService, DataModelToDTOConversionService dataModelToDTOConversionService,
			UserService userService, TokenAuthenticationProvider tokenAuthenticationProvider, TenantResolverService tenantResolverService,
			MultiRequestSecurityContextHolder multiRequestSecurityContextHolder) {
		super(authManager, userIdSource, usersConnectionRepository, authServiceLocator);
		this.userIdSource = userIdSource;
		this.socialUsersDetailsService = socialUsersDetailsService;
		this.userService = userService;
		this.dataModelToDTOConversionService = dataModelToDTOConversionService;
		this.tokenAuthenticationProvider = tokenAuthenticationProvider;
		this.tenantResolverService = tenantResolverService;
		this.multiRequestSecurityContextHolder = multiRequestSecurityContextHolder;

		setAuthenticationFailureHandler(this);
	}

	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		MultiTenantUtils.storeTentantIdToSession(request);
		MultiTenantUtils.storeInviteEmailIdToSession(request);
		MultiTenantUtils.storeSignUpModeToSession(request);

		TenantContextHolder.clearContext();

		Authentication auth = null;
		Set<String> authProviders = getAuthServiceLocator().registeredAuthenticationProviderIds();
		String authProviderId = getRequestedProviderId(request);
		if (!authProviders.isEmpty() && authProviderId != null && authProviders.contains(authProviderId)) {
			SocialAuthenticationService<?> authService = getAuthServiceLocator().getAuthenticationService(authProviderId);

			try {
				auth = attemptAuthService(authService, request, response);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new AuthenticationServiceException(e.getMessage(), e);
			}

			if (auth == null) {
				throw new AuthenticationServiceException("authentication failed");
			}
		}
		return auth;
	}

	/**
	 * {@link deprecated} Not used anymore
	 */
	@Override
	@Deprecated
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {

		if (exception instanceof SocialAuthenticationRedirectException) {
			response.sendRedirect(((SocialAuthenticationRedirectException) exception).getRedirectUrl());
			return;
		}

		SecurityContextHolder.clearContext();

		String erroMsg = ErrorHandler.resolveAuthError(exception);
		((HttpServletResponse) response).setContentType("application/json");
		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
		((HttpServletResponse) response).getOutputStream().println(erroMsg);

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {

		final SocialUser authenticatedUser = (SocialUser) socialUsersDetailsService.loadUserByUserId(authentication.getName());

		final MultiTenantAuthenticationToken userAuthentication = new MultiTenantAuthenticationToken(TenantContextHolder.getTenant(),
				authenticatedUser, WebMode.DASHBOARD);

		try {
			AuthUtils.onSocialAuthSuccessPostProcessor(request, response, userAuthentication, tenantResolverService, userService,
					socialUsersDetailsService, tokenAuthenticationProvider, dataModelToDTOConversionService, multiRequestSecurityContextHolder,
					true);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Detects a callback request after a user rejects authorization to prevent
	 * a never-ending redirect loop. Default implementation detects a rejection
	 * as a request that has one or more parameters (except 'state' parameter
	 * which can be used by application), but none of the expected parameters
	 * (oauth_token, code, scope). May be overridden to customize rejection
	 * detection.
	 * 
	 * @param request
	 *            the request to check for rejection.
	 * @return true if the request appears to be the result of a rejected
	 *         authorization; false otherwise.
	 * @deprecated not used anymore
	 */
	@Deprecated
	protected boolean detectRejection(HttpServletRequest request) {
		Set<?> parameterKeys = request.getParameterMap().keySet();
		if ((parameterKeys.size() < 4) && (parameterKeys.contains("state") || parameterKeys.contains(GlobalConstants.INVITE_EMAIL_ID)
				|| parameterKeys.contains(GlobalConstants.ORG_ID))) {
			return false;
		}
		return parameterKeys.size() > 0 && !parameterKeys.contains("oauth_token") && !parameterKeys.contains("code")
				&& !parameterKeys.contains("scope");
	}

	/*
	 * Call SocialAuthenticationService.getAuthToken() to get
	 * SocialAuthenticationToken: If first phase, throw
	 * AuthenticationRedirectException to redirect to provider website. If
	 * second phase, get token/code from request parameter and call provider API
	 * to get accessToken/accessGrant. Check Authentication object in spring
	 * security context, if null or not authenticated, call doAuthentication()
	 * Otherwise, it is already authenticated, add this connection.
	 */
	private Authentication attemptAuthService(final SocialAuthenticationService<?> authService, final HttpServletRequest request,
			HttpServletResponse response) throws SocialAuthenticationRedirectException, AuthenticationException, IOException {

		final SocialAuthenticationToken token = authService.getAuthToken(request, response);
		if (token == null)
			return null;

		Assert.notNull(token.getConnection());

		Authentication auth = getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return doAuthentication(authService, request, response, token);
		} else {
			addConnection(authService, request, token, auth);
			return null;
		}
	}

	private void addConnection(final SocialAuthenticationService<?> authService, HttpServletRequest request,
			SocialAuthenticationToken token, Authentication auth) {
		// already authenticated - add connection instead
		String userId = userIdSource.getUserId();
		Object principal = token.getPrincipal();
		if (userId == null || !(principal instanceof ConnectionData))
			return;

		Connection<?> connection = addConnection(authService, userId, (ConnectionData) principal);
		if (connection != null) {
			String redirectUrl = authService.getConnectionAddedRedirectUrl(request, connection);
			if (redirectUrl == null) {
				// use default instead
				redirectUrl = connectionAddedRedirectUrl;
			}
			throw new SocialAuthenticationRedirectException(redirectUrl);
		}
	}

	private void updateConnections(SocialAuthenticationService<?> authService, SocialAuthenticationToken token, Authentication success) {
		String userId = ((SocialUserDetails) success.getPrincipal()).getUserId();
		Connection<?> connection = token.getConnection();
		ConnectionRepository repo = getUsersConnectionRepository().createConnectionRepository(userId);
		repo.updateConnection(connection);
	}

	private Authentication doAuthentication(SocialAuthenticationService<?> authService, HttpServletRequest request,
			HttpServletResponse response, SocialAuthenticationToken token) throws IOException {

		try {
			if (!authService.getConnectionCardinality().isAuthenticatePossible())
				return null;
			token.setDetails(authenticationDetailsSource.buildDetails(request));
			Authentication success = getAuthenticationManager().authenticate(token);
			Assert.isInstanceOf(SocialUserDetails.class, success.getPrincipal(), "unexpected principle type");
			SocialUser socialUser = (SocialUser) success.getPrincipal();
			String orgIdOrTenantId = MultiTenantUtils.getTenantId(request);

			String signUpMode = MultiTenantUtils.getSignUpMode(request);

			// if login then pass the auth
			if (!isSignupMode(request))
				return success;

			if (orgIdOrTenantId == null) {
				throw new RecruizAuthException("Missing key information during registration", ErrorHandler.SIGN_UP_FAILED);
			}

			// if signup mode then check if user belongs to same org then throw
			// exception else redirect the user to signup url
			if (tenantResolverService.userExistsForOrg(socialUser.getUsername(), orgIdOrTenantId)) {
				// TODO: temp fix with java contains ..the signUpMode invite string has a double quotes in the beginning to be fixed later
				if (signUpMode.contains(GlobalConstants.SIGNUP_MODE_INVITED)) {
					User user = userService.getUserByEmail(socialUser.getUsername());
					if (user != null) {
						user.setJoinedStatus(true);
						userService.save(user);
					}
					return success;
				} else {
					MultiTenantUtils.clearSignUpModeInRequest(request);
					throw new RecruizAuthException("User already registered", ErrorHandler.ALREADY_SIGNED_UP);
				}
			} else {
				updateConnections(authService, token, success);
				throw new UsernameNotFoundException("User not registered for the given organization " + orgIdOrTenantId);
			}
		} catch (BadCredentialsException | UsernameNotFoundException e) {
			// connection unknown, register new user?
			if (isSignupMode(request) && signupUrl != null) {
				// store ConnectionData in session and redirect to recruiz
				// server signup controller
				sessionStrategy.setAttribute(new ServletWebRequest(request), ProviderSignInAttempt.SESSION_ATTRIBUTE,
						new ProviderSignInAttempt(token.getConnection()));
				throw new SocialAuthenticationRedirectException(AuthUtils.buildSignupUrl(signupUrl, request));
			} else {
				throw new SocialAuthenticationRedirectException(GlobalConstants.SIGNUP_UI_URL);
			}
		} catch (AuthenticationException exception) {
			logger.error(exception.getMessage(), exception);
			SecurityContextHolder.clearContext();

			String erroMsg = ErrorHandler.resolveAuthError(exception);
			((HttpServletResponse) response).setContentType("application/json");
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
			((HttpServletResponse) response).getOutputStream().println(erroMsg);

			throw exception;
		}

	}

	private boolean isSignupMode(HttpServletRequest request) {
		if (MultiTenantUtils.getSignUpMode(request) != null)
			return true;

		return false;
	}

	private String getRequestedProviderId(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');

		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}

		// uri must start with context path
		uri = uri.substring(request.getContextPath().length());

		// remaining uri must start with filterProcessesUrl
		if (!uri.startsWith(filterProcessesUrl)) {
			return null;
		}
		uri = uri.substring(filterProcessesUrl.length());

		// expect /filterprocessesurl/provider, not /filterprocessesurlproviderr
		if (uri.startsWith("/")) {
			return uri.substring(1);
		} else {
			return null;
		}
	}

	protected Connection<?> addConnection(SocialAuthenticationService<?> authService, String userId, ConnectionData data) {
		return super.addConnection(authService, userId, data);
	}

	private Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public void setUpdateConnections(boolean updateConnections) {
		super.setUpdateConnections(updateConnections);
	}

	@Override
	public void setFilterProcessesUrl(String filterProcessesUrl) {
		super.setFilterProcessesUrl(filterProcessesUrl);
		this.filterProcessesUrl = filterProcessesUrl;
	}

	@Override
	public void setConnectionAddedRedirectUrl(String connectionAddedRedirectUrl) {
		super.setConnectionAddedRedirectUrl(connectionAddedRedirectUrl);
		this.connectionAddedRedirectUrl = connectionAddedRedirectUrl;
	}

	/**
	 * Sets a strategy to use when persisting information that is to survive
	 * past the boundaries of a request. The default strategy is to set the data
	 * as attributes in the HTTP Session.
	 * 
	 * @param sessionStrategy
	 *            the session strategy.
	 */
	@Override
	public void setSessionStrategy(SessionStrategy sessionStrategy) {
		super.setSessionStrategy(sessionStrategy);
		this.sessionStrategy = sessionStrategy;
	}

	/**
	 * Sets the signup URL; the URL to redirect to if authentication fails so
	 * that the user can register with the application. May be fully-qualified
	 * URL (e.g., "http://somehost/somepath/signup") or a path relative to
	 * application's servlet context path (e.g., "/signup").
	 * 
	 * @param signupUrl
	 *            The signup URL
	 */
	@Override
	public void setSignupUrl(String signupUrl) {
		super.setSignupUrl(signupUrl);
		this.signupUrl = signupUrl;
	}
}
