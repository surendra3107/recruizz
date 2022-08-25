package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.RoundDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class RoundControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(RoundControllerTest.class);

	String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	// Test case for adding candidates into source round.
	@Test
	public void sourceCandidate() throws Exception {

		List<String> candidateList = new ArrayList<String>();
		candidateList.add("sajin@beyondbytes.co.in");

		CandidateToRoundDTO roundCandidate = new CandidateToRoundDTO();
		roundCandidate.setCandidateEmailList(candidateList);
		roundCandidate.setRoundId("1");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundCandidate);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/round/candidate/source").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void sourceCandidateFromOutside() throws Exception {

		List<String> candidateList = new ArrayList<String>();
		candidateList.add("sourav@beyondbytes.co");

		CandidateToRoundDTO roundCandidate = new CandidateToRoundDTO();
		roundCandidate.setCandidateEmailList(candidateList);
		roundCandidate.setPositionCode("cp_4");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundCandidate);
		logger.debug("\n" + requestJson);
		System.out.println(requestJson);

		mockMvc.perform(post("/api/v1/round/candidate/source").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson).param("sourceMode", GlobalConstants.SOURCE_MODE_OUTSIDE +"e"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	// Test case for updating all rounds.
	@Test
	public void saveRound() throws Exception {

		RoundDTO round1 = new RoundDTO();
		round1.setRoundId("1");
		round1.setRoundName("Round 1");
		round1.setRoundType("Source");

		RoundDTO round2 = new RoundDTO();
		round2.setRoundId("2");
		round2.setRoundName("Round 2");
		round2.setRoundType("Shortlist");

		RoundDTO round3 = new RoundDTO();
		round3.setRoundName("Final");
		round3.setRoundType("Final");

		List<RoundDTO> roundsToAdd = new ArrayList<RoundDTO>();
		roundsToAdd.add(round1);
		roundsToAdd.add(round2);
		roundsToAdd.add(round3);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundsToAdd);
		logger.error("\n" + requestJson);

		mockMvc.perform(put("/api/v1/board/1/round").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)).andExpect(status().isOk())
				.andDo(print());
	}

	// Test case for changing status of candidates.
	@Test
	public void changeRoundCandidateStatus() throws Exception {

		List<String> candidateList = new ArrayList<String>();
		candidateList.add("cad2@freeware.com");

		CandidateToRoundDTO roundCandidate = new CandidateToRoundDTO();
		roundCandidate.setCandidateEmailList(candidateList);
		roundCandidate.setPositionCode("SWE_05");
		roundCandidate.setStatus(BoardStatus.OnHold.toString());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundCandidate);
		logger.error("\n" + requestJson);

		mockMvc.perform(put("/api/v1/round/candidate/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	// Test case for move candidates from one round to another.
	@Test
	public void moveCandidate() throws Exception {

		List<String> candidateList = new ArrayList<String>();
		candidateList.add("cad2@freeware.com");

		CandidateToRoundDTO roundCandidate = new CandidateToRoundDTO();
		roundCandidate.setCandidateEmailList(candidateList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundCandidate);
		logger.error("\n" + requestJson);

		mockMvc.perform(put("/api/v1/round/candidate/move").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson).param("destRoundId", "2"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	// Test case for deleting round.
	@Test
	public void deleteRound() throws Exception {
		mockMvc.perform(delete("/api/v1/board/1/round/2").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	// Test case for deleting candidates from rounds.
	@Test
	public void deleteRoundCandidate() throws Exception {

		CandidateToRoundDTO candidates = new CandidateToRoundDTO();
		candidates.getCandidateEmailList().add("sourav@beyondbytes.co.in");
		candidates.setPositionCode("SWE_05");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidates);
		logger.error("\n" + requestJson);

		mockMvc.perform(delete("/api/v1/round/candidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
