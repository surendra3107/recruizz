package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ImportJobBatch;

public interface ImportJobBatchRepository extends JpaRepository<ImportJobBatch, Long> {

	public ImportJobBatch findByBatchId(String batchId);

	public List<ImportJobBatch> findByStatus(String status);

	public ImportJobBatch findTop1ByStatus(String status);

	public List<ImportJobBatch> findTop10ByStatusIn(List<String> statuses);

	public List<ImportJobBatch> findTop100ByOwnerOrderByIdDesc(String owner);

	public List<ImportJobBatch> findByOwnerAndStatus(String owner, String status);

	public Page<ImportJobBatch> findByStatus(String status, Pageable pageRequest);

	public Page<ImportJobBatch> findTop1000ByOwner(String owner, Pageable pageRequest);

	public Page<ImportJobBatch> findByOwnerAndStatus(String owner, String status, Pageable pageRequest);

}
