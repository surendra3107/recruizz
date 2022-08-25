package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.repository.CandidateResumeBulkUploadRepository;
import com.bbytes.recruiz.repository.CandidateResumeUploadItemRepository;

@Service
public class CandidateResumeBulkUploadBatchService extends AbstractService<CandidateResumeBulkUploadBatch, Long> {

	private CandidateResumeBulkUploadRepository candidateResumeBulkUploadRepository;

	@Autowired
	private CandidateResumeUploadItemRepository candidateResumeUploadItemRepository;

	@Autowired
	public CandidateResumeBulkUploadBatchService(CandidateResumeBulkUploadRepository candidateResumeBulkUploadRepository) {
		super(candidateResumeBulkUploadRepository);
		this.candidateResumeBulkUploadRepository = candidateResumeBulkUploadRepository;
	}

	@Transactional(readOnly = true)
	public CandidateResumeBulkUploadBatch findByBatchId(String batchId) {
		CandidateResumeBulkUploadBatch candidateResumeBulkUpload = candidateResumeBulkUploadRepository.findByBatchId(batchId);
		if (candidateResumeBulkUpload != null && candidateResumeBulkUpload.getCandidateResumeUploadItems() != null) {
			candidateResumeBulkUpload.getCandidateResumeUploadItems().size();
		}
		return candidateResumeBulkUpload;
	}

	@Transactional
	public void updateCandidateResumeBulkUploadBatchStatus(String batchId) {
		CandidateResumeBulkUploadBatch candidateResumeBulkUpload = candidateResumeBulkUploadRepository.findByBatchId(batchId);

		List<String> itemStatusList = new ArrayList<String>();
		itemStatusList.add(ResumeUploadFileStatus.PENDING.toString());
		itemStatusList.add(ResumeUploadFileStatus.PROCESSING.toString());

		Long count = candidateResumeUploadItemRepository.countByBatchIdAndStatusIn(batchId, itemStatusList);

		if (count == null || count == 0) {
			candidateResumeBulkUpload.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.toString());
			save(candidateResumeBulkUpload);
		}

	}

	public List<CandidateResumeBulkUploadBatch> findByStatus(String status) {
		return candidateResumeBulkUploadRepository.findByStatus(status);
	}

	public CandidateResumeBulkUploadBatch findTop1ByStatus(String status) {
		return candidateResumeBulkUploadRepository.findTop1ByStatus(status);
	}

	public List<CandidateResumeBulkUploadBatch> findTop10ByStatusIn(List<String> statuses) {
		return candidateResumeBulkUploadRepository.findTop10ByStatusIn(statuses);
	}

	public List<CandidateResumeBulkUploadBatch> findTop100ByOwner(String owner) {
		List<CandidateResumeBulkUploadBatch> candidateResumeBulkUploadBatches = candidateResumeBulkUploadRepository
				.findTop100ByOwnerOrderByIdDesc(owner);

		for (CandidateResumeBulkUploadBatch candidateResumeBulkUploadBatch : candidateResumeBulkUploadBatches) {
			if (candidateResumeBulkUploadBatch.getCandidateResumeUploadItems() != null
					&& candidateResumeBulkUploadBatch.getCandidateResumeUploadItems().size() > 0)
				updateCandidateResumeBulkUploadBatchStatus(candidateResumeBulkUploadBatch.getBatchId());
		}

		candidateResumeBulkUploadBatches = candidateResumeBulkUploadRepository.findTop100ByOwnerOrderByIdDesc(owner);

		return candidateResumeBulkUploadBatches;
	}

	public List<CandidateResumeBulkUploadBatch> findByOwnerAndStatus(String owner, String status) {
		return candidateResumeBulkUploadRepository.findByOwnerAndStatus(owner, status);
	}

	public Page<CandidateResumeBulkUploadBatch> findByStatus(String status, Pageable pageRequest) {
		return candidateResumeBulkUploadRepository.findByStatus(status, pageRequest);
	}

	public Page<CandidateResumeBulkUploadBatch> findByOwner(String owner, Pageable pageRequest) {
		return candidateResumeBulkUploadRepository.findByOwner(owner, pageRequest);
	}

	public Page<CandidateResumeBulkUploadBatch> findByOwnerAndStatus(String owner, String status, Pageable pageRequest) {
		return candidateResumeBulkUploadRepository.findByOwnerAndStatus(owner, status, pageRequest);
	}

	public Long deleteBulkItemByBatchandStatus(String batchId,String status) {
	    return candidateResumeBulkUploadRepository.deleteByBatchIdAndStatus(batchId, status);
	}
}
