package com.bbytes.recruiz.auth.social;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;

import com.bbytes.recruiz.auth.jwt.MultiRequestSecurityContextHolder;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;

/**
 * Configurer that adds {@link SocialAuthenticationFilter} to Spring Security's
 * filter chain. Used with Spring Security 3.2's Java-based configuration
 * support, when overriding
 * WebSecurityConfigurerAdapter#configure(HttpSecurity):
 * 
 * <pre>
 * protected void configure(HttpSecurity http) throws Exception { http. // HTTP
 * security configuration details snipped .and() .apply(new
 * SpringSocialHttpConfigurer()); }
 * 
 */
public class SocialSecurityConfigurer extends SpringSocialConfigurer {

	private String postLoginUrl = "/";

	private String postFailureUrl = null;

	private String signupUrl = "/auth/social/signup";

	private String defaultFailureUrl = null;

	private boolean alwaysUsePostLoginUrl = false;



	@Override
	public void configure(HttpSecurity http) throws Exception {
		ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
		UsersConnectionRepository usersConnectionRepository = getDependency(applicationContext,
				UsersConnectionRepository.class);
		SocialAuthenticationServiceLocator authServiceLocator = getDependency(applicationContext,
				SocialAuthenticationServiceLocator.class);
		SocialUserDetailsService socialUsersDetailsService = getDependency(applicationContext,
				SocialUserDetailsService.class);
		DataModelToDTOConversionService dataModelToDTOConversionService = getDependency(applicationContext,
				DataModelToDTOConversionService.class);
		TenantResolverService tenantResolverService = getDependency(applicationContext, TenantResolverService.class);
		MultiRequestSecurityContextHolder multiRequestSecurityContextHolder = getDependency(applicationContext,
				MultiRequestSecurityContextHolder.class);
		UserService userService = getDependency(applicationContext, UserService.class);
		TokenAuthenticationProvider tokenAuthenticationProvider = getDependency(applicationContext,
				TokenAuthenticationProvider.class);

		SocialMultiTenantAuthenticationFilter filter = new SocialMultiTenantAuthenticationFilter(
				http.getSharedObject(AuthenticationManager.class), new AuthenticationNameUserIdSource(),
				usersConnectionRepository, authServiceLocator, socialUsersDetailsService,
				dataModelToDTOConversionService, userService, tokenAuthenticationProvider, tenantResolverService,
				multiRequestSecurityContextHolder);

		RememberMeServices rememberMe = http.getSharedObject(RememberMeServices.class);
		if (rememberMe != null) {
			filter.setRememberMeServices(rememberMe);
		}

		if (postLoginUrl != null) {
			filter.setPostLoginUrl(postLoginUrl);
			filter.setAlwaysUsePostLoginUrl(alwaysUsePostLoginUrl);
		}

		if (postFailureUrl != null) {
			filter.setPostFailureUrl(postFailureUrl);
		}

		if (signupUrl != null) {
			filter.setSignupUrl(signupUrl);
		}

		if (defaultFailureUrl != null) {
			filter.setDefaultFailureUrl(defaultFailureUrl);
		}

		http.authenticationProvider(
				new SocialAuthenticationProvider(usersConnectionRepository, socialUsersDetailsService))
				.addFilterBefore(postProcess(filter), AbstractPreAuthenticatedProcessingFilter.class);
	}

	private <T> T getDependency(ApplicationContext applicationContext, Class<T> dependencyType) {
		try {
			T dependency = applicationContext.getBean(dependencyType);
			return dependency;
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalStateException("SpringSocialConfigurer depends on " + dependencyType.getName()
					+ ". No single bean of that type found in application context.", e);
		}
	}

}
