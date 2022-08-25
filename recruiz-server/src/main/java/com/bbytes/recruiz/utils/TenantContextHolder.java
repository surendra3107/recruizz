package com.bbytes.recruiz.utils;

import com.bbytes.recruiz.service.SpringProfileService;

public class TenantContextHolder {

	private static final ThreadLocal<String> tenantIdentifier = new ThreadLocal<String>();

	public static String getTenant() {
		return tenantIdentifier.get();
	}

	public static void setTenant(String tenant) {
		if (SpringProfileService.runningSaasMode()) {
			tenantIdentifier.set(tenant);
		} else {
			tenantIdentifier.set(null);
		}
	}


	public static void clearContext() {
		tenantIdentifier.remove();
	}

	public static void setDefaultTenant() {
		// when we set null the default tenant is called
		tenantIdentifier.set(null);
	}

	public static boolean isDefaultTenantActive() {
		return tenantIdentifier.get() == null || tenantIdentifier.get().isEmpty() ? true : false;
	}

}
