package com.bbytes.recruiz.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude={"rounds"})
@ToString(exclude={"rounds"})
@NoArgsConstructor
@Entity(name = "board")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class Board extends AbstractEntity {

	private static final long serialVersionUID = 2059711191818268950L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean status;
	
	private String positionStatus = Status.Active.toString();
	private String clientStatus = Status.Active.toString();;

	@OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("orderNo ASC")
	private Set<Round> rounds = new HashSet<Round>();

}
