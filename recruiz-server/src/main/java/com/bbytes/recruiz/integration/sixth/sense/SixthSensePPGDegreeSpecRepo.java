package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSensePPGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;

public interface SixthSensePPGDegreeSpecRepo extends JpaRepository<SixthSensePPGDegreeSpecialization, Long> {

	List<SixthSenseBaseDTO> degreeSpecWithBaseDTO();
}
