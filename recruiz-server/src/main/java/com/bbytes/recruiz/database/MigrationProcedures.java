package com.bbytes.recruiz.database;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.MigrationProcedureInfo;
import com.bbytes.recruiz.repository.MigrationProcedureRepository;
import com.bbytes.recruiz.service.MigrationProceduresService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service("MigrationProcedures")
public class MigrationProcedures {

	private static final Logger logger = LoggerFactory.getLogger(MigrationProcedures.class);

	@Autowired
	private MigrationProcedureRepository migrationProcedureRepository;

	@Autowired
	private MigrationProceduresService migrationProceduresService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@PostConstruct
	public void init() throws Exception {

		List<String> existingTenants = tenantResolverService.findAllTenants();
		if (existingTenants != null && !existingTenants.isEmpty()) {
			for (String tenant : existingTenants) {
				try {
					TenantContextHolder.setTenant(tenant);
					migrateExistingClientInterviewer(tenant);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				} finally {
					TenantContextHolder.clearContext();
				}
			}
		}

	}

	// create and save migration info in database
	public void createMigrationObject(String procedureName, String procedureDescription, String version, boolean status, String tenantId) {
		MigrationProcedureInfo migrationInfo = new MigrationProcedureInfo();
		migrationInfo.setId(System.currentTimeMillis());
		migrationInfo.setCreationDate(new Date());
		migrationInfo.setModificationDate(new Date());
		migrationInfo.setDescription(procedureDescription);
		migrationInfo.setProcedureName(procedureName);
		migrationInfo.setExecutionState(status);
		migrationInfo.setTenantId(tenantId);
		migrationInfo.setVersion(version);

		migrationProcedureRepository.save(migrationInfo);
	}

	/**
	 * To Migrate existing client interviewer
	 */
	public void migrateExistingClientInterviewer(String tenantId) {

		String procedureName = "Migrate existing interviewer";
		String procedureDescription = "This procedure will update all existing interviewer to generic list. So that the existing interviewer be available in application as generic list as well.";
		String version = "1.0";
		boolean status = false;

		if (migrationProcedureRepository.findByProcedureNameVersionTenant(procedureName, version, tenantId) != null) {
			logger.info(procedureName + " already installed for organization " + tenantId);
			return;
		}
		createMigrationObject(procedureName, procedureDescription, version, status, tenantId);
		migrationProceduresService.migrateExistingClientInterviewer(procedureName, version, tenantId);

	}

}