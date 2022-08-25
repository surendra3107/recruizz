package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.ProspectSearch;
import com.bbytes.recruiz.search.repository.ProspectSearchRepo;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.service.DomainToSearchDomainService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;
import com.bbytes.recruiz.utils.MultiTenantUtils;

/**
 * Equivalent of a domain method annotated by <code>PrePersist</code>.
 * <p/>
 * This handler shows how to implement your custom UUID generation.
 * 
 */
public class ProspectDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private ProspectSearchRepo prospectSearchRepo;
	
	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(Prospect prospectSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.prospectSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		prospectSearchRepo.save(domainToSearchDomainConversionService.convertProspect(prospectSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertProspectForSuggest(prospectSaved, tenantName));
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(Prospect prospectDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.prospectSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		ProspectSearch prospectSearch = prospectSearchRepo.findOne(AbstractSearchEntity.getId(prospectDeleted.getProspectId(), tenantName));
		if (prospectSearch != null){
			prospectSearchRepo.delete(prospectSearch);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(ProspectSearch.INDEX_NAME, prospectDeleted.getProspectId(), tenantName));	
		}
	}

}