package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalAreaRole;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;

public interface SixthSenseFuncAreaRoleRepository extends JpaRepository<SixthSenseFunctionalAreaRole, Long> {

	List<SixthSenseBaseDTO> roleWithBaseDTO();
}
