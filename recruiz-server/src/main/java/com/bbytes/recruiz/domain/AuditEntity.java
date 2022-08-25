package com.bbytes.recruiz.domain;

import javax.persistence.Entity;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@RevisionEntity(AuditRevisionListener.class)
public class AuditEntity extends DefaultRevisionEntity {

	private static final long serialVersionUID = -2635510380884137343L;
	
	private String username;
}
