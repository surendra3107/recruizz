package com.bbytes.recruiz.auth.jwt;

import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.core.userdetails.UserDetails;

import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;

import lombok.Data;

@Data
public final class TokenDataHolder {

	private final UserDetails user;
	private final String tenantId;
	private final WebMode webMode;
	private WebRequestMode webRequestMode = WebRequestMode.WEB_APP;
	private DateTimeZone userTimeZone;
	private Locale userLocale;
	private Date creationTime;

	public TokenDataHolder(UserDetails user, String tenantId, WebMode webMode, DateTimeZone userTimeZone, Locale userLocale) {
		this(user, tenantId, webMode);
		this.userTimeZone = userTimeZone;
		this.userLocale = userLocale;
	}

	public TokenDataHolder(UserDetails user, String tenantId, WebMode webMode, WebRequestMode webRequestMode, DateTimeZone userTimeZone,
			Locale userLocale) {
		this(user, tenantId, webMode, userTimeZone, userLocale);
		this.webRequestMode = webRequestMode;
	}

	public TokenDataHolder(UserDetails user, String tenantId, WebMode webMode, WebRequestMode webRequestMode) {
		this(user, tenantId, webMode);
		this.webRequestMode = webRequestMode;
	}

	public TokenDataHolder(UserDetails user, String tenantId, WebMode webMode) {
		this.user = user;
		this.tenantId = tenantId;
		this.webMode = webMode;
		this.creationTime = DateTime.now().toDate();
	}
}
