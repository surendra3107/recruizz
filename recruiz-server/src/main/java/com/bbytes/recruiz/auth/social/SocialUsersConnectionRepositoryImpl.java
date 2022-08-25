package com.bbytes.recruiz.auth.social;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

import com.bbytes.recruiz.domain.SocialUserConnection;
import com.bbytes.recruiz.repository.SocialUserConnectionRepository;

public class SocialUsersConnectionRepositoryImpl implements UsersConnectionRepository {

	private final SocialUserConnectionRepository socialUserConnectionRepository;

	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;
	
	public SocialUsersConnectionRepositoryImpl(SocialUserConnectionRepository socialUserConnectionRepository,
			ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.socialUserConnectionRepository = socialUserConnectionRepository;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	@Override
	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		ConnectionKey key = connection.getKey();
		List<SocialUserConnection> socialUserConnections = socialUserConnectionRepository
				.findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserId(key.getProviderId(),
						key.getProviderUserId());
		List<String> localUserIds = new ArrayList<>();
		for (SocialUserConnection socialUserConnection : socialUserConnections) {
			localUserIds.add(socialUserConnection.getUserId());
		}
		
		return localUserIds;
	}

	@Override
	public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
		List<SocialUserConnection> socialUserConnections = socialUserConnectionRepository
				.findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserIdIn(providerId,
						providerUserIds);
		Set<String> localUserIds = new HashSet<String>();
		for (SocialUserConnection socialUserConnection : socialUserConnections) {
			localUserIds.add(socialUserConnection.getUserId());
		}

		return localUserIds;
	}

	@Override
	public ConnectionRepository createConnectionRepository(String userId) {
		return new SocialConnectionRepositoryImpl(userId, socialUserConnectionRepository, connectionFactoryLocator,
				textEncryptor);
	}

}
