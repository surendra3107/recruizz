package com.bbytes.recruiz.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public class AbstractService<T, ID extends Serializable> {

	protected JpaRepository<T, ID> jpaRepository;

	public AbstractService(JpaRepository<T, ID> jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	/*
	 * Save all given entities
	 */
	@Transactional
	public <S extends T> List<S> save(Iterable<S> entites) {
		return jpaRepository.save(entites);
	}

	@Transactional
	public <S extends T> List<S> saveAndFlush(Iterable<S> entites) {
		List<S> tList = new ArrayList<>();
		for (S s : entites) {
			tList.add(jpaRepository.saveAndFlush(s));
		}
		return tList;
	}

	/*
	 * find all entities
	 */
	@Transactional(readOnly = true)
	public List<T> findAll() {
		try{
			return jpaRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Returns all entities sorted by the given options
	 */
	@Transactional(readOnly = true)
	public List<T> findAll(Sort sort) {
		return jpaRepository.findAll(sort);
	}

	/*
	 * Inserts the given entity
	 */
	@Transactional
	public <S extends T> S insert(S entity) {
		return jpaRepository.save(entity);
	}

	/*
	 * Returns a Page of entities meeting the paging restriction provided in the
	 * Pageable object.
	 */
	@Transactional(readOnly = true)
	Page<T> findAll(Pageable pageable) {
		return jpaRepository.findAll(pageable);
	}

	/*
	 * Inserts the given entities
	 */
	@Transactional
	public <S extends T> List<S> insert(Iterable<S> entities) {
		return jpaRepository.save(entities);
	}

	/*
	 * Saves a given entity
	 */
	@Transactional
	public <S extends T> S save(S entity) {
		return jpaRepository.save(entity);
	}

	/*
	 * Saves a given entity
	 */
	@Transactional
	public <S extends T> S saveAndFlush(S entity) {
		return jpaRepository.saveAndFlush(entity);
	}

	/*
	 * Retrieves an entity by its id.
	 */
	@Transactional(readOnly = true)
	public T findOne(ID id) {
		return jpaRepository.findOne(id);
	}

	/*
	 * Returns whether an entity with the given id exists.
	 */
	@Transactional(readOnly = true)
	public boolean exists(ID id) {
		return jpaRepository.exists(id);
	}

	/*
	 * Returns all instances of the type with the given IDs.
	 */
	@Transactional(readOnly = true)
	public Iterable<T> findAll(Iterable<ID> ids) {
		return jpaRepository.findAll(ids);
	}

	/*
	 * Returns the number of entities available.
	 */
	@Transactional(readOnly = true)
	public long count() {
		return jpaRepository.count();
	}

	/*
	 * Deletes the entity with the given id.
	 */
	@Transactional
	public void delete(ID id) {
		jpaRepository.delete(id);
	}

	/*
	 * Deletes a given entity.
	 */
	@Transactional
	public void delete(T entity) {
		jpaRepository.delete(entity);
	}

	/*
	 * Deletes the given entities.
	 */
	@Transactional
	public void delete(Iterable<? extends T> entities) {
		jpaRepository.delete(entities);
	}

	/*
	 * Deletes all entities managed by the repository.
	 */
	@Transactional
	public void deleteAll() {
		jpaRepository.deleteAll();
	}
}
