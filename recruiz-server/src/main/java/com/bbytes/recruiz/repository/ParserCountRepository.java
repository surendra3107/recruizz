package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.ParserCount;


//@JaversSpringDataAuditable
public interface ParserCountRepository extends JpaRepository<ParserCount, Long> {
	
	@Query(value = "SELECT max(id) FROM parser_count", nativeQuery = true)
	String findMaxId();
	
	long count();
}
