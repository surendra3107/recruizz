package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSensePGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSensePGDegreeSpecService extends AbstractService<SixthSensePGDegreeSpecialization, Long> {

	private SixthSensePGDegreeSpecRepo sixthSensePGDegreeSpecRepo;

	@Autowired
	public SixthSensePGDegreeSpecService(SixthSensePGDegreeSpecRepo sixthSensePPGDegreeSpecRepo) {
		super(sixthSensePPGDegreeSpecRepo);
		this.sixthSensePGDegreeSpecRepo = sixthSensePPGDegreeSpecRepo;
	}
	
	public List<SixthSenseBaseDTO> getPGDegreeSpecList() {
		return sixthSensePGDegreeSpecRepo.degreeSpecWithBaseDTO();
	}
}
