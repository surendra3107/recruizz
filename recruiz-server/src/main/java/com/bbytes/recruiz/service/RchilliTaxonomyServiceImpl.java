package com.bbytes.recruiz.service;

import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.bbytes.recruiz.exception.RecruizException;

@Service("taxonomyService")
public class RchilliTaxonomyServiceImpl implements ITaxonomyService {

	private static Logger logger = LoggerFactory.getLogger(RchilliTaxonomyServiceImpl.class);

	@Value("${rchilli.taxonomy.service.url}")
	private String serviceUrl;

	@Value("${rchilli.taxonomy.user.key}")
	private String userKey;

	@Value("${rchilli.taxonomy.user.id}")
	private String subUserId;

	// api version
	String version = "1.0";

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

	/**
	 * Skill alias method can be used to search alias name for a Skill.
	 * 
	 * @param keyword
	 *            (e.g. Java)
	 * @return
	 * @throws Exception
	 */
	public String skillAlias(final String skillKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/skillalias");

		try {
			String input = "{\"keyword\":\"" + skillKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}

	/**
	 * Similar job profile method can be used for suggestion and auto complete.
	 * We use Like % keyword% for searching similar job profile and return max
	 * 100 matching records
	 * 
	 * @param skillKeyword
	 *            (e.g. Java)
	 * @return
	 * @throws Exception
	 */
	public String similarSkills(final String skillKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/similarskills");

		try {
			String input = "{\"keyword\":\"" + skillKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}
	
	/**
	 * Similar job profile method can be used for suggestion and auto complete.
	 * We use Like % keyword% for searching similar job profile and return max
	 * 100 matching records
	 * 
	 * @param jobProfileKeyword
	 * 
	 * @return
	 * @throws Exception
	 */
	public String similarJobProfiles(final String jobProfileKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/similarjobprofiles");

		try {
			String input = "{\"keyword\":\"" + jobProfileKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}

	

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param JobProfileKeyword
	 *            (e.g. Java Developer)
	 * @return
	 * @throws Exception
	 */
	public String jobSkillRelation(final String JobProfileKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/jobskillrelation");

		try {
			String input = "{\"keyword\":\"" + JobProfileKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param JobProfileKeyword
	 *            (e.g. Java Developer)
	 * @return
	 * @throws Exception
	 */
	public String jobDomain(final String JobProfileKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/jobdomain");

		try {
			String input = "{\"keyword\":\"" + JobProfileKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param skillKeyword
	 *            (e.g.Quality Control)
	 * @return
	 * @throws Exception
	 */
	public String skillDomain(final String skillKeyword) throws Exception {

		URL url = new URL(serviceUrl + "/skilldomain");

		try {
			String input = "{\"keyword\":\"" + skillKeyword + "\",\"userkey\":\"" + userKey + "\",\"version\":\"" + version
					+ "\",\"subuserid\":\"" + subUserId + "\"}";

			return sendRequest(url, input);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new RecruizException(e);
		}

	}

	/**
	 * @param url
	 * @param input
	 * @return
	 * @throws URISyntaxException
	 */
	private String sendRequest(URL url, String input) throws URISyntaxException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(input, headers);
		ResponseEntity<String> response = restTemplate.exchange(url.toURI(), HttpMethod.POST, entity, String.class);
		return response.getBody();
	}

}
