package com.bbytes.recruiz.service;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class TestAuditQueryService extends RecruizBaseApplicationTests {

	@Autowired
	private AuditQueryService auditQueryService;

	String tenantId = "acme";

	Organization org;

	User testUser;

	@Before
	public void init() {

		org = new Organization(tenantId, tenantId);

		testUser = new User("testsamemail", "same@gmail");
		testUser.setOrganization(org);

		TenantContextHolder.setTenant(org.getOrgId());
		organizationService.save(org);

		Client client = new Client();
		client.setId(22L);
		client.setAddress("300 , 4th block , northwest bound");
		client.setClientLocation("Bengal");
		client.setClientName("Bengal tech");
		client.setNotes("good in tech");
		client.setWebsite("http://bentech.com");

		client = clientRepo.save(client);

		Client client2 = new Client();
		client2.setId(52L);
		client2.setAddress("25 , 5th main , Hyd road");
		client2.setClientLocation("Bangaldesh");
		client2.setClientName("Hyd sol");
		client2.setNotes("Service and app dev");
		client2.setWebsite("http://hydsol.com");

		client2 = clientRepo.save(client2);

		Set<String> keySkills = new HashSet<>();
		keySkills.add("C");
		keySkills.add("Java");
		keySkills.add("Css");
		keySkills.add("Angular");
		keySkills.add("JavaScript");
		keySkills.add("ERP");
		keySkills.add("jquery");
		keySkills.add("oracle");
		keySkills.add("sql");
		keySkills.add("jsp");
		keySkills.add("JWT");
		keySkills.add("C");

		Candidate candidate1 = new Candidate();
		candidate1.setCid(25L);
		candidate1.setFullName("John Smith");
		candidate1.setMobile("8022620063");
		candidate1.setEmail("cad211@freeware.com");
		candidate1.setCurrentCompany("Infosys");
		candidate1.setCurrentTitle("Software Tester");
		candidate1.setCurrentLocation("Pune");
		candidate1.setHighestQual("B.Tech");
		candidate1.setTotalExp(1.8);
		candidate1.setEmploymentType(EmploymentType.Contract.toString());
		candidate1.setCurrentCtc(120000);
		candidate1.setExpectedCtc(150000);
		candidate1.setNoticePeriod(30);
		candidate1.setNoticeStatus(false);
		candidate1.setLastWorkingDay(DateTime.now().plusDays(20).toDate());
		candidate1.setKeySkills(keySkills);
		candidate1.setResumeLink("www.g.gle.com/id?val=100");
		candidate1.setDob(DateTime.now().minusYears(20).toDate());
		candidate1.setGender("Male");
		candidate1.setCommunication("Good");
		candidate1.setPreferredLocation("Chennai");
		candidate1.setTwitterProf("https://twitter.com/statusnap/followers");
		candidate1.setGithubProf("https://github.com/akshay-bbytes");
		candidate1.setFacebookProf("https://www.facebook.com/akshay.nagpurkar");
		candidate1.setLinkedinProf("https://www.linkedin.com/in/akshay-nagpurkar-26b73526");
		candidate1.setComments(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor aenean massa. Cum parturient montes");
		candidate1.setStatus(Status.Active.toString());

		candidate1 = candidateRepo.save(candidate1);

		Candidate candidate2 = new Candidate();
		candidate2.setCid(32L);
		candidate2.setFullName("Wayne Land");
		candidate2.setMobile("99022620063");
		candidate2.setEmail("cad215@test.com");
		candidate2.setCurrentCompany("BBytes");
		candidate2.setCurrentTitle("Software Eng");
		candidate2.setCurrentLocation("Bangalore");
		candidate2.setHighestQual("B.Tech");
		candidate2.setTotalExp(1.8);
		candidate2.setEmploymentType("Permanent");
		candidate2.setCurrentCtc(250000);
		candidate2.setExpectedCtc(300000);
		candidate2.setNoticePeriod(10);
		candidate2.setNoticeStatus(true);
		candidate2.setLastWorkingDay(DateTime.now().plusDays(10).toDate());
		candidate2.setKeySkills(keySkills);
		candidate2.setResumeLink("www.g.gle.com/id?val=100");
		candidate2.setDob(DateTime.now().minusYears(20).toDate());
		candidate2.setGender("Male");
		candidate2.setCommunication("Good");
		candidate2.setPreferredLocation("Delhi");
		candidate2.setComments(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor aenean massa. Cum parturient montes");
		candidate2.setStatus("Active");

		candidate2 = candidateRepo.save(candidate2);

		Set<String> goodSkillSet = new HashSet<>();
		goodSkillSet.add("JAVA");
		goodSkillSet.add("gulp");
		goodSkillSet.add("ajs");

		Set<String> reSkillSet = new HashSet<>();
		reSkillSet.add("JS");
		reSkillSet.add("Node");
		reSkillSet.add("Spring");

		Position position = new Position();
		position.setId(31L);
		position.setPositionCode("TSWE_05");
		position.setTitle("Test JS Developer");
		position.setLocation("Kolkata");
		position.setTotalPosition(15);
		position.setOpenedDate(DateTime.now().toDate());
		position.setCloseByDate(DateTime.now().plusDays(10).toDate());
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(goodSkillSet);
		position.setReqSkillSet(reSkillSet);
		position.setType("Payroll");
		position.setRemoteWork(true);
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setDescription(
				"commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		position.setStatus(Status.Active.getDisplayName());

		position = positionRepo.save(position);

	}

	@After
	public void cleanup() {
		TenantContextHolder.setTenant(testUser.getOrganization().getOrgId());
		clientService.deleteAll();
		candidateService.deleteAll();
		positionService.deleteAll();
		userService.deleteAll();
		organizationService.deleteAll();
	}

	@Test
	public void testAuditQueryForCandidate() throws RecruizException {
		// auditQueryService.getChanges(Candidate.class, "orgadmin@acc.in",
		// 100);
		auditQueryService.getSnapShotChanges(Candidate.class, null, 100);

	}

}
