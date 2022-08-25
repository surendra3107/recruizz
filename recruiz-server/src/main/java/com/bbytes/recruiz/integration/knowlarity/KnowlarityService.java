package com.bbytes.recruiz.integration.knowlarity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.KnowlarityCallDetailLogs;
import com.bbytes.recruiz.domain.KnowlarityCallDetails;
import com.bbytes.recruiz.domain.KnowlarityIntegration;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.integration.servetel.ServetelService;
import com.bbytes.recruiz.rest.dto.models.KnowlarityCallLogsActivity;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionActivityService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.ProspectActivityService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.google.api.client.repackaged.com.google.common.base.Objects;

@Service	
public class KnowlarityService {

	@Autowired
	KnowlarityIntegrationService knowlarityIntegrationService;

	@Autowired
	KnowlarityCallDetailService knowlarityCallDetailService;

	@Autowired
	OrganizationService organizationService;

	@Autowired
	UserService userService;
	
	@Autowired
	ServetelService servetelService;
	
	@Autowired
	ProspectActivityService prospectActivityService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	KnowlarityCallDetailLogService knowlarityCallDetailLogService;

	@Autowired
	CandidateActivityService candidateActivityService;

	@Autowired
	PositionActivityService positionActivityService;

	@Autowired
	PositionService positionService;

