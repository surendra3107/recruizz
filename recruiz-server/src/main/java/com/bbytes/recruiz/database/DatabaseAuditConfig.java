package com.bbytes.recruiz.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.javers.core.Javers;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.javers.spring.auditable.EmptyPropertiesProvider;
import org.javers.spring.auditable.SpringSecurityAuthorProvider;
import org.javers.spring.auditable.aspect.JaversAuditableRepositoryAspect;
import org.javers.spring.boot.sql.DialectMapper;
import org.javers.spring.boot.sql.JaversProperties;
import org.javers.spring.jpa.JpaHibernateConnectionProvider;
import org.javers.spring.jpa.TransactionalJaversBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

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
@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = { JaversProperties.class, JpaProperties.class })
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class DatabaseAuditConfig {

	private static Logger logger = LoggerFactory.getLogger(DatabaseAuditConfig.class);

	private final DialectMapper dialectMapper = new DialectMapper();

	@Autowired
	private JaversProperties javersProperties;

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
    @PersistenceContext
    private EntityManager em;

	@Bean
	public DialectName javersSqlDialectName() {
		SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) entityManagerFactory
				.unwrap(SessionFactory.class);

		Dialect hibernateDialect = sessionFactory.getDialect();
		logger.info("detected Hibernate dialect: " + hibernateDialect.getClass().getSimpleName());

		return dialectMapper.map(hibernateDialect);
	}

	@Bean
	public Javers javers(ConnectionProvider connectionProvider) {
		JaversSqlRepository sqlRepository = SqlRepositoryBuilder.sqlRepository()
				.withConnectionProvider(connectionProvider).withDialect(javersSqlDialectName()).build();

		return TransactionalJaversBuilder.javers().registerJaversRepository(sqlRepository)
				.withObjectAccessHook(new HibernateUnproxyEntityAuditHook(em))
				.withListCompareAlgorithm(ListCompareAlgorithm.valueOf(javersProperties.getAlgorithm().toUpperCase()))
				.withMappingStyle(MappingStyle.valueOf(javersProperties.getMappingStyle().toUpperCase()))
				.withNewObjectsSnapshot(javersProperties.isNewObjectSnapshot())
				.withPrettyPrint(javersProperties.isPrettyPrint())
				.withTypeSafeValues(javersProperties.isTypeSafeValues()).build();
	}

	@Bean(name = "authorProvider")
	public AuthorProvider springSecurityAuthorProvider() {
		return new SpringSecurityAuthorProvider();
	}

	@Bean(name = "commitPropertiesProvider")
	@ConditionalOnMissingBean
	public CommitPropertiesProvider commitPropertiesProvider() {
		return new EmptyPropertiesProvider();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConnectionProvider jpaConnectionProvider() {
		return new JpaHibernateConnectionProvider();
	}

	@Bean
	public JaversAuditableRepositoryAspect javersAuditableRepositoryAspect(Javers javers, AuthorProvider authorProvider,
			CommitPropertiesProvider commitPropertiesProvider) {
		return new JaversAuditableRepositoryAspect(javers, authorProvider, commitPropertiesProvider);
	}

}
