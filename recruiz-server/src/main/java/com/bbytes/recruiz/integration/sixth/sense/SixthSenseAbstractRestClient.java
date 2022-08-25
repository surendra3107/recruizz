package com.bbytes.recruiz.integration.sixth.sense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

import com.bbytes.recruiz.ApplicationConstant;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.jwt.IExternalAppJWTAuthTokenGenerator;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Sixth Sense Abstract Rest Client
 * 
 * @author akshay
 *
 */
public class SixthSenseAbstractRestClient {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseAbstractRestClient.class);

	private static final Integer TIMEOUT_IN_SECS = 90;

	private RestTemplate restTemplate;

	@Autowired
	private UserService userService;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Autowired
	private SixthSenseSessionTokenStore sixthSenseSessionTokenStore;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Value("${sixth.sense.admin.userid:administrator}")
	protected String sixthSenseAdminUserId;

	protected IExternalAppJWTAuthTokenGenerator externalAppJWTAuthTokenGenerator;

	private String getSixthSenseBaseUrl() {
		return integrationProfileService.getSixthSenseBaseUrl();
	}

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

	public SixthSenseAbstractRestClient() {
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
		interceptors.add(new SixthSenseTokenRequestInterceptor());
		getRestTemplate().setInterceptors(interceptors);
	}

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	private final class SixthSenseTokenRequestInterceptor implements ClientHttpRequestInterceptor {

		public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, ClientHttpRequestExecution execution)
				throws IOException {

			// adding clientId as "tenantId" and userEmail
			String tenantId = TenantContextHolder.getTenant();
			String email = userService.getLoggedInUserEmail();

			// if api key update token call
			if (request.getURI().getPath().equals(IntegrationConstants.SIXTH_SENSE_UPDATE_API_SECRET_KEY_URL)) {
				String xApiKeyUpdateAuthToken = externalAppJWTAuthTokenGenerator
						.generateAPIKeyUpdateAuthToken(IntegrationConstants.SIXTH_SENSE_APP_ID, tenantId);

				if (xApiKeyUpdateAuthToken != null)
					request.getHeaders().add(ApplicationConstant.HEADER_API_KEY_UPDATE_AUTH_TOKEN, xApiKeyUpdateAuthToken);

			} else {

				String xAuthToken = externalAppJWTAuthTokenGenerator.generateAuthToken(IntegrationConstants.SIXTH_SENSE_APP_ID, tenantId,
						null);

				SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(email);
				String sessionToken = null;
				if (sixthSenseUser != null)
					sessionToken = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(), tenantId);

				if (request.getHeaders().containsKey(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION))
					sessionToken = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseAdminUserId, tenantId);

				if (xAuthToken != null)
					request.getHeaders().add(ApplicationConstant.HEADER_AUTH_TOKEN, xAuthToken);

				if (sessionToken != null) {
					request.getHeaders().add(ApplicationConstant.HEADER_SIXTH_SENSE_SESSION_ID, sessionToken);
				}

				if (request.getURI() != null && sixthSenseUser != null)
					logger.error("API : " + request.getURI().getPath() + ", User : " + sixthSenseUser.getUserName() + ", Session id : "
							+ sessionToken);
			}

			// adding appId and client id in every request
			request.getHeaders().add(ApplicationConstant.HEADER_APP_ID, IntegrationConstants.SIXTH_SENSE_APP_ID);
			request.getHeaders().add(ApplicationConstant.HEADER_CLIENT_ID, tenantId);

			// traceRequest(request, body);
			ClientHttpResponse response = execution.execute(request, body);
			// traceResponse(response);
			return response;
		}
	}

	protected <T> ResponseEntity<T> exchange(String relativeUrl, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
		return restTemplate.exchange(getSixthSenseBaseUrl() + relativeUrl, method, requestEntity, responseType);
	}

	protected <T> ResponseEntity<T> exchange(String sixthSenseBaseUrl, String relativeUrl, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType) {

		return restTemplate.exchange(sixthSenseBaseUrl + relativeUrl, method, requestEntity, responseType);
	}

	private void traceRequest(HttpRequest request, byte[] body) throws IOException {
		logger.info("===========================request begin================================================");
		logger.info("URI         : {}", request.getURI());
		logger.info("Method      : {}", request.getMethod());
		logger.info("Headers     : {}", request.getHeaders());
		logger.info("Request body: {}", new String(body, "UTF-8"));
		logger.info("==========================request end================================================");
	}

	private void traceResponse(ClientHttpResponse response) throws IOException {
		StringBuilder inputStringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
		String line = bufferedReader.readLine();
		while (line != null) {
			inputStringBuilder.append(line);
			inputStringBuilder.append('\n');
			line = bufferedReader.readLine();
		}
		logger.info("============================response begin==========================================");
		logger.info("Status code  : {}", response.getStatusCode());
		logger.info("Status text  : {}", response.getStatusText());
		logger.info("Headers      : {}", response.getHeaders());
		logger.info("Response body: {}", inputStringBuilder.toString());
		logger.info("=======================response end=================================================");
	}
}
