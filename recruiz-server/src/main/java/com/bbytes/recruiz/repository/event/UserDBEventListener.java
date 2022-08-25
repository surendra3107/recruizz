package com.bbytes.recruiz.repository.event;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.AbstractSearchEntity;
import com.bbytes.recruiz.search.domain.UserSearch;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.search.repository.UserSearchRepo;
import com.bbytes.recruiz.service.DomainToSearchDomainService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;
import com.bbytes.recruiz.utils.MultiTenantUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Equivalent of a domain method annotated by <code>PrePersist</code>.
 * <p/>
 * This handler shows how to implement your custom UUID generation.
 * 
 */

public class UserDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private UserSearchRepo userSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainConversionService;

	@PostPersist
	@PostUpdate
	public void afterSaveOrUpdate(User userSaved) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.userSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = TenantContextHolder.getTenant();
		if (tenantName == null)
			return;

		userSearchRepo.save(domainToSearchDomainConversionService.convertUser(userSaved, tenantName));
		suggestSearchRepo.save(domainToSearchDomainConversionService.convertUserForSuggest(userSaved, tenantName));

		// tenantResolverService.saveTenantResolverForUser(userSaved);
	}

	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(User userDeleted) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		if (!springProfileService.isSaasMode())
			return;

		AutowireHelper.autowire(this, this.userSearchRepo);
		AutowireHelper.autowire(this, this.domainToSearchDomainConversionService);

		String tenantName = MultiTenantUtils.getTenant();
		if (tenantName == null)
			return;

		UserSearch user = userSearchRepo.findOne(AbstractSearchEntity.getId(userDeleted.getUserId(), tenantName));
		if (user != null){
			userSearchRepo.delete(user);
			suggestSearchRepo.delete(AbstractSearchEntity.getId(UserSearch.INDEX_NAME, userDeleted.getUserId(), tenantName));
		}
		// tenantResolverService.deleteTenantResolverForUserId(userDeleted.getUserId());
	}

}