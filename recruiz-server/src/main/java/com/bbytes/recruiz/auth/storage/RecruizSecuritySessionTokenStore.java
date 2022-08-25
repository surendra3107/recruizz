package com.bbytes.recruiz.auth.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecruizSecuritySessionTokenStore {

	@Autowired
	private ITokenStore tokenStore;

	private final String KEY_PREFIX = "Recruiz";

	public String getSecurityToken(String email, String tenantId, String deviceType) {

		String tokenDataHolderFromKeyStorage = null;

		String jwtStringTokenFromKeyStorage = getTokenKey(email, tenantId, deviceType);
		tokenDataHolderFromKeyStorage = (String) tokenStore.getToken(jwtStringTokenFromKeyStorage);

		return tokenDataHolderFromKeyStorage;
	}

	public void addSecurityToken(String email, String tenantId, String deviceType, String jwtStringToken) {

		String jwtStringTokenFromKeyStorage = getTokenKey(email, tenantId, deviceType);

		tokenStore.putToken(jwtStringTokenFromKeyStorage, jwtStringToken);
	}

	public void deleteSecurityToken(String email, String tenantId, String deviceType) {

		String jwtStringTokenFromKeyStorage = getTokenKey(email, tenantId, deviceType);

		tokenStore.deleteToken(jwtStringTokenFromKeyStorage);
	}

	private String getTokenKey(String email, String tenantId, String deviceType) {
		String jwtStringTokenFromKeyStorage = KEY_PREFIX + ":" + email + ":" + tenantId + ":" + deviceType;
		return jwtStringTokenFromKeyStorage;
	}

}
