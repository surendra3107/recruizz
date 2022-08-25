package com.bbytes.recruiz.service;

import java.util.List;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditQueryService {

	@Autowired
	private Javers javers;

	public void getChanges(Class<?> entityType, String ownerEmailId, int limit, LocalDateTime startDate,
			LocalDateTime endTime) {
		QueryBuilder queryBuilder = QueryBuilder.byClass(entityType).limit(limit);

		if (ownerEmailId != null) {
			queryBuilder = queryBuilder.byAuthor(ownerEmailId);
		}

		if (startDate != null && endTime != null) {
			queryBuilder = queryBuilder.from(startDate).to(startDate);
		}

		List<Change> changes = javers.findChanges(queryBuilder.build());
		for (Change change : changes) {
			System.out.println("Object : " + change.getAffectedObject());
			System.out.println("Local id : " + change.getAffectedLocalId());
			System.out.println(change);
		}
	}

	public void getSnapShotChanges(Class<?> entityType, String ownerEmailId, int limit, LocalDateTime startDate,
			LocalDateTime endTime) {
		QueryBuilder queryBuilder = QueryBuilder.byClass(entityType).limit(limit);

		if (ownerEmailId != null) {
			queryBuilder = queryBuilder.byAuthor(ownerEmailId);
		}

		if (startDate != null && endTime != null) {
			queryBuilder = queryBuilder.from(startDate).to(startDate);
		}

		List<CdoSnapshot> snapShotChanges = javers.findSnapshots(queryBuilder.build());
		for (CdoSnapshot snapShotchange : snapShotChanges) {
			System.out.println("SnapShot : ");
			System.out.println(snapShotchange);
		}
	}

	public void getChanges(Class<?> entityType, String ownerEmailId, int limit) {
		getChanges(entityType, ownerEmailId, limit, null, null);
	}

	public void getSnapShotChanges(Class<?> entityType, String ownerEmailId, int limit) {
		getSnapShotChanges(entityType, ownerEmailId, limit, null, null);
	}

}
