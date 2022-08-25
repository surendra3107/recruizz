package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ImportJobUploadItem;

public interface ImportJobUploadItemRepository extends JpaRepository<ImportJobUploadItem, Long> {

	public List<ImportJobUploadItem> findByBatchId(String batchId);

	public List<ImportJobUploadItem> findByBatchIdAndStatus(String batchId, String status);

	public List<ImportJobUploadItem> findByStatus(String status);

	public List<ImportJobUploadItem> findTop1000ByStatus(String status);

	public List<ImportJobUploadItem> findTop1000ByBatchIdAndStatus(String batchId, String status);

	public Page<ImportJobUploadItem> findByBatchId(String batchId, Pageable pageRequest);

	public Page<ImportJobUploadItem> findByBatchIdAndStatus(String batchId, String status, Pageable pageRequest);

	public Long countByBatchIdAndStatus(String batchId, String status);

	public Long countByStatus(String status);

	public Page<ImportJobUploadItem> findByStatus(String status, Pageable pageRequest);

}
