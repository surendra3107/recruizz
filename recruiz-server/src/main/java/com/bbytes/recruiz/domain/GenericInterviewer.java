package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity(name = "generic_interviewer")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class GenericInterviewer extends AbstractEntity {

	private static final long serialVersionUID = 1741638677089135118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 1000, nullable = false)
	private String name;

	@Column(unique = true, nullable = false)
	private String mobile;

	@Column(unique = true, nullable = false)
	private String email;

	@javax.persistence.Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> clientName = new ArrayList<String>();

}
