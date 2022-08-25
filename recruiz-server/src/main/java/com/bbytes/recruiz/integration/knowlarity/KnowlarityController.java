package com.bbytes.recruiz.integration.knowlarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.google.common.base.Strings;

@RestController
public class KnowlarityController {

	private static final Logger logger = LoggerFactory.getLogger(KnowlarityController.class);

	@Autowired
	KnowlarityService knowlarityService;

	/**
	 * @author Narinder_Tanwar
	 * @param knowlarityIntegrationDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/knowlarity/setting/addIntegration", method = RequestMethod.POST)
	public RestResponse addKnowlarityIntegration(@RequestBody KnowlarityIntegrationDto knowlarityIntegrationDto) throws RecruizException {

		if (Strings.isNullOrEmpty(knowlarityIntegrationDto.getAuthorizationKey())
			    || Strings.isNullOrEmpty(knowlarityIntegrationDto.getCallerId())
				   || Strings.isNullOrEmpty(knowlarityIntegrationDto.getSrNumber())
				       || Strings.isNullOrEmpty(knowlarityIntegrationDto.getXApikey()))
			new RestResponse(RestResponse.SUCCESS,"some field value is missing..");


		return knowlarityService.addKnowlarityIntegration(knowlarityIntegrationDto);

	}
	
	/**
	 * @author Narinder_Tanwar
	 * @param tenantName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/knowlarity/integration/getByTenantName", method = RequestMethod.GET)
	public RestResponse getKnowlarityIntegrationByTenantName(@RequestParam(value = "tenantName") String tenantName) throws RecruizException {

		if (Strings.isNullOrEmpty(tenantName))
			    return new RestResponse(RestResponse.SUCCESS,"tenant name required !");

		return knowlarityService.getKnowlarityIntegrationByTenantName(tenantName);

	}
	
	/**
	 * @author Narinder_Tanwar
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/knowlarity/integration/deleteKnowlarityIntegration", method = RequestMethod.GET)
	public RestResponse deleteKnowlarityIntegration(@RequestParam(value = "id") long id) throws RecruizException {

		return knowlarityService.deleteKnowlarityIntegration(id);

	}

	/**
	 * @author Narinder_Tanwar
	 * @param knowlarityIntegrationDto
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/knowlarity/call/makeCall", method = RequestMethod.POST)
	public RestResponse makeCall(@RequestBody KnowlarityCallDto knowlarityCallDto) throws RecruizException {

		if (Strings.isNullOrEmpty(knowlarityCallDto.getCandidateEmail())
			    || Strings.isNullOrEmpty(knowlarityCallDto.getCandidateMobile())
				   || Strings.isNullOrEmpty(knowlarityCallDto.getCandidateName()))
			new RestResponse(RestResponse.SUCCESS,"some field value is missing..");


		return knowlarityService.makeCall(knowlarityCallDto);

	}
	
	
	@RequestMapping(value = "/api/v1/knowlarity/activity/callActivity", method = RequestMethod.POST)
	public RestResponse callActivity(@RequestParam(value = "id") long id,@RequestParam(value = "ivr") String ivr) throws RecruizException {

		return knowlarityService.callActivity(id,ivr);

	}

	
	@RequestMapping(value = "/api/v1/knowlarity/call/prospects/makeCall", method = RequestMethod.POST)
	public RestResponse makeCallForProspects(@RequestBody KnowlarityCallDto knowlarityCallDto) throws RecruizException {

		if (Strings.isNullOrEmpty(knowlarityCallDto.getCandidateEmail())
			    || Strings.isNullOrEmpty(knowlarityCallDto.getCandidateMobile())
				   || Strings.isNullOrEmpty(knowlarityCallDto.getCandidateName()))
			new RestResponse(RestResponse.SUCCESS,"some field value is missing..");


		return knowlarityService.makeCallForProspects(knowlarityCallDto);

	}
	
}
