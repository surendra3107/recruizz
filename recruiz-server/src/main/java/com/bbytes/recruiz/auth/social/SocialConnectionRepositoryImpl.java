package com.bbytes.recruiz.auth.social;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bbytes.recruiz.domain.SocialUserConnection;
import com.bbytes.recruiz.repository.SocialUserConnectionRepository;

public class SocialConnectionRepositoryImpl implements ConnectionRepository {

	private final String userId;

	private final ConnectionFactoryLocator connectionFactoryLocator;

	private SocialUserConnectionRepository socialUserConnectionRepository;

	private final TextEncryptor textEncryptor;

	public SocialConnectionRepositoryImpl(String userId, SocialUserConnectionRepository socialUserConnectionRepository,
			ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.socialUserConnectionRepository = socialUserConnectionRepository;
		this.userId = userId;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public MultiValueMap<String, Connection<?>> findAllConnections() {
		List<Connection<?>> resultList = connectionMapper
				.mapEntities(socialUserConnectionRepository.findBySocialUserConnectionKeyUserId(userId));

		MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
		Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
		for (String registeredProviderId : registeredProviderIds) {
			connections.put(registeredProviderId, Collections.<Connection<?>> emptyList());
		}
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			if (connections.get(providerId).size() == 0) {
				connections.put(providerId, new LinkedList<Connection<?>>());
			}
			connections.add(providerId, connection);
		}
		return connections;
	}

	public List<Connection<?>> findConnections(String providerId) {
		return connectionMapper.mapEntities(socialUserConnectionRepository
				.findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(userId, providerId));
	}

	@SuppressWarnings("unchecked")
	public <A> List<Connection<A>> findConnections(Class<A> apiType) {
		List<?> connections = findConnections(getProviderId(apiType));
		return (List<Connection<A>>) connections;
	}

