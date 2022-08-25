
package com.bbytes.recruiz.auth.social;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import com.bbytes.recruiz.repository.SocialUserConnectionRepository;

@Configuration
@EnableSocial
public class SocialConfig implements SocialConfigurer {

	@Autowired
	private SocialUserConnectionRepository socialUserConnectionRepository;

	@Override
	public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
//		TextEncryptor textEncryptor = Encryptors.text(GlobalConstants.TEXT_ENCRYPT_PASSWORD, KeyGenerators.string().generateKey());
		TextEncryptor textEncryptor = Encryptors.noOpText();
		SocialUsersConnectionRepositoryImpl repository = new SocialUsersConnectionRepositoryImpl(
				socialUserConnectionRepository, connectionFactoryLocator, textEncryptor);
		return repository;
	}

	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer,
			Environment environment) {
		LinkedInConnectionFactory linkedInConnectionFactory = new LinkedInConnectionFactory(
				environment.getProperty("spring.social.linkedin.clientId"),
				environment.getProperty("spring.social.linkedin.clientSecret"));

		GoogleConnectionFactory googleConnectionFactory = new GoogleConnectionFactory(
				environment.getProperty("spring.social.google.clientId"),
				environment.getProperty("spring.social.google.clientSecret"));
		// googleConnectionFactory.setScope(
		// "https://www.googleapis.com/auth/plus.login
		// https://www.googleapis.com/auth/plus.profile.emails.read");
		googleConnectionFactory.setScope(
				"email https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/plus.me "
				+ "https://www.googleapis.com/auth/tasks https://www.googleapis.com/auth/drive "
				+ "https://www.googleapis.com/auth/latitude.all.best");

		connectionFactoryConfigurer.addConnectionFactory(linkedInConnectionFactory);
		connectionFactoryConfigurer.addConnectionFactory(googleConnectionFactory);
	}

	@Override
	public UserIdSource getUserIdSource() {
		return new AuthenticationNameUserIdSource();
	}

}