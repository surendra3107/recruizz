package com.bbytes.recruiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EmailActivity;

public interface EmailRepository extends JpaRepository<EmailActivity, Long> {

    Page<EmailActivity> findByEmailFrom(String fromEmail,Pageable pageable);
    
}
