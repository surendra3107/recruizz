package com.bbytes.recruiz.integration.sixth.sense;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.SixthSenseCity;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseCityService extends AbstractService<SixthSenseCity, Long> {

	private SixthSenseCityRepository sixthSenseCityRepository;

	@Autowired
	public SixthSenseCityService(SixthSenseCityRepository sixthSenseIndustryRepository) {
		super(sixthSenseIndustryRepository);
		this.sixthSenseCityRepository = sixthSenseIndustryRepository;
	}

	public List<SixthSenseBaseDTO> getCityList() {
		return sixthSenseCityRepository.cityWithBaseDTO();
	}

	public SixthSenseCity getCityByCode(String code) {
		return sixthSenseCityRepository.findByCode(code);
	}

	public List<String> findCityNameByCodes(List<String> codeList) {
		if (codeList != null && !codeList.isEmpty())
			return sixthSenseCityRepository.findCityNameByCodes(codeList);
		else
			return new ArrayList<String>();
	}
}
