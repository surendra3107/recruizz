package com.bbytes.recruiz.search;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.search.repository.CandidateSearchRepo;
import com.bbytes.recruiz.search.repository.ClientSearchRepo;
import com.bbytes.recruiz.search.repository.PositionRequestSearchRepo;
import com.bbytes.recruiz.search.repository.PositionSearchRepo;
import com.bbytes.recruiz.search.repository.ProspectSearchRepo;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.search.repository.UserSearchRepo;
import com.bbytes.recruiz.service.TenantResolverService;

@Service
public class ElasticsearchReIndexService {

	private static Logger logger = LoggerFactory.getLogger(ElasticsearchReIndexService.class);

	@Resource
	private Environment environment;

	@Autowired
	private ClientSearchRepo clientSearchRepo;

	@Autowired
	private CandidateSearchRepo candidateSearchRepo;

	@Autowired
	private UserSearchRepo userSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private PositionSearchRepo positionSearchRepo;
	
	@Autowired
	private ProspectSearchRepo prospectSearchRepo;

	@Autowired
	private PositionRequestSearchRepo positionRequestSearchRepo;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private ElasticsearchReIndexPerTenantService elasticsearchReIndexPerTenantService;

	@Async
	public void reindex() {

		String reindexOn = environment.getProperty("elasticsearch.reindex");
		// if re-index turned off then return
		if (!"true".equalsIgnoreCase(reindexOn.trim())){
			logger.info("Skipping elastic search re-indexing step.. ");
			return;
		}
			

		logger.info("Sleep for 5 secs before reindex ..jus to make sure server is started");
		try {
			// start the reindex after 5 secs
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}

		logger.debug("Starting to clear data in all index");
		
		
		clientSearchRepo.deleteAll();
		prospectSearchRepo.deleteAll();
		positionSearchRepo.deleteAll();
		positionRequestSearchRepo.deleteAll();
		candidateSearchRepo.deleteAll();
		userSearchRepo.deleteAll();
		suggestSearchRepo.deleteAll();

		logger.info("Done deleting data in all index");

		List<String> allTenants = tenantResolverService.findAllTenants();
		try {
			for (String tenantId : allTenants) {
				try {
					// start the reindex for each tenant after a delay of 1 secs
					// to avoid load on es server
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException ex) {
					logger.error(ex.getMessage(), ex);
				}
						
				elasticsearchReIndexPerTenantService.runIndexForTenant(tenantId);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void reindexTenantData(String tenantId) {
			
		clientSearchRepo.deleteByTenantName(tenantId);
		prospectSearchRepo.deleteByTenantName(tenantId);
		candidateSearchRepo.deleteByTenantName(tenantId);
		candidateSearchRepo.deleteByTenantName(tenantId);
		positionSearchRepo.deleteByTenantName(tenantId);
		prospectSearchRepo.deleteByTenantName(tenantId);
		suggestSearchRepo.deleteByTenantName(tenantId);
		userSearchRepo.deleteByTenantName(tenantId);
		
		elasticsearchReIndexPerTenantService.runIndexForTenant(tenantId);
	}

}
