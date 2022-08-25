package com.bbytes.recruiz.search.repository;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.search.domain.UserSearch;

public class UserSearchRepoImpl extends AbstractSearchRepoImpl implements UserSearchRepoCustom {

	@Autowired
	protected UserRepository userRepository;

	@Override
	public Class<?> getSearchClass() {
		return UserSearch.class;
	}
	
	
}
