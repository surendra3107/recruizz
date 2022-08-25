package com.bbytes.recruiz.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.InterviewerTimeSlot;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class ClientRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	ClientRepository clientRepo;

	private String tenantName = "acme";

	@Before
	public void setup() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addClient() throws ParseException {
		Client client = new Client();
		client.setClientName("Client 1");
		client.setAddress("#57, #57, Mina road");
		client.setWebsite("www.bbytes.com");
		client.setEmpSize("100");
		client.setTurnOvr("15 Cr.");
		client.setClientLocation("Bangalore");
		client.setNotes("No additional notes");

		// Setting Decison makers for the client

		ClientDecisionMaker clientDecisionMaker1 = new ClientDecisionMaker();
		clientDecisionMaker1.setName("Decision Maker1");
		clientDecisionMaker1.setEmail("email@emialasdsad.com");
		clientDecisionMaker1.setMobile("772277272");
		clientDecisionMaker1.setClient(client);

		ClientDecisionMaker clientDecisionMaker2 = new ClientDecisionMaker();
		clientDecisionMaker2.setName("Decision Maker2");
		clientDecisionMaker2.setEmail("emazxzil@emial.com");
		clientDecisionMaker2.setMobile("1122277272");
		clientDecisionMaker2.setClient(client);

		client.getClientDecisionMaker().add(clientDecisionMaker1);
		client.getClientDecisionMaker().add(clientDecisionMaker2);

		Date statTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 01:00 pm");
		Date endTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		Date statTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 12:55 am");
		Date endTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		// Setting interview panel for the client
		ClientInterviewerPanel clientInterviewerPanel = new ClientInterviewerPanel();
		InterviewerTimeSlot interviewerTimeSlot = new InterviewerTimeSlot(statTime1, endTime1);
		InterviewerTimeSlot interviewerTimeSlot2 = new InterviewerTimeSlot(statTime2, endTime2);
		clientInterviewerPanel.setName("Inetrviewer 1");
		clientInterviewerPanel.setEmail("sks@sks.com");
		clientInterviewerPanel.setMobile("81272637");
		clientInterviewerPanel.getInterviewerTimeSlots().add(interviewerTimeSlot);
		clientInterviewerPanel.getInterviewerTimeSlots().add(interviewerTimeSlot2);
		clientInterviewerPanel.setClient(client);

		interviewerTimeSlot.setClientInterviewerPanel(clientInterviewerPanel);
		interviewerTimeSlot2.setClientInterviewerPanel(clientInterviewerPanel);

		client.getClientInterviewerPanel().add(clientInterviewerPanel);

		clientRepo.saveAndFlush(client);
	}

	@Transactional
	@Test
	public void findClientDetails() {
		List<Client> clients = clientRepo.findAll();
		for (Client c : clients) {
			System.out.println("\n\nName : " + c.getClientName());
			System.out.println("Address : " + c.getAddress());
			System.out.println("Website : " + c.getWebsite());
			System.out.println("Emp Range :" + c.getEmpSize() + " - " + c.getEmpSize());
			System.out.println("Turn Over :" + c.getTurnOvr());
			System.out.println("Notes :" + c.getNotes());
			System.out.println("Decison Makers");
			Set<ClientDecisionMaker> dms = c.getClientDecisionMaker();
			for (ClientDecisionMaker dm : dms)
				System.out.print("\t" + dm.getName());

			System.out.println("\nInterview Panels");
			Set<ClientInterviewerPanel> panels = c.getClientInterviewerPanel();
			for (ClientInterviewerPanel panel : panels)
				System.out.print("\t" + panel.getName());
		}

		System.out.println("\n\n");
	}

	// test case to delete client
	@Test
	public void deleteClient() {
		clientRepo.delete((long) 6);
	}

	@Test
	public void clientNamesTest() {
		List<String> names = clientRepo.getClientNames();
		Assert.isTrue(names.size() > 0);
	}

	// Test Case for name query to return list of client along with position
	// count
	@Test
	public void clientListAndPositionPass() {
		List<Long> ids = new ArrayList<Long>();
		ids.add(1L);
		ids.add(2L);
		List<ClientOpeningCountDTO> clientList = clientRepo.clientListWithTotalOpening(ids);
		System.out.println(clientList.get(0).getClient().getClientName());
	}

	// Test Case for name query to return client object along with position
	// count
	@Test
	public void clientAndPositionPass() {

		ClientOpeningCountDTO client = clientRepo.clientWithTotalOpening(1L);
		System.out.println(client.getClient().getClientName());
	}
}