	public RestResponse addKnowlarityIntegration(KnowlarityIntegrationDto knowlarityIntegrationDto) {

		try{

			KnowlarityIntegration knowlarityIntegration = new KnowlarityIntegration();
			Organization org = organizationService.getOrgInfo();

			if(org!=null){
				KnowlarityIntegration ki = knowlarityIntegrationService.findByOrgName(org.getOrgId());

				if(ki!=null)
					return new RestResponse(RestResponse.FAILED, "knowlarity already integrated with "+ki.getOrganization_id());

				knowlarityIntegration.setSr_number(knowlarityIntegrationDto.getSrNumber());
				knowlarityIntegration.setAuthorization_key(knowlarityIntegrationDto.getAuthorizationKey());
				knowlarityIntegration.setCaller_id(knowlarityIntegrationDto.getCallerId());
				knowlarityIntegration.setOrganization_id(org.getOrgId());
				knowlarityIntegration.setXApi_key(knowlarityIntegrationDto.getXApikey());
				knowlarityIntegration.setStatus("Active");

				knowlarityIntegrationService.save(knowlarityIntegration);
			}

		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		return new RestResponse(RestResponse.SUCCESS, "knowlarity integrated successfully !");
	}

	public RestResponse getKnowlarityIntegrationByTenantName(String tenantName) {

		try{
			KnowlarityIntegration ki = knowlarityIntegrationService.findByOrgName(tenantName);

			if(ki==null)
				return new RestResponse(RestResponse.FAILED, "knowlarity integration not found !");

			return new RestResponse(RestResponse.SUCCESS, ki);

		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
	}

	public RestResponse deleteKnowlarityIntegration(long id) {

		try{
			KnowlarityIntegration ki = knowlarityIntegrationService.findOne(id);

			if(ki==null)
				return new RestResponse(RestResponse.FAILED, "knowlarity integration not found !");

			knowlarityIntegrationService.delete(id);
			return new RestResponse(RestResponse.SUCCESS,"deleted successfully !");

		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
	}

	public RestResponse makeCall(KnowlarityCallDto knowlarityCallDto) {

		KnowlarityIntegration knowlarityIntegration = null;
		try{
			Organization org = organizationService.getOrgInfo();
			Candidate candidate = candidateService.getCandidateByEmail(knowlarityCallDto.getCandidateEmail());

			if(candidate==null)
				return new RestResponse(RestResponse.FAILED, "candidate details not found !");
			
			if(org.getIvrCallingIntegration()==null || org.getIvrCallingIntegration().equalsIgnoreCase("none")){
				return new RestResponse(RestResponse.FAILED, "IVR Configuration details not found");
			}else{
				if(org.getIvrCallingIntegration().equalsIgnoreCase("servetel")){
					return servetelService.clickToCall(candidate.getCid());
				}
			}

			if(org!=null){
				knowlarityIntegration = knowlarityIntegrationService.findByOrgName(org.getOrgId());

				if(knowlarityIntegration==null)
					return new RestResponse(RestResponse.FAILED, "knowlarity not integrated with "+org.getOrgId());
			}

			
			User user = userService.getLoggedInUserObject();

			if(user!=null){

				String userMobile = user.getMobile();

				if(userMobile==null || userMobile.trim().equalsIgnoreCase(""))
					return new RestResponse(RestResponse.FAILED, "LoggedIn user mobile number does not exist.");


				String result = sendPOSTCall(GlobalConstants.KNOWLARITY_MAKECALL_URL,knowlarityIntegration.getSr_number(),
						userMobile,knowlarityCallDto.getCandidateMobile(),knowlarityIntegration.getCaller_id(),knowlarityIntegration.getAuthorization_key(),
						knowlarityIntegration.getXApi_key());

				String message ="";String call_id ="";

				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(result);

				if(result.contains("call_id")){
					JSONObject data = (JSONObject) json.get("success");
					message =  (String) data.get("message");
					call_id =  (String) data.get("call_id");


					KnowlarityCallDetails knowlarityCallDetails = new KnowlarityCallDetails(); 

					knowlarityCallDetails.setAgentMobile(userMobile);
					knowlarityCallDetails.setAgentEmail(user.getEmail());
					knowlarityCallDetails.setCall_id(call_id);
					knowlarityCallDetails.setCandidateEmail(knowlarityCallDto.getCandidateEmail());
					knowlarityCallDetails.setCandidateMobile(knowlarityCallDto.getCandidateMobile());
					knowlarityCallDetails.setCandidateName(knowlarityCallDto.getCandidateName());
					knowlarityCallDetails.setCandidateId(candidate.getCid());
					knowlarityCallDetails.setStatus(message);
					knowlarityCallDetails.setCall_logs_status("pending");

					knowlarityCallDetails = knowlarityCallDetailService.save(knowlarityCallDetails);

					candidateActivityService.addActivityForCallLog(userService.getLoggedInUserObject().getName()+"("+userService.getLoggedInUserObject().getEmail()+"/"+userService.getLoggedInUserObject().getMobile()+") initiated a call to "+candidate.getFullName()+"("+candidate.getEmail()+"/"+knowlarityCallDto.getCandidateMobile()+")",
							userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
							candidate.getCid() + "", CandidateActivityType.CALL_LOG.getDisplayName(),String.valueOf(knowlarityCallDetails.getId()),"knowlarity");		


					if(knowlarityCallDto.getPositionId()!=null && knowlarityCallDto.getPositionId().equals("")){

						Position position = positionService.findOne(Long.valueOf(knowlarityCallDto.getPositionId()));

						if(position!=null){
							PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
									candidate.getFullName()+" recieved call by "+userService.getLoggedInUserObject().getName(),
									candidate.getFullName()+" recieved call by "+userService.getLoggedInUserObject().getName(), new Date(), position.getPositionCode(),
									position.getTeam());
							positionActivity.setCandidateId(Long.valueOf(candidate.getCid()));
							positionActivity.setKnowlarityCallDetailId(String.valueOf(knowlarityCallDetails.getId()));
							positionActivityService.addActivity(positionActivity);
						}
					}


				}else if(result.contains("error")){
					JSONObject data = (JSONObject) json.get("error");
					message =  (String) data.get("message");
					return new RestResponse(RestResponse.FAILED, message); 
				}else if(result.contains("Forbidden")){
					message =  (String) json.get("message");
					return new RestResponse(RestResponse.FAILED, message);
				}

				return new RestResponse(RestResponse.SUCCESS, message);

			}
			return new RestResponse(RestResponse.FAILED, "user details not found !");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}

	}


	private static String sendPOSTCall(String url,String srNumber,String agentNumber,String customerNumber,String callerId,String authorization,String xkey) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(url);

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"k_number\":\"+91"+srNumber+"\",");
		json.append("\"agent_number\":\"+91"+agentNumber+"\",");
		json.append("\"customer_number\":\"+91"+customerNumber+"\",");
		json.append("\"caller_id\":\"+91"+callerId+"\"");
		json.append("}");


		post.addHeader("authorization", authorization);
		post.addHeader("x-api-key", xkey);
		post.addHeader("content-type", "application/json");


		// send a JSON data
		post.setEntity(new StringEntity(json.toString()));

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(post)) {

			result = EntityUtils.toString(response.getEntity());
		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}


	private static String getCallDetailLog(String url,String callId,String authorization,String xkey) throws IOException, URISyntaxException {

		String result = "";
		URIBuilder builder = new URIBuilder(url);
		builder.setParameter("uuid", callId);

		HttpGet post = new HttpGet(builder.build());

		post.addHeader("authorization", authorization);
		post.addHeader("x-api-key", xkey);
		post.addHeader("content-type", "application/json");


		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(post)) {

			result = EntityUtils.toString(response.getEntity());
		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}

