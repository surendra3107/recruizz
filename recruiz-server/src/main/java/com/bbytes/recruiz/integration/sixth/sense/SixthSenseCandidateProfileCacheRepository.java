package com.bbytes.recruiz.integration.sixth.sense;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bbytes.recruiz.domain.integration.SixthSenseCandidateProfileCache;

public interface SixthSenseCandidateProfileCacheRepository
		extends MongoRepository<SixthSenseCandidateProfileCache, String> {

	SixthSenseCandidateProfileCache findByResumeIdAndTenantId(String resumeId, String tenantId);
}
