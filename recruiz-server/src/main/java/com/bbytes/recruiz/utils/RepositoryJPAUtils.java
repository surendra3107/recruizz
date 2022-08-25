
package com.bbytes.recruiz.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import com.bbytes.recruiz.domain.AbstractEntity;

/**
 * Utility class for dealing with JPA API
 * 
 */
public class RepositoryJPAUtils {

	private final static Logger LOG = LoggerFactory.getLogger(RepositoryJPAUtils.class);

	/**
	 * Initialize a entity.
	 * 
	 * @param em
	 *            entity manager to use
	 * @param entity
	 *            entity to initialize
	 * @param depth
	 *            max depth on recursion
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void initialize(EntityManager em, Object entity, int depth) {
		// return on nulls, depth = 0 or already initialized objects
		if (entity == null || depth == 0 || !(entity instanceof AbstractEntity)) {
			return;
		}

		PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();

		Class entityClazz = null;
		if (entity instanceof HibernateProxy) {
			entityClazz = Hibernate.getClass(entity);
		}

		if (entityClazz == null)
			entityClazz = entity.getClass();

		EntityType entityType = em.getMetamodel().entity(entityClazz);
		Set<Attribute> attributes = entityType.getDeclaredAttributes();

		Object id = unitUtil.getIdentifier(entity);

		if (id != null) {
			Object attached = em.find(entityClazz, id);
			if (attached != null) {
				for (Attribute a : attributes) {
					if (!unitUtil.isLoaded(entity, a.getName())) {
						LOG.debug("Entity class : " + entityClazz);
						LOG.debug("Attribute : " + a.getName());
						if (a.isCollection()) {
							intializeCollection(em, entity, attached, a, depth);
						} else if (a.isAssociation()) {
							intialize(em, entity, attached, a, depth);
						}
					}
				}
			}

		}
	}

	/**
	 * Initialize entity attribute
	 * 
	 * @param em
	 * @param entity
	 * @param a
	 * @param depth
	 */
	@SuppressWarnings("rawtypes")
	private static void intialize(EntityManager em, Object entity, Object attached, Attribute a, int depth) {
		Object value = PropertyAccessorFactory.forDirectFieldAccess(attached).getPropertyValue(a.getName());
		if (!em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(value)) {
			em.refresh(value);
		}

		if (value instanceof HibernateProxy) {
			value = initializeAndUnproxy(value);
		}

		PropertyAccessorFactory.forDirectFieldAccess(entity).setPropertyValue(a.getName(), value);

		initialize(em, value, depth - 1);
	}

	/**
	 * Initialize collection
	 * 
	 * @param em
	 * @param entity
	 * @param a
	 * @param i
	 */
	@SuppressWarnings("rawtypes")
	private static void intializeCollection(EntityManager em, Object entity, Object attached, Attribute a, int depth) {
		PropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(attached);
		try {
			Collection c = (Collection) accessor.getPropertyValue(a.getName());

			if (c != null) {
				for (Object o : c)
					initialize(em, o, depth - 1);

				PropertyAccessorFactory.forDirectFieldAccess(entity).setPropertyValue(a.getName(), c);
			}

		} catch (ClassCastException ex) {
			initialize(em, accessor.getPropertyValue(a.getName()), depth - 1);
		}

	}

	/**
	 * Get all attributes where type or element type is assignable from class
	 * and has persistent type
	 * 
	 * @param type
	 *            entity type
	 * @param persistentType
	 *            persistentType
	 * @param clazz
	 *            class
	 * @return Set with matching attributes
	 */
	public static Set<Attribute<?, ?>> getAttributes(EntityType<?> type, PersistentAttributeType persistentType,
			Class<?> clazz) {
		Set<Attribute<?, ?>> attributes = new HashSet<Attribute<?, ?>>();

		for (Attribute<?, ?> a : type.getAttributes()) {
			if (a.getPersistentAttributeType() == persistentType && isTypeOrElementType(a, clazz)) {
				attributes.add(a);
			}
		}

		return attributes;
	}

	/**
	 * Get all attributes of type by persistent type
	 * 
	 * @param type
	 * @param persistentType
	 * @return a set with all attributes of type with persistent type
	 *         persistentType.
	 */
	public static Set<Attribute<?, ?>> getAttributes(EntityType<?> type, PersistentAttributeType persistentType) {
		return getAttributes(type, persistentType, Object.class);
	}

	/**
	 * Test if attribute is type or in collections has element type
	 * 
	 * @param attribute
	 *            attribute to test
	 * @param clazz
	 *            Class to test
	 * @return true if clazz is asignable from type or element type
	 */
	public static boolean isTypeOrElementType(Attribute<?, ?> attribute, Class<?> clazz) {
		if (attribute.isCollection()) {
			return clazz.isAssignableFrom(((CollectionAttribute<?, ?>) attribute).getBindableJavaType());
		}

		return clazz.isAssignableFrom(attribute.getJavaType());
	}

	/**
	 * Gets the mappedBy value from an attribute
	 * 
	 * @param attribute
	 *            attribute
	 * @return mappedBy value or null if none.
	 */
	public static String getMappedBy(Attribute<?, ?> attribute) {
		String mappedBy = null;

		if (attribute.isAssociation()) {
			Annotation[] annotations = null;
			Member member = attribute.getJavaMember();
			if (member instanceof Field) {
				annotations = ((Field) member).getAnnotations();
			} else if (member instanceof Method) {
				annotations = ((Method) member).getAnnotations();
			}

			for (Annotation a : annotations) {
				if (a.annotationType().equals(OneToMany.class)) {
					mappedBy = ((OneToMany) a).mappedBy();
					break;
				} else if (a.annotationType().equals(ManyToMany.class)) {
					mappedBy = ((ManyToMany) a).mappedBy();
					break;
				} else if (a.annotationType().equals(OneToOne.class)) {
					mappedBy = ((OneToOne) a).mappedBy();
					break;
				}
			}
		}

		return "".equals(mappedBy) ? null : mappedBy;
	}

	public static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}
}
