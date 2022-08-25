package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.SocialUserConnection;
import com.bbytes.recruiz.domain.SocialUserConnectionKey;

public interface SocialUserConnectionRepository extends JpaRepository<SocialUserConnection, SocialUserConnectionKey> {

	List<SocialUserConnection> findBySocialUserConnectionKeyUserId(String userId);
	
	List<SocialUserConnection> findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(String userId,
			String providerId);
	
	List<SocialUserConnection> findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserId(String providerId,
			String providerUserId);
	
	List<SocialUserConnection> findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserIdIn(String providerId,
			Set<String> providerUserIds);
	
	List<SocialUserConnection> findBySocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserIdIn(String providerId,
			List<String> providerUserIds);
	
	List<SocialUserConnection> findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderUserIdIn(String userId,
			Set<String> providerUserIds);
	
	SocialUserConnection findBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserId(String userId, String providerId, String providerUserId);
	
	void deleteBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderId(String userId, String providerId);

    void deleteBySocialUserConnectionKeyUserIdAndSocialUserConnectionKeyProviderIdAndSocialUserConnectionKeyProviderUserId(String userId, String providerId, String providerUserId);
	

}