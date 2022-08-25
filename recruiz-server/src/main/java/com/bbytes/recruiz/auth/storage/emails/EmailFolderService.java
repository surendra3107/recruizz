package com.bbytes.recruiz.auth.storage.emails;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EmailFolderService extends MongoAbstractService<EmailFolder, String> {

	private EmailFolderListRepository folderListRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	public EmailFolderService(EmailFolderListRepository folderListRepository) {
		super(folderListRepository);
		this.folderListRepository = folderListRepository;
	}

	public long getMaxUidForEmail(String email) {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "uid"));
		query.addCriteria(Criteria.where("recieverEmail").is(email));
		query.limit(1);

		EmailData maxObject = mongoTemplate.findOne(query, EmailData.class);
		if (null == maxObject)
			return 0;

		return maxObject.getUid();
	}

	/**
	 * To get distinct folder name
	 * 
	 * @return
	 */
	public List<String> getFolderNames(String emailAccount) {

		Query query = new Query();
		query.addCriteria(Criteria.where("accountEmail").is(emailAccount));

		@SuppressWarnings("unchecked")
		List<String> distinctFolderRecord = mongoTemplate.getCollection("FolderList").distinct("folderName",
				query.getQueryObject());
		return distinctFolderRecord;
	}

	public List<EmailFolder> getAllFolderByAccount(String accountEmailId) {
		return folderListRepository.findByAccountEmail(accountEmailId);
	}

	public int getCountByFolderAndAccount(String folder, String emailId) {
		EmailFolder fldr = folderListRepository.findByFolderNameAndAccountEmail(folder, emailId);
		if (null != fldr) {
			return fldr.getNoOfEmails();
		}

		return 0;
	}

	public EmailFolder getFolderByKey(String folderKey) {
		EmailFolder folder = folderListRepository.findByUniqKey(folderKey);
		if (folder == null) {
			folder = folderListRepository.findOne(folderKey);
		}
		return folder;
	}

	public EmailFolder getFolderAccountEmailAndName(String email, String folderName) {
		EmailFolder folder = folderListRepository.findByFolderNameAndAccountEmail(folderName, email);
		return folder;
	}

}
