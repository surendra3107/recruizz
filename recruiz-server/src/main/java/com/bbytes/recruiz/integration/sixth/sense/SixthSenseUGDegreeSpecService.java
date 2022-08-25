package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseUGDegreeSpecialization;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseUGDegreeSpecService extends AbstractService<SixthSenseUGDegreeSpecialization, Long> {

	private SixthSenseUGDegreeSpecRepo sixthSenseUGDegreeSpecRepo;

	@Autowired
	public SixthSenseUGDegreeSpecService(SixthSenseUGDegreeSpecRepo sixthSensePPGDegreeSpecRepo) {
		super(sixthSensePPGDegreeSpecRepo);
		this.sixthSenseUGDegreeSpecRepo = sixthSensePPGDegreeSpecRepo;
	}
	
	public List<SixthSenseBaseDTO> getUGDegreeSpecList() {
		return sixthSenseUGDegreeSpecRepo.degreeSpecWithBaseDTO();
	}
}
