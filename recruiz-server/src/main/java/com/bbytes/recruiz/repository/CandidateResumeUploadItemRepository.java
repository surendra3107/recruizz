package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.CandidateResumeUploadItem;

public interface CandidateResumeUploadItemRepository extends JpaRepository<CandidateResumeUploadItem, Long> {

	public List<CandidateResumeUploadItem> findByBatchId(String batchId);

	public List<CandidateResumeUploadItem> findByStatus(String status);

	public List<CandidateResumeUploadItem> findTop1000ByStatus(String status);

	public List<CandidateResumeUploadItem> findTop1000ByBatchIdAndStatus(String batchId, String status);

	public Page<CandidateResumeUploadItem> findByBatchId(String batchId, Pageable pageRequest);

	public Page<CandidateResumeUploadItem> findByBatchIdAndStatus(String batchId, String status, Pageable pageRequest);

	public Long countByBatchIdAndStatus(String batchId, String status);
	
	public List<CandidateResumeUploadItem> findByBatchIdAndStatus(String batchId, String status);

	public Long countByBatchIdAndStatusIn(String batchId, List<String> statusList);

	public Long countByStatus(String status);
	
	public Long countByStatusIn(List<String> statuses) ;

	@Query("SELECT DISTINCT c.batchId FROM candidate_resume_upload_item c WHERE c.status = ?1")
	public List<String> findDistinctBatchIdsByStatus(String status);

	public Page<CandidateResumeUploadItem> findByStatus(String status, Pageable pageRequest);

	public List<CandidateResumeUploadItem> findTop1000ByBatchIdAndStatusInOrderByStatusDesc(String batchId, List<String> statusList);

	public Long deleteByBatchIdAndStatus(String batchId, String status);
	
}
