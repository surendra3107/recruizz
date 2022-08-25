package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Campaign;
import com.bbytes.recruiz.domain.CampaignCandidate;

public interface CampaignCandidateRepositiory extends JpaRepository<CampaignCandidate, Long> {

	List<CampaignCandidate> findByCampaign(Campaign campaign);

	Page<CampaignCandidate> findByCampaign(Campaign campaign, Pageable pageable);

	CampaignCandidate findDistinctByMailgunEmailId(String messageId);

}
