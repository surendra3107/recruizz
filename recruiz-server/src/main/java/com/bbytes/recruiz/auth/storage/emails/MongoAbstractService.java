package com.bbytes.recruiz.auth.storage.emails;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public class MongoAbstractService<T, ID extends Serializable> {

	protected MongoRepository<T, ID> mongoRepository;

	public MongoAbstractService(MongoRepository<T, ID> mongoRepository) {
		this.mongoRepository = mongoRepository;
	}

	/*
	 * Save all given entities into mongodb
	 */
	public <S extends T> List<S> save(Iterable<S> entites) {
		return mongoRepository.save(entites);
	}

	/*
	 * find all entities from mongodb
	 */
	public List<T> findAll() {
		return mongoRepository.findAll();
	}

	/*
	 * Returns all entities sorted by the given options
	 */
	public List<T> findAll(Sort sort) {
		return mongoRepository.findAll(sort);
	}

	/*
	 * Inserts the given entity
	 */
	public <S extends T> S insert(S entity) {
		return mongoRepository.insert(entity);
	}

	/*
	 * Inserts the given entities
	 */
	public <S extends T> List<S> insert(Iterable<S> entities) {
		return mongoRepository.insert(entities);
	}

	/*
	 * Returns a Page of entities meeting the paging restriction provided in the
	 * Pageable object.
	 */
	Page<T> findAll(Pageable pageable) {
		return mongoRepository.findAll(pageable);
	}

	/*
	 * Saves a given entity
	 */
	public <S extends T> S save(S entity) {
		return mongoRepository.save(entity);
	}

	/*
	 * Retrieves an entity by its id.
	 */
	public T findOne(ID id) {
		return mongoRepository.findOne(id);
	}

	/*
	 * Returns whether an entity with the given id exists.
	 */
	public boolean exists(ID id) {
		return mongoRepository.exists(id);
	}

	/*
	 * Returns all instances of the type with the given IDs.
	 */
	public Iterable<T> findAll(Iterable<ID> ids) {
		return mongoRepository.findAll(ids);
	}

	/*
	 * Returns the number of entities available.
	 */
	public long count() {
		return mongoRepository.count();
	}

	/*
	 * Deletes the entity with the given id.
	 */
	public void delete(ID id) {
		mongoRepository.delete(id);
	}

	/*
	 * Deletes a given entity.
	 */
	public void delete(T entity) {
		mongoRepository.delete(entity);
	}

	/*
	 * Deletes the given entities.
	 */
	public void delete(Iterable<? extends T> entities) {
		mongoRepository.delete(entities);
	}

	/*
	 * Deletes all entities managed by the repository.
	 */
	public void deleteAll() {
		mongoRepository.deleteAll();
	}
}
