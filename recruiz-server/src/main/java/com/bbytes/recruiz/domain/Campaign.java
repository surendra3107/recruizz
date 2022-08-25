package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "campaignCandidates", "campaignHrMembers" })
@ToString(exclude = { "campaignCandidates", "campaignHrMembers" })
@NoArgsConstructor
@Entity(name = "campaign")
public class Campaign extends AbstractEntity {

	private static final long serialVersionUID = 8926948821196302140L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(columnDefinition = "longtext")
	private String campaignRenderedTemplate;

	@Column(columnDefinition = "longtext")
	private String campaignSubjectTemplate;

	private String positionCode;

	private Long clientId;

	@OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CampaignCandidate> campaignCandidates = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.REMOVE })
	private Set<User> campaignHrMembers = new HashSet<>();

	private String status;

	private String owner;

	private Date startDate;

	@Transient
	@JsonSerialize
	@JsonDeserialize
	private int candidateCount;

	public int getCandidateCount() {
		return this.campaignCandidates.size();
	}

}
