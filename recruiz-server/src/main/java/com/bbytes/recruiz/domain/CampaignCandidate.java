package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "actions" })
@ToString(exclude = { "actions" })
@NoArgsConstructor
@Entity(name = "campaign_candidate")
public class CampaignCandidate extends AbstractEntity {

	private static final long serialVersionUID = 5725821261835973425L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String memberEmailId;

	@Column
	private String memberName;

	@Column
	private Date addedToCampaignDate = new Date();

	@Column
	private Date campaignRunDate;

	@Column
	private Boolean campaignRunStatus = false;

	@OneToMany(mappedBy = "campaignCandidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CampaignCandidateAction> actions = new ArrayList<>();

	@JsonProperty(access = Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	private Campaign campaign;
	
	@Column(columnDefinition = "longtext") 
	private String mailgunEmailId;
	 
}
