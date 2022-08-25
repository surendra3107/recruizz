package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.GenericDecisionMaker;

public interface GenericDecisionMakerRepository extends JpaRepository<GenericDecisionMaker, Long> {

	GenericDecisionMaker findByEmail(String email);

	List<GenericDecisionMaker> findByMobile(String mobile);

}
