package com.bbytes.recruiz.repository;


import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.RoundCandidate;

//@JaversSpringDataAuditable
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	@Query(value = "SELECT roundId FROM Feedback WHERE RoundCandidateId = ?1 group by roundId order by roundId desc", nativeQuery = true)
	List<String> findRoundCandidateIdList(String RoundCandidateId);

	List<Feedback> findByRoundCandidateIdAndRoundIdOrderByIdDesc(String roundCandidateId, String roundId);

	List<Feedback> findByRoundCandidateAndRoundIdAndStatusNot(RoundCandidate candidate, String roundId, String status);

	List<Feedback> findByRoundCandidateAndRoundIdAndActive(RoundCandidate candidate, String roundId, boolean active);

	List<Feedback> findByRoundCandidateAndRoundIdAndActiveAndType(RoundCandidate candidate, String roundId,
			boolean active, String type);

	@Query(value = "SELECT count(status) FROM Feedback WHERE roundId = ?1 AND status = ?2 AND roundCandidateId = ?3 AND active = 1", nativeQuery = true)
	String findFeedbackCount(String roundId, String status, String roundCandidateId);

	@Query(value = "SELECT count(*) FROM Feedback WHERE roundId = ?1 AND roundCandidateId = ?2 AND active = 1", nativeQuery = true)
	String findTotalActiveFeedbackCount(String roundId, String roundCandidateId);

	List<Feedback> findByRoundCandidateIdAndRoundIdAndFeedbackByAndActiveAndType(String roundCandidateId,
			String roundId, String feedbackBy, boolean active, String type);

	Page<Feedback> findByCandidateIdAndActiveIsTrueOrderByRoundCandidateIdDesc(String candidateId, Pageable pageable);

	@Query(value = "SELECT count(*) FROM Feedback WHERE roundCandidateId = ?1 AND active = 1", nativeQuery = true)
	String findTotalActiveFeedbackCountByCandidate(String roundCandidateId);

	@Query(value = "SELECT count(status) FROM Feedback WHERE status = ?1 AND roundCandidateId = ?2 AND active = 1", nativeQuery = true)
	String findFeedbackCountByCandidateAndStatus(String status, String roundCandidateId);

	@Query(value = "select distinct(roundCandidateId) from feedback where candidateId= ?1 order by roundCandidateId desc", nativeQuery = true)
	List<String> findRoundCandidateIdByCandidate(String candidateId);

	Page<Feedback> findByRoundCandidateIdOrderByCreationDateDesc(String roundCandidateId, Pageable pageable);

	Long countByFeedbackNot(String feedback);

	Feedback findByLevelbarFeedbackShareResultId(String levelbarkey);

	List<Feedback> findByFeedbackByAndActive(String email,boolean activeStatus);

	@Query(value = "SELECT count(*) FROM Feedback WHERE positionName = ?1 AND clientName = ?2 AND active = 1 AND creation_date between (?3 AND ?4) AND type='Forwarded'", nativeQuery = true)
	Long findTotalActiveFeedbackCountByCandidate(String positionName,String clientName,Date startDate,Date endDate);

	Long countByPositionNameAndClientNameAndEventCreatorEmailAndType(String position,String client,String createdby,String type);
	
	Long countByPositionNameAndClientNameAndEventCreatorEmailAndTypeNot(String position,String client,String createdby,String type);

	//@author - Sajin
	@Query(value = "SELECT count(*) FROM Feedback WHERE positionName in (?1) AND event_creator_email = ?2 AND modification_date between (?3 AND ?4) AND type='Forwarded'", nativeQuery = true)
	Long countByPositionListAndClientNameAndEventCreatorEmail(List<String> position, String createdby, Date startDate, Date endDate);

	
	Long countByPositionNameAndClientNameAndEventCreatorEmailAndTypeNotAndModificationDateBetween(String position,String client,String createdby,String type,Date startDate,Date endDate);
	
	Long countByPositionNameAndClientNameAndTypeNotAndModificationDateBetween(String position,String client,String type,Date startDate,Date endDate);
	
	Long countByPositionNameInAndClientNameAndEventCreatorEmailAndTypeNot(List<String> positions,String client,String createdby,String type);

	@Query(value="select * from feedback where roundCandidateId = ?1 and type='Forwarded' and feedback != 'Yet to give' order by creation_date desc Limit 1",nativeQuery=true)
	Feedback getTopFeedbackByRoundCandidateForForwardType(Long id);

	@Query(value="select distinct(count(*)) from feedback where roundId in (select distinct id from rounds where board_id in(select board_id from position where positionCode in (?1)))",nativeQuery=true)
	Long getTotalFeedbackCountByPositionCodes(List<String> positionCodes);
	
	@Query(value="select distinct(count(*)) from feedback where modification_date between (?2 AND ?3) and roundId in (select distinct id from rounds where board_id in(select board_id from position where positionCode in (?1)))",nativeQuery=true)
	Long getTotalFeedbackCountByPositionCodesAndDateRange(List<String> positionCodes,Date startDate,Date endDate);

}
