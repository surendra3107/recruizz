package com.bbytes.recruiz.auth.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.joda.time.DateTimeZone;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MultiTenantAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = -7643009266392747593L;

	private Object credentials;
	private Object principal;
	private String tenantId;
	private DateTimeZone userTimeZone;
	private Locale userLocale;

	private WebMode webMode = WebMode.DASHBOARD;

	private WebRequestMode webRequestMode = WebRequestMode.WEB_APP;

	private ExternalUserAccessDataHolder externalUserAccessDataHolder;

	private UserDetails user;
	private boolean authenticated = true;

	public MultiTenantAuthenticationToken(String tenantId, UserDetails user, WebMode webMode) {
		super(null);
		this.tenantId = tenantId;
		this.user = user;
		this.webMode = webMode;
	}

	public MultiTenantAuthenticationToken(String tenantId, UserDetails user, WebMode webMode, WebRequestMode webRequestMode) {
		super(null);
		this.tenantId = tenantId;
		this.user = user;
		this.webMode = webMode;
		this.webRequestMode = webRequestMode;
	}

	public MultiTenantAuthenticationToken(String tenantId, UserDetails user, WebMode webMode, WebRequestMode webRequestMode,
			DateTimeZone userTimeZone, Locale userLocale) {
		super(null);
		this.tenantId = tenantId;
		this.user = user;
		this.webMode = webMode;
		this.userLocale = userLocale;
		this.userTimeZone = userTimeZone;
		this.webRequestMode = webRequestMode;
	}

	public MultiTenantAuthenticationToken(Object principal, Object credentials, String tenantId, WebMode webMode,
			WebRequestMode webRequestMode) {
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		this.tenantId = tenantId;
		this.webMode = webMode;
		this.webRequestMode = webRequestMode;
	}

	public MultiTenantAuthenticationToken(Object principal, Object credentials, String tenantId, WebMode webMode, DateTimeZone userTimeZone,
			Locale userLocale) {
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		this.tenantId = tenantId;
		this.webMode = webMode;
		this.userLocale = userLocale;
		this.userTimeZone = userTimeZone;
	}

	@JsonIgnore
	public Object getCredentials() {
		if (this.credentials != null)
			return this.credentials;
		else {
			if (this.user == null)
				return null;

			return this.user.getPassword();
		}
	}

	public Object getPrincipal() {
		if (this.principal != null)
			return this.principal;
		else {
			if (this.user == null)
				return null;

			return this.user.getUsername();
		}

	}

	public String getTenantId() {
		return tenantId;
	}

	public WebMode getWebMode() {
		return webMode;
	}

	public void setWebMode(WebMode webMode) {
		this.webMode = webMode;
	}

	/**
	 * @return the webRequestMode
	 */
	public WebRequestMode getWebRequestMode() {
		return webRequestMode;
	}

	/**
	 * @param webRequestMode
	 *            the webRequestMode to set
	 */
	public void setWebRequestMode(WebRequestMode webRequestMode) {
		this.webRequestMode = webRequestMode;
	}

	@Override
	public String getName() {
		return getPrincipal().toString();
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		if (this.user == null)
			return new ArrayList<GrantedAuthority>();
		
		return (Collection<GrantedAuthority>) this.user.getAuthorities();
	}

	@Override
	@JsonIgnore
	public UserDetails getDetails() {
		return user;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public ExternalUserAccessDataHolder getExternalUserAccessDataHolder() {
		return externalUserAccessDataHolder;
	}

	public void setExternalUserAccessDataHolder(ExternalUserAccessDataHolder externalUserAccessDataHolder) {
		this.externalUserAccessDataHolder = externalUserAccessDataHolder;
	}

	public DateTimeZone getUserTimeZone() {
		return userTimeZone;
	}

	public void setUserTimeZone(DateTimeZone userTimeZone) {
		this.userTimeZone = userTimeZone;
	}

	public Locale getUserLocale() {
		return userLocale;
	}

	public void setUserLocale(Locale userLocale) {
		this.userLocale = userLocale;
	}
}
