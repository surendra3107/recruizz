package com.bbytes.recruiz.auth;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bbytes.recruiz.RecruizApplication;
import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.auth.jwt.AuthUserDetailsService;
import com.bbytes.recruiz.auth.jwt.SpringSecurityConfig;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.GlobalConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { RecruizApplication.class, SpringSecurityConfig.class })
@WebAppConfiguration
public class TestLoginAuthRequest extends RecruizBaseApplicationTests {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private AuthUserDetailsService authUserDetailsService;

	private MockMvc mockMvc;

	@Autowired
	private FilterChainProxy filterChainProxy;

	private User adminUser1;

	private Organization testOrg;

	private String password;
	private String email;

	private String xauthToken;

	@Before
	public void setUp() throws RecruizException {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).dispatchOptions(true).apply(springSecurity()).build();
		email = "orgadmin@acc.in";
		password = "rcrz!23";
	}

	@Test
	public void testAnonymousAccess() throws Exception {
		mockMvc.perform(get("/api/user/account")).andExpect(status().is4xxClientError());
	}

	@Test
	public void testLoginFailed() throws Exception {
		mockMvc.perform(
				get("/auth/login").param("username", "email@test.com").param("password", "plainttext")
						.header(GlobalConstants.HEADER_TENANT_ID, "wrong")).andExpect(status().is2xxSuccessful())
				.andExpect(content().string(allOf(containsString("\"success\":false")))).andDo(print());
	}

	@Test
	public void testLoginSuccess() throws RecruizAuthException, Exception {
		mockMvc.perform(get("/auth/login").param("username", email).param("password", password))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testPostTenantSeletion() throws Exception {
		// org.springframework.security.core.userdetails.User user =
		// authUserDetailsService.loadUserByUsername(email);
		// SecurityContextHolder.getContext().setAuthentication(new
		// MultiTenantAuthenticationToken("acme", user));

		mockMvc.perform(get("/auth/tenant/selected").param("tenant", "acme").param("email", email))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testLogin_badCredential() throws Exception {
		mockMvc.perform(
				get("/auth/login").param("username", "sourav.1rx12mca5q4@gmail.com").param("password", "rcrz!2"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void testURLAccessDenied() throws Exception {
		mockMvc.perform(get("/app/status")).andExpect(status().isUnauthorized()).andDo(print());
	}

	@Test
	public void testLoginAndAccessProtectedUrl() throws Exception {
		mockMvc.perform(get("/auth/login").param("username", email).param("password", password)).andDo(
				new ResultHandler() {

					@Override
					public void handle(MvcResult result) throws Exception {
						xauthToken = result.getResponse().getHeader(GlobalConstants.HEADER_AUTH_TOKEN);
					}
				});

		mockMvc.perform(
				get("/app/status").param("username", email).param("password", password)
						.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk())
				.andDo(print());
	}

}
