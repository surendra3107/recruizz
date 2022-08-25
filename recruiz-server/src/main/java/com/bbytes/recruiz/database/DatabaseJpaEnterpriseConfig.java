package com.bbytes.recruiz.database;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.Isolation;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;
import com.bbytes.recruiz.utils.DbUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Setting for enterprise version where there is no multi tenant , only default
 * database is used to store all data
 * 
 * @author Thanneer
 *
 */
@Configuration
@EnableConfigurationProperties(JpaProperties.class)
@Profile("enterprise")
public class DatabaseJpaEnterpriseConfig {

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
	
	@Autowired
	private SpringProfileService profileService;

	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean locationEntityManagerFactory() throws SQLException {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(defaultDataSource());
		em.setPackagesToScan(User.class.getPackage().getName());
		em.setPersistenceUnitName("enterprise_mode_pu");
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(DbUtils.additionalHibernateProperties(dialect, ddlAuto));
		return em;
	}

	@Bean(name = "tenant-mgmt")
	public DataSource defaultDataSource() throws SQLException {
		
		int maxPoolSize = 10;
		int idlePoolSize = 2;
		
		if (profileService.isHybridMode()) {
			maxPoolSize = 15;
			idlePoolSize = 5;
		}
		
		HikariConfig defaultConfig = new HikariConfig();
		defaultConfig.setDataSourceClassName(dataSourceClassName);
		defaultConfig.addDataSourceProperty("url", url);
		defaultConfig.addDataSourceProperty("user", user);
		defaultConfig.addDataSourceProperty("password", password);
		defaultConfig.setPoolName("pool-default");
		defaultConfig.setMinimumIdle(idlePoolSize);
		defaultConfig.setMaximumPoolSize(maxPoolSize);
	//	defaultConfig.setInitializationFailFast(true);
		defaultConfig.setTransactionIsolation(Isolation.READ_COMMITTED.value()+"");
		DataSource defaultDataSource = new HikariDataSource(defaultConfig);
		// init enterprise with migration scripts 
		DbUtils.initDb(defaultDataSource);
		return defaultDataSource;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor locationExceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean
	public AutowireHelper autowireHelper() {
		return AutowireHelper.getInstance();
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("org.hibernate.envers.audit_table_suffix", "_AUDIT_LOG");

		return properties;
	}

}
