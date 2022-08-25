package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.PositionOfferCost;

public interface PositionOfferCostRepository extends JpaRepository<PositionOfferCost, Long> {

	@Query(value ="select * from position_offer_cost p where p.position_id=?1", nativeQuery = true)
	public List<PositionOfferCost> findAllByPosition(String positionId);
	
}
