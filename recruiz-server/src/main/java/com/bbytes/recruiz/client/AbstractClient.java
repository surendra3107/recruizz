package com.bbytes.recruiz.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.utils.GlobalConstants;

public class AbstractClient {

	private static final Integer TIMEOUT_IN_SECS = 30;

	private RestTemplate restTemplate;

	private String subscriptionId;

	private String subscriptionKey;

	@Value("${plutus.server.url}")
	protected String plutusBaseURL;

	public AbstractClient() {
		List<HttpMessageConverter<?>> messageConverters = getMessageConverters();
		restTemplate = new RestTemplate(clientHttpRequestFactory());
		if (messageConverters != null && !messageConverters.isEmpty()) {
			restTemplate.setMessageConverters(messageConverters);
		}

		registerInterceptors();
		setBaseURL(plutusBaseURL);

	}

	private ClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setReadTimeout(getReadTimeoutInSecs() * 1000);
		factory.setConnectTimeout(getConnectionTimeoutInSecs() * 1000);
		return factory;
	}

	protected Integer getReadTimeoutInSecs() {
		return TIMEOUT_IN_SECS;
	}

	protected Integer getConnectionTimeoutInSecs() {
		return TIMEOUT_IN_SECS;
	}

	private void registerInterceptors() {
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();
		interceptors.add(new PlutusTokenRequestInterceptor());
		getRestTemplate().setInterceptors(interceptors);
	}

	protected List<HttpMessageConverter<?>> getMessageConverters() {
		return new ArrayList<>();
	}

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	protected String getBaseURL() {
		return plutusBaseURL;
	}

	protected void setBaseURL(String plutusBaseURL) {
		this.plutusBaseURL = plutusBaseURL;
	}

	/**
	 * @return the subscriptionId
	 */
	public String getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * @param subscriptionId
	 *            the subscriptionId to set
	 */
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	/**
	 * @return the subscriptionKey
	 */
	public String getSubscriptionKey() {
		return subscriptionKey;
	}

	/**
	 * @param subscriptionKey
	 *            the subscriptionKey to set
	 */
	public void setSubscriptionKey(String subscriptionKey) {
		this.subscriptionKey = subscriptionKey;
	}

	protected <T> T post(String reativeURL, Object postEntity, Class<T> type, String subscriptionId,
			String subscriptionKey) throws PlutusClientException {
		try {
			setSubscriptionId(subscriptionId);
			setSubscriptionKey(subscriptionKey);
			HttpEntity<?> entity = new HttpEntity<Object>(postEntity);
			return restTemplate.postForObject(plutusBaseURL + reativeURL, entity, type);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}
	}

	protected void put(String reativeURL, Object postEntity, String subscriptionId, String subscriptionKey)
			throws PlutusClientException {
		try {
			setSubscriptionId(subscriptionId);
			setSubscriptionKey(subscriptionKey);
			restTemplate.put(plutusBaseURL + reativeURL, postEntity);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}
	}

	protected <T> ResponseEntity<T> exchange(String reativeURL, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, String subscriptionId, String subscriptionKey) throws PlutusClientException {
		try {
			setSubscriptionId(subscriptionId);
			setSubscriptionKey(subscriptionKey);
			return restTemplate.exchange(plutusBaseURL + reativeURL, method, requestEntity, responseType);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}

	}

	protected <T> ResponseEntity<T> exchange(String reativeURL, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, MultiValueMap<String, String> paramMap, String subscriptionId,
			String subscriptionKey) throws PlutusClientException {
		try {
			setSubscriptionId(subscriptionId);
			setSubscriptionKey(subscriptionKey);
			UriComponents uriComponents = UriComponentsBuilder.fromUriString(plutusBaseURL + reativeURL)
					.queryParams(paramMap).build();
			return restTemplate.exchange(uriComponents.toUriString(), method, requestEntity, responseType);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}

	}

	protected void delete(String reativeURL, String subscriptionId, String subscriptionKey)
			throws PlutusClientException {
		try {
			setSubscriptionId(subscriptionId);
			setSubscriptionKey(subscriptionKey);
			restTemplate.delete(plutusBaseURL + reativeURL);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}

	}

	protected <T> T get(String reativeURL, MultiValueMap<String, String> paramMap, Class<T> type, String subscriptionId,
			String subscriptionKey) throws PlutusClientException {
		try {
			UriComponents uriComponents = UriComponentsBuilder.fromUriString(plutusBaseURL + reativeURL)
					.queryParams(paramMap).build();
			return restTemplate.getForObject(uriComponents.toUriString(), type);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}

	}

	protected <T> T get(String reativeURL, Class<T> type, String subscriptionId, String subscriptionKey)
			throws PlutusClientException {
		try {
			return restTemplate.getForObject(plutusBaseURL + reativeURL, type);
		} catch (Throwable e) {
			throw new PlutusClientException(e);
		}

	}

	private final class PlutusTokenRequestInterceptor implements ClientHttpRequestInterceptor {

		public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
				ClientHttpRequestExecution execution) throws IOException {

			request.getHeaders().add(GlobalConstants.AUTHORIZATION,
					GlobalConstants.BASIC + " " + createAuthorizationToken(getSubscriptionId(), getSubscriptionKey()));
			return execution.execute(request, body);
		}

		public String createAuthorizationToken(String subscriptionId, String subscriptionKey) {
			String authString = subscriptionId + ":" + subscriptionKey;
			String encodedAuth = new String(Base64.encodeBase64(authString.getBytes()));
			return encodedAuth;
		}

	}

}
