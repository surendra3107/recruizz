package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.GenericDecisionMaker;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.GenericDecisionMakerRepository;

@Service
public class GenericDecisionMakerService extends AbstractService<GenericDecisionMaker, Long> {

    private static final Logger logger = LoggerFactory.getLogger(GenericDecisionMakerService.class);

    private GenericDecisionMakerRepository genericDecisionMakerRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private DecisionMakerService decisionMakerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClientActivityService clientActivityService;

    @Autowired
    public GenericDecisionMakerService(GenericDecisionMakerRepository genericDecisionMakerRepository) {
	super(genericDecisionMakerRepository);
	this.genericDecisionMakerRepository = genericDecisionMakerRepository;
    }

    @Transactional
    public List<GenericDecisionMaker> saveDM(List<GenericDecisionMaker> dms) {
	for (GenericDecisionMaker genericDecisionMaker : dms) {
	    if (null == genericDecisionMakerRepository.findByEmail(genericDecisionMaker.getEmail())) {
		genericDecisionMakerRepository.save(genericDecisionMaker);
	    }
	}
	return dms;
    }

    // adding decision maker to client
    public void addDMToClient(Long clientId, GenericDecisionMaker savedDM) throws RecruizException {
	Client client = clientService.findOne(clientId);
	ClientDecisionMaker dmToAdd = new ClientDecisionMaker();
	dmToAdd.setClient(client);
	dmToAdd.setEmail(savedDM.getEmail());
	dmToAdd.setMobile(savedDM.getMobile());
	dmToAdd.setName(savedDM.getName());

	java.util.List<ClientDecisionMaker> dmsToAdd = new ArrayList<>();
	dmsToAdd.add(dmToAdd);
	decisionMakerService.addDecisionMaker(dmsToAdd);

	// adding client DM removed activity
	String dmDetails = dmToAdd.getName() + " (" + dmToAdd.getEmail() + ")";
	ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		NotificationEvent.CLIENT_DECISIONMAKER_ADDED.getDisplayName(),
		"Decision Maker " + dmDetails + " added to client", new Date(), clientId);
	clientActivityService.addActivity(activity);
    }

    public GenericDecisionMaker getDecisionMakerByEmail(String email) {
	return genericDecisionMakerRepository.findByEmail(email);
    }

    public List<GenericDecisionMaker> getDecisionMakerByMobile(String mobile) {
	return genericDecisionMakerRepository.findByMobile(mobile);
    }

    public List<GenericDecisionMaker> getAllDecisionMaker() throws RecruizException {
	List<GenericDecisionMaker> allDms = genericDecisionMakerRepository.findAll();
	for (GenericDecisionMaker genericDecisionMaker : allDms) {
	    List<ClientDecisionMaker> clientDecisionMaker = decisionMakerService
		    .getDecisionMakerByEmail(genericDecisionMaker.getEmail());
	    Set<ClientDecisionMaker> clientDMs = new java.util.HashSet<>();
	    clientDMs.addAll(clientDecisionMaker);

	    List<Client> clients = clientService.getClientByDecisionMakerIn(clientDMs);
	    if (null != clients && !clients.isEmpty()) {
		for (Client client : clients) {
		    genericDecisionMaker.getClientNames().add(client.getClientName());
		}

	    }
	}
	return allDms;
    }

    @Transactional
    public GenericDecisionMaker updateDM(String email, GenericDecisionMaker dmFromRequest) throws RecruizException {
	GenericDecisionMaker gdm = genericDecisionMakerRepository.findByEmail(email);
	if (null != gdm) {
	    dmFromRequest.setId(gdm.getId());
	    genericDecisionMakerRepository.save(dmFromRequest);

	    // updating decision makers if it is part of any client
	    List<ClientDecisionMaker> existingClientDms = decisionMakerService
		    .getDecisionMakerByEmail(dmFromRequest.getEmail());

	    if (existingClientDms != null && !existingClientDms.isEmpty()) {
		for (ClientDecisionMaker clientDecisionMaker : existingClientDms) {
		    clientDecisionMaker.setName(dmFromRequest.getName());
		    clientDecisionMaker.setMobile(dmFromRequest.getMobile());
		}
		decisionMakerService.save(existingClientDms);
	    }

	} else {
	    throw new RecruizException("Interview does not exists", "interviewer_not_found");
	}
	return dmFromRequest;
    }

    /**
     * Adding client decision makers to generic list if it does not exists there
     * 
     * @param decisionMakerList
     */
    @Transactional
    public void addDMsFromClient(Set<ClientDecisionMaker> decisionMakerList) {
	List<GenericDecisionMaker> dms = new ArrayList<>();
	if (null != decisionMakerList && !decisionMakerList.isEmpty()) {
	    for (ClientDecisionMaker clientDM : decisionMakerList) {
		// checking if there is no decision maker with this email and
		// mobile then adding it to generic list
		if (null == genericDecisionMakerRepository.findByEmail(clientDM.getEmail())) {
		    if (null != clientDM.getMobile() && !clientDM.getMobile().trim().isEmpty()
			    && (null == genericDecisionMakerRepository.findByMobile(clientDM.getMobile())
				    || genericDecisionMakerRepository.findByMobile(clientDM.getMobile()).isEmpty())) {
			addDM(dms, clientDM);
		    } else if (null == clientDM.getMobile() || clientDM.getMobile().isEmpty()) {
			addDM(dms, clientDM);
		    }
		}
	    }

	    if (null != dms && !dms.isEmpty()) {
		genericDecisionMakerRepository.save(dms);
	    }
	}
    }

    public void addDM(List<GenericDecisionMaker> dms, ClientDecisionMaker clientDM) {
	GenericDecisionMaker genericDm = new GenericDecisionMaker();
	genericDm.setEmail(clientDM.getEmail());
	genericDm.setMobile(clientDM.getMobile());
	genericDm.setName(clientDM.getName());
	dms.add(genericDm);
    }

}