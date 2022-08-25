package com.bbytes.recruiz.web.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The filter that checks for url that is allowed for external user in stand
 * alone mode . If the link request is not allowed to be viewed by external user
 * then we throw access denied error
 * 
 * @author Thanneer
 *
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExternalUserStandAloneModeLinkFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(ExternalUserStandAloneModeLinkFilter.class);

	private List<RequestMatcher> requestMatchers;

	private OrRequestMatcher standaloneUserAllowedUrls;

	private final String EXTERNAL_LINK_PROPERTY = "allowed.external.user.links";

	@Autowired
	private Environment environment;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String allowedURLs = environment.getProperty(EXTERNAL_LINK_PROPERTY);

		if (allowedURLs != null) {
			requestMatchers = new ArrayList<>();
			List<String> allowedURLLinks = Arrays.asList(allowedURLs.split(","));

			for (String urlPattern : allowedURLLinks) {
				AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(urlPattern.trim());
				requestMatchers.add(antPathRequestMatcher);
			}
			standaloneUserAllowedUrls = new OrRequestMatcher(requestMatchers);
		} else {
			throw new ServletException("'allowed.external.user.links' property not set");
		}

	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;

		if ("OPTIONS".equals(((HttpServletRequest) request).getMethod()))
			return;

		if (isStandaloneUserMode()) {
			logger.debug("Standalone mode url request : " + request.getServletPath());
			if (standaloneUserAllowedUrls.matches(request)) {
				// allow user access
				chain.doFilter(req, res);
			} else {
				forwardToAccessDeniedPage(request, response);
			}
		} else {
			// if dashboard mode then no check jus allow user access
			chain.doFilter(req, res);
		}
	}

	@Override
	public void destroy() {
		standaloneUserAllowedUrls = null;
		if (requestMatchers != null) {
			requestMatchers.clear();
			requestMatchers = null;
		}
	}

	private boolean isStandaloneUserMode() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth instanceof MultiTenantAuthenticationToken))
			return false;

		MultiTenantAuthenticationToken authToken = (MultiTenantAuthenticationToken) auth;

		return WebMode.STANDALONE.equals(authToken.getWebMode());
	}

	private void forwardToAccessDeniedPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String erroMsg = ErrorHandler.EXTERNAL_USER_URL_ACCESS_DENIED;
		RestResponse result = new RestResponse(false, "External User not allowed", erroMsg);
		ObjectMapper mapper = new ObjectMapper();
		String responseObject = mapper.writeValueAsString(result);
		((HttpServletResponse) response).setContentType(MediaType.APPLICATION_JSON_VALUE);
		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
		((HttpServletResponse) response).getWriter().append(responseObject);

	}

	private String getDescription(HttpServletRequest request) {
		return "[" + request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo()) + "]";
	}

}