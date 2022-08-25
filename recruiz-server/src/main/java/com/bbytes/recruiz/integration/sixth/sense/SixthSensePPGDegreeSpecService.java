package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSensePPGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSensePPGDegreeSpecService extends AbstractService<SixthSensePPGDegreeSpecialization, Long> {

	private SixthSensePPGDegreeSpecRepo sixthSensePPGDegreeSpecRepo;

	@Autowired
	public SixthSensePPGDegreeSpecService(SixthSensePPGDegreeSpecRepo sixthSensePPGDegreeSpecRepo) {
		super(sixthSensePPGDegreeSpecRepo);
		this.sixthSensePPGDegreeSpecRepo = sixthSensePPGDegreeSpecRepo;
	}
	
	public List<SixthSenseBaseDTO> getPPGDegreeSpecList() {
		return sixthSensePPGDegreeSpecRepo.degreeSpecWithBaseDTO();
	}
}
