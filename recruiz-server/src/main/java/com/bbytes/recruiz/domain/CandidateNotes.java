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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false,exclude = {"candidateId"})
@ToString(exclude = {"candidateId"})
@NoArgsConstructor
@Entity(name = "candidate_notes")

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class CandidateNotes extends AbstractEntity {

	private static final long serialVersionUID = -3021098947855656314L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String addedBy;

	@Column(columnDefinition="longtext")
	private String notes;
	
	@JsonProperty(access=Access.WRITE_ONLY)
	@ManyToOne(fetch=FetchType.LAZY)
	private Candidate candidateId;

	
}