	public void saveKnowlarityCallLogs(KnowlarityCallDetails knowlarityCallDetails, KnowlarityIntegration knowlarityIntegration) {

		try{
			String json = getCallDetailLog(GlobalConstants.KNOWLARITY_CALL_LOGS_URL,knowlarityCallDetails.getCall_id(),knowlarityIntegration.getAuthorization_key(),knowlarityIntegration.getXApi_key());

			KnowlarityCallDetailLogs logs = knowlarityCallDetailLogService.getByKnowlarityCallDetailId(knowlarityCallDetails.getId());
			if(logs==null)		
				logs = new KnowlarityCallDetailLogs(); 

			if((json.split("pickup_time")[1].split(",")[0].trim()).contains("null")){


				String posting_time = json.split("posting_time")[1].split(",")[0].trim().split("\"")[2].replace("\\", "").trim();

				String hangup_time = json.split("hangup_time")[1].split("\\[")[0].trim().split("\"")[2].replace("\\", "").trim();

				String outcall_pickup_time = json.split("outcall_pickup_time")[1].split("\"")[2].replace("\\", "").trim();

				logs.setPosting_time(posting_time);
				logs.setHangup_time(hangup_time);
				logs.setKnowlarityCallDetails_Id(knowlarityCallDetails.getId());
				logs.setOutcall_pickup_time(outcall_pickup_time);
				logs.setRec_timetaken("");
				logs.setRecordingurl_system("");
				logs.setStatus(GlobalConstants.KNOWLARITY_AGENT_MISSED_CALL);

			}else if(!json.contains("rec_timetaken")){

				String posting_time = json.split("posting_time")[1].split(",")[0].trim().split("\"")[2].replace("\\", "").trim();

				String hangup_time = json.split("hangup_time")[1].split("\\[")[0].trim().split("\"")[2].replace("\\", "").trim();

				String outcall_pickup_time = json.split("outcall_pickup_time")[1].split("\"")[2].replace("\\", "").trim();

				logs.setPosting_time(posting_time);
				logs.setHangup_time(hangup_time);
				logs.setKnowlarityCallDetails_Id(knowlarityCallDetails.getId());
				logs.setOutcall_pickup_time(outcall_pickup_time);
				logs.setRec_timetaken("");
				logs.setRecordingurl_system("");
				logs.setStatus(GlobalConstants.KNOWLARITY_CUSTOMER_MISSED_CALL);

			}else{

				String posting_time = json.split("posting_time")[1].split(",")[0].trim().split("\"")[2].replace("\\", "").trim();

				String hangup_time = json.split("hangup_time")[1].split("\\[")[0].trim().split("\"")[2].replace("\\", "").trim();

				String outcall_pickup_time = json.split("outcall_pickup_time")[1].split("\"")[2].replace("\\", "").trim();

				String rec_timetaken = json.split("rec_timetaken")[1].split(":")[1].trim().split(",")[0];

				String recordingurl_system = json.split("recordingurl_system")[1].split("\"")[2];

				logs.setPosting_time(posting_time);
				logs.setHangup_time(hangup_time);
				logs.setKnowlarityCallDetails_Id(knowlarityCallDetails.getId());
				logs.setOutcall_pickup_time(outcall_pickup_time);
				logs.setRec_timetaken(rec_timetaken);
				logs.setRecordingurl_system(recordingurl_system);
				logs.setStatus(GlobalConstants.KNOWLARITY_CALL_ANSWERED);

				
				
				downloadRecordingFile(recordingurl_system);
				
			}

			knowlarityCallDetails.setCall_logs_status("completed");
			knowlarityCallDetailService.save(knowlarityCallDetails);
			knowlarityCallDetailLogService.save(logs);
		}catch(Exception e){
			
		}

	}

	

