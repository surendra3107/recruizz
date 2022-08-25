package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.GenericInterviewerRepository;

@Service
public class GenericInterviewerService extends AbstractService<GenericInterviewer, Long> {

    private static final Logger logger = LoggerFactory.getLogger(GenericInterviewerService.class);

    private GenericInterviewerRepository genericInterviewerRepository;

    @Autowired
    public GenericInterviewerService(GenericInterviewerRepository genericInterviewerRepository) {
	super(genericInterviewerRepository);
	this.genericInterviewerRepository = genericInterviewerRepository;
    }

    @Autowired
    private InterviewPanelService interviewPanelService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private PositionActivityService positionActivityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClientActivityService clientActivityService;

    @Transactional
    public List<GenericInterviewer> saveInterviewer(List<GenericInterviewer> interviewers) {
	for (GenericInterviewer genericInterviewer : interviewers) {
	    if (null == genericInterviewerRepository.findByEmail(genericInterviewer.getEmail())) {
		genericInterviewerRepository.save(genericInterviewer);
	    }
	}
	return interviewers;
    }

    public GenericInterviewer getInterviewerByEmail(String email) {
	return genericInterviewerRepository.findByEmail(email);
    }

    public List<GenericInterviewer> getInterviewerByMobile(String mobile) {
	return genericInterviewerRepository.findByMobile(mobile);
    }

    public List<GenericInterviewer> getAllInterviewer() {
	List<GenericInterviewer> allInterviewer = genericInterviewerRepository.findAll();
	for (GenericInterviewer genericInterviewer : allInterviewer) {
	    Set<ClientInterviewerPanel> interviewPanelList = new HashSet<>();
	    List<ClientInterviewerPanel> interviewers = interviewPanelService
		    .getInterviewerByEmail(genericInterviewer.getEmail());
	    interviewPanelList.addAll(interviewers);

	    List<Client> clientsForInterviewer = clientService.getClientByInterviewPanelIn(interviewPanelList);
	    if (clientsForInterviewer != null && !clientsForInterviewer.isEmpty()) {
		for (Client client : clientsForInterviewer) {
		    genericInterviewer.getClientName().add(client.getClientName());
		}
	    }
	}
	return allInterviewer;
    }

    @Transactional
    public GenericInterviewer updateInterviewer(String email, GenericInterviewer interviewerFromRequest)
	    throws RecruizException {
	GenericInterviewer genericInterviewer = genericInterviewerRepository.findByEmail(email);
	if (null != genericInterviewer) {
	    genericInterviewer.setName(interviewerFromRequest.getName());
	    genericInterviewer.setMobile(interviewerFromRequest.getMobile());
	    
	    genericInterviewerRepository.save(genericInterviewer);

	    // updating client interviewer
	    List<ClientInterviewerPanel> clientInterviewers = interviewPanelService
		    .getInterviewerByEmail(genericInterviewer.getEmail());
	    if (null != clientInterviewers && !clientInterviewers.isEmpty()) {
		for (ClientInterviewerPanel clientInterviewerPanel : clientInterviewers) {
		    clientInterviewerPanel.setName(interviewerFromRequest.getName());
		    clientInterviewerPanel.setMobile(interviewerFromRequest.getMobile());
		}
		interviewPanelService.save(clientInterviewers);
	    }
	    
	    // updating scheduled interview information here
	    genericInterviewerRepository.updateInterviewerEventAttendeeOnInterviewerUpdate(genericInterviewer.getName(), genericInterviewer.getEmail());

	} else {
	    throw new RecruizException("Interview does not exists", "interviewer_not_found");
	}
	return genericInterviewer;
    }

