package com.bbytes.recruiz.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Notification findById(long id);

	List<Notification> findByUser(String userEmail);

	Page<Notification> findByUser(String userEmail, Pageable pageable);

	Page<Notification> findByUserAndViewStateIsFalse(String userEmail, Pageable pageable);

	List<Notification> findByViewStateAndReadStateAndUser(boolean readState, boolean viewState, String userEmail);

	Page<Notification> findByReadState(boolean readState, Pageable pageable);

	Long countByReadStateAndUser(boolean readState, String userEmail);

	Long countByViewStateAndUser(boolean readState, String userEmail);

	Long countByViewStateAndUserAndClientIdGreaterThan(boolean readState, String userEmail, long clientId);

	Long countByViewStateAndUserAndPositionCodeIsNotNull(boolean readState, String userEmail);

	Long countByViewStateAndUserAndCandidateIdGreaterThan(boolean readState, String userEmail, long candidateId);

	Long countByViewStateAndUserAndRoundCandidateIdGreaterThan(boolean readState, String userEmail,
			long roundCandidateId);

	Long countByViewStateAndUserAndRoundIdGreaterThan(boolean readState, String userEmail, long roundId);

	Long countByViewStateAndUserAndRequestedPositionIdGreaterThan(boolean readState, String userEmail,
			long requestedPositionId);

	Long countByViewStateAndUserAndInterviewScheduleIdGreaterThan(boolean readState, String userEmail,
			long interviewScheduleId);

	List<Notification> findByViewStateAndUserAndClientId(boolean readState, String userEmail, long clientId);

	List<Notification> findByUserAndClientIdAndViewStateFalse(String userEmail, long clientId);

	List<Notification> findByViewStateAndUserAndPositionCode(boolean readState, String userEmail, String positionCode);

	List<Notification> findByViewStateAndUserAndCandidateId(boolean readState, String userEmail, long candidateId);

	List<Notification> findByViewStateAndUserAndRoundId(boolean readState, String userEmail, long clientId);

	List<Notification> findByViewStateAndUserAndRoundCandidateId(boolean readState, String userEmail,
			long roundCandidateId);

	@Query("select distinct n from notification n where clientId = ?1 AND notificationEventType IN ?2 AND user=?3")
	List<Notification> getClientActivity(Long clientId, Set<String> eventType, String userEmail);

	@Query("select distinct n from notification n where positionCode = ?1 AND notificationEventType IN ?2 order by modification_date desc")
	LinkedList<Notification> getPositionActivity(String positionCode, Set<String> eventType);

	List<Notification>  findByPositionCode(String positionCode);
	
	List<Notification>  findByClientId(Long id);
	
	List<Notification>  findByCandidateId(Long id);
	
	
	
}
