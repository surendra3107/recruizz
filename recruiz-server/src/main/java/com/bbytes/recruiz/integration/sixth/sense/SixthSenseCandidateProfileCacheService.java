package com.bbytes.recruiz.integration.sixth.sense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.auth.storage.emails.MongoAbstractService;
import com.bbytes.recruiz.domain.integration.SixthSenseCandidateProfileCache;

@Service
public class SixthSenseCandidateProfileCacheService
		extends MongoAbstractService<SixthSenseCandidateProfileCache, String> {

	private SixthSenseCandidateProfileCacheRepository candidateProfileCacheRepository;

	@Autowired
	public SixthSenseCandidateProfileCacheService(
			SixthSenseCandidateProfileCacheRepository candidateProfileCacheRepository) {
		super(candidateProfileCacheRepository);
		this.candidateProfileCacheRepository = candidateProfileCacheRepository;
	}

	public SixthSenseCandidateProfileCache findByResumeId(String resumeId, String tenantId) {
		return candidateProfileCacheRepository.findByResumeIdAndTenantId(resumeId, tenantId);
	}

}
