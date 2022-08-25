package com.bbytes.recruiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ClientActivity;

public interface ClientActivityRepository extends JpaRepository<ClientActivity, Long> {

	Page<ClientActivity> findByClientId(Long clientId,Pageable pageable);

}
