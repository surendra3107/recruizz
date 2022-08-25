package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.bbytes.recruiz.enums.TeamRole;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "user", "team" })
@ToString(exclude = { "user", "team" })
@Table(name = "team_member", uniqueConstraints = @UniqueConstraint(columnNames = { "team_id", "user_id" }))
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NoArgsConstructor
@Entity(name = "team_member")
public class TeamMember extends AbstractEntity {

	private static final long serialVersionUID = -745950146513475420L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY,optional=false)
	private Team team;

	@Column(name = "target_amount")
	private Long targetAmount = 0L;
	
	@Column(name = "target_position_opening_closure")
	private Long targetPositionOpeningClosure = 0L;

	@ManyToOne(fetch = FetchType.EAGER,optional=false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	private TeamRole role;

}
