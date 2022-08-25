package com.bbytes.recruiz.database;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;
import org.javers.core.graph.ObjectAccessHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.utils.RepositoryJPAUtils;

/**
 * The hibernate unproxy hook for javers audit feature
 * 
 * @deprecated - not used anymore..needs to be deleted later
 */
public class HibernateUnproxyEntityAuditHook implements ObjectAccessHook {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUnproxyEntityAuditHook.class);

	private EntityManager em;

	public HibernateUnproxyEntityAuditHook(EntityManager em) {
		this.em = em;
	}

	public <T> T access(T entity) {
		if (entity instanceof HibernateProxy) {
			Hibernate.initialize(entity);
			HibernateProxy proxy = (HibernateProxy) entity;
			T unproxed = (T) proxy.getHibernateLazyInitializer().getImplementation();
			logger.info("unproxying instance of " + entity.getClass().getSimpleName() + " to " + unproxed.getClass().getSimpleName());
			return unproxed;
		}

		if (entity instanceof JavassistLazyInitializer) {
			JavassistLazyInitializer proxy = (JavassistLazyInitializer) entity;
			T unproxed = (T) proxy.getImplementation();
			logger.info("unproxying instance of " + entity.getClass().getSimpleName() + " to " + unproxed.getClass().getSimpleName());
			return unproxed;

		}

		logger.debug("Unproxing entity for audit service : " + entity);
		RepositoryJPAUtils.initialize(em, entity, 10);
		return entity;
	}
}
