package com.bbytes.recruiz.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author Michael Lavelle
 */
@Entity
@Table(name = "user_social_connection", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "userId", "providerId", "rank" }) })
public class SocialUserConnection {

	@EmbeddedId
	private SocialUserConnectionKey socialUserConnectionKey = new SocialUserConnectionKey();

	private String accessToken;
	private String displayName;
	private Long expireTime;
	private String imageUrl;
	private String profileUrl;
	private int rank;
	private String refreshToken;
	private String secret;

	public String getProviderId() {
		return socialUserConnectionKey.getProviderId();
	}

	public void setProviderId(String providerId) {
		socialUserConnectionKey.setProviderId(providerId);
	}

	public String getProviderUserId() {
		return socialUserConnectionKey.getProviderUserId();
	}

	public void setProviderUserId(String providerUserId) {
		socialUserConnectionKey.setProviderUserId(providerUserId);
	}

	public String getUserId() {
		return socialUserConnectionKey.getUserId();
	}

	public SocialUserConnectionKey getUserConnectionKeySocial() {
		return socialUserConnectionKey;
	}

	public void setUserConnectionKeySocial(SocialUserConnectionKey socialUserConnectionKey) {
		this.socialUserConnectionKey = socialUserConnectionKey;
	}

	public void setUserId(String userId) {
		socialUserConnectionKey.setUserId(userId);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public static SocialUserConnectionKey getKey(String userId, String providerId, String providerUserId) {
		SocialUserConnectionKey key = new SocialUserConnectionKey();
		key.setProviderId(providerId);
		key.setProviderUserId(providerUserId);
		key.setUserId(userId);
		return key;
	}

}