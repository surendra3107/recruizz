package com.bbytes.recruiz.service;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseSearchService;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseAdvanceSearchRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseGrouptResultResponse;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class TestSixthSenseAdvanceSearchService extends RecruizBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(TestSixthSenseAdvanceSearchService.class);

	@Autowired
	private SixthSenseSearchService sixthSenseService;

	String tenantId = "Service_Max";

	Organization org;

	User testUser;

	@Test
	public void testKeywordAdvancedSearch() throws Exception {
		TenantContextHolder.setTenant(tenantId);

		SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest = new SixthSenseAdvanceSearchRequest();
		sixthSenseAdvanceSearchRequest.setSources("monster,naukri");
		sixthSenseAdvanceSearchRequest.setPageNo(0);
		sixthSenseAdvanceSearchRequest.setBooleanSearchKeyword("java or ruby not angular");
		sixthSenseAdvanceSearchRequest.setTotalExperince("2-5");
		sixthSenseAdvanceSearchRequest.setCurrentLocation("TC0008");
		sixthSenseAdvanceSearchRequest.setUniversity("Pune");
		sixthSenseAdvanceSearchRequest.setJobStatus("2");
		sixthSenseAdvanceSearchRequest.setJobType("2");
		sixthSenseAdvanceSearchRequest.setDesignation("Software Developer");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(sixthSenseAdvanceSearchRequest);

		logger.error("\n" + requestJson);
		SixthSenseGrouptResultResponse response = sixthSenseService.getSearchResult(sixthSenseAdvanceSearchRequest, 0);
		Assert.assertNotNull(response);
	}

}
