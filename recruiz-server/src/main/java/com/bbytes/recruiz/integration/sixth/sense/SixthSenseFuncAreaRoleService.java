package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalAreaRole;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseFuncAreaRoleService extends AbstractService<SixthSenseFunctionalAreaRole, Long> {

	private SixthSenseFuncAreaRoleRepository sixthSenseFuncAreaRolesRepository;

	@Autowired
	public SixthSenseFuncAreaRoleService(SixthSenseFuncAreaRoleRepository sixthSenseIndustryRepository) {
		super(sixthSenseIndustryRepository);
		this.sixthSenseFuncAreaRolesRepository = sixthSenseIndustryRepository;
	}
	
	@Autowired
	public List<SixthSenseBaseDTO> getRoleList() {
		return sixthSenseFuncAreaRolesRepository.roleWithBaseDTO();
	}
}
