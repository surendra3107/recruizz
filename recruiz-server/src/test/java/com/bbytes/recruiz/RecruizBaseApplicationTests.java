package com.bbytes.recruiz;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.database.TenantDBService;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.repository.BoardRepository;
import com.bbytes.recruiz.repository.CandidateRepository;
import com.bbytes.recruiz.repository.ClientDecisionMakerRepository;
import com.bbytes.recruiz.repository.ClientInterviewPanelRepository;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.repository.InterviewScheduleRepository;
import com.bbytes.recruiz.repository.InterviewerTimeSlotRepository;
import com.bbytes.recruiz.repository.OrganizationRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.RoundRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.repository.UserRolesRepository;
import com.bbytes.recruiz.repository.VendorRepository;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;

public class RecruizBaseApplicationTests extends RecruizApplicationTests {

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;
	
	@Autowired
	protected OrganizationService organizationService;

	@Autowired
	protected UserService userService;
	
	@Autowired
	protected PositionService positionService;
	
	@Autowired
	protected CandidateService candidateService;

	@Autowired
	protected ClientService clientService;
	
	@Autowired
	protected OrganizationRepository organizationRepository;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected TenantResolverService tenantResolverService;

	@Autowired
	protected SpringProfileService springProfileService;

	@Autowired
	protected UserRoleService userRoleService;

	@Autowired
	protected UserRolesRepository userRolesRepository;

	@Autowired
	protected InterviewerTimeSlotRepository interviewerTimeSlotRepository;

	@Autowired
	protected ClientInterviewPanelRepository clientInterviewerPanelRepo;

	@Autowired
	protected ClientDecisionMakerRepository clientDecisonMakerRepo;

	@Autowired
	protected ClientRepository clientRepo;

	@Autowired
	protected CandidateRepository candidateRepo;

	@Autowired
	protected RoundRepository roundRepo;

	@Autowired
	protected BoardRepository boardRepo;

	@Autowired
	protected PositionRepository positionRepo;

	@Autowired
	protected InterviewScheduleRepository interviewScheduleRepo;

	@Autowired
	protected VendorRepository vendorRepo;

	@Autowired
	protected TenantDBService dbService;

	@Before
	public void init() {

	}

	public void initRoles() {
		Set<Permission> list = new HashSet<Permission>();
		list.add(new Permission("ALL"));
		UserRole userRoles = new UserRole();
		userRoles.setRoleName("ORG ADMIN");
		userRoles.setPermissions(list);
		userRoleService.save(userRoles);

		list = new HashSet<Permission>();
		list.add(new Permission("CLIENT"));
		list.add(new Permission("POSITION"));
		userRoles = new UserRole();
		userRoles.setRoleName("MANAGER");
		userRoles.setPermissions(list);
		userRoleService.save(userRoles);
	}

	@After
	public void cleanUpData() {
		// tenantResolverService.deleteAll();
		// userRoleService.deleteAll();

	}

	@Test
	@Ignore
	public void contextLoads() {
	}

}
