package com.bbytes.recruiz.auth.jwt;

import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class MultiRequestSecurityContextHolder {
	// ~ Static fields/initializers
	// =====================================================================================

	private Map<String,Authentication> emailToAuthentication = new WeakHashMap<>();

	// ~ Methods
	// ========================================================================================================

	public void clearAuthentication(String email) {
		this.emailToAuthentication.remove(email);
	}

	public Authentication getAuthentication(String email) {
		return this.emailToAuthentication.get(email);
	}

	public void setAuthentication(String email,Authentication authentication) {
		this.emailToAuthentication.put(email, authentication);
	}

}
