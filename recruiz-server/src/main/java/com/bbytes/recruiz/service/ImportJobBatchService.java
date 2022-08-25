package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.ImportJobBatch;
import com.bbytes.recruiz.repository.ImportJobBatchRepository;

@Service
public class ImportJobBatchService extends AbstractService<ImportJobBatch, Long> {

	private ImportJobBatchRepository importJobBatchRepository;

	@Autowired
	public ImportJobBatchService(ImportJobBatchRepository importJobBatchRepository) {
		super(importJobBatchRepository);
		this.importJobBatchRepository = importJobBatchRepository;
	}

	@Transactional(readOnly = true)
	public ImportJobBatch findByBatchId(String batchId) {
		ImportJobBatch importJobBatch = importJobBatchRepository.findByBatchId(batchId);
		if (importJobBatch != null && importJobBatch.getImportJobUploadItems() != null) {
			importJobBatch.getImportJobUploadItems().size();
		}
		return importJobBatch;
	}

	public List<ImportJobBatch> findByStatus(String status) {
		return importJobBatchRepository.findByStatus(status);
	}

	public ImportJobBatch findTop1ByStatus(String status) {
		return importJobBatchRepository.findTop1ByStatus(status);
	}

	public List<ImportJobBatch> findTop10ByStatusIn(List<String> statuses) {
		return importJobBatchRepository.findTop10ByStatusIn(statuses);
	}

	public List<ImportJobBatch> findTop100ByOwner(String owner) {
		List<ImportJobBatch> importJobBatches = importJobBatchRepository.findTop100ByOwnerOrderByIdDesc(owner);
		return importJobBatches;
	}

	public List<ImportJobBatch> findByOwnerAndStatus(String owner, String status) {
		return importJobBatchRepository.findByOwnerAndStatus(owner, status);
	}

	public Page<ImportJobBatch> findByStatus(String status, Pageable pageRequest) {
		return importJobBatchRepository.findByStatus(status, pageRequest);
	}

	public Page<ImportJobBatch> findByOwner(String owner, Pageable pageRequest) {
		return importJobBatchRepository.findTop1000ByOwner(owner, pageRequest);
	}

	public Page<ImportJobBatch> findByOwnerAndStatus(String owner, String status, Pageable pageRequest) {
		return importJobBatchRepository.findByOwnerAndStatus(owner, status, pageRequest);
	}

}
