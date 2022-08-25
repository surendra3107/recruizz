package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.OrganizationBankInfo;

public interface OrganizationBankInfoRepository extends JpaRepository<OrganizationBankInfo, Long>{

	List<OrganizationBankInfo> findByAddedBy(String email);
	
	OrganizationBankInfo findByAccountNumber(String accountNumber);
}
