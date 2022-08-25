package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.rest.dto.models.PricingPlanDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class PlutusControllerTest extends RecruizWebBaseApplicationTests {
	String xauthToken = null;

	private static Logger logger = LoggerFactory.getLogger(PlutusController.class);

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void updateOrgSettings() throws Exception {

		PricingPlanDTO planDTO = new PricingPlanDTO();
		planDTO.setPricingPlanId("1");
		planDTO.setPlanName("Demo plan");
		planDTO.setFeatureMap(
				"oKVjZnDoJF5WGwN1iIyFjd8d1QHy_NguKqrh7wQrpmOe2vG12zMXLQr2UTLD3Gdn9HUnIWcV_xiJ5reHDmF-Sjiba8KOPgeyK2l67UmEHvSc7JktSJNMwRdgiJ8s9EFd-MlR5a3W7xhdHs42uRS1at3TIhXYwyykGKvnAdYT1Ibv0bmnP_u-IWBfXKa8sqV2Ivdi9HBmrwEsShJpRNIK6AZa0rOj1WX7pFIkjOE9uaN5pK2Ck7O7BdIg7zi-_wPBnLUKgUu7Awh6fITqadThQ8vK29iJJZRPbWLwrqmgWmdAylPLTIfRp3uHvaX4JB9UIrCz8VhgvMBtCBU5ckB_8g");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(planDTO);
		logger.error("\n" + requestJson);

		// String settings =
		// "oKVjZnDoJF5WGwN1iIyFjd8d1QHy_NguKqrh7wQrpmOe2vG12zMXLQr2UTLD3Gdn9HUnIWcV_xiJ5reHDmF-Sjiba8KOPgeyK2l67UmEHvSc7JktSJNMwRdgiJ8s9EFd-MlR5a3W7xhdHs42uRS1at3TIhXYwyykGKvnAdYT1Ibv0bmnP_u-IWBfXKa8sqV2Ivdi9HBmrwEsShJpRNIK6AZa0rOj1WX7pFIkjOE9uaN5pK2Ck7O7BdIg7zi-_wPBnLUKgUu7Awh6fITqadThQ8vK29iJJZRPbWLwrqmgWmdAylPLTIfRp3uHvaX4JB9UIrCz8VhgvMBtCBU5ckB_8g";
		mockMvc.perform(put("/auth/plutus/api/v1/org/settings").contentType(APPLICATION_JSON_UTF8).content(requestJson).param("orgId",
				"Beyond_bytes")).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void getCurrentOrgInfo() throws Exception {
		mockMvc.perform(get("/api/v1/plutus/billing/info").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void getInvoices() throws Exception {
		mockMvc.perform(get("/api/v1/org/invoice").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void getCustomerInfoFromPlutus() throws Exception {
		mockMvc.perform(get("/api/v1/plutus/org/billing/info").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void getInvoiceFile() throws Exception {
		mockMvc.perform(get("/api/v1/org/invoice/download/8").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}
}
