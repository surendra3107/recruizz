package com.bbytes.recruiz.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;

//@JaversSpringDataAuditable
public interface RoundCandidateRepository extends JpaRepository<RoundCandidate, Long> {

    RoundCandidate findOneByCandidate(Candidate candidate);

    RoundCandidate findOneByCandidateAndRoundId(Candidate candidate, String roundId);

    List<RoundCandidate> findByRoundId(String roundId);

    RoundCandidate findTop1ByRoundIdOrderByCardIndexAsc(String roundId);

    RoundCandidate findOneByCandidateAndPositionCode(Candidate candidate, String positionCode);

    List<RoundCandidate> findByCandidate(Candidate candidate);

    @Query(value = "SELECT roundId,count(candidate_cid) FROM round_candidate where positionCode = ?1  group by roundId order by roundId desc", nativeQuery = true)
    List<Object> findBoardCandidateCount(String positionCode);

    @Query(value = "select id from rounds where board_id = ?1 order by orderNo asc", nativeQuery = true)
    List<Object> findBoardRound(String boardID);

    @Query(value = "select count(candidate_cid) from round_candidate where roundId = ?1", nativeQuery = true)
    String findCandidateCountForRound(String roundId);

    @Query(value = "select count(candidate_cid) from round_candidate where roundId = ?1 and sourcedBy = ?2", nativeQuery = true)
    String findCandidateCountForRoundAndSourcedBy(String roundId, String sourcedBy);

    RoundCandidate findByPositionCodeAndCandidate(String positionCode, Candidate candidate);

    List<RoundCandidate> findByStatusAndCandidate(String status, Candidate candidate);

    List<RoundCandidate> findByPositionCodeAndStatus(String postionCode, String status);

    List<RoundCandidate> findByPositionCode(String positionCode);

    Set<RoundCandidate> findByRoundIdAndSourcedBy(String roundId, String sourcedBy);

    Set<RoundCandidate> findByModificationDateBetweenAndRoundIdAndSourcedByAndStatus(Date startDate, Date endDate,
	    String roundId, String sourcedBy, String status);

    Set<RoundCandidate> findByModificationDateBetweenAndRoundIdAndSourcedBy(Date startDate, Date endDate,
	    String roundId, String sourcedBy);

    List<RoundCandidate> findByPositionCodeAndStatusAndModificationDateBefore(String positionCode, String status,
	    Date modificationDate);

    Long countByPositionCodeAndModificationDateBeforeAndStatusIn(String positionCode, Date modificationDate,
	    Set<String> status);

    @Query(value = "select candidate_cid from round_candidate where positionCode IN (?1) and sourcedBy = ?2 and status IN (?3) and modification_date between ?4 and ?5", nativeQuery = true)
    List<Object> findCandidateIdsByPositionCodeAndSourcebyAndStatusBetweenDate(Collection<String> positionCodes,
	    String sourcedBy, Collection<String> statuses, Date startDate, Date endDate);

    @Query(value = "select candidate_cid from round_candidate where sourcedBy = ?1 and status IN (?2) and modification_date between ?3 and ?4", nativeQuery = true)
    List<Object> findCandidateIdsBySourcebyAndStatusBetweenDate(String sourcedBy, Collection<String> statuses,
	    Date startDate, Date endDate);
    
    @Query(value = "select candidate_cid from round_candidate where sourcedBy = ?1 and status = ?2 and modification_date between ?3 and ?4", nativeQuery = true)
    List<Object> findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(String sourcedBy, String status, Date startDate, Date endDate);

    @Query(value = "select candidate_cid from round_candidate where positionCode IN (?1) and sourcedBy = ?2 and creation_date between ?3 and ?4", nativeQuery = true)
    List<Object> findCandidateIdsByPositionCodeAndSourcebyBetweenDate(Collection<String> positionCodes,
	    String sourcedBy, Date startDate, Date endDate);

    @Query(value = "select candidate_cid from round_candidate where sourcedBy = ?1 and creation_date between ?2 and ?3", nativeQuery = true)
    List<Object> findCandidateIdsBySourcebyBetweenDate(String sourcedBy, Date startDate, Date endDate);

    Long countByPositionCodeAndStatusIn(String positionCode, List<String> status);
    
    Long countByPositionCodeAndStatusInAndSourcedBy(String positionCode, List<String> status,String ownerEmail);
    
    Long countByPositionCodeInAndStatusInAndSourcedBy(List<String> positionCode, List<String> status,String ownerEmail);
    
    Long countByPositionCodeAndStatusInAndSourcedByAndModificationDateBetween(String positionCode, List<String> status,String ownerEmail,Date startDate,Date endDate);
    
    Long countByPositionCodeAndStatusInAndModificationDateBetween(String positionCode, List<String> status,Date startDate,Date endDate);

    Long countByPositionCodeAndStatus(String positionCode, String status);
    
    Long countByPositionCodeAndStatusAndSourcedBy(String positionCode, String status,String ownerEmail);
    
    Long countByPositionCodeAndStatusAndSourcedByAndModificationDateBetween(String positionCode, String status,String ownerEmail,Date startDate,Date endDate);

    Long countByPositionCodeAndStatusAndSourcedByAndModificationDateBetween(List<String> positionCode, String status,String ownerEmail,Date startDate,Date endDate);
    
    Long countByPositionCodeInAndStatus(List<String> positionCodes, String status);
    
