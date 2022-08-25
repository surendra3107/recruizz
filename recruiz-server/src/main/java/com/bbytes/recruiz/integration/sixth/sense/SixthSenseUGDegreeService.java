package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseUGDegree;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseUGDegreeService extends AbstractService<SixthSenseUGDegree, Long> {

	private SixthSenseUGDegreeRepository sixthSenseUGDegreeRepository;

	@Autowired
	public SixthSenseUGDegreeService(SixthSenseUGDegreeRepository sixthSensePPGDegreeRepository) {
		super(sixthSensePPGDegreeRepository);
		this.sixthSenseUGDegreeRepository = sixthSensePPGDegreeRepository;
	}

	public List<BaseDTO> getUGDegreeList() {
		return sixthSenseUGDegreeRepository.degreeWithBaseDTO();
	}
}
