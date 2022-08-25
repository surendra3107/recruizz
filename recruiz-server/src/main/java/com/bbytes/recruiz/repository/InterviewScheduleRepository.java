package com.bbytes.recruiz.repository;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.InterviewSchedule;

//@JaversSpringDataAuditable
public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {

	public InterviewSchedule findOneByInterviewerEventIdOrCandidateEventId(String inetrviewerEventId,
			String candidateEventId);

	long countByCandidateEmail(String candidateEmail);

	public List<InterviewSchedule> findByPositionCode(String positionCode);

	@Query(value = "SELECT * FROM interview_schedule WHERE Month(startsAt) = ?1", nativeQuery = true)
	List<InterviewSchedule> findByMonth(String monthInNumber);

	@Query(value = "SELECT * FROM interview_schedule WHERE Month(startsAt) = ?1 AND positionCode = ?2", nativeQuery = true)
	List<InterviewSchedule> findByMonthAndPosition(String monthInNumber, String positionCode);

	List<InterviewSchedule> findByPositionCodeAndRoundIdAndCandidateEmail(String positionCode, String roundId,
			String candidateEmail);

	List<InterviewSchedule> findByPositionCodeAndRoundIdAndCandidateEmailAndActiveAndStartsAtAfter(String positionCode,
			String roundId, String candidateEmail, boolean active, Date date);

	List<InterviewSchedule> findByPositionCodeAndRoundIdAndCandidateEmailAndActiveAndEndsAtAfter(String positionCode,
			String roundId, String candidateEmail, boolean active, Date endDate);

	@Query(value = "SELECT * FROM interview_schedule WHERE Month(startsAt) = ?1 AND interviewSchedulerEmail = ?2", nativeQuery = true)
	List<InterviewSchedule> findByMonthAndOwner(String monthInNumber, String email);

	@Query(value = "SELECT * FROM interview_schedule WHERE Day(startsAt) = ?1 AND interviewSchedulerEmail = ?2", nativeQuery = true)
	List<InterviewSchedule> findTodaysScheduleByOwner(String day, String email);

	@Query(value = "SELECT distinct * FROM interview_schedule WHERE (Day(startsAt) >= ?1 AND Day(startsAt) <= ?2) AND (Month(startsAt) = ?3 OR Month(startsAt) = ?4) AND (Year(startsAt) = ?5 OR Year(startsAt) = ?6) AND interviewSchedulerEmail = ?7", nativeQuery = true)
	List<InterviewSchedule> findTodaysScheduleByOwner(String startDay, String endDay, String month, String nextMonth,
			String year, String nextYear, String email);

	@Query(value = "SELECT distinct * FROM interview_schedule where startsAt between ?1 and ?2 and interviewSchedulerEmail=?3 order by startsAt desc", nativeQuery = true)
	List<InterviewSchedule> findTodaysScheduleByOwner(Date startDate, Date endDate, String email);

	List<InterviewSchedule> findByInterviewSchedulerEmail(String schedulerEmail);
	
	@Query(value = "SELECT distinct * FROM interview_schedule" , nativeQuery = true)
	List<InterviewSchedule> findByAllInterview();

	@Query(value = "select distinct * from interview_schedule where id in(select Schedule_ID from interview_hr where HR_ID = ?1);", nativeQuery = true)
	List<InterviewSchedule> findByScheduleHrExecutivesIn(String id);

	List<InterviewSchedule> findByStartsAtBetween(java.util.Date startAtFrom, java.util.Date startAtEnd);

	@Query(value = "SELECT distinct * FROM interview_schedule where startsAt between ?1 and ?2 and positionCode=?3 order by startsAt desc", nativeQuery = true)
	List<InterviewSchedule> findTodaysScheduleByPositionCode(Date startDate, Date endDate, String positionCode);

	LinkedList<InterviewSchedule> findByPositionCodeAndCandidateEmailAndActiveAndEndsAtAfter(String positionCode,
			String candidateEmail, boolean active, Date endDate);

	Long countByPositionCodeAndCandidateEmailAndActiveAndEndsAtAfter(String positionCode,
			String candidateEmail, boolean active, Date endDate);

	Long countByPositionCode(String positionCode);

	Long countByPositionCodeAndInterviewSchedulerEmail(String positionCode,String email);
	
	Long countByPositionCodeInAndInterviewSchedulerEmail(List<String> positionCodes,String email);
	
	Long countByPositionCodeAndInterviewSchedulerEmailAndModificationDateBetween(String positionCode,String email,Date startDate,Date endDate);
	
	Long countByPositionCodeAndModificationDateBetween(String positionCode,Date startDate,Date endDate);

	List<InterviewSchedule> findByActiveAndAttendeeIn(boolean activeStatus,Set<EventAttendee> attendee);

	@Query(value = "SELECT * FROM interview_schedule where active=true AND id in (select interview_schedule_id from interview_schedule_event_attendee where attendee_id in (select id from event_attendee where email=?1))", nativeQuery = true)
	public List<InterviewSchedule> getSchedulesByInterviewerEmail(String email);

	Long countByPositionCodeAndCreationDateBetween(String positionCode,java.util.Date startDate,java.util.Date endDate);

	@Query(value="select * from interview_schedule where positionCode=?1 and roundName=?2 and candidateEmail = ?3 order by startsAt desc Limit 1",nativeQuery=true)
	public List<InterviewSchedule> getScheduleByPcodAndRoundNameAndCanEmail(String pcode, String rname,
		String cemail);

	Long countByPositionCodeIn(List<String> positionCodes);

	@Query(value = "SELECT distinct(id) FROM interview_schedule where startsAt between ?1 and ?2 and clientName=?3 order by startsAt desc", nativeQuery = true)
	public Set<Long> findTodaysScheduleByClient(Date startDate, Date endDate, String clientName);
}
