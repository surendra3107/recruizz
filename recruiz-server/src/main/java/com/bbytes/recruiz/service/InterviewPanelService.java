package com.bbytes.recruiz.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.InterviewerTimeSlot;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.ClientInterviewPanelRepository;
import com.bbytes.recruiz.repository.InterviewerTimeSlotRepository;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class InterviewPanelService extends AbstractService<ClientInterviewerPanel, Long> {

	@Autowired
	private ClientInterviewPanelRepository interviewPanelRepo;

	@Autowired
	private InterviewerTimeSlotRepository timeSlotRepo;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	public InterviewPanelService(ClientInterviewPanelRepository interviewPanelRepo) {
		super(interviewPanelRepo);
		this.interviewPanelRepo = interviewPanelRepo;
	}

	public boolean interviewerExists(String emailID) {
		List<ClientInterviewerPanel> list = interviewPanelRepo.findOneByEmail(emailID);
		if(list != null && !list.isEmpty())
			return true;
		return false;
	}
	
	public boolean interviewerMobileExists(String mobile) {
		boolean state = interviewPanelRepo.findOneByMobile(mobile) == null ? false : true;
		return state;
	}

	public boolean interviewerExists(long interviewerId) {
		boolean state = interviewPanelRepo.findOne(interviewerId) == null ? false : true;
		return state;
	}

	@Transactional
	public ClientInterviewerPanel addInterviewer(ClientInterviewerPanel clientInterviewerPanel)
			throws RecruizException {
		if (clientInterviewerPanel == null)
			throw new RecruizWarnException(ErrorHandler.INTERVIEW_NULL, ErrorHandler.INVLID_DATA);
		
		return save(clientInterviewerPanel);
	}

	@Transactional
	public void deleteInterviewer(ClientInterviewerPanel clientInterviewerPanel) throws RecruizException {
		if (clientInterviewerPanel == null)
			throw new RecruizWarnException(ErrorHandler.INTERVIEW_NULL, ErrorHandler.INVLID_DATA);

		delete(clientInterviewerPanel);
	}

	@Transactional
	public void addTimeSlot(InterviewerTimeSlot interviewerTimeSlot) {
		timeSlotRepo.save(interviewerTimeSlot);
	}

	/**
	 * Method will return list of interviewers by client.
	 * 
	 * @param client
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Set<ClientInterviewerPanel> getAllInterviewerByClient(Client client) throws RecruizException {
		List<ClientInterviewerPanel> interviewPanelList = new java.util.LinkedList<ClientInterviewerPanel>();
		interviewPanelList.addAll(interviewPanelRepo.findOneByClient(client));

		if(interviewPanelList != null && !interviewPanelList.isEmpty()){
			// doing sorting here
			Collections.sort(interviewPanelList, new Comparator<ClientInterviewerPanel>() {
			    public int compare(ClientInterviewerPanel dto1, ClientInterviewerPanel dto2) {
			    	
			    	 int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
				        if (res == 0) {
				            res = dto1.getName().compareTo(dto2.getName());
				        }
				        return res;
			    }
			});
			return new LinkedHashSet<>(interviewPanelList);
		}
		return null;
	}
	
	@Transactional
	public List<ClientInterviewerPanel> getInterviewerListByClient(Client client) throws RecruizException {
		return interviewPanelRepo.findByClient(client);
	}
	
	@Transactional
	public List<ClientInterviewerPanel> getInterviewerByEmail(String email) {
		return interviewPanelRepo.findOneByEmail(email);
	}
	
	@Transactional(readOnly = true)
	public ClientInterviewerPanel getInterviewerByEmailAndClient(String email,Client client) {
		return interviewPanelRepo.findOneByEmailAndClient(email, client);
	}

}
