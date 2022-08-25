package com.bbytes.recruiz.search.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.bbytes.recruiz.enums.BooleanSearchType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.builder.CandidateSearchBuilder;
import com.bbytes.recruiz.search.builder.PositionSearchBuilder;
import com.bbytes.recruiz.search.builder.ProspectSearchBuilder;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.MultiTenantUtils;

public abstract class AbstractSearchRepoImpl {

	@Autowired
	protected ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	protected SpringProfileService springProfileService;

	@Autowired
	protected PageableService pageableService;

	@Autowired
	protected UserService userService;

	protected BooleanSearchType booleanSearchType = BooleanSearchType.AND;

	public abstract Class<?> getSearchClass();

	public String getCurrentTenant() throws RecruizException {
		String tenantId = MultiTenantUtils.getTenant();
		if (springProfileService.isSaasMode() && (tenantId == null || tenantId.isEmpty()))
			throw new RecruizException("Tenant id cannot be missing for elastic search query", ErrorHandler.ELASTICSEARCH_ERROR);

		return tenantId;
	}

	/**
	 * Apply additional filter before search
	 * 
	 * @param CandidateSearchBuilder
	 * @throws RecruizException
	 */
	public void applyAdditionalFilter(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {
		applySourceEmailFilter(candidateSearchBuilder);
	}

	/**
	 * This check is applied for vendor login to show only candidate added by
	 * vendor user and vendor team . Method withSourceEmails() does that filter
	 * . We add the vendor main email id to SourceEmails list to apply the
	 * filter
	 * 
	 * @param CandidateSearchBuilder
	 * @throws RecruizException
	 */
	protected void applySourceEmailFilter(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {
		if (userService.isLoggedInUserVendor()) {
			candidateSearchBuilder.withSourceEmails(userService.getVendorEmail());
		}
	}

	/**
	 * Apply additional filter before search
	 * 
	 * @param positionSearchBuilder
	 * @throws RecruizException
	 */
	public void applyAdditionalFilter(PositionSearchBuilder positionSearchBuilder) throws RecruizException {
		applyVendorEmailFilter(positionSearchBuilder);
	}

	/**
	 * This check is applied for vendor login to show only positions shared with
	 * vendor user and vendor team . Method withVendorEmails() does that filter
	 * . We add the vendor main email id to VendorEmails list to apply the
	 * filter
	 * 
	 * @param positionSearchBuilder
	 * @throws RecruizException
	 */
	protected void applyVendorEmailFilter(PositionSearchBuilder positionSearchBuilder) throws RecruizException {
		if (userService.isLoggedInUserVendor()) {
			positionSearchBuilder.withVendorEmails(userService.getVendorEmail());
		}
	}
	
	
	protected void applyAdditionalFilter(ProspectSearchBuilder positionSearchBuilder) {
//		if (userService.isLoggedInUserVendor()) {
//			positionSearchBuilder.withSourceEmails(userService.getVendorEmail());
//		}
		
	}

}