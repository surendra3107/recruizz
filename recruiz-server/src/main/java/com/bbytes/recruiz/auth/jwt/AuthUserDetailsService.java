package com.bbytes.recruiz.auth.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.connect.Connection;

import com.bbytes.recruiz.domain.SocialUserConnection;
import com.bbytes.recruiz.repository.SocialUserConnectionRepository;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;

public class AuthUserDetailsService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(AuthUserDetailsService.class);

	@Autowired
	private TenantResolverService tenantResolverService;
	
	@Autowired
	private SocialUserConnectionRepository socialUserConnectionRepository;

	/**
	 * The user is loaded from user table in tenant mgmt db .We load the user
	 * for password verification only
	 */
	@Override
	public final User loadUserByUsername(String username) throws UsernameNotFoundException {
		com.bbytes.recruiz.domain.User user = null;
		try {
			user = tenantResolverService.findUserByEmail(username);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (user == null) {
			throw new UsernameNotFoundException("User not found with email '" + username + "'");
		}
		try {
			String password = "N/A";
			if (user.getPassword() != null)
				password = user.getPassword();

			User userDetail = new User(user.getEmail(), password, AuthorityUtils.createAuthorityList(GlobalConstants.NORMAL_USER_ROLE));
			return userDetail;
		} catch (Exception ex) {
			throw new UsernameNotFoundException(ex.getMessage());
		}
	}


	public final User loadUserBySocialConnect(Connection<?> connection) throws UsernameNotFoundException {

		SocialUserConnection socialUserConnection = socialUserConnectionRepository
				.findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserId(
						connection.fetchUserProfile().getEmail(), connection.getKey().getProviderId(),
						connection.getKey().getProviderUserId());

		if (socialUserConnection == null) {
			throw new UsernameNotFoundException("User not found with email '" + connection.fetchUserProfile().getEmail() + "'");
		}
		try {
			String password = "N/A";
			if (socialUserConnection.getAccessToken() != null)
				password = socialUserConnection.getAccessToken();

			User userDetail = new User(socialUserConnection.getUserId(), password,
					AuthorityUtils.createAuthorityList(GlobalConstants.NORMAL_USER_ROLE));
			return userDetail;
		} catch (Exception ex) {
			throw new UsernameNotFoundException(ex.getMessage());
		}
	}

}
