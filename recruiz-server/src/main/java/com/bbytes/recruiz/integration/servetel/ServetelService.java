package com.bbytes.recruiz.integration.servetel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.KnowlarityCallDetailLogs;
import com.bbytes.recruiz.domain.KnowlarityCallDetails;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.ServetelAgent;
import com.bbytes.recruiz.domain.ServetelCallDetails;
import com.bbytes.recruiz.domain.ServetelIntegration;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.rest.dto.models.KnowlarityCallLogsActivity;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.UserService;
import com.google.common.base.Strings;

@Service
public class ServetelService {

	private static final Logger logger = LoggerFactory.getLogger(ServetelService.class);

	@Autowired
	ServetelIntegrationService servetelIntegrationService;

	@Autowired
	ServetelAgentService servetelAgentService;

	@Autowired
	CandidateActivityService candidateActivityService;

	@Autowired
	ServetelCallDetailsService servetelCallDetailsService;

	@Autowired
	OrganizationService organizationService;

	@Autowired
	UserService userService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	ServetelRecruizClient servetelRecruizClient;

	public RestResponse addServetelIntegration(ServetelIntegrationDto servetelIntegrationDto) {

		try {

			ServetelIntegration servetelIntegration = new ServetelIntegration();
			Organization org = organizationService.getOrgInfo();

			if (org != null) {
				ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());

				if (servetel != null)
					return new RestResponse(RestResponse.FAILED,
							"servetel already integrated with " + servetel.getOrganizationId());

				ServetelLoginEntityResponse result = servetelRecruizClient
						.servetelLogin(servetelIntegrationDto.getLoginId(), servetelIntegrationDto.getPassword());

				if (result.success) {
					servetelIntegration.setLoginId(servetelIntegrationDto.getLoginId());
					servetelIntegration.setPassword(servetelIntegrationDto.getPassword());
					servetelIntegration.setProductId(servetelIntegrationDto.getProductId());
					servetelIntegration.setOrganizationId(org.getOrgId());
					servetelIntegration.setToken(result.access_token);
					servetelIntegration.setStatus("Active");

					servetelIntegrationService.save(servetelIntegration);
				} else {
					return new RestResponse(RestResponse.FAILED, result.message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "Servetel integrated successfully !");

	}

	public RestResponse addServetelAgent(AddServetelAgentDto addServetelAgentDto) {

		try {
			ServetelAgent servetelAgent = new ServetelAgent();
			Organization org = organizationService.getOrgInfo();

			if (org != null) {
				ServetelAgent agent = servetelAgentService.findByOrganizationIdAndUserId(org.getOrgId(),
						Long.parseLong(addServetelAgentDto.getUserId()));

				if (agent != null)
					return new RestResponse(RestResponse.FAILED, "agent already exists.. ");

				User user = userService.findOne(Long.valueOf(addServetelAgentDto.getUserId()));
				ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());
				if (servetel == null)
					return new RestResponse(RestResponse.FAILED,
							"servetel not integrated with " + servetel.getOrganizationId());

				if (user.getMobile() == null || user.getMobile().trim().equalsIgnoreCase(""))
					return new RestResponse(RestResponse.FAILED, "Please update mobile number on your profile.. ");

				if (Strings.isNullOrEmpty(servetel.getProductId()))
					new RestResponse(RestResponse.FAILED,"productId is missing..");
				
				ServetelEntityResponse result = servetelRecruizClient.addServetelAgent(user.getName(), user.getMobile(),
						servetel.getToken(),servetel.getProductId());
				if (!result.success) {
					ServetelLoginEntityResponse res = servetelRecruizClient.servetelLogin(servetel.getLoginId(),
							servetel.getPassword());
					if (res.success) {
						servetel.setToken(res.access_token);
						servetel = servetelIntegrationService.save(servetel);
						result = servetelRecruizClient.addServetelAgent(user.getName(), user.getMobile(),
								servetel.getToken(),servetel.getProductId());
						if (!result.success) {
							return new RestResponse(RestResponse.FAILED, result.message);
						}
					} else {
						return new RestResponse(RestResponse.FAILED, result.message);
					}
				}

				servetelAgent.setAgentName(user.getName());
				servetelAgent.setAgentId(result.getAgent_id());
				servetelAgent.setMobile(user.getMobile());
				;
				servetelAgent.setOrganizationId(org.getOrgId());
				servetelAgent.setUserId(user.getUserId());
				servetelAgent.setStatus("Active");

				servetelAgentService.save(servetelAgent);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "agent added successfully !");

	}

	public RestResponse updateServetelAgent(AddServetelAgentDto addServetelAgentDto) {
		// TODO Auto-generated method stub
		try {

			ServetelAgent agent = servetelAgentService.findOne(Long.valueOf(addServetelAgentDto.getAgentId()));

			if (agent == null)
				return new RestResponse(RestResponse.FAILED, "agent details not found.. ");

			// to do add agent in servetel

			agent.setAgentName(addServetelAgentDto.getAgentName());
			;
			agent.setMobile(addServetelAgentDto.getMobile());
			;

			servetelAgentService.save(agent);

		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "agent updated successfully !");
	}

	public RestResponse deleteServetelAgent(Long id) {
		// TODO Auto-generated method stub
		try {
			ServetelAgent agent = servetelAgentService.findOne(id);
			if (agent == null)
				return new RestResponse(RestResponse.FAILED, "agent details not found.. ");

			Organization org = organizationService.getOrgInfo();

			ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());
			if (servetel == null)
				return new RestResponse(RestResponse.FAILED, "servetel not integrated");

			ServetelEntityResponse result = servetelRecruizClient.deleteServetelAgent(agent.getAgentId(),
					servetel.getToken());
			if (!result.success) {
				ServetelLoginEntityResponse res = servetelRecruizClient.servetelLogin(servetel.getLoginId(),
						servetel.getPassword());
				if (res.success) {
					servetel.setToken(res.access_token);
					servetel = servetelIntegrationService.save(servetel);
					result = servetelRecruizClient.deleteServetelAgent(agent.getAgentId(), servetel.getToken());
					if (!result.success) {
						return new RestResponse(RestResponse.FAILED, result.message);
					}
				} else {
					return new RestResponse(RestResponse.FAILED, result.message);
				}
			}
			servetelAgentService.delete(id);

		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "agent deleted successfully !");
	}

	public RestResponse getAllServetelAgents() {
		try {
			Organization org = organizationService.getOrgInfo();
			List<ServetelAgent> agents = servetelAgentService.findAllByOrganizationId(org.getOrgId());
			return new RestResponse(RestResponse.SUCCESS, agents);
		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
	}

	public RestResponse clickToCall(Long candidateId) throws IOException, URISyntaxException {

		try {
			Candidate candidate = candidateService.findOne(candidateId);

			if (candidate == null)
				return new RestResponse(RestResponse.FAILED, "Candidate details not found");

			if (candidate.getMobile() == null)
				return new RestResponse(RestResponse.FAILED, "Candidate Mobile number not found");

			Organization org = organizationService.getOrgInfo();
			ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());
			if (servetel == null)
				return new RestResponse(RestResponse.FAILED, "servetel not integrated with " + org.getOrgId());

			User user = userService.getLoggedInUserObject();
			/*ServetelAgent agent = servetelAgentService.findByOrganizationIdAndUserId(org.getOrgId(), user.getUserId());

			if (agent == null) {
				AddServetelAgentDto agentDto = new AddServetelAgentDto();
				agentDto.setUserId(String.valueOf(user.getUserId()));
				RestResponse response = addServetelAgent(agentDto);
				if (!response.isSuccess())
					return response;
				agent = servetelAgentService.findByOrganizationIdAndUserId(org.getOrgId(), user.getUserId());
			}
*/
			ServetelCallDetails details = new ServetelCallDetails();
			details.setAgentId(Long.valueOf(user.getMobile()));
			details.setCandiateMobile(candidate.getMobile());
			details.setAgentMobile(user.getMobile());
			details.setCandidateId(candidateId);
			details.setOrganizationId(org.getOrgId());
			details.setUserId(user.getUserId());
			details.setCallStatus("pending");
			details.setDuration("0");
			details.setRecordingUrl("");
			details = servetelCallDetailsService.save(details);

			logger.error("Click to call ====== step 1"+servetel.getToken());
			ServetelEntityResponse result = servetelRecruizClient.clickToCall(user.getMobile(), candidate.getMobile(),
					servetel.getToken(), String.valueOf(details.getId()));
			if (!result.success) {
				logger.error("Click to call ====== step 4   ");
				ServetelLoginEntityResponse res = servetelRecruizClient.servetelLogin(servetel.getLoginId(),
						servetel.getPassword());
				logger.error("Click to call ====== step 5   login iid = "+servetel.getLoginId()+"  password = "+servetel.getPassword());
				if (res.success) {
					servetel.setToken(res.access_token);
					servetel = servetelIntegrationService.save(servetel);
					result = servetelRecruizClient.clickToCall(user.getMobile(), candidate.getMobile(),
							servetel.getToken(), String.valueOf(details.getId()));
					if (!result.success) {
						logger.error("Click to call ====== step 6   ");

						servetelCallDetailsService.delete(details.getId());
						return new RestResponse(RestResponse.FAILED, result.message);
					}

				} else {
					logger.error("Click to call ====== step 7   ");
					servetelCallDetailsService.delete(details.getId());
					return new RestResponse(RestResponse.FAILED, result.message);
				}
			}

			logger.error("Click to call ====== step 8   ");
			candidateActivityService.addActivityForCallLog(
					userService.getLoggedInUserObject().getName() + "(" + userService.getLoggedInUserObject().getEmail()
							+ "/" + userService.getLoggedInUserObject().getMobile() + ") initiated a call to "
							+ candidate.getFullName() + "(" + candidate.getEmail() + "/" + candidate.getMobile() + ")",
					userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
					details.getCandidateId() + "", CandidateActivityType.CALL_LOG.getDisplayName(),
					String.valueOf(details.getId()), "servetel");

		} catch (Exception e) {
			
			logger.error("clickToCall = " + e);
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		logger.error("Click to call ====== step 9   ");
		return new RestResponse(RestResponse.SUCCESS, "");
	}

	public RestResponse deleteServetelIntegration(String orgId) {

		try {
			ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(orgId);
			if (servetel == null)
				return new RestResponse(RestResponse.FAILED, "servetel not integrated with " + orgId);

			servetelIntegrationService.delete(servetel.getId());
		} catch (Exception e) {
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "Removed Servelet Integration Successfully");
	}

	public RestResponse getIntegrationDetails() {

		Organization org = organizationService.getOrgInfo();

		if (org.getIvrCallingIntegration() == null)
			return new RestResponse(RestResponse.SUCCESS, "Not found any details");

		return new RestResponse(RestResponse.SUCCESS, org.getIvrCallingIntegration());
	}

	public RestResponse getServetelLoginDetails() {

		Organization org = organizationService.getOrgInfo();
		ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());
		if (servetel == null)
			return new RestResponse(RestResponse.FAILED, "servetel not integrated with " + org.getOrgId());

		return new RestResponse(RestResponse.SUCCESS, "servetel integrated with " + org.getOrgId());
	}

	public void updateCallActivityDetails(ServetelCallDetailReponseDto dto) {

		String agentMobile = dto.getAnswered_agent().toString().split("number=")[1].split("}")[0].trim().substring(3);
		String candidateMobile = dto.getCall_to_number().substring(2);

		try {
			ServetelCallDetails servetelCallDetails = servetelCallDetailsService
					.getCallDetailsByAgentMobileAndCandidateMobile(agentMobile, candidateMobile);
			if (servetelCallDetails != null) {
				servetelCallDetails.setCallStatus(dto.getCall_status());
				servetelCallDetails.setDuration(dto.getDuration());
				servetelCallDetails.setRecordingUrl(dto.getRecording_url());
				servetelCallDetailsService.save(servetelCallDetails);
			}
		} catch (Exception e) {
			logger.error("" + e);
		}

	}

	public RestResponse getCallActivity(long id) {

		ServetelCallDetails servetelCallDetails = servetelCallDetailsService.findOne(id);

		if (servetelCallDetails == null)
			return new RestResponse(RestResponse.FAILED, "Details not available !");

		KnowlarityCallLogsActivity data = new KnowlarityCallLogsActivity();
		Candidate candidate = candidateService.findOne(servetelCallDetails.getCandidateId());
		User user = userService.findOne(servetelCallDetails.getUserId());
		data.setCallerMobileNumber(servetelCallDetails.getAgentMobile());
		data.setCallStatus(servetelCallDetails.getCallStatus());
		data.setCandidateEmail(candidate.getEmail());
		data.setInitiatedBy(user.getEmail());
		data.setCandidateMobile(servetelCallDetails.getCandiateMobile());
		data.setCallerAltMobileNumber(candidate.getAlternateMobile());
		data.setCallLogStatus(servetelCallDetails.getCallStatus());
		data.setCallRecordingUrl(servetelCallDetails.getRecordingUrl());
		data.setDurationOfCall(servetelCallDetails.getDuration());

		if (servetelCallDetails.getCallStatus().equalsIgnoreCase("pending")) {
			data.setCallStatus("Missed by Agent");
			data.setCallLogStatus("Missed by Agent");
		}

		return new RestResponse(RestResponse.SUCCESS, data);

	}

	public void updateServetelAgentMobile(User user, Organization org) {

		ServetelAgent agent = servetelAgentService.findByOrganizationIdAndUserId(org.getOrgId(), user.getUserId());
		ServetelIntegration servetel = servetelIntegrationService.findByOrganizationId(org.getOrgId());
		if (agent != null) {
			try {
				String agentId = agent.getAgentId();
				try {
					servetelRecruizClient.updateServeletAgent(agentId, user.getMobile(), user.getName(),
							servetel.getToken());
				} catch (Exception e) {
					ServetelLoginEntityResponse res = servetelRecruizClient.servetelLogin(servetel.getLoginId(),
							servetel.getPassword());
					if (res.success) {
						servetel.setToken(res.access_token);
						servetel = servetelIntegrationService.save(servetel);
						servetelRecruizClient.updateServeletAgent(agentId, user.getMobile(), user.getName(),
								servetel.getToken());

					}
				}
				agent.setMobile(user.getMobile());
				servetelAgentService.save(agent);
			} catch (Exception ex) {
				System.out.println(""+ex);
			}
		}
	}

}
