package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseUGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;

public interface SixthSenseUGDegreeRepository extends JpaRepository<SixthSenseUGDegree, Long> {

	List<BaseDTO> degreeWithBaseDTO();
}
