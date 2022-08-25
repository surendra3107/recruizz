package com.bbytes.recruiz.repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;

//@JaversSpringDataAuditable
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Candidate findOneByEmailOrAlternateEmail(String email, String alternateEmail);

    Candidate findOneByEmail(String email);

    List<Candidate> findByHighestQualLike(String qual);

    List<Candidate> findByCurrentLocationAndKeySkills(String location, String keySkill);

    List<Candidate> findByStatus(String status);

    List<Candidate> findByKeySkillsAndTotalExpAndHighestQual(String keySkill, String totalExp, String qual);

    List<Candidate> findByKeySkillsAndTotalExpAndHighestQualAndExpectedCtc(String keySkill, String totalExp,
	    String qual, String ctc);

    @Query("select c.email from candidate c")
    List<String> getAllCandidateEmail();

    List<String> findByKeySkillsLike(String skills);

    List<Candidate> findByCidNotIn(Collection<Long> cids);

    List<Candidate> findTop100ByCidNotInOrderByModificationDateDesc(Collection<Long> cids);

    List<Candidate> findTop100ByOwnerAndCidNotInOrderByModificationDateDesc(String ownerEmail, Collection<Long> cids);

    List<Candidate> findTop100ByOrderByModificationDateDesc();

    List<Candidate> findTop100ByOwnerOrderByModificationDateDesc(String ownerEmail);

    @Query("select count(c.email) from candidate c where c.status = 'Active'")
    public Integer getActiveCandidateCount();

    public List<Candidate> findByMobileOrAlternateMobile(String number, String alternateMobile);

    Page<Candidate> findAll(Pageable pageable);

    Page<Candidate> findByCidNotIn(Collection<Long> cids, Pageable pageable);

    Page<Candidate> findByOwner(Pageable pageable, String ownerEmail);
    
    @Query(value = "select * from candidate c where c.owner =?1", nativeQuery = true)
    List<Candidate> findListByOwner(String ownerEmail);
    
    Long countByOwner(String ownerEmail);

    Page<Candidate> findByOwnerAndCidNotIn(String ownerEmail, Collection<Long> cids, Pageable pageable);

    Page<Candidate> findBySourceEmail(String email, Pageable pageable);

    List<Candidate> findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndCidNotInOrderByModificationDateDesc(
	    String fullName, String email, Collection<Long> cids);

    List<Candidate> findTop100ByOwnerAndFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndCidNotInOrderByModificationDateDesc(
	    String ownerEmail, String fullName, String email, Collection<Long> cids);

    List<Candidate> findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingOrderByModificationDateDesc(
	    String fullName, String email);

    List<Candidate> findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndOwnerOrderByModificationDateDesc(
	    String fullName, String email, String ownerEmail);

    List<Candidate> findBySourceEmail(String email);

    Page<Candidate> findByCidIn(Collection<Long> cids, Pageable pageable);

    List<Candidate> findByCidIn(Collection<Long> cids, Sort sort);

    List<Candidate> findByOwnerAndCidIn(String ownerEmail, Collection<Long> cids, Sort sort);

    List<Candidate> findByCidIn(Collection<Long> cids);

    List<Candidate> findByOwnerAndCidIn(String ownerEmail, Collection<Long> cids);

    Page<Candidate> findByOwnerAndCidIn(String ownerEmail, Collection<Long> cids, Pageable pageable);

    List<Candidate> findByExternalAppCandidateIdIn(Collection<String> externalIds);

    List<Candidate> findByCandidateSha1HashIn(Collection<String> candidateSha1HashList);

    Candidate findByCandidateSha1HashOrExternalAppCandidateId(String candidateHash, String externalId);

    List<Candidate> findDistinctByEmailOrAlternateEmail(String email, String alternateEmail);

    Candidate findByEmail(String email);

    List<Candidate> findBySourceEmailAndCidNotIn(String email, Collection<Long> cids);

    @Query("select c.cid from candidate c")
    List<Long> getCandidateIds();

    @Query(value = "select cid from candidate where owner = ?1 and creation_date between ?2 and ?3", nativeQuery = true)
    List<Long> findCandidateIdsBySourcebyBetweenDate(String owner, Date startDate, Date endDate);

    @Query(value = "select c.cid from candidate c where s3Enabled = '0' LIMIT 1000", nativeQuery = true)
    List<BigInteger> getCandidateIdsForlocalFiles();

    List<Candidate> findByEmailIn(List<String> emails);

    List<Candidate> findTop1000ByS3EnabledIsTrue();

    Page<Candidate> findByS3EnabledIsTrue(Pageable pageable);

    Long countByS3EnabledIsTrue();

    List<Candidate> findByDummy(boolean dummyState);

    Page<Candidate> findByCandidateRandomId(Pageable pageable, String randomId);

    long countByCandidateRandomId(String randomId);

    long countByStatusAndOwner(String status, String ownerEmail);

    Page<Candidate> findAll(Specification<Candidate> spec, Pageable pageable);

    Page<Candidate> findByCidNotIn(Collection<Long> cids, Specification<Candidate> spec, Pageable pageable);

    Page<Candidate> findByOwner(String ownerEmail, Specification<Candidate> spec, Pageable pageable);

    Page<Candidate> findByOwnerAndCidNotIn(String ownerEmail, Collection<Long> cids, Specification<Candidate> spec,
	    Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete from custom_field_candidate where name = :name", nativeQuery = true)
    void deleteCustomFieldWithName(@Param("name") String name);

    @Query(value = "select count(cid) from candidate where ?1 LIKE CONCAT('%',owner,'%')  and creation_date between ?2 and ?3", nativeQuery = true)
    Long getCountByOwnerLikeAndDateBetween(String owner, Date startDate, Date endDate);
    
    @Query(value = "select count(cid) from candidate where owner = ?1 and creation_date between ?2 and ?3", nativeQuery = true)
    Long getCountByOwnerAndDateBetween(String owner, Date startDate, Date endDate);

    @Query(value = "select c.cid,c.fullName,c.mobile, c.email,c.currentCompany,c.noticePeriod,c.currentLocation,c.currentCtc,c.expectedCtc,c.totalExp,c.preferredLocation from candidate c where c.cid = (select candidate_cid from round_candidate where id = ?1)", nativeQuery = true)
    Object getCandidateDetailsFromRoundCandidateID(Long roundCandidateId);

    @Query(value = "SELECT IFNULL( (select notes from candidate_notes where candidateId_cid = ?1 order by creation_date desc limit 1) ,'NA') notes", nativeQuery = true)
    String getLatestNoteForCandidate(String candidateID);

    @Modifying
    @Transactional
    @Query(value = "select cid from custom_field_candidate where name = :fieldName and value = :fieldvalue", nativeQuery = true)
	List<BigInteger> findCandidateByCustomFields(@Param("fieldName") String fieldName, @Param("fieldvalue") String fieldvalue);

    @Query(value = "select * from candidate c where c.mobile = ?1", nativeQuery = true)
	List<Candidate> findByMobileNo(String text);

    @Query(value = "SELECT CONCAT(c.source) AS 'Source Channel', COUNT(c.cid) AS 'Total Number' FROM candidate c INNER JOIN round_candidate rc ON rc.candidate_cid = c.cid INNER JOIN POSITION p ON p.positionCode = rc.positionCode GROUP BY c.source", nativeQuery = true)
    Object[] getAllPositionSoucingChannelMix();

    /*@Query(value = "select c from candidate c where c.modificationDate between :startDate and :endDate")*/
	Page<Candidate> findAllByModificationDateBetween(Date startDate, Date endDate,Pageable pageable);

    /*@Query(value = "select * from candidate where owner = owner and modificationDate between startDate and endDate")*/
	Page<Candidate> findAllByOwnerAndModificationDateBetween(String owner, Date startDate, Date endDate, Pageable pageable);

 
}
