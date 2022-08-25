package com.bbytes.recruiz.integration.sixth.sense;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SixthSenseSessionTokenStore {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseSessionTokenStore.class);
	
//	@Autowired
//	private ITokenStore securityTokenStore;
	
	private Map<String,String> securityTokenStore= new HashMap<>();
	
	private final String KEY_PREFIX = "SixthSense";

	public String getSixthSenseSessionId(String email, String tenantId) {

		String sessionIdFromKeyStorage = null;

		String sessionStorageKey = getSessionKey(email, tenantId);
		sessionIdFromKeyStorage = (String) securityTokenStore.get(sessionStorageKey);
		logger.error(" GETTING username from sessionIdFromKeyStorage: " + email +",  session id : " + sessionIdFromKeyStorage);
		return sessionIdFromKeyStorage;
	}

	public void addSixthSenseSessionId(String email, String tenantId, String sixthSenseSessionId) {
		String sessionStorageKey = getSessionKey(email, tenantId);
		securityTokenStore.put(sessionStorageKey, sixthSenseSessionId);
		logger.error(" ADDING username from sessionIdFromKeyStorage: " + email +",  session id : " + sixthSenseSessionId);
	}

	public void deleteSixthSenseSessionId(String email, String tenantId) {
		String sessionStorageKey = getSessionKey(email, tenantId);
		String sessionIdFromKeyStorage = (String) securityTokenStore.get(sessionStorageKey);
		securityTokenStore.remove(sessionStorageKey);
		logger.error("DELETING username from sessionIdFromKeyStorage: " + email +",  session id : " + sessionIdFromKeyStorage);
	}

	private String getSessionKey(String email, String tenantId) {
		String sessionStorageKey = KEY_PREFIX + ":" + email + ":" + tenantId;
		return sessionStorageKey;
	}

	public void deleteAndPutSixthSenseSessionId(String email, String tenantId, String newSessionId) {

		String sessionStorageKey = getSessionKey(email, tenantId);

		securityTokenStore.remove(sessionStorageKey);
		securityTokenStore.put(sessionStorageKey, newSessionId);
		logger.error(" DELETE AND ADDING username : " + email +",  session id : " + newSessionId);
	}
}
