package com.bbytes.recruiz.auth.jwt;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.service.PasswordHashService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

public class MutliTenantAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory.getLogger(MutliTenantAuthenticationProvider.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository tenantMgmtUserRepository;

	@Autowired
	private PasswordHashService passwordHashService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {

		MultiTenantAuthenticationToken token = (MultiTenantAuthenticationToken) auth;

		String username = (auth.getPrincipal() == null) ? null : auth.getPrincipal().toString();
		String password = (auth.getCredentials() == null) ? null : auth.getCredentials().toString();

		if (username == null)
			throw new UsernameNotFoundException("Login request missing username");

		if (password == null)
			throw new BadCredentialsException("Login request missing password");

		com.bbytes.recruiz.domain.User userFromTenantMgmtDB = tenantMgmtUserRepository.findOneByEmail(username);
	
		if (userFromTenantMgmtDB == null) {
			throw new UsernameNotFoundException("User not found with email '" + username + "'");
		}

		UserDetails userDetail = new User(userFromTenantMgmtDB.getEmail(), userFromTenantMgmtDB.getPassword(),
				AuthorityUtils.createAuthorityList(GlobalConstants.NORMAL_USER_ROLE));

		if (!passwordHashService.passwordMatches(password, userDetail.getPassword())) {
			throw new BadCredentialsException("Login Failed. Bad credentials");
		}

		List<String> tenantIds = tenantResolverService.findAllTenantsForUserId(username);

		if (tenantIds == null || tenantIds.isEmpty()) {
			throw new RecruizAuthException("User not part of any organization", ErrorHandler.USER_NOT_PART_OF_ORG);
		}

		updateLoggedOnTime(username);

		logger.debug("Login successful..");

		return token;

	}

	@Transactional
	private void updateLoggedOnTime(String username) {
		com.bbytes.recruiz.domain.User user = null;
		try {
			user = tenantMgmtUserRepository.findOneByEmail(username);
			if (user != null) {
				user.setLoggedOn(DateTime.now().toDate());
				tenantMgmtUserRepository.save(user);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return MultiTenantAuthenticationToken.class.isAssignableFrom(authentication);
	}

}