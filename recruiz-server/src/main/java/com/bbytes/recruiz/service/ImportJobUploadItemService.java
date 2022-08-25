package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.ImportJobUploadItem;
import com.bbytes.recruiz.repository.ImportJobUploadItemRepository;

@Service
public class ImportJobUploadItemService extends AbstractService<ImportJobUploadItem, Long> {

	private ImportJobUploadItemRepository importJobUploadItemRepository;

	@Autowired
	public ImportJobUploadItemService(ImportJobUploadItemRepository importJobUploadItemRepository) {
		super(importJobUploadItemRepository);
		this.importJobUploadItemRepository = importJobUploadItemRepository;
	}

	public List<ImportJobUploadItem> findByBatchId(String batchId) {
		return importJobUploadItemRepository.findByBatchId(batchId);
	}

	public List<ImportJobUploadItem> findByBatchIdAndStatus(String batchId, String status) {
		return importJobUploadItemRepository.findByBatchIdAndStatus(batchId, status);
	}

	public List<ImportJobUploadItem> findByStatus(String status) {
		return importJobUploadItemRepository.findByStatus(status);
	}

	public List<ImportJobUploadItem> findTop1000ByStatus(String status) {
		return importJobUploadItemRepository.findTop1000ByStatus(status);
	}

	public Page<ImportJobUploadItem> findByBatchId(String batchId, Pageable pageRequest) {
		return importJobUploadItemRepository.findByBatchId(batchId, pageRequest);
	}

	public Page<ImportJobUploadItem> findByBatchIdAndStatus(String batchId, String status, Pageable pageRequest) {
		return importJobUploadItemRepository.findByBatchIdAndStatus(batchId, status, pageRequest);
	}

	public List<ImportJobUploadItem> findTop1000ByBatchIdAndStatus(String batchId, String status) {
		return importJobUploadItemRepository.findTop1000ByBatchIdAndStatus(batchId, status);
	}

	public Long countByBatchIdAndStatus(String batchId, String status) {
		return importJobUploadItemRepository.countByBatchIdAndStatus(batchId, status);
	}

	public Long countByStatus(String status) {
		return importJobUploadItemRepository.countByStatus(status);
	}

	public Page<ImportJobUploadItem> findByStatus(String status, Pageable pageRequest) {
		return importJobUploadItemRepository.findByStatus(status, pageRequest);
	}

}
