package com.bbytes.recruiz.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OfferLetterApprovals;

public interface OfferLetterApprovalsRepository extends JpaRepository<OfferLetterApprovals, Long>  {

	@Query(value ="select * from offerletter_approvals p where p.position_id=?1 and p.candidate_id=?2", nativeQuery = true)
	List<OfferLetterApprovals> getApprovalDetailsByPositionIdAndCandidateId(long positionId, long candidateId);

	@Query(value ="select * from offerletter_approvals where request_send_from_user=?1 and creation_date between ?2 AND ?3", nativeQuery = true)
	List<OfferLetterApprovals> getApprovalListByRequestSender(Long userId, Date startReportDate, Date endReportDate);
	
	@Query(value ="select * from offerletter_approvals where approval_status=?4 and request_send_from_user=?1 and creation_date between ?2 AND ?3", nativeQuery = true)
	List<OfferLetterApprovals> getApprovalListByRequestSenderAndStatus(Long userId, Date startReportDate, Date endReportDate, String status);
	
	@Query(value ="select * from offerletter_approvals where creation_date between ?1 AND ?2 ", nativeQuery = true)
	List<OfferLetterApprovals> findAllByDate(Date startReportDate, Date endReportDate);
	
	@Query(value ="select * from offerletter_approvals where approval_status=?3 and creation_date between ?1 AND ?2 ", nativeQuery = true)
	List<OfferLetterApprovals> findAllByDateAndStatus(Date startReportDate, Date endReportDate, String status);

	//@Query(value ="select * from offerletter_approvals p where p.position_code=?1", nativeQuery = true)
	@Query(value ="select * from offerletter_approvals where request_send_from_user=?1 and position_code=?4 and creation_date between ?2 AND ?3", nativeQuery = true)
	List<OfferLetterApprovals> findAllByPositionCode(Long userId, Date startReportDate, Date endReportDate, String positionCode);

	@Query(value ="select * from offerletter_approvals where creation_date between ?1 AND ?2 and position_code=?3", nativeQuery = true)
	List<OfferLetterApprovals> findAllByPositionCode(Date startReportDate, Date endReportDate, String positionCode);

}
