package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSensePPGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;

public interface SixthSensePPGDegreeRepository extends JpaRepository<SixthSensePPGDegree, Long> {

	List<BaseDTO> degreeWithBaseDTO();
}
