package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.InterviewerTimeSlot;

//@JaversSpringDataAuditable
public interface InterviewerTimeSlotRepository extends JpaRepository<InterviewerTimeSlot, Long> {

	public InterviewerTimeSlot findOneById(long id);
}
