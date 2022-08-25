package com.bbytes.recruiz.integration.servetel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class ServetelRecruizClient {

	private static final Logger logger = LoggerFactory.getLogger(ServetelRecruizClient.class);

	static String LOGIN_URL = "https://api.servetel.in/v1/auth/login";
	static String ADD_AGENT_URL = "https://api.servetel.in/v1/agent";
	static String DELETE_AGENT_URL = "https://api.servetel.in/v1/agent/";
	static String CLICK_TO_CALL_URL = "https://api.servetel.in/v1/click_to_call";

	private RestTemplate restTemplate;

	public ServetelRecruizClient() {
		restTemplate = new RestTemplate();
	}

	public ServetelLoginEntityResponse servetelLogin(String email, String password)
			throws IOException, URISyntaxException {

		ServetelLoginEntityResponse response = new ServetelLoginEntityResponse();

		Map<String, Object> data = new HashMap<>();
		data.put("email", email);
		data.put("password", password);

		String requestJson = getRequestJSON(data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
		try {
			ResponseEntity<ServetelLoginEntityResponse> result = restTemplate.exchange(LOGIN_URL, HttpMethod.POST,
					request, ServetelLoginEntityResponse.class);
			response.setAccess_token(result.getBody().access_token);
			response.setSuccess(result.getBody().success);
		} catch (Exception e) {
			logger.error("servetelLogin api = " + e);
			response.setMessage("Internal server error");
			response.setSuccess(false);
		}
		return response;
	}

	public ServetelEntityResponse addServetelAgent(String name, String mobile, String authorization, String productId)
			throws IOException, URISyntaxException {

		ServetelEntityResponse response = new ServetelEntityResponse();
		Long mob = Long.valueOf(mobile);
		Long prodId = Long.valueOf(productId);
		Map<String, Object> data = new HashMap<>();
		data.put("follow_me_number", mob);
		data.put("name", name);
		data.put("user_product_id",prodId);
		
		String requestJson = getRequestJSON(data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", authorization);

		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
		try {
			ResponseEntity<ServetelEntityResponse> result = restTemplate.exchange(ADD_AGENT_URL, HttpMethod.POST,
					request, ServetelEntityResponse.class);
			response.setMessage(result.getBody().message);
			response.setSuccess(result.getBody().success);
			response.setAgent_id(result.getBody().agent_id);
		} catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == 422) {
				response.setMessage("Number of allowed agents already added.");
				response.setSuccess(false);
			} else {
				logger.error("addServetelAgent api = " + e);
				response.setMessage("Unable to complete the call. Please try again.");
				response.setSuccess(false);
			}
		}

		return response;
	}

	public ServetelEntityResponse deleteServetelAgent(String agentId, String authorization)
			throws IOException, URISyntaxException {

		ServetelEntityResponse response = new ServetelEntityResponse();
		Map<String, Object> data = new HashMap<>();
		data.put("id", agentId);

		String requestJson = getRequestJSON(data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", authorization);

		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
		try {
			ResponseEntity<ServetelEntityResponse> result = restTemplate.exchange(DELETE_AGENT_URL + agentId,
					HttpMethod.DELETE, request, ServetelEntityResponse.class);
			response.setMessage(result.getBody().message);
			response.setSuccess(result.getBody().success);
		} catch (Exception e) {
			response.setMessage("Internal server error");
			response.setSuccess(false);
		}
		return response;
	}

	public ServetelEntityResponse clickToCall(String agentId, String candidateMobile, String authorization,
			String callerId) throws IOException, URISyntaxException {

		int call_id = Integer.parseInt(callerId);
		Long canMobile = Long.valueOf(candidateMobile);
		Long servetelAgentID = Long.valueOf(agentId);
		callerId = String.format("%011d", call_id);
		callerId = callerId.replaceAll("0", "9");
		Long caller_id = Long.valueOf(callerId);

		ServetelEntityResponse response = new ServetelEntityResponse();
		Map<String, Object> data = new HashMap<>();
		data.put("destination_number", canMobile);
		data.put("agent_number", servetelAgentID);
		data.put("caller_id", caller_id);

		String requestJson = getRequestJSON(data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", authorization);
		logger.error("Click to call ====== step 2   authorization = "+authorization+"       requestJson = "+requestJson);
		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
		try {
			ResponseEntity<ServetelEntityResponse> result = restTemplate.exchange(CLICK_TO_CALL_URL, HttpMethod.POST,
					request, ServetelEntityResponse.class);
			response.setMessage(result.getBody().message);
			response.setSuccess(result.getBody().success);
			
			logger.error("Click to call ====== step 3   result message = "+result.getBody().message);
		} catch (Exception e) {
			
			logger.error("clickToCall api error  = " + e);
			response.setMessage("Unable to complete the call. Please try again.");
			response.setSuccess(false);
		}

		return response;
	}

	public String getRequestJSON(Map<String, Object> data) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(data);
		return requestJson.replace("\n", "");
	}

	public void updateServeletAgent(String agentId, String mobile,String name, String authorization) throws JsonProcessingException {
		
		Long mob = Long.valueOf(mobile);
		Map<String, Object> data = new HashMap<>();
		data.put("follow_me_number", mob);
		data.put("name", name);

		String requestJson = getRequestJSON(data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", authorization);

		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
	    restTemplate.exchange(DELETE_AGENT_URL + agentId,
					HttpMethod.PUT, request, String.class);
		
	}

	
	
		/*public static void main(String[] args) throws JsonProcessingException {
		
			ServetelRecruizClient cl = new ServetelRecruizClient();
	        RestTemplate rest  = new RestTemplate(); 
			
			//int call_id = Integer.parseInt("");
			Long canMobile = Long.valueOf("7982657529");
			Long servetelAgentID = Long.valueOf("8368176312");
			callerId = String.format("%011d", call_id);
			callerId = callerId.replaceAll("0", "9");
			Long caller_id = Long.valueOf(callerId);

			ServetelEntityResponse response = new ServetelEntityResponse();
			Map<String, Object> data = new HashMap<>();
			data.put("destination_number", canMobile);
			data.put("agent_number", servetelAgentID);
			

			String requestJson = cl.getRequestJSON(data);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvY3VzdG9tZXIuc2VydmV0ZWwuaW5cL2FwaVwvdjFcL2F1dGhcL2xvZ2luIiwiaWF0IjoxNjI0MjA5MTQyLCJleHAiOjE2MjQyMTI3NDIsIm5iZiI6MTYyNDIwOTE0MiwianRpIjoiMGZWcmJEd2NMdVRXY0tzcCIsInN1YiI6MTQ0Njh9.eqKCNRu4GZxkh4sysXKF8oX9KqgGMNMR1hMmYTOG_HM");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
			try {
				ResponseEntity<ServetelEntityResponse> result = rest.exchange(CLICK_TO_CALL_URL, HttpMethod.POST,
						request, ServetelEntityResponse.class);
				response.setMessage(result.getBody().message);
				response.setSuccess(result.getBody().success);
			} catch (Exception e) {
				logger.error("clickToCall api = " + e);
				response.setMessage("Unable to complete the call. Please try again.");
				response.setSuccess(false);
			}
			

		}
*/	
	
	
	
}
