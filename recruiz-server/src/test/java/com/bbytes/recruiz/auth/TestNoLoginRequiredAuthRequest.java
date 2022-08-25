package com.bbytes.recruiz.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bbytes.recruiz.RecruizApplication;
import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.auth.jwt.SpringSecurityConfig;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { RecruizApplication.class, SpringSecurityConfig.class })
@WebAppConfiguration
public class TestNoLoginRequiredAuthRequest extends RecruizBaseApplicationTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Autowired
	private FilterChainProxy filterChainProxy;
	
	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	private User adminUser1;

	private Organization testOrg;

	private String password;
	
	private String email;

	private String xauthToken;

	@Before
	public void setUp() throws RecruizException {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).dispatchOptions(true).addFilters(filterChainProxy)
				.build();

		testOrg = new Organization("test", "Test-Org");

		password = "test123";
		email = "admin1@test.com";
		adminUser1 = new User("admin-1", email);
		adminUser1.setOrganization(testOrg);

		// adminUser1.setUserRole(UserRole.ADMIN_USER_ROLE);

		TenantContextHolder.setTenant(adminUser1.getOrganization().getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();

		TenantContextHolder.setTenant(adminUser1.getOrganization().getOrgId());
		initRoles();
		organizationRepository.save(testOrg);
		userService.save(adminUser1);
		userService.updatePassword(password, adminUser1);

	}

	@After
	public void cleanUp() {
		TenantContextHolder.setTenant(adminUser1.getOrganization().getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();
	}

	@Test
	public void testURLAccessDenied() throws Exception {
		mockMvc.perform(get("/app/status")).andExpect(status().isUnauthorized());
	}

	@Test
	public void testLoginAndAccessProtectedUrl() throws Exception {
		xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(adminUser1.getEmail(), testOrg.getOrgId(), WebMode.DASHBOARD,1);

		mockMvc.perform(get("/app/status").param(GlobalConstants.URL_AUTH_TOKEN, xauthToken)).andExpect(status().isOk())
				.andExpect(header().string(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken));

	}

}
