package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.domain.User;

public interface EmailClientDetailsRepository extends JpaRepository<EmailClientDetails, Long> {
	
	List<EmailClientDetails> findByUser(User user);
	
	List<EmailClientDetails> findByUserAndMarkedDefault(User user,Boolean markedDefault);
	
	List<EmailClientDetails> findByMarkedDefault(Boolean defaultStatus);
	
	List<EmailClientDetails> findByEmailId(String emailID);

	@Query(value = "delete from email_client_details where user_user_id =?1", nativeQuery = true)
	void deleteByUserId(Long userId);
	
}
