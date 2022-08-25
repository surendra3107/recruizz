package com.bbytes.recruiz.auth.storage.emails;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailFolderListRepository extends MongoRepository<EmailFolder, String> 
{
	List<EmailFolder> findByAccountEmail(String accountEmail);
	
	EmailFolder findByFolderNameAndAccountEmail(String folderName,String email);
	
	EmailFolder findByUniqKey(String uniqueKey);
}