    Long countByPositionCodeInAndStatusAndModificationDateBetween(List<String> positionCodes, String status,Date startDate,Date endDate);
    
    Long countByPositionCodeInAndStatusIn(List<String> positionCodes, List<String> statuses);
    
    Long countByPositionCodeInAndStatusInAndModificationDateBetween(List<String> positionCodes, List<String> statuses,Date startDate,Date endDate);
    
    Long countByPositionCodeInAndStatusInAndSourcedByAndModificationDateBetween(List<String> positionCodes, List<String> statuses,String ownerEmail,Date startDate,Date endDate);
    
    Long countByPositionCodeInAndStatusAndSourcedBy(List<String> positionCodes, String status,String ownerEmail);
    
    Long countByPositionCodeInAndStatusAndSourcedByAndModificationDateBetween(List<String> positionCodes, String status,String ownerEmail,Date startDate,Date endDate);
    
    //Add newly to take care of JoindByHR also in the query
    Long countByPositionCodeInAndStatusAndSourcedByOrJoinedByHrAndModificationDateBetween (List<String> positionCodes, String status,String ownerEmail, String jonedByHR, Date startDate, Date endDate);

    Long countByPositionCodeIn(List<String> positionCodes);
    
    Long countByPositionCodeInAndModificationDateBetween(List<String> positionCodes,Date startDate,Date endDate);

    Long countByPositionCode(String positionCode);

    Long countByPositionCodeAndRound(String positionCode, Round round);

    @Query(value = "select count(*) from round_candidate where positionCode = ?1 AND roundId = (?2) AND status = (?3);", nativeQuery = true)
    Long countByPositionCodeAndRoundAndStatus(String positionCode, Long round, String status);

    Page<RoundCandidate> findDistinctByStatusIn(List<String> status, Pageable pageable);

    List<RoundCandidate> findByStatusInAndCandidate(List<String> status, Candidate candidate);

    @Query(value = "select distinct(candidate_cid) from round_candidate where status IN ?1 order by 'modification_date' desc ", nativeQuery = true)
    List<Long> findDistinctCandidateIds(List<String> status);

    List<RoundCandidate> findByIdIn(Set<Long> ids);

    Long countByPositionCodeAndStatusAndModificationDateBetween(String positionCode, String status, Date startDate,
	    Date endDate);
    
    Long countByPositionCodeAndModificationDateBetween(String positionCode, Date startDate,
    	    Date endDate);

    Long countByPositionCodeAndSourcedBy(String positionCode, String sourcedBy);

    Long countByPositionCodeAndSourcedByAndModificationDateBetween(String positionCode, String sourcedBy,Date startDate, Date endDate);

    Long countByPositionCodeInAndSourcedBy(List<String> positionCodes, String sourcedBy);
    
    //Original
    Long countByPositionCodeInAndSourcedByAndModificationDateBetween(List<String> positionCodes, String sourcedBy,Date startDate, Date endDate);
    
    //Modified by Sajin
    @Query(value="select count(*) from round_candidate where positionCode IN (?1) AND sourcedBy = ?2 AND Date(creation_date) between Date(?3) AND Date(?4)",nativeQuery=true)
    Long countByPositionIDsInAndSourcedByAndModificationDateBetween(List<Long> positionIds, String sourcedBy,Date startDate, Date endDate);

    @Query(value="select * from round_candidate where positionCode = ?1 AND Date(creation_date) between Date(?2) AND Date(?3);",nativeQuery=true)
    List<RoundCandidate> getCandidateByPositionCodeAndDateBetween(String positionCode, Date startDate, Date endDate);

    @Query(value="select count(*) from round_candidate where positionCode = ?1 AND Date(creation_date) between Date(?2) AND Date(?3);",nativeQuery=true)
    Long getCandidateCountByPositionCodeAndDateBetween(String positionCode, Date startDate, Date endDate);

    @Query(value = "select * from round_candidate where positionCode = ?1 AND sourcedBy = ?2", nativeQuery = true)
	List<RoundCandidate> getRoundCandidateByPositionCodeAndSourcedBy(String positionCode, String email);

    @Query(value = "select sourcedBy from round_candidate where positionCode = ?1", nativeQuery = true)
	Set<String> getRecruitersEmailByPositionCode(String positionCode);

    @Query(value = "select * from round_candidate where positionCode = ?1 AND sourcedBy = ?2 AND modification_date between ?3 and ?4", nativeQuery = true)
	List<RoundCandidate> getRoundCandidateByPositionCodeAndSourcedByForCustomStatus(String positionCode, String hrEmail,
			Date startDate, Date endDate);

    @Query(value = "select candidate_cid from round_candidate where positionCode IN (?1) and status IN (?2) and modification_date between ?3 and ?4", nativeQuery = true)
	List<Object> findCandidateIdsByClientbyAndStatusBetweenDate(List<String> positionCodeList, List<String> statusList,
			Date startDate, Date endDate);
    
    @Query(value = "select candidate_cid from round_candidate where positionCode IN (?1) and status = ?2 and modification_date between ?3 and ?4", nativeQuery = true)
    List<Object> findCandidateIdsByClientbyAndSelectedStatusBetweenDate(List<String> positionCodeList, String status, Date startDate, Date endDate);

    @Query(value = "select * from round_candidate where sourcedBy = ?1", nativeQuery = true)
	List<RoundCandidate> getCandidateSourcedBy(String email);

}
