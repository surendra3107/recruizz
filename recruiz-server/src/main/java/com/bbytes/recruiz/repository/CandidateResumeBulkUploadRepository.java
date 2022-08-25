package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;

public interface CandidateResumeBulkUploadRepository extends JpaRepository<CandidateResumeBulkUploadBatch, Long> {

	public CandidateResumeBulkUploadBatch findByBatchId(String batchId);

	public List<CandidateResumeBulkUploadBatch> findByStatus(String status);

	public CandidateResumeBulkUploadBatch findTop1ByStatus(String status);
	
	public List<CandidateResumeBulkUploadBatch> findTop10ByStatusIn(List<String> statuses);

	public List<CandidateResumeBulkUploadBatch> findTop100ByOwnerOrderByIdDesc(String owner);

	public List<CandidateResumeBulkUploadBatch> findByOwnerAndStatus(String owner, String status);

	public Page<CandidateResumeBulkUploadBatch> findByStatus(String status, Pageable pageRequest);

	public Page<CandidateResumeBulkUploadBatch> findByOwner(String owner, Pageable pageRequest);

	public Page<CandidateResumeBulkUploadBatch> findByOwnerAndStatus(String owner, String status, Pageable pageRequest);
	
	public Long deleteByBatchIdAndStatus(String batchId,String status);
	
}
