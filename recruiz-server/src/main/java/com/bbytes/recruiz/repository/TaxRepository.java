package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Tax;

public interface TaxRepository extends JpaRepository<Tax, Long>{

	public Tax findByTaxNameIgnoreCase(String taxName);
}
