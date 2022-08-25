package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.PositionSearch;
import com.bbytes.recruiz.search.repository.PositionSearchRepo;
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
public class PositionDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private PositionSearchRepo positionSearchRepo;
	
	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(Position positionSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.positionSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		positionSearchRepo.save(domainToSearchDomainConversionService.convertPosition(positionSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertPositionForSuggest(positionSaved, tenantName));
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(Position positionDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.positionSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		PositionSearch positionSearch = positionSearchRepo.findOne(AbstractSearchEntity.getId(positionDeleted.getId(), tenantName));
		if (positionSearch != null){
			positionSearchRepo.delete(positionSearch);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(PositionSearch.INDEX_NAME, positionDeleted.getId(), tenantName));	
		}
	}

}