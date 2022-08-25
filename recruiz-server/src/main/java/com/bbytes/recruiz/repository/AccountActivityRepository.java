package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.AccountActivity;

//@JaversSpringDataAuditable
public interface AccountActivityRepository extends JpaRepository<AccountActivity, Long> {

}
