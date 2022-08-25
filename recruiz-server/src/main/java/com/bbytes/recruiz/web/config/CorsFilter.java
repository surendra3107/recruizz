package com.bbytes.recruiz.web.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.ApplicationConstant;
import com.bbytes.recruiz.utils.GlobalConstants;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
// @Profile("dev")
public class CorsFilter implements Filter {

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;

		String accessControlAllowHeaders = GlobalConstants.HEADER_AUTH_TOKEN + "," + GlobalConstants.HEADER_API_TOKEN
				+ "," + GlobalConstants.HEADER_EXT_ACC_INFO_TOKEN + "," + GlobalConstants.HEADER_TENANT_ID + ","
				+ GlobalConstants.HEADER_FILE_NAME + "," + ApplicationConstant.HEADER_APP_ID + ","
				+ ApplicationConstant.HEADER_CLIENT_ID + "," + ApplicationConstant.HEADER_EMAIL + ","
				+ ApplicationConstant.HEADER_AUTH_TOKEN + "," + ApplicationConstant.HEADER_REQUEST_DATE_TIME + ","
				+ ApplicationConstant.HEADER_API_KEY_UPDATE_AUTH_TOKEN + ","
				+ ApplicationConstant.HEADER_SIXTH_SENSE_SESSION_ID+","
				+ "Origin, If-Modified-Since, X-Requested-With, Content-Type, Accept, cache-control";

		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		response.setHeader("Access-Control-Allow-Headers", accessControlAllowHeaders);
		response.setHeader("Access-Control-Expose-Headers", accessControlAllowHeaders);
		response.setHeader("Access-Control-Max-Age", "3600");

		if ("OPTIONS".equals(((HttpServletRequest) request).getMethod()))
			return;
		else {
			chain.doFilter(req, res);
		}

		// if (request.getMethod() != "OPTIONS") {
		// chain.doFilter(req, res);
		// }

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}