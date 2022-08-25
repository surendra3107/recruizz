package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Prospect;

public interface ProspectRepository extends JpaRepository<Prospect, Long> {

	List<Prospect> findByName(String name);

	Prospect findByCompanyName(String companyName);
	
	List<Prospect> findByDummy(boolean flag);

	Prospect findByEmail(String email);

	Prospect findByMobile(String mobile);

	Page<Prospect> findByProspectIdIn(List<Long> ids, Pageable pageable);

	List<Prospect> findDistinctByOwnerAndProspectIdIn(String ownerEmail, List<Long> ids);

	List<Prospect> findDistinctByOwnerAndProspectIdIn(String ownerEmail, List<Long> ids, Sort sort);
	
	Page<Prospect> findDistinctByOwner(String ownerEmail, Pageable pageable);
	
	List<Prospect> findDistinctByOwner(String ownerEmail);
	
	Prospect findByClient(Client client);

	@Modifying
	@Transactional
	@Query(value = "delete from custom_field_prospect where name = :name" , nativeQuery=true)
	void deleteCustomFieldWithName(@Param("name") String name);

}
