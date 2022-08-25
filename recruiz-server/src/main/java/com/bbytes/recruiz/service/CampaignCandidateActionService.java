package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CampaignCandidate;
import com.bbytes.recruiz.domain.CampaignCandidateAction;
import com.bbytes.recruiz.repository.CampaignCandidateActionRepositiory;

@Service
public class CampaignCandidateActionService extends AbstractService<CampaignCandidateAction, Long> {

	private CampaignCandidateActionRepositiory campaignCandidateActionRepositiory;

	@Autowired
	public CampaignCandidateActionService(CampaignCandidateActionRepositiory campaignCandidateActionRepositiory) {
		super(campaignCandidateActionRepositiory);
		this.campaignCandidateActionRepositiory = campaignCandidateActionRepositiory;
	}

	@Transactional(readOnly = true)
	public CampaignCandidateAction getCampaignCandidateactionByActionTypeAndCandidate(String actionType,
			CampaignCandidate campaignCandidate) {
		return campaignCandidateActionRepositiory.findByActionTypeAndCampaignCandidate(actionType, campaignCandidate);
	}

	@Transactional(readOnly = true)
	public Long getCountByCampaignCandidatesAndActionType(List<CampaignCandidate> campaignCandidates,
			String actionType) {
		return campaignCandidateActionRepositiory.countByCampaignCandidateInAndActionType(campaignCandidates,
				actionType);
	}

	@Transactional(readOnly = true)
	public double getAvgTimeToOpenEmail(List<CampaignCandidate> campaignCandidates, String actionType) {

		if (campaignCandidates != null && !campaignCandidates.isEmpty()) {
			List<Long> campaignCandidateIds = new ArrayList<>();
			for (CampaignCandidate campaignCandidate : campaignCandidates) {
				campaignCandidateIds.add(campaignCandidate.getId());
			}
			Long sumOfOpenedinterval = campaignCandidateActionRepositiory.findSumOfIntervalByType(actionType,campaignCandidateIds);
			if(sumOfOpenedinterval == null){
				sumOfOpenedinterval = 0L;
			}
			double avgOfOpenedInterval = (sumOfOpenedinterval.doubleValue()/campaignCandidateIds.size());
			return avgOfOpenedInterval;
		}

		return 0.0;
	}
}
