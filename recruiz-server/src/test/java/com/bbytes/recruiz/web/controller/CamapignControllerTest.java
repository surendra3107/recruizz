package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.enums.CampaignType;
import com.bbytes.recruiz.rest.dto.models.CampaignCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.CampaignDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CamapignControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void getCampaignType() throws Exception {
		mockMvc.perform(get("/api/v1/campaign/type").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getAllCampaign() throws Exception {
		mockMvc.perform(get("/api/v1/campaign/all").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void changeCampaignStatus() throws Exception {
		mockMvc.perform(get("/api/v1/campaign/status/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", "completed"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void runCampaign() throws Exception {
		mockMvc.perform(put("/api/v1/campaign/run/2").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getCamaignStat() throws Exception {
		mockMvc.perform(get("/api/v1/campaign/stat/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void addNewCampaign() throws Exception {

		CampaignDTO dto = new CampaignDTO();
		dto.setName("Camp 11");
		dto.setClientId(1L);
		dto.setPositionCode("a_1");
		dto.setCampaignRenderedSubject("Camapin subject");
		dto.setType(CampaignType.EmailReachOut.getDisplayName());
		dto.setCampaignRenderedTemplate("This is campaign template");
		dto.setCampaignStartDate(new Date());

		CampaignCandidateDTO candidateDTO = new CampaignCandidateDTO();
		candidateDTO.setMemberEmailId("dhdh@hdhd.in");
		candidateDTO.setMemberName("Sourav Kumar");

		List<CampaignCandidateDTO> candidateList = new ArrayList<>();
		candidateList.add(candidateDTO);

		Set<String> hrEmail = new HashSet<>();
		hrEmail.add("sourav@beyondbytes.co.in");

		dto.setCampaignCandidates(candidateList);

		dto.setCampaignHrMembersEmail(hrEmail);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(dto);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/campaign/new").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void EditCampaign() throws Exception {

		CampaignDTO dto = new CampaignDTO();
		dto.setName("Edited Camp 1");
		dto.setClientId(1L);
		dto.setPositionCode("j_1");
		dto.setCampaignRenderedSubject("Camapign subject edited");
		dto.setType(CampaignType.EmailReachOut.getDisplayName());
		dto.setCampaignRenderedTemplate("This is eited campaign template");
		dto.setCampaignStartDate(new Date());

		CampaignCandidateDTO candidateDTO = new CampaignCandidateDTO();
		candidateDTO.setMemberEmailId("sourav@beyondbytes.co.in");
		candidateDTO.setMemberName("Sourav Kumar");

		List<CampaignCandidateDTO> candidateList = new ArrayList<>();
		candidateList.add(candidateDTO);

		dto.setCampaignCandidates(candidateList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(dto);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/campaign/edit/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getCampaignStat() throws Exception {
		mockMvc.perform(get("/api/v1/campaign/stat/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getCampaignTemplates() throws Exception {
		mockMvc.perform(get("/api/v1/admin/template/campaign").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getRenderedCampaignTemplates() throws Exception {
		mockMvc.perform(get("/api/v1/template/campaign/rendered").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("templateName", "position"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void deleteCampaign() throws Exception {
		mockMvc.perform(delete("/api/v1/campaign/delete/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
}
