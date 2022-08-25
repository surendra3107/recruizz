package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.integration.SixthSenseCity;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;

public interface SixthSenseCityRepository extends JpaRepository<SixthSenseCity, Long> {

	List<SixthSenseBaseDTO> cityWithBaseDTO();

	SixthSenseCity findByCode(String code);

	@Query(value = "select c.name from sixth_sense_city c where c.code IN (?1)", nativeQuery = true)
	List<String> findCityNameByCodes(List<String> list);

}
