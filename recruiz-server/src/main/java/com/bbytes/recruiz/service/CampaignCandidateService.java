package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Campaign;
import com.bbytes.recruiz.domain.CampaignCandidate;
import com.bbytes.recruiz.repository.CampaignCandidateRepositiory;

@Service
public class CampaignCandidateService extends AbstractService<CampaignCandidate, Long> {

	private CampaignCandidateRepositiory campaignCandidateRepositiory;

	@Autowired
	public CampaignCandidateService(CampaignCandidateRepositiory campaignMemberRepositiory) {
		super(campaignMemberRepositiory);
		this.campaignCandidateRepositiory = campaignMemberRepositiory;
	}

	@Transactional(readOnly = true)
	public List<CampaignCandidate> getCampaignMembersbyCampaign(Campaign campaign) {
		return campaignCandidateRepositiory.findByCampaign(campaign);
	}

	@Transactional(readOnly = true)
	public Page<CampaignCandidate> getCampaignMembersbyCampaign(Campaign campaign, Pageable pageable) {
		return campaignCandidateRepositiory.findByCampaign(campaign, pageable);
	}

	@Transactional(readOnly = true)
	public CampaignCandidate getCamapignCandidateByMessageId(String messaegId) {
		return campaignCandidateRepositiory.findDistinctByMailgunEmailId(messaegId);
	}
}
