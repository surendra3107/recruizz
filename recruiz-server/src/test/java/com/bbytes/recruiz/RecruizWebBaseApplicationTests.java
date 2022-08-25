package com.bbytes.recruiz;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bbytes.recruiz.auth.jwt.SpringSecurityConfig;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.database.TenantDBService;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.repository.OrganizationRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DecisionMakerService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.TaskFolderService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { RecruizApplication.class, SpringSecurityConfig.class })
@WebAppConfiguration
public class RecruizWebBaseApplicationTests extends RecruizApplicationTests {
	
	public Logger logger = LoggerFactory.getLogger(RecruizWebBaseApplicationTests.class);

	@Autowired
	protected OrganizationService organizationService;

	@Autowired
	protected UserService userService;

	@Autowired
	protected OrganizationRepository organizationRepository;

	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	protected PositionRepository positionRepository;

	@Autowired
	protected UserRoleService userRoleService;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mockMvc;

	@Autowired
	protected FilterChainProxy filterChainProxy;

	@Autowired
	protected TenantResolverService tenantResolverService;

	@Autowired
	protected SpringProfileService springProfileService;

	@Autowired
	protected TenantDBService dbService;

	@Autowired
	protected ClientService clientService;
	
	@Autowired
	protected TaskFolderService taskFolderService;

	@Autowired
	protected DecisionMakerService decisionMakerService;
	
	protected final static String TENANT_ID = "Fresh_Account";
	
	protected final static String authEmail = "sourav@beyondbytes.co.in";
	
	protected final static String vendorAuthEmail = "sourav@beyondbytes.co.in";

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).dispatchOptions(true).apply(springSecurity())
				.build();
	}

	@After
	public void cleanUpData() {
		// tenantResolverService.deleteAll();
	}

	@Test
	@Ignore
	public void contextLoads() {
	}

	public String getAuthToken() {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1,"Asia/Kolkata","en");
		return xauthToken;
	}
	
	public String getAuthTokenForVendor() {
		String tenant = TENANT_ID;
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(vendorAuthEmail, tenant, WebMode.DASHBOARD, 1,"Asia/Kolkata","en");
		return xauthToken;
	}

}
