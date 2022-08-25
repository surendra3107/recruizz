package com.bbytes.recruiz.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.utils.TenantContextHolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RoundRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	RoundRepository roundRepo;

	@Autowired
	CandidateRepository candidateRepo;

	private String tenantName = "dummy";

	@Before
	public void init() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addRounds() {
		Round round = new Round();
		round.setRoundType("Technical 1");
		round.setOrderNo(3);
		List<Candidate> candidates = candidateRepo.findAll();
		/*Set<Candidate> rejectedCands = (Set<Candidate>) candidateRepo.findAll();
		round.setActiveCandidates((Set<Candidate>) candidates);
		round.setRejectedCandidates(rejectedCands);
*/
		roundRepo.saveAndFlush(round);
	}

	@Transactional
	@Test
	public void fetchRoundDetails() {
		List<Round> round = roundRepo.findAll();
		for (Round rounds : round) {
			System.out.print(rounds.getRoundType() + "  " + rounds.getOrderNo() + "\n");
			/*List<Candidate> candidates = (List<Candidate>) rounds.getActiveCandidates();
			for (Candidate cand : candidates) {
				System.out.print(cand.getFullName());
			}

			List<Candidate> rCandidates = (List<Candidate>) rounds.getRejectedCandidates();
			for (Candidate cand : rCandidates) {
				System.out.print(cand.getFullName());
			}*/
		}
	}

}
