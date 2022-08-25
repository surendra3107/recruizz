package com.bbytes.recruiz.auth.storage.emails;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEmailStoreRepositoryCustom extends PagingAndSortingRepository<EmailData, Long> {

	List<EmailData> findByUid(Long uid);

	Page<EmailData> findByRecieverEmailOrderByUidDesc(String userEmail, Pageable pageable);

	List<EmailData> findByRecieverEmail(String userEmail);
	
	List<EmailData> findTop10ByRecieverEmailOrderByUid(String userEmail);

	
	
}
