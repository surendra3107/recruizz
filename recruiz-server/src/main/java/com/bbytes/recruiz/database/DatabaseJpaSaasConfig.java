package com.bbytes.recruiz.database;

import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;
import com.bbytes.recruiz.utils.DbUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This multi tenant is using spring AbstractRoutingDataSource strategy and not
 * hibernate inbuilt multi tenant feature. Here database and connection pooling
 * per tenant is created. The db init called for the first time . We have to
 * create a default db to maintain tenant info ..it should be called tenant
 * management db, that db should be directly connected using jdbc template and
 * not thru hibernate as hibernate repository has all the tables and is multi
 * tenant by default
 * 
 * @author Thanneer
 *
 */
@Configuration
@EnableConfigurationProperties(JpaProperties.class)
@Profile({"saas","hybrid"})
@EnableTransactionManagement
public class DatabaseJpaSaasConfig {

	@Value("${spring.datasource.url}")
	private String url;
	
	@Value("${spring.tenant.usage.datasource.url}")
	private String usageDbUrl;
	

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

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(tenantRoutingDataSource());
		em.setPackagesToScan(User.class.getPackage().getName());
		em.setPersistenceUnitName("saas_multi_tenant_pu");
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(DbUtils.additionalHibernateProperties(dialect, ddlAuto));
		return em;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}

	@Bean
	@Primary
	public DataSource tenantRoutingDataSource() {
		TenantRoutingDataSource tenantRoutingDataSource = new TenantRoutingDataSource();
		return tenantRoutingDataSource;
	}

	@Bean(name = "tenant-mgmt")
	public DataSource tenantMgmtDataSource() throws SQLException {
		
		int maxPoolSize = 30;
		int idlePoolSize = 5;
		
		if (profileService.isHybridMode()) {
			maxPoolSize = 30;
			idlePoolSize = 5;
		}
		
		HikariConfig defaultConfig = new HikariConfig();
		defaultConfig.setDataSourceClassName(dataSourceClassName);
		defaultConfig.addDataSourceProperty("url", url);
		defaultConfig.addDataSourceProperty("user", user);
		defaultConfig.addDataSourceProperty("password", password);
		defaultConfig.setPoolName("pool-default");
		defaultConfig.addDataSourceProperty("cachePrepStmts", "true");
		defaultConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		defaultConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		defaultConfig.setConnectionTestQuery("select 1");
		defaultConfig.setMaximumPoolSize(maxPoolSize);
		defaultConfig.setMaxLifetime(600000);
		defaultConfig.setMinimumIdle(idlePoolSize);
		defaultConfig.setIdleTimeout(120000);
		defaultConfig.setLeakDetectionThreshold(50000);
//		defaultConfig.setInitializationFailTimeout(true);
		defaultConfig.setTransactionIsolation(Isolation.READ_COMMITTED.value()+"");
		DataSource defaultDataSource = new HikariDataSource(defaultConfig);
		return defaultDataSource;
	}
	
	
	@Bean(name = "tenant-usage-stat")
	public DataSource tenantUsageStatDataSource() throws SQLException {
		HikariConfig defaultConfig = new HikariConfig();
		defaultConfig.setDataSourceClassName(dataSourceClassName);
		defaultConfig.addDataSourceProperty("url", usageDbUrl);
		defaultConfig.addDataSourceProperty("user", user);
		defaultConfig.addDataSourceProperty("password", password);
		defaultConfig.setPoolName("pool-default");
		defaultConfig.addDataSourceProperty("cachePrepStmts", "true");
		defaultConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		defaultConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		defaultConfig.setConnectionTestQuery("select 1");
		defaultConfig.setMaximumPoolSize(5);
		defaultConfig.setMaxLifetime(600000);
		defaultConfig.setMinimumIdle(1);
		defaultConfig.setIdleTimeout(120000);
		defaultConfig.setLeakDetectionThreshold(30000);
//		defaultConfig.setInitializationFailTimeout(true);
		defaultConfig.setTransactionIsolation(Isolation.READ_COMMITTED.value()+"");
		DataSource defaultDataSource = new HikariDataSource(defaultConfig);
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
	
}
