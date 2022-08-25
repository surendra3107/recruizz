package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.domain.EmailedFileItem;

public interface EmailedFileItemRepository extends JpaRepository<EmailedFileItem, Long> {

	public List<EmailedFileItem> findByStatus(String status);

	public Page<EmailedFileItem> findByStatus(String status,Pageable pageable);

	public List<EmailedFileItem> findTop100ByStatusAndRecruizEmailId(String status, String recruizEmail);
	
	public Long countByStatusAndEmailIdAndRecruizEmailId(String status,String emailId,String recruizEmail);
	
}
