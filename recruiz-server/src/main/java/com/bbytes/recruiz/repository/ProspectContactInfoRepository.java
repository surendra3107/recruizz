package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectContactInfo;

public interface ProspectContactInfoRepository extends JpaRepository<ProspectContactInfo, Long> {

	List<ProspectContactInfo> findByProspect(Prospect prospect);
	
	ProspectContactInfo findByMobile(String mobile);
	
	ProspectContactInfo findByEmail(String email);
}
