package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseIndustry;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseIndustryService extends AbstractService<SixthSenseIndustry, Long> {

	private SixthSenseIndustryRepository sixthSenseIndustryRepository;

	@Autowired
	public SixthSenseIndustryService(SixthSenseIndustryRepository sixthSenseIndustryRepository) {
		super(sixthSenseIndustryRepository);
		this.sixthSenseIndustryRepository = sixthSenseIndustryRepository;
	}
	
	public List<BaseDTO> getIndustryList(){
		return sixthSenseIndustryRepository.industryWithBaseDTO();
	}
}
