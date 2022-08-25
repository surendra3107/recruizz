package com.bbytes.recruiz.service;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.utils.AppSettingsGenerator;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.google.common.base.Splitter;

@Service
public class CheckAppSettingsService {

	private static Logger logger = LoggerFactory.getLogger(CheckAppSettingsService.class);

	@Autowired
	private UserService userService;

	@Autowired
	private ParserCountService parserCountService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private OrganizationService orgService;

	@Autowired
	private OrganizationConfigurationService organizationConfigurationService;
	
	@Autowired
	private SpringProfileService springProfileService;
	

	public Map<String, String> getOrgSettingsMap() {
		String encryptedKey = null;
		OrganizationConfiguration orgConfig = orgService.getCurrentOrganization().getOrganizationConfiguration();
		if (orgConfig != null)
			encryptedKey = userService.getLoggedInUserObject().getOrganization().getOrganizationConfiguration()
					.getSettingInfo();
		if (encryptedKey == null || encryptedKey.isEmpty()) {
			return null;
		}
		String decryptedKey = EncryptKeyUtils.getDecryptedKey(encryptedKey).replace("{", "").replace("}", "")
				.replace(" ", "");
		Map<String, String> settingsMap = Splitter.on(",").withKeyValueSeparator("=").split(decryptedKey);
		return settingsMap;
	}

