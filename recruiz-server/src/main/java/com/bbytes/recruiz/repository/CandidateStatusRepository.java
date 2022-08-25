package com.bbytes.recruiz.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;

public interface CandidateStatusRepository extends JpaRepository<CandidateStatus, Long>{
	
	List<CandidateStatus> findByCandidateCidAndClientIdAndPositionPositionCodeAndCurrentIsTrue(long clientId ,long candidateId ,String positionCode);
	
	List<CandidateStatus> findByCandidateCidAndClientIdAndPositionPositionCode(long clientId ,long candidateId ,String positionCode);
	
	List<CandidateStatus> findByStatus(String status);
	
	@Query("select max(statusChangedDate) from candidate_status where client = ?1 AND position = ?2 AND candidate =?3 AND status =?4")
	Date getOfferedDate(Client client,Position position,Candidate candidate,String status);

	List<CandidateStatus> findByCandidateCidAndClientIdAndPositionPositionCodeAndStatusAndCurrentIsTrue(long candidateId,long clientId,String positionCode,String status);

	List<CandidateStatus> findByStatusAndCurrentIsTrueOrderByCreationDateDesc(String status);
	
	List<CandidateStatus> findByStatusInAndCurrentIsTrueOrderByCreationDateDesc(List<String> status);
	
	List<CandidateStatus> findByClientClientNameAndPositionPositionCodeAndStatusAndCurrentIsTrueOrderByCreationDateDesc(String clientName,String positionCode,String status);
	
	List<CandidateStatus> findByCandidateCid(long cid);
	
	List<CandidateStatus> findByCandidateCidAndPositionPositionCode(long cid ,String postionCode);
	
	List<CandidateStatus> findByCandidate(Candidate candidate);
	
	List<CandidateStatus> findByPositionId(long positionId);
	
	List<CandidateStatus> findByClient(Client client);
}
