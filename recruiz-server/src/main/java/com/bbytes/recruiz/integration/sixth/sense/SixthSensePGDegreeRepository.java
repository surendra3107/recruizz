package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSensePGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;

public interface SixthSensePGDegreeRepository extends JpaRepository<SixthSensePGDegree, Long> {

	List<BaseDTO> degreeWithBaseDTO();
}
