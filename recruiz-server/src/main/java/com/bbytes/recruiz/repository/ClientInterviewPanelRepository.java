package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;

//@JaversSpringDataAuditable
public interface ClientInterviewPanelRepository extends JpaRepository<ClientInterviewerPanel, Long> {

	List<ClientInterviewerPanel> findOneByEmail(String emailID);
	
	ClientInterviewerPanel findOneByEmailAndClient(String emailID, Client client);
	
	ClientInterviewerPanel findOneByMobile(String mobile);

	Set<ClientInterviewerPanel> findOneByClient(Client clientId);

	List<ClientInterviewerPanel> findByClient(Client client);

}
