package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;

@Transactional(readOnly = true)
public class PositionRepositoryImpl implements PositionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Position> findPositionForClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail,
	    Set<User> users, List<Team> teams, Pageable pageable) {

	TypedQuery<Long> qCount = entityManager.createQuery(
		"SELECT COUNT(DISTINCT p.id) "
		+ "FROM position p where (p.client = :client AND p.owner = :ownerEmail) OR (p.client = :client AND :users MEMBER OF p.hrExecutives ) OR (p.client = :client AND p.team IN :teams)",
		Long.class);
	qCount.setParameter("client", client);
	qCount.setParameter("ownerEmail", ownerEmail);
	qCount.setParameter("users", users);
	qCount.setParameter("teams", teams);

	Long count = qCount.getSingleResult();

	TypedQuery<Position> q = entityManager.createQuery(
		"SELECT p "
		+ "FROM position p where (p.client = :client AND p.owner = :ownerEmail) OR (p.client = :client AND :users MEMBER OF p.hrExecutives ) OR (p.client = :client AND p.team IN :teams)",
		Position.class);

	q.setParameter("client", client);
	q.setParameter("ownerEmail", ownerEmail);
	q.setParameter("users", users);
	q.setParameter("teams", teams);

	q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
	q.setMaxResults(pageable.getPageSize());

	List<Position> positionList = q.getResultList();
	Page<Position> positionPage = new PageImpl<>(positionList, pageable, count);
	return positionPage;

    }

    @Override
    public Page<Position> findPositionForClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail,
	    Set<User> users, Pageable pageable) {

	TypedQuery<Long> qCount = entityManager.createQuery(
		"SELECT COUNT(DISTINCT p.id) FROM position p where (p.client = :client AND p.owner = :ownerEmail) OR (p.client = :client AND :users MEMBER OF p.hrExecutives )",
		Long.class);
	qCount.setParameter("client", client);
	qCount.setParameter("ownerEmail", ownerEmail);
	qCount.setParameter("users", users);

	Long count = qCount.getSingleResult();

	TypedQuery<Position> q = entityManager.createQuery(
		"SELECT p FROM position p where (p.client = :client AND p.owner = :ownerEmail) OR (p.client = :client AND :users MEMBER OF p.hrExecutives )",
		Position.class);

	q.setParameter("client", client);
	q.setParameter("ownerEmail", ownerEmail);
	q.setParameter("users", users);

	q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
	q.setMaxResults(pageable.getPageSize());

	List<Position> positionList = q.getResultList();
	Page<Position> positionPage = new PageImpl<>(positionList, pageable, count);
	return positionPage;

    }

}
