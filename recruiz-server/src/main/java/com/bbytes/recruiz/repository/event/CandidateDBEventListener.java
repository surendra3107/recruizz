package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.people.service.PeopleSocialProfileService;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.repository.CandidateSearchRepo;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.service.DomainToSearchDomainService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;
import com.bbytes.recruiz.utils.MultiTenantUtils;

public class CandidateDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private CandidateSearchRepo candidateSearchRepo;
	
	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@Autowired
	private PeopleSocialProfileService peopleSocialProfileService;
	
	@PrePersist
	public void preSave(Candidate candidateSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.peopleSocialProfileService);
		
		if (candidateSaved.getCid() == 0)
			peopleSocialProfileService.updateSocialProfile(candidateSaved);
	}

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(Candidate candidateSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		// if (!springProfileService.isSaasMode())
		// return;

		AutowireHelper.autowire(this, this.candidateSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		candidateSearchRepo.save(domainToSearchDomainConversionService.convertCandidate(candidateSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertCandidateForSuggest(candidateSaved, tenantName));
		
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(Candidate candidateDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		// if (!springProfileService.isSaasMode())
		// return;

		AutowireHelper.autowire(this, this.candidateSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		CandidateSearch candidate = candidateSearchRepo.findOne(AbstractSearchEntity.getId(candidateDeleted.getCid(), tenantName));
		if (candidate != null){
			candidateSearchRepo.delete(candidate);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(CandidateSearch.INDEX_NAME, candidateDeleted.getCid(), tenantName));
		}

	}

}