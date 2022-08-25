package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSensePGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSensePGDegreeService extends AbstractService<SixthSensePGDegree, Long> {

	private SixthSensePGDegreeRepository sixthSensePGDegreeRepository;

	@Autowired
	public SixthSensePGDegreeService(SixthSensePGDegreeRepository sixthSensePPGDegreeRepository) {
		super(sixthSensePPGDegreeRepository);
		this.sixthSensePGDegreeRepository = sixthSensePPGDegreeRepository;
	}
	
	public List<BaseDTO> getPGDegreeList() {
		return sixthSensePGDegreeRepository.degreeWithBaseDTO();
	}
}
