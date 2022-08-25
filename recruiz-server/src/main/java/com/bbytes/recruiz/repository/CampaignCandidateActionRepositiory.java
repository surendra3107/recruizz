package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.CampaignCandidate;
import com.bbytes.recruiz.domain.CampaignCandidateAction;

public interface CampaignCandidateActionRepositiory extends JpaRepository<CampaignCandidateAction, Long> {

	CampaignCandidateAction findByActionTypeAndCampaignCandidate(String actionType,
			CampaignCandidate campaignCandidate);

	Long countByCampaignCandidateInAndActionType(List<CampaignCandidate> campaignCandidates, String actionType);

	@Query(value = "select sum(actionInterval) from campaign_candidate_action where actionType=?1 and campaignCandidate_id IN ?2", nativeQuery = true)
	Long findSumOfIntervalByType(String actionType, List<Long> campaignCandidateIds);

}