	public RestResponse callActivity(long id, String ivr) {

		try{
			if(ivr!=null && ivr.equalsIgnoreCase("servetel")){
				return servetelService.getCallActivity(id);
			}
						
			KnowlarityCallDetails knowlarityCallDetails = knowlarityCallDetailService.findOne(id);

			if(knowlarityCallDetails==null)
				return new RestResponse(RestResponse.FAILED, "Details not available !");

			KnowlarityCallLogsActivity data = new KnowlarityCallLogsActivity();	
			Candidate candidate = candidateService.getCandidateByEmail(knowlarityCallDetails.getCandidateEmail());
			data.setCallerMobileNumber(knowlarityCallDetails.getAgentMobile());
			data.setCallStatus(knowlarityCallDetails.getStatus());
			data.setCandidateEmail(knowlarityCallDetails.getCandidateEmail());
			data.setInitiatedBy(knowlarityCallDetails.getAgentEmail());
			data.setCandidateMobile(knowlarityCallDetails.getCandidateMobile());
			data.setCallerAltMobileNumber(candidate.getAlternateMobile());

			KnowlarityCallDetailLogs knowlarityCallDetailLog = knowlarityCallDetailLogService.getByKnowlarityCallDetailId(knowlarityCallDetails.getId());

			if(knowlarityCallDetailLog!=null){
				data.setCallLogStatus(knowlarityCallDetailLog.getStatus());
				data.setCallRecordingUrl(knowlarityCallDetailLog.getRecordingurl_system());
				data.setDurationOfCall(knowlarityCallDetailLog.getRec_timetaken());
			}
			
			return new RestResponse(RestResponse.SUCCESS, data);
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
		
	}

	
	
	private void downloadRecordingFile(String recordingurl_system) {
		
		try{
			FileUtils.copyURLToFile(new URL(recordingurl_system), new File("E:\\recruiz_project_files\\logs\\recording.mp3"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public RestResponse makeCallForProspects(KnowlarityCallDto knowlarityCallDto) {
		
		KnowlarityIntegration knowlarityIntegration = null;
		try{
			Organization org = organizationService.getOrgInfo();

			if(org!=null){
				knowlarityIntegration = knowlarityIntegrationService.findByOrgName(org.getOrgId());

				if(knowlarityIntegration==null)
					return new RestResponse(RestResponse.FAILED, "knowlarity not integrated with "+org.getOrgId());
			}

/*			Candidate candidate = candidateService.getCandidateByEmail(knowlarityCallDto.getCandidateEmail());

			if(candidate==null)
				return new RestResponse(RestResponse.FAILED, "candidate details not found !");

*/
			User user = userService.getLoggedInUserObject();

			if(user!=null){

				String userMobile = user.getMobile();

				if(userMobile==null || userMobile.trim().equalsIgnoreCase(""))
					return new RestResponse(RestResponse.FAILED, "LoggedIn user mobile number does not exist.");


				String result = sendPOSTCall(GlobalConstants.KNOWLARITY_MAKECALL_URL,knowlarityIntegration.getSr_number(),
						userMobile,knowlarityCallDto.getCandidateMobile(),knowlarityIntegration.getCaller_id(),knowlarityIntegration.getAuthorization_key(),
						knowlarityIntegration.getXApi_key());

				String message ="";String call_id ="";

				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(result);

				if(result.contains("call_id")){
					JSONObject data = (JSONObject) json.get("success");
					message =  (String) data.get("message");
					call_id =  (String) data.get("call_id");

					/*candidateActivityService.addActivityForCallLog(userService.getLoggedInUserObject().getName()+"("+userService.getLoggedInUserObject().getEmail()+"/"+userService.getLoggedInUserObject().getMobile()+") initiated a call to "+"("+knowlarityCallDto.getCandidateEmail()+"/"+knowlarityCallDto.getCandidateMobile()+")",
							userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
							candidate.getCid() + "", CandidateActivityType.CALL_LOG.getDisplayName(),String.valueOf(knowlarityCallDetails.getId()));	

*/					
					prospectActivityService.addActivity(userService.getLoggedInUserObject().getName()+"("+userService.getLoggedInUserObject().getEmail()+"/"+userService.getLoggedInUserObject().getMobile()+") initiated a call to "+"("+knowlarityCallDto.getCandidateEmail()+"/"+knowlarityCallDto.getCandidateMobile()+")",
							userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")", knowlarityCallDto.getProspectId(),
							CandidateActivityType.CALL_LOG.getDisplayName());
					

				}else if(result.contains("error")){
					JSONObject data = (JSONObject) json.get("error");
					message =  (String) data.get("message");
					return new RestResponse(RestResponse.FAILED, message); 
				}else if(result.contains("Forbidden")){
					message =  (String) json.get("message");
					return new RestResponse(RestResponse.FAILED, message);
				}

				return new RestResponse(RestResponse.SUCCESS, message);

			}
			return new RestResponse(RestResponse.FAILED, "user details not found !");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error !");
		}
	}

}
