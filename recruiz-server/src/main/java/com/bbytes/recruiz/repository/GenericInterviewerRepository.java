package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.GenericInterviewer;

public interface GenericInterviewerRepository extends JpaRepository<GenericInterviewer, Long> {

    GenericInterviewer findByEmail(String email);

    List<GenericInterviewer> findByMobile(String mobile);

    List<GenericInterviewer> findByEmailStartingWith(String prefix);
    
    // update event attendee on updating interviewer information if any
    @Modifying
    @Query(value = "update event_attendee set name = ?1 where email =?2", nativeQuery = true)
    void updateInterviewerEventAttendeeOnInterviewerUpdate(String name, String emailId);

}
