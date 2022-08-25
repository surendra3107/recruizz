package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.rest.dto.models.CandidateSearchDTO;
import com.bbytes.recruiz.rest.dto.models.ClientSearchDTO;
import com.bbytes.recruiz.rest.dto.models.PositionSearchDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * This Test class only runs and give result for Dev or Prod Mode elastic
 * search. Test mode wont work. For running this test cases, you need to start
 * actual elastic search service.
 * 
 * @author akshay
 *
 */
public class SearchControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void testGetGlobalSearchResult() throws Exception {

		mockMvc.perform(get("/api/v1/search/global").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("searchText", "Developer in Bangalore"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedPositionLocation() throws Exception {

		mockMvc.perform(get("/api/v1/search/position/location").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("location", "Kol"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedPositionTitle() throws Exception {

		mockMvc.perform(get("/api/v1/search/position/title").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("title", "J")).andExpect(status().isOk())
				.andDo(print());

	}

	@Test
	public void testGetSuggestedPositionSkill() throws Exception {

		mockMvc.perform(get("/api/v1/search/position/skill").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("skill", "node"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedcandidateSkill() throws Exception {

		mockMvc.perform(get("/api/v1/search/candidate/skill").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("skill", "java"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedCandidatePrefLocation() throws Exception {

		mockMvc.perform(get("/api/v1/search/candidate/preflocation").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("prefLocation", "p"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedCandidateCurrentLocation() throws Exception {

		mockMvc.perform(get("/api/v1/search/candidate/currentlocation").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("currentLocation", "ban"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedClientLocation() throws Exception {

		mockMvc.perform(get("/api/v1/search/client/location").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("location", "ban"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedCandidateCurrentCompany() throws Exception {

		mockMvc.perform(get("/api/v1/search/candidate/company").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("company", "inf"))
				.andExpect(status().isOk()).andDo(print());

	}

	@Test
	public void testGetSuggestedClientName() throws Exception {

		mockMvc.perform(get("/api/v1/search/client/name").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("name", "f")).andExpect(status().isOk())
				.andDo(print());

	}

	@Test
	public void testGetSuggestedClientList() throws Exception {

		String[] nameList = { "Finserv" };
		String[] locationList = { "Bangalore" };

		ClientSearchDTO clientSearchDTO = new ClientSearchDTO();
		clientSearchDTO.setNameList(nameList);
		clientSearchDTO.setLocationList(locationList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(clientSearchDTO);

		mockMvc.perform(post("/api/v1/search/client").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void testGetSuggestedPostionList() throws Exception {

		String[] skills = { "Node" };
		String[] locationList = { "Kolkatta" };
		String[] typeList = { "Payroll" };

		PositionSearchDTO positionSearchDTO = new PositionSearchDTO();
		positionSearchDTO.setLocationList(locationList);
		positionSearchDTO.setSkills(skills);
		positionSearchDTO.setTypeList(typeList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(positionSearchDTO);

		mockMvc.perform(post("/api/v1/search/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void testGetSuggestedCandidateList() throws Exception {

		String[] currentLocationList = { "Pune" };
		String[] prefLocationList = { "Chennai" };
		String[] typeList = { "FullTime" };

		CandidateSearchDTO candidateSearchDTO = new CandidateSearchDTO();
		candidateSearchDTO.setCurrentLocationList(currentLocationList);
		candidateSearchDTO.setPreferredLocationList(prefLocationList);
		candidateSearchDTO.setEmpTypeList(typeList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidateSearchDTO);

		mockMvc.perform(post("/api/v1/search/candidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}
}
