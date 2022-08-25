package com.bbytes.recruiz;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.database.UpdateDBTemplateService;
import com.bbytes.recruiz.search.ElasticsearchReIndexService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;

@Component
public class AppInit {

	private static final Logger logger = LoggerFactory.getLogger(AppInit.class);

	@Value("${db.templates.update}")
	private boolean isTemplateUpdateable;

	@Autowired
	private ElasticsearchReIndexService elasticsearchReIndexService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UpdateDBTemplateService updateDBTemplateService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;
	
	@PostConstruct
	protected void init() {

		// start elastic search reindex , it is a async method so will not block
		// main thread
		// @sajin -- Commenting reindex coz its hogging lot of connections to DB
		
		elasticsearchReIndexService.reindex();

		updateTemplateForAllTenants();

	}

	/**
	 * 
	 */
	private void updateTemplateForAllTenants() {
		// this flag will check if the templates should be updated in database
		if (!isTemplateUpdateable) {
			return;
		}

		List<String> existingTenants = tenantResolverService.findAllTenants();

		if (existingTenants != null && !existingTenants.isEmpty()) {
			for (String tenant : existingTenants) {
				try {
					//updateDBTemplateService.updateDatabaseTemplateFromFile(tenant,null);
					tenantUsageStatService.createUsageTableForTenant(tenant);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		}
	}

}
