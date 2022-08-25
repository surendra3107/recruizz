package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalArea;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseFuncAreaService extends AbstractService<SixthSenseFunctionalArea, Long> {

	private SixthSenseFuncAreaRepository sixthSenseFuncAreaRepository;

	@Autowired
	public SixthSenseFuncAreaService(SixthSenseFuncAreaRepository sixthSenseIndustryRepository) {
		super(sixthSenseIndustryRepository);
		this.sixthSenseFuncAreaRepository = sixthSenseIndustryRepository;
	}
	
	public List<BaseDTO> getFuncAreaList(){
		return sixthSenseFuncAreaRepository.funcAreaWithBaseDTO();
	}
}
