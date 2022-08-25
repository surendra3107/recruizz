package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseUGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;

public interface SixthSenseUGDegreeSpecRepo extends JpaRepository<SixthSenseUGDegreeSpecialization, Long> {

	List<SixthSenseBaseDTO> degreeSpecWithBaseDTO();
}
