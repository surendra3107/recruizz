package com.bbytes.recruiz.auth.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bbytes.recruiz.auth.social.SocialSecurityConfigurer;
import com.bbytes.recruiz.mail.service.imap.ImapClient;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.EmailAccountDetailService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;

@Configuration
@EnableWebSecurity
@Order(2)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private UserService userService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private MultiRequestSecurityContextHolder multiRequestSecurityContextHolder;

	@Autowired
	private ImapClient imapClient;

	@Autowired
	private TenantUsageStatService  tenantUsageStatService;
	
	@Autowired
	private EmailAccountDetailService emailClientDetailsService;

	@Override
	public void configure(WebSecurity webSecurity) throws Exception {
		webSecurity.ignoring()
				// All of Spring Security will ignore the requests.
				// '/{[path:[^\\.]*}' is to avoid all the angualr internal urls
				.antMatchers("/").antMatchers("/web/**").antMatchers("/public/**").antMatchers("/pubset/**")
				.antMatchers("/pub/**").antMatchers("/resources/**").antMatchers("/assets/**")
				.antMatchers("/favicon.ico").antMatchers("/**/*.html").antMatchers("/static/**").antMatchers("/app/**")
				.antMatchers("/**/*.css").antMatchers("/**/*.js").antMatchers("/websocket/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// CSRF Disabled and it is ok. Plz read the explanation from
		// stackoverflow
		/*
		 * Note : "If we go down the cookies way, you really need to do CSRF to
		 * avoid cross site requests. That is something we can forget when using
		 * JWT as you will see." (JWT = Json Web Token, a Token based
		 * authentication for stateless apps)
		 */

		http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling().and().servletApi().and().authorizeRequests()

				// allow all request to /ws/**
				.antMatchers("/websocket/**").permitAll()
				// Allow logins urls
				.antMatchers("/auth/**").permitAll().antMatchers("/signin/**").permitAll().antMatchers("/api/**")
				.authenticated()

				// All other request need to be authenticated
				.anyRequest().authenticated().and()

				// Custom Token based authentication based on the header
				// previously given to the client

				.addFilterAfter(getPostTenantSelectionFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(getJWTAuthHeaderTokenVerificationFilter(), PostTenantSelectionFilter.class)
				.addFilterBefore(getUsernamePasswordLoginFilter(), PostTenantSelectionFilter.class).headers()
				.cacheControl().and();

		// social config
		http.apply(new SocialSecurityConfigurer());

		// new security setting added by Thanneer - Needs to be tested
		http.headers().cacheControl();

		// adding extra security to app
		// XSS protection from Cross-Site Scripting (XSS) attack
		http.headers().xssProtection();

		// avoid possibility of a Man in the Middle attack
		http.headers().httpStrictTransportSecurity();

		// avoid clickjacking attacks (this is disable for sixthsense local env,
		// it should be enable with same origin in prod)
		http.headers().frameOptions().disable();

		http.headers().contentTypeOptions();

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(createMutliTenantAuthenticationProvider()).userDetailsService(userDetailsService())
				.passwordEncoder(new BCryptPasswordEncoder());
	}

	@Bean
	public AuthenticationProvider createMutliTenantAuthenticationProvider() {
		return new MutliTenantAuthenticationProvider();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	public JWTAuthHeaderTokenVerificationFilter getJWTAuthHeaderTokenVerificationFilter() throws Exception {
		return new JWTAuthHeaderTokenVerificationFilter("/auth/**", tokenAuthenticationProvider());
	}

	public PostTenantSelectionFilter getPostTenantSelectionFilter() throws Exception {
		return new PostTenantSelectionFilter("/auth/tenant/selected", userService, dataModelToDTOConversionService,
				userDetailsService(), tokenAuthenticationProvider(), tenantResolverService, authenticationManager,
				multiRequestSecurityContextHolder, emailClientDetailsService, imapClient,tenantUsageStatService);
	}

	public UsernamePasswordAuthLoginFilter getUsernamePasswordLoginFilter() throws Exception {
		return new UsernamePasswordAuthLoginFilter("/auth/login", userService, dataModelToDTOConversionService,
				userDetailsService(), tokenAuthenticationProvider(), tenantResolverService, authenticationManager,
				multiRequestSecurityContextHolder);
	}

	@Bean
	@Override
	public AuthUserDetailsService userDetailsService() {
		return new AuthUserDetailsService();
	}

	// @Bean
	public TokenAuthenticationProvider tokenAuthenticationProvider() {
		return tokenAuthenticationProvider;
	}

}