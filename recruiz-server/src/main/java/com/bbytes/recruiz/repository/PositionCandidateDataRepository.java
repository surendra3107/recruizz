package com.bbytes.recruiz.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.PositionCandidateData;

public interface PositionCandidateDataRepository extends JpaRepository<PositionCandidateData, Long> {

	@Query(value = "select distinct(candidate_id) from position_candidate_data  where to_status = :inProgress and logged_user_id = :userId and to_stage = :clientSubmission and modification_date between :start and :end", nativeQuery = true)
	Set<BigInteger> getCandidatesByUserAndToStage(@Param("inProgress") String inProgress,@Param("userId") Long userId, @Param("clientSubmission") String clientSubmission, @Param("start") Date start, @Param("end") Date end);

	@Query(value = "select distinct(candidate_id) from position_candidate_data  where to_status = :inProgress and client_id = :clientId and to_stage = :clientSubmission and modification_date between :start and :end", nativeQuery = true)
	Set<BigInteger> getCandidatesByClientAndToStage(@Param("inProgress") String inProgress,@Param("clientId") Long clientId, @Param("clientSubmission") String clientSubmission, @Param("start") Date start, @Param("end") Date end);

	@Query(value = "select distinct(count(candidate_id)) from position_candidate_data where position_id IN (?1) and to_status = ?2 and logged_user_id = ?3 and modification_date between ?4 AND ?5", nativeQuery = true)
	Long getCountByPositionIDsAndStatusAndOwnerAndDateRange(List <Long> positionIds, String status, Long userId, Date startDate, Date endDate);
	
	@Query(value = "select distinct(count(candidate_id)) from position_candidate_data where position_id IN (?1) and to_status IN (?2) and logged_user_id = ?3 and modification_date between ?4 AND ?5", nativeQuery = true)
	Long getCountByPositionIDsAndStatusesAndOwnerAndDateRange(List <Long> positionIds, List <String> statuses, Long userId, Date startDate, Date endDate);
	
	@Query(value = "select distinct(count(candidate_id)) from position_candidate_data where position_id IN (?1) and to_status IN (?2) and modification_date between ?3 AND ?4", nativeQuery = true)
    Long countByPositionIdAndStatusesInAndModificationDateBetween(BigInteger positionId, List<String> status, Date startDate, Date endDate);
	
	@Query(value = "select distinct(count(candidate_id)) from position_candidate_data where position_id IN (?1) and to_status = ?2 and modification_date between ?3 AND ?4", nativeQuery = true)
	Long countByPositionIdAndStatusAndModificationDateBetween(BigInteger positionId, String status, Date startDate, Date endDate);
	
	@Query(value = "select distinct(count(candidate_id)) from position_candidate_data where position_id IN (?1) and modification_date between ?2 AND ?3", nativeQuery = true)
	Long countByPositionIdAndModificationDateBetween(BigInteger positionId, Date startDate, Date endDate);

	@Query(value = "select candidate_id from position_candidate_data where logged_user_id = ?1 and to_status IN (?2) and to_stage = ?5 and modification_date between ?3 and ?4", nativeQuery = true)
	List<Object> findCandidateIdsBySourcebyAndStatusBetweenDate(Long userId, List<String> statusList, Date startDate,Date endDate, String clientSubmission);

	@Query(value = "select candidate_id from position_candidate_data where logged_user_id = ?1 and to_status = ?2 and to_stage = ?5 and modification_date between ?3 and ?4", nativeQuery = true)
	List<Object> findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(Long userId, String selected, Date startDate,Date endDate, String clientSubmission);

	@Query(value = "select candidate_id from position_candidate_data where position_id IN (?1) and to_status IN (?2) and to_stage = ?5 and modification_date between ?3 and ?4", nativeQuery = true)
	List<Object> findCandidateIdsByClientbyAndStatusBetweenDate(List<Long> positionIdList, List<String> statusList,Date startDate, Date endDate, String clientSubmission);

	@Query(value = "select candidate_id from position_candidate_data where position_id IN (?1) and to_status = ?2 and to_stage = ?5 and modification_date between ?3 and ?4", nativeQuery = true)
	List<Object> findCandidateIdsByClientbyAndSelectedStatusBetweenDate(List<Long> positionIdList, String selected,Date startDate, Date endDate, String clientSubmission);
}
