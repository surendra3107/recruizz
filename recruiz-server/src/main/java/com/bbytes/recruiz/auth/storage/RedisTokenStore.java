package com.bbytes.recruiz.auth.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenStore implements ITokenStore {

	private static final String TOKEN_STORAGE_CACHE = "recruiz-token-store";

	@Autowired
	private CacheManager cacheManager;

	@Override
	public Object getToken(String key) {
		ValueWrapper value = cacheManager.getCache(TOKEN_STORAGE_CACHE).get(key);
		return value != null ? value.get() : null;
	}

	@Override
	public void putToken(String key, Object token) {
		cacheManager.getCache(TOKEN_STORAGE_CACHE).put(key, token);
	}

	@Override
	public void deleteToken(String key) {
		cacheManager.getCache(TOKEN_STORAGE_CACHE).evict(key);
	}

	@Override
	public void deleteAllToken() {
		cacheManager.getCache(TOKEN_STORAGE_CACHE).clear();
	}

	@Override
	public boolean isTokenAvailable(String key) {
		return getToken(key) != null;
	}

}
