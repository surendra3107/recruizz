package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.AdvancedSearchRepository;

@Service
public class AdvancedSearchService extends AbstractService<AdvancedSearchQueryEntity, Long> {

	private AdvancedSearchRepository advancedSearchRepository;
	
	@Autowired
	private UserService userService;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainService;

	@Autowired
	public AdvancedSearchService(AdvancedSearchRepository advancedSearchRepository) {
		super(advancedSearchRepository);
		this.advancedSearchRepository = advancedSearchRepository;
	}

	@Transactional(readOnly = true)
	public boolean isSearchQueryExists(String queryName) {
		User loggedInUser = userService.getLoggedInUserObject();
		boolean queryExists = advancedSearchRepository.findDistinctByQueryNameAndOwner(queryName, loggedInUser) == null ? false : true;
		return queryExists;
	}
	
	@Transactional(readOnly = true)
	public List<AdvancedSearchQueryEntity> getAllQueries() throws RecruizException {
		User loggedInUser = userService.getLoggedInUserObject();
		return advancedSearchRepository.findByOwner(loggedInUser);
	}
	
	@Transactional(readOnly = true)
	public List<AdvancedSearchQueryEntity> getByOwner(User owner) throws RecruizException {
		return advancedSearchRepository.findByOwner(owner);
	}

	@Transactional
	public AdvancedSearchQueryEntity saveSearchQuery(AdvancedSearchQueryEntity advancedSearchQueryEntity) throws RecruizException {
		User loggedInUser = userService.getLoggedInUserObject();
		advancedSearchQueryEntity.setOwner(loggedInUser);
		domainToSearchDomainService.convertToPersistEntity(advancedSearchQueryEntity);
		return save(advancedSearchQueryEntity);
	}
	
	@Transactional
	public AdvancedSearchQueryEntity updateSavedQuery(AdvancedSearchQueryEntity advancedSearchQueryEntity,long Id) throws RecruizException {
	    	User loggedInUser = userService.getLoggedInUserObject();
		advancedSearchQueryEntity.setOwner(loggedInUser);
		domainToSearchDomainService.convertToPersistEntity(advancedSearchQueryEntity);
		advancedSearchQueryEntity.setId(Id);
		return save(advancedSearchQueryEntity);
	}
	
	@Transactional
	public void deleteSearchQueryById(Long id) throws RecruizException {
		delete(id);
	}

}
