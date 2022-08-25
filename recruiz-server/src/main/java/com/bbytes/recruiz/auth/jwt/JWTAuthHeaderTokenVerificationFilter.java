package com.bbytes.recruiz.auth.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.common.base.Preconditions;

public class JWTAuthHeaderTokenVerificationFilter extends GenericFilterBean {

	private final TokenAuthenticationProvider tokenAuthenticationService;

	private RequestMatcher skipAuthenticationRequestMatcher;

	public JWTAuthHeaderTokenVerificationFilter(String urlMappingToIgnore, TokenAuthenticationProvider tokenAuthenticationService) {
		skipAuthenticationRequestMatcher = new AntPathRequestMatcher(urlMappingToIgnore);
		this.tokenAuthenticationService = Preconditions.checkNotNull(tokenAuthenticationService);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

		if (skipAuthenticationRequestMatcher.matches((HttpServletRequest) request)) {
			filterChain.doFilter(request, response); // skip auth and continue
			return;
		}

		// if the url is not part of url pattern to skip then we check auth
		try {
			MultiTenantAuthenticationToken authentication = (MultiTenantAuthenticationToken) tokenAuthenticationService
					.getAuthentication((HttpServletRequest) request, (HttpServletResponse) response);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// if authentication is null then token expired so send http 401
			// back
			if (authentication != null) {
				// Very important for multi tenant to work : set tenant to
				// current db resolver after successful verification
				TenantContextHolder.setTenant(((MultiTenantAuthenticationToken) authentication).getTenantId());

				// verify if the api token in db matches with the given token. it should be called after tenant is set 
				String jwtAPIToken = ((HttpServletRequest) request).getHeader(GlobalConstants.HEADER_API_TOKEN);
				if(jwtAPIToken!=null){
					if (authentication.getWebRequestMode().equals(WebRequestMode.API)
							&& !tokenAuthenticationService.validApiTokenInDB(authentication.getPrincipal().toString(), jwtAPIToken)) {
						throw new AuthenticationServiceException("API token header missing or not valid");
					}	
				}
				
				
				// if org token is used in header 
				String jwtOrgAPIToken = ((HttpServletRequest) request).getHeader(GlobalConstants.HEADER_ORG_API_TOKEN);
				if(jwtOrgAPIToken!=null){
					if (authentication.getWebRequestMode().equals(WebRequestMode.API)
							&& !tokenAuthenticationService.validOrganizationApiTokenInDB(authentication.getTenantId(), jwtOrgAPIToken)) {
						throw new AuthenticationServiceException("Organization API token header missing or not valid");
					}
				}
				
				
				((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
				filterChain.doFilter(request, response); // always continue
			}
		} catch (AuthenticationServiceException authenticationException) {
			SecurityContextHolder.clearContext();
			String erroMsg = ErrorHandler.resolveAuthError(authenticationException);
			((HttpServletResponse) response).setContentType(MediaType.APPLICATION_JSON_VALUE);
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((HttpServletResponse) response).getOutputStream().println(erroMsg);
		}

	}
}
