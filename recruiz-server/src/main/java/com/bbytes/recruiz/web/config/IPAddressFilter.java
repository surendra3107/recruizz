package com.bbytes.recruiz.web.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.catalina.filters.RemoteAddrFilter;
import org.apache.catalina.filters.RequestFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 5)
public final class IPAddressFilter extends RequestFilter {

	private static final Log log = LogFactory.getLog(RemoteAddrFilter.class);

	@Value("${ipaddress.filter.apply:true}")
	private String applyFilter;

	@Value("${ipaddress.filter.pattern.allow:d+\\.\\d+\\.\\d+\\.\\d+|0:0:0:0:0:0:0:1|127.0.0.1}")
	private String allowPattern;

	@Value("${ipaddress.filter.pattern.deny:none}")
	private String denyPattern;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if ("true".equalsIgnoreCase(applyFilter)) {
			setAllow(allowPattern);
			
			if (!"none".equalsIgnoreCase(denyPattern))
				setDeny(denyPattern);
		}
	}

	/**
	 * Extract the desired request property, and pass it (along with the
	 * specified request and response objects and associated filter chain) to
	 * the protected <code>process()</code> method to perform the actual
	 * filtering.
	 *
	 * @param request
	 *            The servlet request to be processed
	 * @param response
	 *            The servlet response to be created
	 * @param chain
	 *            The filter chain for this request
	 *
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet error occurs
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if ("true".equalsIgnoreCase(applyFilter))
			process(request.getRemoteAddr(), request, response, chain);
		else
			chain.doFilter(request, response);
	}

	@Override
	protected Log getLogger() {
		return log;
	}
}