package com.bbytes.recruiz.connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectCorporateDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectVendorDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ConnectURLConstants;

@Service
public class RecruizConnectClient extends AbstractRestClient {

	private static Logger logger = LoggerFactory.getLogger(RecruizConnectClient.class);

	private static final String INSTANCE_ID = "instanceId";

	RecruizConnectClient() {
		super();
	}

	/**
	 * Add corporate in recruiz connect
	 * 
	 * @param connectCorporateDTO
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> addConnectCorporate(ConnectCorporateDTO connectCorporateDTO, String authToken)
			throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(connectCorporateDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			response = exchange(ConnectURLConstants.CONNECT_CORPORATE_URL, HttpMethod.POST, request, RestResponse.class,
					paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Add Vendor in recruiz connect
	 * 
	 * @param connectVendorDTO
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> addConnectVendor(ConnectVendorDTO connectVendorDTO, String authToken)
			throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(connectVendorDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			response = exchange(ConnectURLConstants.CONNECT_VENDOR_URL, HttpMethod.POST, request, RestResponse.class,
					paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Update Vendor in recruiz connect
	 * 
	 * @param connectVendorDTO
	 * @param tenantId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> updateConnectVendor(ConnectVendorDTO connectVendorDTO, String tenantId,
			String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(connectVendorDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			response = exchange(ConnectURLConstants.CONNECT_VENDOR_URL + "/" + tenantId, HttpMethod.PUT, request,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Get Corporate in recruiz connect
	 * 
	 * @param connectVendorDTO
	 * @param tenantId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> getConnectCorporate(String tenantId, String instanceId, String authToken)
			throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add(INSTANCE_ID, instanceId);
			response = exchange(ConnectURLConstants.CONNECT_CORPORATE_URL + "/" + tenantId, HttpMethod.GET, null,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Get Vendor in recruiz connect
	 * 
	 * @param connectVendorDTO
	 * @param tenantId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> getConnectVendor(String tenantId, String instanceId, String authToken)
			throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add(INSTANCE_ID, instanceId);
			response = exchange(ConnectURLConstants.CONNECT_VENDOR_URL + "/" + tenantId, HttpMethod.GET, null,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Get All Vendor in recruiz connect
	 * 
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public List<ConnectVendorDTO> getAllConnectVendor(String authToken) throws Exception {

		ResponseEntity<ConnectVendorDTO[]> response = null;
		List<ConnectVendorDTO> connectVendorDTOs = new ArrayList<ConnectVendorDTO>();
		try {
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			response = exchange(ConnectURLConstants.CONNECT_VENDOR_URL, HttpMethod.GET, null, ConnectVendorDTO[].class,
					paramMap, authToken);

			if (response.getStatusCode().is2xxSuccessful()) {
				connectVendorDTOs = (List<ConnectVendorDTO>) Arrays.asList(response.getBody());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			logger.error(
					"Failed to get connect vendor list from from recruiz connect server on " + new Date().toString());
		}
		return connectVendorDTOs;
	}

	/**
	 * Get All Vendor in recruiz connect
	 * 
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public List<ConnectPositionDTO> getAllPendingPositionVendor(String tenantId, String instanceId, String authToken)
			throws Exception {

		ResponseEntity<ConnectPositionDTO[]> response = null;
		List<ConnectPositionDTO> connectPositionDTOs = new ArrayList<ConnectPositionDTO>();
		try {
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add(INSTANCE_ID, instanceId);
			response = exchange(ConnectURLConstants.CONNECT_POSITION_URL + "/" + tenantId, HttpMethod.GET, null,
					ConnectPositionDTO[].class, paramMap, authToken);

			if (response.getStatusCode().is2xxSuccessful()) {
				connectPositionDTOs = (List<ConnectPositionDTO>) Arrays.asList(response.getBody());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			logger.error("Failed to get connect vendor pending position list from from recruiz connect server on "
					+ new Date().toString());
		}
		return connectPositionDTOs;
	}

	/**
	 * Source candidate for recruiz connect
	 * 
	 * @param connectCandidateEventDTO
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> sourceCandidate(ConnectCandidateEventDTO connectCandidateEventDTO,
			String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(connectCandidateEventDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			response = exchange(ConnectURLConstants.CONNECT_CANDIDATE_SOURCE_URL, HttpMethod.POST, request,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

}
