package com.bbytes.recruiz.integration.levelbar;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.bbytes.recruiz.utils.IntegrationConstants;

public class LevelbarAbstractRestClient {

	private static final Logger logger = LoggerFactory.getLogger(LevelbarAbstractRestClient.class);

	private static final Integer TIMEOUT_IN_SECS = 30;

	private RestTemplate restTemplate;

	private String xAuthToken;

	@Value("${levelbar.server.url}")
	protected String levelbarBaseUrl;

	public HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		
		final TrustStrategy trustAllStrategy = new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		};

		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(trustAllStrategy);
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		return httpclient;
	}

	private ClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory factory = null;
		try {
			factory = new HttpComponentsClientHttpRequestFactory(getHttpClient());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		factory.setReadTimeout(TIMEOUT_IN_SECS * 1000);
		factory.setConnectTimeout(TIMEOUT_IN_SECS * 1000);
		return factory;
	}

	protected List<HttpMessageConverter<?>> getMessageConverters() {
		return new ArrayList<>();
	}

	public LevelbarAbstractRestClient() {
		List<HttpMessageConverter<?>> messageConverters = getMessageConverters();
		restTemplate = new RestTemplate(clientHttpRequestFactory());
		if (messageConverters != null && !messageConverters.isEmpty()) {
			restTemplate.setMessageConverters(messageConverters);
		}
		// add interceptor code here
		registerInterceptors();
	}

	private void registerInterceptors() {
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();
		interceptors.add(new LevelbarTokenRequestInterceptor());
		getRestTemplate().setInterceptors(interceptors);
	}

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	private final class LevelbarTokenRequestInterceptor implements ClientHttpRequestInterceptor {

		public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
				ClientHttpRequestExecution execution) throws IOException {

			request.getHeaders().add(IntegrationConstants.LEVELBAR_AUTH_HEADER, xAuthToken); 
			return execution.execute(request, body);
		}
	}

	public String getxAuthToken() {
		return xAuthToken;
	}

	public void setxAuthToken(String xAuthToken) {
		this.xAuthToken = xAuthToken;
	}

	protected <T> ResponseEntity<T> exchange(String relativeUrl, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, String authToken) throws Exception {
		try {
			setxAuthToken(authToken);
			return restTemplate.exchange(levelbarBaseUrl + relativeUrl, method, requestEntity, responseType);
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}

	protected <T> ResponseEntity<T> exchange(String relativeUrl, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, String authToken, Map<String, String> params) throws Exception {
		try {
			setxAuthToken(authToken);
			return restTemplate.exchange(levelbarBaseUrl + relativeUrl, method, requestEntity, responseType, params);
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}

	protected <T> ResponseEntity<T> exchange(String reativeURL, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, MultiValueMap<String, String> paramMap, String authToken) throws Exception {
		try {
			setxAuthToken(authToken);
			UriComponents uriComponents = UriComponentsBuilder.fromUriString(levelbarBaseUrl + reativeURL)
					.queryParams(paramMap).build();
			return restTemplate.exchange(uriComponents.toUriString(), method, requestEntity, responseType);
		} catch (Throwable e) {
			throw new Exception(e);
		}

	}
}
