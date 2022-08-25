package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;
import com.bbytes.recruiz.search.repository.PositionRequestSearchRepo;
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
public class PositionRequestDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private PositionRequestSearchRepo positionSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(PositionRequest positionSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.positionSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		positionSearchRepo.save(domainToSearchDomainConversionService.convertPositionRequest(positionSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertPositionRequestForSuggest(positionSaved, tenantName));
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(PositionRequest positionRequestDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.positionSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		PositionRequestSearch positionRequestSearch = positionSearchRepo
				.findOne(AbstractSearchEntity.getId(positionRequestDeleted.getId(), tenantName));
		if (positionRequestSearch != null) {
			positionSearchRepo.delete(positionRequestSearch);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(PositionRequestSearch.INDEX_NAME, positionRequestDeleted.getId(), tenantName));
		}
	}

}