package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.ClientSearch;
import com.bbytes.recruiz.search.repository.ClientSearchRepo;
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
public class ClientDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private ClientSearchRepo clientSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(Client clientSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.clientSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		clientSearchRepo.save(domainToSearchDomainConversionService.convertClient(clientSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertClientForSuggest(clientSaved, tenantName));
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(Client clientDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.clientSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		ClientSearch clientSearch = clientSearchRepo.findOne(AbstractSearchEntity.getId(clientDeleted.getId(), tenantName));
		if (clientSearch != null){
			clientSearchRepo.delete(clientSearch);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(ClientSearch.INDEX_NAME, clientDeleted.getId(), tenantName));
		}
	}

}