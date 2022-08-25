package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;

//@JaversSpringDataAuditable
public interface ClientDecisionMakerRepository extends JpaRepository<ClientDecisionMaker, Long> {

	List<ClientDecisionMaker> findOneByClient(long id);

	List<ClientDecisionMaker> findByClient(Client client);

	List<ClientDecisionMaker> findOneByEmail(String emailId);
	
	ClientDecisionMaker findOneByEmailAndClient(String emailId,Client client);
	
	ClientDecisionMaker findOneByMobile(String mobile);

	/*
	 * @Query(
	 * "select dm.fullName,dm.emailID from decision_maker dm where dm.client = ?"
	 * ) List<ClientDecisionMaker> findByClient(Client clientID);
	 */

}
