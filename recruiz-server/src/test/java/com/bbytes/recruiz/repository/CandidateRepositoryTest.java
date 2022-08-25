package com.bbytes.recruiz.repository;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.utils.TenantContextHolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CandidateRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	CandidateRepository candidateRepository;

	private String tenantName = "acme";

	@Before
	public void setup() {
		TenantContextHolder.setTenant(tenantName);
	}

	// Test case for adding candidate
	@Test
	public void addCandidate() {

		
		Set<String> keySkills =new HashSet<>();
		keySkills.add("C");
		keySkills.add("Java");
		keySkills.add("Css");

		Candidate candidate = new Candidate();
		candidate.setFullName("Harry Potter");
		candidate.setMobile("8149922038");
		candidate.setEmail("akshay.nag@beyondbytes.co.in");
		candidate.setCurrentCompany("Thoughworks");
		candidate.setCurrentTitle("Software Developer");
		candidate.setCurrentLocation("Bangalore");
		candidate.setHighestQual("B.Tech");
		candidate.setTotalExp(1.8);
		candidate.setEmploymentType("Permanent");
		candidate.setCurrentCtc(12000);
		candidate.setExpectedCtc(15000);
		candidate.setNoticePeriod(30);
		candidate.setNoticeStatus(false);
		candidate.setLastWorkingDay(DateTime.now().plusDays(20).toDate());
		candidate.setKeySkills(keySkills);
		candidate.setResumeLink("www.g.gle.com/id?val=100");
		candidate.setDob(DateTime.now().minusYears(20).toDate());
		candidate.setGender("Male");
		candidate.setCommunication("Good");
		candidate.setPreferredLocation("Pune");
		candidate.setTwitterProf("https://twitter.com/statusnap/followers");
		candidate.setGithubProf("https://github.com/akshay-bbytes");
		candidate.setFacebookProf("https://www.facebook.com/akshay.nagpurkar");
		candidate.setLinkedinProf("https://www.linkedin.com/in/akshay-nagpurkar-26b73526");
		candidate.setComments(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor aenean massa. Cum parturient montes");
		candidate.setStatus("Active");

		candidateRepository.saveAndFlush(candidate);
	}

	@Test
	public void b_updateCandidate() {
		Candidate candidate = candidateRepository.findOne((long) 1);
		candidate.setFullName("Updated Name");

		candidateRepository.saveAndFlush(candidate);
	}

	// Test Case to delete candidate
	@Test
	public void deleteCandidate() {
		Candidate candidate = candidateRepository.findOne((long) 1);
		candidateRepository.delete(candidate);
	}

}
