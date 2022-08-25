package com.bbytes.recruiz.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ThreadTenantSecurityUtil {

	public static void setAuthAndTenantThreadContext(String tenantId, Authentication auth) {
		// this block is required if the method runs in s new thread
		TenantContextHolder.setTenant(tenantId);

		SecurityContext ctx = SecurityContextHolder.createEmptyContext();
		ctx.setAuthentication(auth);
		SecurityContextHolder.setContext(ctx);

	}
}
