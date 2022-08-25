package com.bbytes.recruiz.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.DbUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

@Service("TenantDBService")
@Data
public class TenantDBService {

	private static final Logger logger = LoggerFactory.getLogger(TenantDBService.class);

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.dataSourceClassName}")
	private String dataSourceClassName;

	@Value("${spring.datasource.username}")
	private String user;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.jpa.properties.hibernate.dialect}")
	private String dialect;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddlAuto;

	@Value("${flyway.migration.file.location.tenant}")
	private String flywayScriptTenantDb;

	private ApplicationContext appContext;

	@Value("${tenant.mgmt.db}")
	private String defaultDB;

	private MapDataSourceLookup mapDataSourceLookup;

	private Map<String, HikariConfig> tenantToHikariConfig;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private SpringProfileService profileService;

	@PostConstruct
	public void init() throws Exception {
		// run the logic only if saas mode
		if (!profileService.isSaasMode())
			return;

		mapDataSourceLookup = new MapDataSourceLookup();
		tenantToHikariConfig = new HashMap<String, HikariConfig>();

		// during server startup we need to reintialize the
		// mapDataSourceLookup
		// from the list of already existing tenants
		List<String> allTenants = tenantResolverService.findAllTenants();
		logger.error("Total number of tenants = " +allTenants.size());
		int index = 0;
		for (String tenantId : allTenants) {
			createOrUpdateDBSchema(tenantId);
			index++;
			logger.error(index + ") Intializing tenant DB : " + tenantId);
		}
	}

	/**
	 * Init all the tenant database , conn pool and run init sql
	 */
	public void createOrUpdateDBSchema(String tenantName) throws SQLException {
		// run the logic only if saas mode , in other mode only one db so no
		// need to init tenant db
		if (!profileService.isSaasMode())
			return;
		
		int maxPoolSize = 30;
		int idlePoolSize = 5;
		
		if (profileService.isHybridMode()) {
			maxPoolSize =30;
			idlePoolSize = 5;
			logger.error("Profile = Hybrid" +"maxPoolSize: " +maxPoolSize +"idlePoolSize: " +idlePoolSize);
		}
			

		String tenantDbUrl = DbUtils.databaseURLFromMYSQLJdbcUrl(url, tenantName);
		logger.debug("Configuring datasource {} {} {}", dataSourceClassName, tenantDbUrl, user);
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName(dataSourceClassName);
		config.addDataSourceProperty("url", tenantDbUrl);
		config.addDataSourceProperty("user", user);
		config.addDataSourceProperty("password", password);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.setConnectionTestQuery("select 1");
		config.setPoolName("pool-" + tenantName);
		config.setMaximumPoolSize(maxPoolSize);
		config.setMaxLifetime(600000);
		config.setMinimumIdle(idlePoolSize);
		config.setIdleTimeout(120000);
		config.setLeakDetectionThreshold(50000);
	//	config.setInitializationFailFast(true);
		HikariDataSource tenantDataSource = new HikariDataSource(config);
		mapDataSourceLookup.addDataSource(tenantName, tenantDataSource);
		tenantToHikariConfig.put(tenantName, tenantDataSource);
		// init tenant dbs
		DbUtils.initDb(tenantDataSource);

		// close data source and shutdown pool after data init (flyway init) is
		// executed this is to save memory as we dont need the connection to be
		// active
		logger.error("Closing datasource connection and datasource pool : " + tenantDataSource.getPoolName());
		tenantDataSource.close();

	}

	@PreDestroy
	void destroy() {

		for (String lookUp : mapDataSourceLookup.getDataSources().keySet()) {
			closeDataSource(lookUp);
		}
	}

	public void closeDataSource(String tenant) {
		HikariDataSource dataSource = (HikariDataSource) mapDataSourceLookup.getDataSource(tenant);
		if (dataSource != null && !dataSource.isClosed()) {
			logger.info("Closing datasource connection and datasource pool : " + dataSource.getPoolName());
			dataSource.close();
		}
	}

}
