package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.search.builder.ClientSearchBuilder;
import com.bbytes.recruiz.search.domain.ClientSearch;

public class ClientSearchRepoImpl extends AbstractSearchRepoImpl implements ClientSearchRepoCustom {

	@Autowired
	protected ClientRepository clientRepository;

	@Override
	public Class<?> getSearchClass() {
		return ClientSearch.class;
	}
	
	@Override
	public List<ClientSearch> getResult(ClientSearchBuilder clientSearchBuilder) throws RecruizException {
		return clientSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<Long> getResultAsIds(ClientSearchBuilder clientSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return clientSearchBuilder.executeQueryForIds(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<ClientOpeningCountDTO> getResultAsNativeModel(ClientSearchBuilder clientSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return clientRepository.clientListWithTotalOpening((getResultAsIds(clientSearchBuilder)));
	}

	/**
	 * Return the list of all clientIds using sort field and sort order
	 */
	@Override
	public List<Long> getResultAsIds(ClientSearchBuilder clientSearchBuilder, Pageable pageable, String sortField,
			SortOrder sortOrder) throws RecruizException, RecruizEmptySearchCriteriaException {

		Page<Long> clientIds = clientSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, sortField,
				sortOrder, elasticsearchTemplate);
		return clientIds.getContent();
	}

}
