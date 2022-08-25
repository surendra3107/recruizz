package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "client", "position" })
@ToString(exclude = { "client", "position" })
@NoArgsConstructor
@Entity(name = "decision_maker")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "email", "client_id", "position_id" }) })
public class ClientDecisionMaker {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;
	private String mobile = "";

	@Column
	private String email;

	@ManyToOne
	@JsonProperty(access = Access.WRITE_ONLY)
	private Client client;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Position position;

}
