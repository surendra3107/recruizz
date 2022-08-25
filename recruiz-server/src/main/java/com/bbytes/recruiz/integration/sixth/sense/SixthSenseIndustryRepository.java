package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseIndustry;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;

public interface SixthSenseIndustryRepository extends JpaRepository<SixthSenseIndustry, Long> {

	
	List<BaseDTO> industryWithBaseDTO();
}
