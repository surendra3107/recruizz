package com.bbytes.recruiz.auth.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.auth.jwt.AuthUserDetailsService;

/**
 * This class delegates requests forward to our UserDetailsService
 * implementation. This is possible because we use the username of the user as
 * the account ID.
 */
@Component
public class SocialUserDetailsServiceImpl implements SocialUserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SocialUserDetailsServiceImpl.class);

	private AuthUserDetailsService userDetailsService;

	@Autowired
	public SocialUserDetailsServiceImpl(AuthUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Loads the username by using the account ID of the user.
	 * 
	 * @param userId
	 *            The account ID of the requested user.
	 * @return The information of the requested user.
	 * @throws UsernameNotFoundException
	 *             Thrown if no user is found.
	 * @throws DataAccessException
	 */
	@Override
	public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
		LOGGER.debug("Loading user by user id: {}", userId);

		UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
		LOGGER.debug("Found user details: {}", userDetails);

		return new SocialUser(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(),
				userDetails.isAccountNonExpired(), userDetails.isCredentialsNonExpired(),
				userDetails.isAccountNonLocked(), userDetails.getAuthorities());

	}

}