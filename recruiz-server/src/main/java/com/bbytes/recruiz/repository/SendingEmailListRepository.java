package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.SendingEmailList;

public interface SendingEmailListRepository extends JpaRepository<SendingEmailList, Long>{

	@Query(value ="select * from sending_email_id_list s where s.list_type =?1", nativeQuery = true)
	List<SendingEmailList> findAllByType(String type);

	@Query(value ="select * from sending_email_id_list s where s.email_id =?1", nativeQuery = true)
	SendingEmailList findByEmail(String email);

}
