package com.bbytes.recruiz.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.bbytes.recruiz.enums.Currency;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "members", "positions", "children", "parent" })
@ToString(exclude = { "members", "positions", "children", "parent" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NoArgsConstructor
@Entity(name = "team")
public class Team extends AbstractEntity {

	private static final long serialVersionUID = -745950146513475420L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "team_name", unique = true)
	private String teamName;

	@Column(name = "team_desc", length = 500)
	private String teamDesc;

	@Column(name = "root_team")
	private Boolean rootTeam = false;

	@Column(name = "team_target_amount")
	private Long teamTargetAmount = 0L;

	@Column(name = "team_target_position_opening_closure")
	private Long teamTargetPositionOpeningClosure = 0L;

	@Column(name = "team_target_amount_currency")
	@Enumerated(EnumType.STRING)
	private Currency teamTargetAmountCurrency = Currency.Rupee;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TeamMember> members = new HashSet<TeamMember>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
	private Set<Position> positions = new HashSet<>();

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.PERSIST,
			CascadeType.REFRESH }, optional = true)
	private Team parent;

	@OneToMany(fetch = FetchType.LAZY,mappedBy = "parent",cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.PERSIST,
			CascadeType.REFRESH })
	private Set<Team> children = new HashSet<>();

	@JsonGetter("memeberCount")
	public Integer getMemberCount() {
		return members.size();
	}

	public Long getTeamTargetAmount() {
		Long total = 0L;
		if (getMembers() != null) {
			for (TeamMember teamMember : getMembers()) {
				total = total + teamMember.getTargetAmount();
			}
		}
		teamTargetAmount = total;
		return teamTargetAmount;
	}

	public Long getTeamTargetPositionOpeningClosure() {
		Long total = 0L;
		if (getMembers() != null) {
			for (TeamMember teamMember : getMembers()) {
				total = total + teamMember.getTargetPositionOpeningClosure();
			}
		}
		teamTargetPositionOpeningClosure = total;
		return teamTargetPositionOpeningClosure;
	}

}
