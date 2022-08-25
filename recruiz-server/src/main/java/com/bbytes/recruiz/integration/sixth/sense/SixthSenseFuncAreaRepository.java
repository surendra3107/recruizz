package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalArea;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;

public interface SixthSenseFuncAreaRepository extends JpaRepository<SixthSenseFunctionalArea, Long> {

	List<BaseDTO> funcAreaWithBaseDTO();
}
