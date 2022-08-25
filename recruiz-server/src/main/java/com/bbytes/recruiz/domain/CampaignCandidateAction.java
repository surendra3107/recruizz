package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "campaign_candidate_action")
public class CampaignCandidateAction {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/*
	 * to store action taken on campaign like mail opened / test taken / profile
	 * updated etc..
	 */
	private String actionType;

	/*
	 * to store the interval between when campaign run and when the member
	 * responded (In Minutes)
	 */
	private int actionInterval;

	/*
	 * to store the response of the action like Test taken and marks obtained.
	 * It will be null if the action does not require a response text. i.e if an
	 * email is opened.
	 */
	@Column(columnDefinition = "longtext")
	private String actionResponse;

	@ManyToOne(fetch = FetchType.LAZY)
	private CampaignCandidate campaignCandidate;

	@Column
	private Date actionTime = new Date();

}
