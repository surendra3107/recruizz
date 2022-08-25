package com.bbytes.recruiz.repository;

import java.text.MessageFormat;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bbytes.recruiz.domain.MigrationProcedureInfo;

@Repository
public class MigrationProcedureRepository {

    private static final Logger logger = LoggerFactory.getLogger(MigrationProcedureRepository.class);

    private JdbcTemplate jdbcMigrationTemplate;

    @Autowired
    @Qualifier("tenant-mgmt")
    private DataSource tenantMgMtDataSource;

    @PostConstruct
    public void init() {
	jdbcMigrationTemplate = new JdbcTemplate(tenantMgMtDataSource);
    }

    /**
     * To get migration info using procedure name, procedure version and Tenant
     * ID
     * 
     * @param procedureName
     * @param version
     * @param tenantId
     * @return
     */
    public MigrationProcedureInfo findByProcedureNameVersionTenant(String procedureName, String version, String tenantId) {

	MessageFormat query = new MessageFormat(
		"SELECT * FROM migration_procedure_info where procedure_name = ''{0}'' AND version = ''{1}'' AND tenant_id=''{2}'' ");

	MigrationProcedureInfo migrationInfo = null;
	try {
	    String sql = query.format(new Object[] { procedureName, version, tenantId });
	    migrationInfo = jdbcMigrationTemplate.queryForObject(sql,
		    new BeanPropertyRowMapper<MigrationProcedureInfo>(MigrationProcedureInfo.class));
	} catch (DataAccessException e) {
	   // logger.error(e.getMessage(), e);
	}

	return migrationInfo;
    }

    /**
     * to save procedure info in database
     * 
     * @param migrationInfo
     * @return
     */
    public boolean save(MigrationProcedureInfo migrationInfo) {
	if (migrationInfo == null)
	    return false;

	String insertProcedureSql = "INSERT INTO migration_procedure_info(`id`,`creation_date`,`modification_date`,`procedure_name`,`tenant_id`,`execution_state`,`version`,`description`) VALUES(?,?,?,?,?,?,?,?)";

	int rowCount = 0;

	rowCount = jdbcMigrationTemplate.update(insertProcedureSql,
		new Object[] { migrationInfo.getId(), DateTime.now().toDate(), DateTime.now().toDate(),
			migrationInfo.getProcedureName(), migrationInfo.getTenantId(),
			migrationInfo.getExecutionState(), migrationInfo.getVersion(),
			migrationInfo.getDescription() });

	if (rowCount > 0)
	    return true;
	else
	    return false;
    }

    /**
     * to update migration execution status on success and failure
     * 
     * @param procedureName
     * @param version
     * @param tentantId
     * @param executionState
     * @return
     */
    public boolean updateExecutionStatus(String procedureName, String version, String tentantId,
	    Boolean executionState) {

	String updateMigrationSQL = "UPDATE migration_procedure_info "
		+ "set `execution_state`={0} where `procedure_name` = ''{1}'' AND `version`=''{2}'' AND `tenant_id`=''{3}'' ";

	MessageFormat query = new MessageFormat(updateMigrationSQL);

	// "SELECT * FROM migration_procedure_info where procedure_name =
	// ''{0}'' AND version = ''{1}'' AND tenant_id=''{2}'' ");

	int rowCount = 0;

	String queryString = query.format(new Object[] { executionState, procedureName, version, tentantId });
	rowCount = jdbcMigrationTemplate.update(queryString);

	if (rowCount > 0)
	    return true;
	else
	    return false;
    }

}