	public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUsers) {
		if (providerUsers == null || providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}

		MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();

		for (String providerId : providerUsers.keySet()) {
			List<String> provideUserIds = providerUsers.get(providerId);
			List<Connection<?>> resultList = getConnections(providerId, provideUserIds);

			for (Connection<?> connection : resultList) {
				List<String> userIds = providerUsers.get(providerId);
				List<Connection<?>> connections = connectionsForUsers.get(providerId);
				if (connections == null) {
					connections = new ArrayList<Connection<?>>(userIds.size());
					for (int i = 0; i < userIds.size(); i++) {
						connections.add(null);
					}
					connectionsForUsers.put(providerId, connections);
				}
				String providerUserId = connection.getKey().getProviderUserId();
				int connectionIndex = userIds.indexOf(providerUserId);
				connections.set(connectionIndex, connection);
			}
		}

		return connectionsForUsers;
	}

	public List<Connection<?>> getConnections(String providerId, List<String> providerUserIds) {
		return connectionMapper.mapEntities(socialUserConnectionRepository
				.findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserIdIn(providerId,
						providerUserIds));

	}

	public Connection<?> getConnection(ConnectionKey connectionKey) {
		try {
			return connectionMapper.mapEntity(socialUserConnectionRepository.findOne(SocialUserConnection.getKey(userId,
					connectionKey.getProviderId(), connectionKey.getProviderUserId())));
		} catch (EmptyResultDataAccessException e) {
			throw new NoSuchConnectionException(connectionKey);
		}
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
		if (connection == null) {
			throw new NotConnectedException(providerId);
		}
		return connection;
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) findPrimaryConnection(providerId);
	}

	@Transactional
	public void addConnection(Connection<?> connection) {
		try {
			ConnectionData data = connection.createData();
			List<SocialUserConnection> resultList = socialUserConnectionRepository
					.findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(userId,
							data.getProviderId());

			int rank = 0;
			if (!resultList.isEmpty()) {
				Collections.sort(resultList, new Comparator<SocialUserConnection>() {
					@Override
					public int compare(SocialUserConnection o1, SocialUserConnection o2) {
						return o1.getRank() - o2.getRank();
					}
				});

				rank = resultList.get(0).getRank();
			}

			SocialUserConnection su = createConnection(userId, rank + 1, connection);
			su = socialUserConnectionRepository.save(su);

		} catch (DuplicateKeyException e) {
			throw new DuplicateConnectionException(connection.getKey());
		}
	}

	@Transactional
	public void updateConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();

		SocialUserConnection su = socialUserConnectionRepository
				.findOne(SocialUserConnection.getKey(userId, data.getProviderId(), data.getProviderUserId()));
		if (su != null) {
			su.setDisplayName(data.getDisplayName());
			su.setProfileUrl(data.getProfileUrl());
			su.setImageUrl(data.getImageUrl());
			su.setAccessToken(encrypt(data.getAccessToken()));
			su.setSecret(encrypt(data.getSecret()));
			su.setRefreshToken(encrypt(data.getRefreshToken()));
			su.setExpireTime(data.getExpireTime());

			su = socialUserConnectionRepository.save(su);
		}
	}

	public SocialUserConnection createConnection(String userId, Integer rank, Connection<?> connection) {
		ConnectionData data = connection.createData();

		SocialUserConnection socialUserConnection = new SocialUserConnection();
		socialUserConnection.setUserId(userId);
		socialUserConnection.setProviderId(data.getProviderId());
		socialUserConnection.setProviderUserId(data.getProviderUserId());

		socialUserConnection.setAccessToken(data.getAccessToken());
		socialUserConnection.setDisplayName(data.getDisplayName());
		socialUserConnection.setExpireTime(data.getExpireTime());
		socialUserConnection.setImageUrl(data.getImageUrl());
		socialUserConnection.setProfileUrl(data.getProfileUrl());
		socialUserConnection.setRank(rank);
		socialUserConnection.setRefreshToken(data.getRefreshToken());
		socialUserConnection.setSecret(data.getSecret());

		return socialUserConnection;

	}

	@Transactional
	public void removeConnections(String providerId) {
		List<SocialUserConnection> connections = socialUserConnectionRepository
				.findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(userId, providerId);
		socialUserConnectionRepository.delete(connections);
	}

	@Transactional
	public void removeConnection(ConnectionKey connectionKey) {
		socialUserConnectionRepository.delete(
				SocialUserConnection.getKey(userId, connectionKey.getProviderId(), connectionKey.getProviderUserId()));
	}

	private Connection<?> findPrimaryConnection(String providerId) {
		List<SocialUserConnection> resultList = socialUserConnectionRepository
				.findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(userId, providerId);

		if (resultList.isEmpty())
			return null;

		// sort high to low
		Collections.sort(resultList, new Comparator<SocialUserConnection>() {
			@Override
			public int compare(SocialUserConnection o1, SocialUserConnection o2) {
				return o2.getRank() - o1.getRank();
			}
		});

		List<Connection<?>> connections = connectionMapper.mapEntities(resultList);
		if (connections.size() > 0) {
			return connections.get(0);
		} else {
			return null;
		}
	}

	private final ServiceProviderConnectionMapper connectionMapper = new ServiceProviderConnectionMapper();

	private final class ServiceProviderConnectionMapper {

		public List<Connection<?>> mapEntities(List<SocialUserConnection> socialUsers) {
			List<Connection<?>> result = new ArrayList<Connection<?>>();
			for (SocialUserConnection su : socialUsers) {
				result.add(mapEntity(su));
			}
			return result;
		}

		public Connection<?> mapEntity(SocialUserConnection socialUser) {
			ConnectionData connectionData = mapConnectionData(socialUser);
			ConnectionFactory<?> connectionFactory = connectionFactoryLocator
					.getConnectionFactory(connectionData.getProviderId());
			return connectionFactory.createConnection(connectionData);
		}

		private ConnectionData mapConnectionData(SocialUserConnection socialUser) {
			return new ConnectionData(socialUser.getProviderId(), socialUser.getProviderUserId(),
					socialUser.getDisplayName(), socialUser.getProfileUrl(), socialUser.getImageUrl(),
					decrypt(socialUser.getAccessToken()), decrypt(socialUser.getSecret()),
					decrypt(socialUser.getRefreshToken()), expireTime(socialUser.getExpireTime()));
		}

		private String decrypt(String encryptedText) {
			return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
		}

		private Long expireTime(Long expireTime) {
			return expireTime == null || expireTime == 0 ? null : expireTime;
		}

	}

	private <A> String getProviderId(Class<A> apiType) {
		return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
	}

	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}
}