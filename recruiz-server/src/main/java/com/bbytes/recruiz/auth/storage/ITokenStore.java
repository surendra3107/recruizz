package com.bbytes.recruiz.auth.storage;

public interface ITokenStore {

	public Object getToken(String key);

	public void putToken(String key, Object token);

	public void deleteToken(String key);

	public void deleteAllToken();

	public boolean isTokenAvailable(String key);

}