    @Transactional
    public void addGenericInterviewerFromClientOrPosition(Set<ClientInterviewerPanel> clientInterviewerPanelList) {
	if (null != clientInterviewerPanelList && !clientInterviewerPanelList.isEmpty()) {
	    List<GenericInterviewer> genericInterviewers = new ArrayList<>();
	    for (ClientInterviewerPanel clientInterviewerPanel : clientInterviewerPanelList) {
		if (null == genericInterviewerRepository.findByEmail(clientInterviewerPanel.getEmail())) {
		    if (null == clientInterviewerPanel.getMobile() || clientInterviewerPanel.getMobile().isEmpty()) {
			addInterviewerToGenericInterviewerList(genericInterviewers, clientInterviewerPanel);
		    } else if (null == genericInterviewerRepository.findByMobile(clientInterviewerPanel.getMobile()) || genericInterviewerRepository.findByMobile(clientInterviewerPanel.getMobile()).isEmpty()) {
			addInterviewerToGenericInterviewerList(genericInterviewers, clientInterviewerPanel);
		    }
		}
	    }
	    if (null != genericInterviewers && !genericInterviewers.isEmpty()) {
		 saveInterviewer(genericInterviewers);
	    }
	}

    }

    public void addInterviewerToGenericInterviewerList(List<GenericInterviewer> genericInterviewers,
	    ClientInterviewerPanel clientInterviewerPanel) {
	GenericInterviewer genInterviewer = new GenericInterviewer();
	genInterviewer.setEmail(clientInterviewerPanel.getEmail());
	genInterviewer.setName(clientInterviewerPanel.getName());
	genInterviewer.setMobile(clientInterviewerPanel.getMobile());
	genericInterviewers.add(genInterviewer);
    }

    /**
     * This will add interviewer to genric list will also add it to client and
     * position.
     * 
     * @param savedInterviewer
     * @param pid
     */
    // @Transactional
    public void addGenericInterviewerToPosition(GenericInterviewer savedInterviewer, Long pid) {
	Position position = positionService.findOne(pid);
	ClientInterviewerPanel interviewPanel;
	ClientInterviewerPanel existingPanel = interviewPanelService
		.getInterviewerByEmailAndClient(savedInterviewer.getEmail(), position.getClient());
	if (null != existingPanel) {
	    interviewPanel = interviewPanelService.getInterviewerByEmailAndClient(savedInterviewer.getEmail(), position.getClient());
	} else {
	    interviewPanel = new ClientInterviewerPanel();
	    interviewPanel.setClient(position.getClient());
	    interviewPanel.setEmail(savedInterviewer.getEmail());
	    interviewPanel.setMobile(savedInterviewer.getMobile());
	    interviewPanel.setName(savedInterviewer.getName());
	    interviewPanelService.save(interviewPanel);
	}

	position.addInterviewerPanel(interviewPanel);
	// position.getClient().addClientInterviewerPanel(interviewPanel);
	clientService.save(position.getClient());

	// adding in position activity
	PositionActivity positionActivity;
	try {
	    positionActivity = new PositionActivity(userService.getLoggedInUserEmail(),
		    userService.getLoggedInUserName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
		    "Interviewer " + interviewPanel.getName() + " added to position. ", new Date(),
		    position.getPositionCode(),position.getTeam());

	    positionActivityService.addActivity(positionActivity);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}

    }

    @Transactional
    public void addGenericInterviewerToClient(GenericInterviewer savedInterviewer, Long cid) {
	Client client = clientService.findOne(cid);

	ClientInterviewerPanel interviewPanel = new ClientInterviewerPanel();
	interviewPanel.setClient(client);
	interviewPanel.setEmail(savedInterviewer.getEmail());
	interviewPanel.setMobile(savedInterviewer.getMobile());
	interviewPanel.setName(savedInterviewer.getName());

	client.addClientInterviewerPanel(interviewPanel);
	clientService.save(client);

	// adding in client activity on interviewer added.
	ClientActivity clientActivity;
	try {
	    clientActivity = new ClientActivity(userService.getLoggedInUserName(), userService.getLoggedInUserEmail(),
		    userService.getLoggedInUserName(), NotificationEvent.POSITION_MODIFIED.getDisplayName(),
		    "Interviewer " + interviewPanel.getName() + " added to position. ", new Date(), cid);

	    clientActivityService.addActivity(clientActivity);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}
    }

  
    public List<GenericInterviewer> getDummyIngterviewer() {
	return genericInterviewerRepository.findByEmailStartingWith("dummy");
    }

}