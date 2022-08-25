package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EventAttendee;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

  List<EventAttendee> findByEmail(String email);
}
