package com.bbytes.recruiz.integration.servetel;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.knowlarity.KnowlarityIntegrationDto;
import com.bbytes.recruiz.integration.knowlarity.KnowlarityService;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.google.common.base.Strings;

@RestController
public class ServetelController {
	
	private static final Logger logger = LoggerFactory.getLogger(ServetelController.class);

	@Autowired
	ServetelService servetelService;

	/**
	 * @author Narinder_Tanwar
	 * @param ServetelIntegrationDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/addIntegration", method = RequestMethod.POST)
	public RestResponse addServetelIntegration(@RequestBody ServetelIntegrationDto servetelIntegrationDto) throws RecruizException {

		if (Strings.isNullOrEmpty(servetelIntegrationDto.getLoginId())
			    || Strings.isNullOrEmpty(servetelIntegrationDto.getPassword()))
			new RestResponse(RestResponse.FAILED,"loginId OR password is missing..");
		
		if (Strings.isNullOrEmpty(servetelIntegrationDto.getProductId()))
			new RestResponse(RestResponse.FAILED,"productId is missing..");

		return servetelService.addServetelIntegration(servetelIntegrationDto);

	}


	/**
	 * @author Narinder_Tanwar
	 * @param 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/getIntegrationDetails", method = RequestMethod.GET)
	public RestResponse getIntegrationDetails() throws RecruizException {

		return servetelService.getIntegrationDetails();
	}
	
	/**
	 * @author Narinder_Tanwar
	 * @param 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/getServetelLoginDetails", method = RequestMethod.GET)
	public RestResponse getServetelLoginDetails() throws RecruizException {

		return servetelService.getServetelLoginDetails();
	}
	
	
	
	
	/**
	 * @author Narinder_Tanwar
	 * @param ServetelIntegrationDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/deleteIntegration", method = RequestMethod.GET)
	public RestResponse deleteServetelIntegration(@RequestParam(value = "orgId") String orgId) throws RecruizException {

		if (Strings.isNullOrEmpty(orgId))
			new RestResponse(RestResponse.FAILED,"orgId is missing..");

		return servetelService.deleteServetelIntegration(orgId);
	}
	
	
	/**
	 * @author Narinder_Tanwar
	 * @param addServetelAgentDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/addServetelAgent", method = RequestMethod.POST)
	public RestResponse addServetelAgent(@RequestBody AddServetelAgentDto addServetelAgentDto) throws RecruizException {

		if (Strings.isNullOrEmpty(addServetelAgentDto.getUserId()))
			new RestResponse(RestResponse.FAILED,"userId is missing..");

		return servetelService.addServetelAgent(addServetelAgentDto);

	}
	
	
	/**
	 * @author Narinder_Tanwar
	 * @param addServetelAgentDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/updateServetelAgent", method = RequestMethod.POST)
	public RestResponse updateServetelAgent(@RequestBody AddServetelAgentDto addServetelAgentDto) throws RecruizException {

		if (Strings.isNullOrEmpty(addServetelAgentDto.getAgentName())
			    || Strings.isNullOrEmpty(addServetelAgentDto.getMobile())
			    || Strings.isNullOrEmpty(addServetelAgentDto.getAgentId()))
			new RestResponse(RestResponse.FAILED,"name OR mobile OR agentID is missing..");

		return servetelService.updateServetelAgent(addServetelAgentDto);

	}

    /**
     * @author Narinder_Tanwar
     * @param id
     * @return
     * @throws RecruizException
     */
	@RequestMapping(value = "/api/v1/servetel/setting/deleteServetelAgent", method = RequestMethod.GET)
	public RestResponse deleteServetelAgent(@RequestParam(value = "id") Long id) throws RecruizException {

		return servetelService.deleteServetelAgent(id);

	}

	/**
     * @author Narinder_Tanwar
     * @return
     * @throws RecruizException
     */
	@RequestMapping(value = "/api/v1/servetel/setting/getAllServetelAgents", method = RequestMethod.GET)
	public RestResponse getAllServetelAgents() throws RecruizException {

		return servetelService.getAllServetelAgents();

	}


	/**
	 * @author Narinder_Tanwar
	 * @param candidateMobile
	 * @return
	 * @throws RecruizException
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	@RequestMapping(value = "/api/v1/servetel/setting/clickToCall", method = RequestMethod.GET)
	public RestResponse clickToCall(@RequestParam(value = "candidateId") String candidateId) throws RecruizException, NumberFormatException, IOException, URISyntaxException {

		if (Strings.isNullOrEmpty(candidateId))
			new RestResponse(RestResponse.FAILED,"candidateId is missing..");

		return servetelService.clickToCall(Long.parseLong(candidateId));

	}
	
}
