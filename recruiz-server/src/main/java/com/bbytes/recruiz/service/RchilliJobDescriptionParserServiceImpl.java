package com.bbytes.recruiz.service;

import java.io.File;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.common.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.integration.RchilliJDParserResponse;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.FileUtils;

@Service("jobDescriptionParserService")
public class RchilliJobDescriptionParserServiceImpl implements IJobDescriptionParserService {

	private static Logger logger = LoggerFactory.getLogger(RchilliJobDescriptionParserServiceImpl.class);

	@Value("${rchilli.jd.parser.service.url}")
	private String serviceUrl;

	@Value("${rchilli.jd.parser.user.key}")
	private String userKey;

	@Value("${rchilli.jd.parser.user.id}")
	private String subUserId;

	// api version
	String version = "3.0";
	
	@Autowired
	private DTOToDomainConverstionService dtoDomainConverstionService;

	private RestTemplate restTemplate;

	@PostConstruct
	private void init() {
		restTemplate = new RestTemplate(getClientHttpRequestFactory());
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 120000; // 2mins
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		return new HttpComponentsClientHttpRequestFactory(client);
	}

	@Override
	public Position parseJobDescription(File jd) throws RecruizException {

		String fileContentString = "";
		try {
			fileContentString = FileUtils.readFileContent(jd);
			return parseJobDescriptionText(fileContentString);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException("File Content : " + fileContentString + " , Error message :  " + e.getMessage(),
					ErrorHandler.JD_PARSER_ERROR);
		}
	}

	@Override
	public Position parseJobDescriptionText(String jdContent) throws RecruizException {

		try {
			String encodedString = Base64.encodeBytes(jdContent.getBytes());
			RchilliJDParserResponse jdParserDataResponse = sendRequest(encodedString);
//			String jdParserData = sendRequest(encodedString);
			logger.debug("Rchilli JD JSON");
			logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
			logger.error(jdParserDataResponse.toString());
			logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
			return dtoDomainConverstionService.convert(jdParserDataResponse.getJDParsedData());
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException("File Content : " + jdContent + " , Error message :  " + e.getMessage(),
					ErrorHandler.JD_PARSER_ERROR);
		}
		
		
	}

//	private String sendRequest(final String jdFileContent, final String fileName) throws Exception {
//
//		URL url = new URL(serviceUrl);
//
//		try {
//
//			String input = "{\"filedata\":\"" + jdFileContent + "\",\"filename\":\"" + fileName + "\",\"userkey\":\"" + userKey
//					+ "\",\"version\":\"" + version + "\",\"subuserid\":\"" + subUserId + "\"}";
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//
//			HttpEntity<String> entity = new HttpEntity<String>(input, headers);
//			ResponseEntity<String> response = restTemplate.exchange(url.toURI(), HttpMethod.POST, entity, String.class);
//			return response.getBody();
//		} catch (Throwable e) {
//			throw new RecruizException(e);
//		}
//
//	}

	private RchilliJDParserResponse sendRequest(final String jdFileContentBase64text) throws Exception {

		URL url = new URL(serviceUrl);

		try {

			String input = "{\"base64text\":\"" + jdFileContentBase64text + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = new HttpEntity<String>(input, headers);
			
			ResponseEntity<RchilliJDParserResponse> jdParsedData = restTemplate.exchange(url.toURI(), HttpMethod.POST, entity, RchilliJDParserResponse.class);
			return jdParsedData.getBody();
			
//			ResponseEntity<String> jdParsedData = restTemplate.exchange(url.toURI(), HttpMethod.POST, entity, String.class);
//			return jdParsedData.getBody();
			

		} catch (Throwable e) {
			throw new RecruizException(e);
		}

	}

}
