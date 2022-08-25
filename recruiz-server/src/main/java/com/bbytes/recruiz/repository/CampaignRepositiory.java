package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Campaign;

public interface CampaignRepositiory extends JpaRepository<Campaign, Long> {

	List<Campaign> findByType(String type);

	Page<Campaign> findByType(String type, Pageable pageable);

	List<Campaign> findByPositionCode(String positionCode);

	Page<Campaign> findByPositionCode(String positionCode, Pageable pageable);

	List<Campaign> findByTypeAndPositionCode(String type, String positionCode);

	Page<Campaign> findByTypeAndPositionCode(String type, String positionCode, Pageable pageable);

	Page<Campaign> findByClientId(Long clientId, Pageable pageable);

}
