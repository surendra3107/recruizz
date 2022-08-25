package com.bbytes.recruiz.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.ClientDecisionMakerRepository;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class DecisionMakerService extends AbstractService<ClientDecisionMaker, Long> {

	@Autowired
	private ClientDecisionMakerRepository clientDecisionMakerRepo;
	
	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	public DecisionMakerService(ClientDecisionMakerRepository clientDecisionMakerRepo) {
		super(clientDecisionMakerRepo);
		this.clientDecisionMakerRepo = clientDecisionMakerRepo;
	}

	public boolean decisionMakerExists(String emailId) {
		boolean state = clientDecisionMakerRepo.findOneByEmail(emailId) == null ? false : true;
		return state;
	}
	
	public boolean decisionMakerMobileExists(String mobile) {
		boolean state = clientDecisionMakerRepo.findOneByMobile(mobile) == null ? false : true;
		return state;
	}
	
	public boolean decisionMakerExists(long dmId) {
		boolean state = clientDecisionMakerRepo.findOne(dmId) == null ? false : true;
		return state;
	}

	@Transactional
	public void addDecisionMaker(List<ClientDecisionMaker> decisionMakers) throws RecruizException {

		for (ClientDecisionMaker clientDecisionMaker : decisionMakers) {
			if (clientDecisionMaker.getClient() == null)
				throw new RecruizWarnException(ErrorHandler.CLIENT_NOT_SET, ErrorHandler.CLIENT_MISSING);

			save(clientDecisionMaker);
		}
	}

	@Transactional
	public void deleteDecisionMaker(ClientDecisionMaker clientDecisionMaker) throws RecruizException {

		if (clientDecisionMaker == null)
			throw new RecruizWarnException(ErrorHandler.DECISION_MAKER_MISSING, ErrorHandler.DECISON_MAKER_NOT_FOUND);
	}

	@Transactional
	public List<ClientDecisionMaker> getDecisionMakerByClient(Client client) throws RecruizException {
		List<ClientDecisionMaker> decisionMakerList = new LinkedList<ClientDecisionMaker>();
		decisionMakerList = clientDecisionMakerRepo.findByClient(client);
		if(decisionMakerList != null && !decisionMakerList.isEmpty()){
			Collections.sort(decisionMakerList, new Comparator<ClientDecisionMaker>() {
			    public int compare(ClientDecisionMaker dto1, ClientDecisionMaker dto2) {
			    	
			    	 int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
				        if (res == 0) {
				            res = dto1.getName().compareTo(dto2.getName());
				        }
				        return res;
			    }
			});
			return decisionMakerList;
		}
		return null;
	}
	
	
	@Transactional
	public List<ClientDecisionMaker> getDecisionMakerByEmail(String email) throws RecruizException {
		return clientDecisionMakerRepo.findOneByEmail(email);
	}
	
	
	@Transactional
	public ClientDecisionMaker getDecisionMakerByEmailAndClient(String email,Client client) throws RecruizException {
		return clientDecisionMakerRepo.findOneByEmailAndClient(email,client);
	}

}
