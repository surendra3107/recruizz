package com.bbytes.recruiz.auth.storage.emails;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.EmailClientDetails;

@Service
public class EmailStoreService extends MongoAbstractService<EmailData, Long> {

	private EmailDataRepository emailRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private EmailFolderService emailFolderService;

	@Autowired
	public EmailStoreService(EmailDataRepository emailDataRepository) {
		super(emailDataRepository);
		this.emailRepository = emailDataRepository;
	}

	private Set<String> runningRefreshThread = new HashSet<>();

	public void addRunningThreadInSet(String emailAccountFolderKey) {
		this.runningRefreshThread.add(emailAccountFolderKey);
	}

	public boolean isRunningThreadExistsInSet(String emailAccountFolderKey) {
		return this.runningRefreshThread.contains(emailAccountFolderKey);
	}

	public void removeRunningThreadFromRefreshSet(String emailAccountFolderKey) {
		if (this.runningRefreshThread.contains(emailAccountFolderKey))
			this.runningRefreshThread.remove(emailAccountFolderKey);
	}

	public Page<EmailData> getUserAllEmailData(String userEmail, Pageable pageable, String folderName) {
		if(null == folderName || folderName.isEmpty()){
			return null;
		}
		if (folderName.toLowerCase().contains("inbox") || folderName.toLowerCase().contains("all mail")
				|| folderName.toLowerCase().contains("important")) {
			return emailRepository.findByFolderNameAndRecieverStringContaining(folderName, userEmail, pageable);
		} else {
			return emailRepository.findByRecieverEmailAndFolderName(userEmail, folderName, pageable);
		}
	}

	public List<EmailData> getUserAllEmailData(String userEmail) {
		return emailRepository.findByRecieverEmail(userEmail);
	}

	public void addEmailToMongo(EmailData email) {
		emailRepository.save(email);
	}

	public List<EmailData> getAll() {
		return emailRepository.findAll();
	}

	public void deleteAll() {
		emailRepository.deleteAll();
	}

	public long getMaxUidForEmail(String email, String folderName) {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "uid"));
		query.addCriteria(Criteria.where("recieverEmail").is(email));
		query.addCriteria(Criteria.where("folderName").is(folderName));
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
		List<String> distinctFolderRecord = mongoTemplate.getCollection("emailFolder").distinct("folderName",
				query.getQueryObject());
		return distinctFolderRecord;
	}

	public void saveFolder(EmailFolder emailFolder) {
		emailFolderService.save(emailFolder);
	}

	public void deleteByUid(List<Long> emailMessageIds, String accountEmail, String folderName) {
		for (Long uid : emailMessageIds) {
			EmailData email = emailRepository.findByUidAndRecieverEmailAndFolderName(uid, accountEmail, folderName);
			emailRepository.delete(email);
		}

	}

	public EmailData findByUidAndRecieverEmail(Long uid, String userEmail) {
		return emailRepository.findByUidAndRecieverEmail(uid, userEmail);
	}

	public EmailData findByUidAndRecieverEmailAndFolder(Long uid, String userEmail, String folderName) {
		return emailRepository.findByUidAndRecieverEmailAndFolderName(uid, userEmail, folderName);
	}

	public List<EmailFolder> getEmailAccountFolder(EmailClientDetails accountDetails) {
		return emailFolderService.getAllFolderByAccount(accountDetails.getEmailId());
	}

	public List<EmailData> findByUidAndRecieverEmailAndFolder(List<Long> uid, String userEmail, String folderName) {
		return emailRepository.findByRecieverEmailAndFolderNameAndUidIn(userEmail, folderName, uid);
	}

	public int getEmailCountForFolder(String folder, String emailId) {
		return emailFolderService.getCountByFolderAndAccount(folder, emailId);
	}

	public Long getTotalAvailableMessageInMongoCache(String folderName, String emailId) {
		return emailRepository.countByRecieverEmailAndFolderName(emailId, folderName);
	}

	public boolean isEmailExists(String id) {
		EmailData email = emailRepository.findById(id);
		if (null != email) {
			return true;
		}

		return false;
	}

	public EmailFolder getFolderByKey(String folderKey) {
		return emailFolderService.getFolderByKey(folderKey);
	}

	public EmailFolder getFolderByAccountEmailAndFolderName(String email, String folderName) {
		return emailFolderService.getFolderAccountEmailAndName(email, folderName);
	}

}
