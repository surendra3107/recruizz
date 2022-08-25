package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name="prospect_notes")
@EqualsAndHashCode(callSuper = false)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@EntityListeners({ AbstractEntityListener.class })
public class ProspectNotes extends AbstractEntity {
	
	private static final long serialVersionUID = -1971323132244492514L;

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String addedBy;

	@Column(columnDefinition = "longtext")
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	private Prospect prospect;
}
