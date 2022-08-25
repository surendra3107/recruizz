package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSensePPGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSensePPGDegreeService extends AbstractService<SixthSensePPGDegree, Long> {

	private SixthSensePPGDegreeRepository sixthSensePPGDegreeRepository;

	@Autowired
	public SixthSensePPGDegreeService(SixthSensePPGDegreeRepository sixthSensePPGDegreeRepository) {
		super(sixthSensePPGDegreeRepository);
		this.sixthSensePPGDegreeRepository = sixthSensePPGDegreeRepository;
	}
	
	public List<BaseDTO> getPPGDegreeList() {
		return sixthSensePPGDegreeRepository.degreeWithBaseDTO();
	}
}
