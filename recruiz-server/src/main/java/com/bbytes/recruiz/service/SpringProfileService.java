package com.bbytes.recruiz.service;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SpringProfileService {

	private static boolean saasMode;

	private static boolean hybridMode;

	private static boolean prodMode;

	@Autowired
	private Environment env;

	@PostConstruct
	public void initProfileInfo() {
		saasMode = isSaasMode();
		prodMode = isProdMode();
		hybridMode = isHybridMode();
	}

	public boolean isDevMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
			return true;
		}
		return false;
	}

	public boolean isTestMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			return true;
		}
		return false;
	}

	public boolean isProdMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("prod") || Arrays.asList(env.getActiveProfiles()).contains("default")
				|| Arrays.asList(env.getActiveProfiles()).isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean isSaasMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("saas") || Arrays.asList(env.getActiveProfiles()).contains("hybrid")
				|| Arrays.asList(env.getActiveProfiles()).contains("default") || Arrays.asList(env.getActiveProfiles()).isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean isEnterpriseMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("enterprise")) {
			return true;
		}
		return false;

	}

	public boolean isHybridMode() {
		if (Arrays.asList(env.getActiveProfiles()).contains("hybrid")) {
			return true;
		}
		return false;

	}

	public static boolean runningSaasMode() {
		return saasMode || hybridMode;
	}

	public static boolean runningProdMode() {
		return prodMode;
	}
	
	public static boolean runningHybridMode() {
		return hybridMode;
	}
}
