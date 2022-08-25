package com.bbytes.recruiz.database;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.bbytes.recruiz.utils.TenantContextHolder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Profile("saas")
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

	private static final Logger logger = LoggerFactory.getLogger(TenantRoutingDataSource.class);

	@Autowired
	private TenantDBService tenantDBService;

	@Autowired
	@Qualifier("tenant-mgmt")
	private DataSource resolvedDefaultDataSource;

	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContextHolder.getTenant();
	}

	@Override
	public void afterPropertiesSet() {
		setDefaultTargetDataSource(resolvedDefaultDataSource);
		setDataSourceLookup(tenantDBService.getMapDataSourceLookup());
		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
	}

	/**
	 * Retrieve the current target DataSource. Determines the
	 * {@link #determineCurrentLookupKey() current lookup key}, performs a
	 * lookup in the {@link #setTargetDataSources targetDataSources} map, falls
	 * back to the specified {@link #setDefaultTargetDataSource default target
	 * DataSource} if necessary.
	 * 
	 * @see #determineCurrentLookupKey()
	 */
	protected DataSource determineTargetDataSource() {
		String lookupKey = (String) determineCurrentLookupKey();
	//	logger.error("Tenant db look up key : " + lookupKey);
		
		DataSource dataSource = null;
		if (lookupKey != null && !lookupKey.isEmpty()) {
			dataSource = tenantDBService.getMapDataSourceLookup().getDataSource(lookupKey);
	//		logger.error("Tenant db is not null or empty " + lookupKey);
		}
		// shutdown the data source and create it back when required as we
		// dont want the hikari pool to be active all the time and we initiate
		// only when first user from that tenant demands a connection. This
		// helps save memory as there will be use cases when a tenant
		// datasource will never be required (inactive tenants)

		// check tenant DS is closed if so initiate it , add it back to
		// getMapDataSourceLookup map
		if (dataSource != null && ((HikariDataSource) dataSource).isClosed()) {
			HikariConfig config = tenantDBService.getTenantToHikariConfig().get(lookupKey);
			if (config != null) {
				dataSource = new HikariDataSource(config);
				tenantDBService.getMapDataSourceLookup().addDataSource(lookupKey, dataSource);
				tenantDBService.getTenantToHikariConfig().put(lookupKey, (HikariConfig) dataSource);
				logger.error("Got tenant from HikariPool " + lookupKey + " and datasource is " + dataSource.toString());

			}

			logger.error("Recreating the datasource for lookup key " + lookupKey + " and datasource is " + dataSource.toString());

		}

		if (dataSource == null) {
			dataSource = this.resolvedDefaultDataSource;
			logger.error("Datasource is null " + lookupKey + " and new datasource is " + dataSource.toString());

		}

		return dataSource;
	}

}