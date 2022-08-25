package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Vendor;

//@JaversSpringDataAuditable
public interface VendorRepository extends JpaRepository<Vendor, Long> {

	Vendor findByEmail(String email);
	
	List<Vendor> findByStatus(Boolean status);

	
}