	@Transactional(readOnly = true)
	public boolean isUserLimitExceeded(String userType) {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String userLimit = settingsMap.get("max.user.count").toString();
		if (userLimit == null || userLimit.isEmpty()) {
			return true;
		} else if (userLimit.equalsIgnoreCase("-1")) {
			return false;
		}
		Long appUserCount = userService.getUserCountByType(userType);
		if (Long.parseLong(userLimit) > appUserCount) {
			return false;
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean isVendorUserLimitExceeded(String vendorId) {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String vendorUserLimit = settingsMap.get("vendor.user.count");
		if (vendorUserLimit == null || vendorUserLimit.isEmpty()) {
			return true;
		} else if (vendorUserLimit.equalsIgnoreCase("-1")) {
			return false;
		}
		Long vendorUserCount = userService.getVendorUserCount(vendorId);
		if (Long.parseLong(vendorUserLimit) > vendorUserCount) {
			return false;
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean isVendorFeatureEnabled() {
		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return false;
		}

		String enabled = settingsMap.get("vendor.feature");
		if (enabled == null || enabled.isEmpty()) {
			return false;
		} else if (enabled.equalsIgnoreCase("on")) {
			return true;
		}

		return false;
	}

	@Transactional(readOnly = true)
	public boolean isVendorLimitExceeded() {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String vendorUserLimit = settingsMap.get("vendor.count");
		if (vendorUserLimit == null || vendorUserLimit.isEmpty()) {
			return true;
		} else if (vendorUserLimit.equalsIgnoreCase("-1")) {
			return false;
		}
		Long vendorCount = userService.getVendorCount();
		if (Long.parseLong(vendorUserLimit) > vendorCount) {
			return false;
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean isPositionSocialSharingFeatureEnabled() {
		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return false;
		}

		String enabled = settingsMap.get("position.social.sharing");
		if (enabled == null || enabled.isEmpty()) {
			return false;
		} else if (enabled.equalsIgnoreCase("on")) {
			return true;
		}
		return false;
	}

	@Transactional(readOnly = true)
	public boolean isAdvancedSearchFeatureEnabled() {
		
		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return false;
		}

		String enabled = settingsMap.get("advanced.search");
		if (enabled == null || enabled.isEmpty()) {
			return false;
		} else if (enabled.equalsIgnoreCase("on")) {
			return true;
		}
		return false;
	}

	
	public boolean isResumeParserLimitExceeded() {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String maxParserLimit = settingsMap.get("max.parser.count");
		if (maxParserLimit == null || maxParserLimit.isEmpty()) {
			return true;
		} else if (maxParserLimit.equalsIgnoreCase("-1")) {
			return false;
		}
		// Long parserCount = parserCountService.count();
		Long parserMaxId = parserCountService.getMaxId();
		// if (parserCount.longValue() != parserMaxId.longValue()) {
		// return true;
		// }
		if (Long.parseLong(maxParserLimit) > parserMaxId) {
			return false;
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean isValidityExpired() {

		if(springProfileService.isDevMode() || springProfileService.isTestMode()){
			return false;
		}
		
		try {
			Map<String, String> settingsMap = getOrgSettingsMap();
			if (settingsMap == null || settingsMap.isEmpty()) {
				return true;
			}

			// commenting not applicable in cloud

			// OrganizationConfiguration config =
			// userService.getLoggedInUserObject().getOrganization()
			// .getOrganizationConfiguration();
			// String lastVerified = config.getLastVerified();
			// if (lastVerified == null || lastVerified.isEmpty()) {
			// return true;
			// }
			// String decryptedlastVerified =
			// EncryptKeyUtils.getDecryptedKey(lastVerified);
			// if (decryptedlastVerified == null ||
			// decryptedlastVerified.isEmpty()) {
			// return true;
			// }
			// Date lastVerifiedTime = new
			// Date(Long.parseLong(decryptedlastVerified));
			// if (lastVerifiedTime.after(new Date())) {
			// return true;
			// }

			String expireOn = settingsMap.get("date.expire.on");
			Date todaysDate = AppSettingsGenerator.getCurrentDateTime(); // getInternetTime();
			Date expiryDate = new Date(Long.parseLong(expireOn));

			if (todaysDate.after(expiryDate)) {
				return true;
			}

			// commenting not applicable in cloud

			// config.setLastVerified(EncryptKeyUtils.getEncryptedKey(new
			// Date().getTime() + ""));
			// organizationConfigurationService.save(config);
			return false;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return true;
		}
	}

	private Date getInternetTime() {
		Date time = new Date();
		return time;
	}

	/**
	 * to check department user limit
	 * 
	 * @param userType
	 * @return
	 */
	@Transactional(readOnly = true)
	public boolean isDepratmentHeadUserExceeded(String userType) {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String departmentHeadUser = settingsMap.get("max.department.head.user");
		if (departmentHeadUser == null || departmentHeadUser.isEmpty()) {
			return true;
		} else if (departmentHeadUser.equalsIgnoreCase("-1")) {
			return false;
		}
		Long currentDepartmentUserCount = userService.getUserCountByType(userType);
		if (Long.parseLong(departmentHeadUser) > currentDepartmentUserCount) {
			return false;
		}
		return true;
	}

	/**
	 * To check if candidate count is exceeded
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public boolean isCandidateCountExceeded() {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String maxCandidateCount = settingsMap.get("max.candidate.count");
		if (maxCandidateCount == null || maxCandidateCount.isEmpty()) {
			return true;
		} else if (maxCandidateCount.equalsIgnoreCase("-1")) {
			return false;
		}
		Long currentCandidateCount = candidateService.count();
		if (Long.parseLong(maxCandidateCount) > currentCandidateCount) {
			return false;
		}
		return true;
	}

	/**
	 * to check if the email usage count is exceeded or not
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public boolean isEmailUsageLimitExceeded(int toBeUsedSize) {

		Map<String, String> settingsMap = getOrgSettingsMap();
		if (settingsMap == null || settingsMap.isEmpty()) {
			return true;
		}

		String allowedEmailUsageLimit = "-1";
		if (settingsMap.containsKey("email.usage.count")) {
			allowedEmailUsageLimit = settingsMap.get("email.usage.count").trim();
		}

		if (allowedEmailUsageLimit.equalsIgnoreCase("-1")) {
			return false;
		}
		int usageCount = orgService.getCurrentOrganization().getOrganizationConfiguration().getBulkEmailUsed();
		if (Integer.parseInt(allowedEmailUsageLimit) > usageCount) {
			if (Integer.parseInt(allowedEmailUsageLimit) < (usageCount + toBeUsedSize)) {
				return true;
			}
			return false;
		}
		return true;
	}

}
