package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSensePGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;

public interface SixthSensePGDegreeSpecRepo extends JpaRepository<SixthSensePGDegreeSpecialization, Long> {

	List<SixthSenseBaseDTO> degreeSpecWithBaseDTO();
}
