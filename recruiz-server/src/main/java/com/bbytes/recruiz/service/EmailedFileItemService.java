package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.EmailedFileItem;
import com.bbytes.recruiz.repository.EmailedFileItemRepository;

@Service
public class EmailedFileItemService extends AbstractService<EmailedFileItem, Long> {

	private Logger logger = LoggerFactory.getLogger(EmailedFileItemService.class);

	private EmailedFileItemRepository emailedFileItemRepository;

	@Autowired
	public EmailedFileItemService(EmailedFileItemRepository emailedFileItemRepository) {
		super(emailedFileItemRepository);
		this.emailedFileItemRepository = emailedFileItemRepository;
	}

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private FileService fileService;

	@Value("${base.url}")
	private String baseUrl;

	
	public Page<EmailedFileItem> findByStatus(String status, Pageable pageRequest) {
		return emailedFileItemRepository.findByStatus(status, pageRequest);
	}	
	
	public List<EmailedFileItem> findByStatusAndRecruizEmail(String status, String recruizEmail) {
		return emailedFileItemRepository.findTop100ByStatusAndRecruizEmailId(status, recruizEmail);
	}
	

	public Long findItemCountByEmailAndStatus(String status, String emailId, String recruizEmail) {
	    return emailedFileItemRepository.countByStatusAndEmailIdAndRecruizEmailId(status, emailId, recruizEmail);
	}
	
}
