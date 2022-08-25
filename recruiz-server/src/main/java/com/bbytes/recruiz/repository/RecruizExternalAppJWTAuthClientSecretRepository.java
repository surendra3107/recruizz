package com.bbytes.recruiz.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.jwt.IExternalAppJWTAuthClientSecretRepository;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Repository
@Primary
public class RecruizExternalAppJWTAuthClientSecretRepository implements IExternalAppJWTAuthClientSecretRepository {

	private static final Logger logger = LoggerFactory.getLogger(RecruizExternalAppJWTAuthClientSecretRepository.class);

	@Autowired
	private IntegrationProfileDetailsRepository integrationProfileDetailsRepository;

	@Value("${career.site.app.id:recruiz_career_site}")
	private String careerSiteAppId;

	@Value("${career.site.jwt.secret:R4sKPzxNgUpRX3356Z8xvtauVB54epVP}")
	private String careerJWTSecret;

	@Value("${plutus.site.app.id:plutus_app_id}")
	private String plutusSiteAppId;

	@Value("${plutus.site.jwt.secret:L2ArgZNq3L6FiIkN}")
	private String plutusJWTSecret;

	@Override
	public String getClientSecret(String appId, String clientId) {
		if (careerSiteAppId.equals(appId)) {
			return careerJWTSecret;
		} else if (plutusSiteAppId.equals(appId)) {
			return plutusJWTSecret;
		} else {
			TenantContextHolder.setTenant(clientId);
			String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
			IntegrationProfileDetails integrationProfileDetails = integrationProfileDetailsRepository
					.findByUserEmailAndIntegrationModuleType(defaulOrgEmail, appId);
			if (integrationProfileDetails != null) {
				String clientSecret = integrationProfileDetails.getIntegrationDetails().get(IntegrationConstants.EXTRANAL_CLIENT_SECRET);
				return clientSecret;
			}
		}

		logger.error("Given appId '"+appId+"' is incorrect or missing to fetch the jwt secret for recruiz external app JWT auth ");
		
		return null;

	}

}