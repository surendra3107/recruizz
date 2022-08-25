package com.bbytes.recruiz.domain;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditRevisionListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {

		final AuditEntity auditEntity = (AuditEntity) revisionEntity;

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			auditEntity.setUsername("Registration Time");
		else
			try {
				auditEntity.setUsername(auth.getName());
			} catch (Throwable e) {
				auditEntity.setUsername("N/A");
			}

	}
}