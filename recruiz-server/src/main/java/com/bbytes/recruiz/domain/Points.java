package com.bbytes.recruiz.domain;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "points")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class Points extends AbstractEntity {

	private static final long serialVersionUID = -4722617451547593800L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String roundId;
	
	private String positionCode;

	private String category;
	
	private double points;	// out of 10, should not exceed more than 10
	
	@ManyToOne
	private RoundCandidate roundCandidate;

}
