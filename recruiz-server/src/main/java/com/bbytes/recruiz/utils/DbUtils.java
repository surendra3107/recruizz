package com.bbytes.recruiz.utils;

import java.net.URI;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtils {

	private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

	public static String databaseURLFromMYSQLJdbcUrl(String url, String newDbName) {
		try {
			String cleanURI = url.substring(5);

			URI uri = URI.create(cleanURI);
			String newUrl = "jdbc:" + uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/" + newDbName
					+ "?createDatabaseIfNotExist=true";
			logger.debug("New DB URL:" + newUrl);
			return newUrl;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Properties additionalHibernateProperties(String dialect, String ddlAuto) {
		Properties properties = new Properties();
		properties.setProperty(AvailableSettings.DIALECT, dialect);
		properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
		properties.setProperty("hibernate.id.new_generator_mappings", "false");
		properties.setProperty("show-sql", "true");
		properties.setProperty("format_sql-sql", "true");
		properties.setProperty("org.hibernate.envers.audit_table_suffix", "_audit");
		properties.setProperty("org.hibernate.envers.store_data_at_delete", "true");
		properties.setProperty("hibernate.order_inserts", "true");
		properties.setProperty("hibernate.order_updates", "true");
		properties.setProperty("hibernate.connection.release_mode", "after_transaction");
		properties.setProperty("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");
		
		
		return properties;
	}

	public static void initDb(DataSource dataSource) {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setBaselineOnMigrate(true);
		flyway.setIgnoreFutureMigrations(true);
		flyway.setValidateOnMigrate(false);
		flyway.setPlaceholderReplacement(false);
		try {
			flyway.migrate();
		} catch (final Exception e) {
			logger.error("Flyway migration failed, doing a repair and retrying ...");
			flyway.repair();
			flyway.migrate();
		}

	}

}