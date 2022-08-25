package com.bbytes.recruiz.auth.storage.emails;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailDataRepository extends MongoRepository<EmailData, Long> 
{
	Page<EmailData> findByRecieverEmailAndFolderName(String userEmail,String folderName,Pageable pageable);

	List<EmailData> findByRecieverEmail(String userEmail);
	
	EmailData findByUidAndRecieverEmail(Long uid,String accountEmail);
	
	EmailData findByUidAndRecieverEmailAndFolderName(Long uid,String accountEmail,String folderName);
	
	List<EmailData> findByRecieverEmailAndFolderNameAndUidIn(String email,String folderName,List<Long> uids);
	
	Long countByRecieverEmailAndFolderName(String email,String folderName);
	
	EmailData findById(String id);
	
	Page<EmailData> findByFolderNameAndRecieverStringContaining(String folderName,String accountEmail,Pageable pageable);
}
