package com.bbytes.recruiz.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;

public class MultiTenantUtils {

	public static String getTenantId(HttpServletRequest request) {
		if (request == null)
			return null;

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return null;
		}

		String tenantId = request.getHeader(GlobalConstants.HEADER_TENANT_ID);
		if (tenantId != null && !tenantId.trim().isEmpty())
			return tenantId;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		tenantId = (String) sessionStrategy.getAttribute(new ServletWebRequest(request), GlobalConstants.ORG_NAME);
		if (tenantId != null && !tenantId.trim().isEmpty())
			return tenantId;

		return null;
	}

	public static String getTenantId(WebRequest request) {
		String tenantName = getTenantName(request);
		String tenantId = null;
		if (tenantName != null)
			tenantId = tenantName.trim().replace(" ", "_");

		return tenantId;
	}

	public static String getTenantName(WebRequest request) {
		if (request == null)
			return null;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String tenantName = (String) sessionStrategy.getAttribute(request, GlobalConstants.ORG_NAME);

		return tenantName;

	}

	public static String getInviteEmailId(WebRequest request) {
		if (request == null)
			return null;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String tenantId = (String) sessionStrategy.getAttribute(request, GlobalConstants.INVITE_EMAIL_ID);

		return tenantId;

	}

	public static String getSignUpMode(WebRequest request) {
		if (request == null)
			return null;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String attributeValue = (String) sessionStrategy.getAttribute(request, GlobalConstants.SIGNUP_MODE);
		return attributeValue;

	}

	public static String getSignUpMode(HttpServletRequest request) {
		if (request == null)
			return null;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String attributeValue = (String) sessionStrategy.getAttribute(new ServletRequestAttributes(request),
				GlobalConstants.SIGNUP_MODE);

		return attributeValue;

	}

	public static void clearTenantIdInRequest(WebRequest request) {
		if (request == null)
			return;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String tenantId = (String) sessionStrategy.getAttribute(request, GlobalConstants.ORG_NAME);
		if (tenantId != null)
			sessionStrategy.removeAttribute(request, GlobalConstants.ORG_NAME);
	}

	public static void clearInviteEmailIdInRequest(WebRequest request) {
		if (request == null)
			return;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String attributeValue = (String) sessionStrategy.getAttribute(request, GlobalConstants.INVITE_EMAIL_ID);
		if (attributeValue != null)
			sessionStrategy.removeAttribute(request, GlobalConstants.INVITE_EMAIL_ID);
	}

	public static void clearSignUpModeInRequest(HttpServletRequest request) {
		if (request == null)
			return;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String attributeValue = (String) sessionStrategy.getAttribute(new ServletRequestAttributes(request),
				GlobalConstants.SIGNUP_MODE);
		if (attributeValue != null)
			sessionStrategy.removeAttribute(new ServletRequestAttributes(request), GlobalConstants.SIGNUP_MODE);
	}

	public static void clearSignUpModeInRequest(WebRequest request) {
		if (request == null)
			return;

		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		String attributeValue = (String) sessionStrategy.getAttribute(request, GlobalConstants.SIGNUP_MODE);
		if (attributeValue != null)
			sessionStrategy.removeAttribute(request, GlobalConstants.SIGNUP_MODE);
	}

	public static boolean storeTentantIdToSession(HttpServletRequest request) {
		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		if (request.getParameter(GlobalConstants.ORG_NAME) != null) {
			sessionStrategy.setAttribute(new ServletWebRequest(request), GlobalConstants.ORG_NAME,
					request.getParameter(GlobalConstants.ORG_NAME));
			return true;
		}

		if (sessionStrategy.getAttribute(new ServletWebRequest(request), GlobalConstants.ORG_NAME) != null)
			return true;

		return false;
	}

	public static boolean storeSignUpModeToSession(HttpServletRequest request) {
		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		if (request.getParameter(GlobalConstants.SIGNUP_MODE) != null) {
			sessionStrategy.setAttribute(new ServletWebRequest(request), GlobalConstants.SIGNUP_MODE,
					request.getParameter(GlobalConstants.SIGNUP_MODE));
			return true;
		}

		if (sessionStrategy.getAttribute(new ServletWebRequest(request), GlobalConstants.SIGNUP_MODE) != null)
			return true;

		return false;
	}

	public static boolean storeInviteEmailIdToSession(HttpServletRequest request) {
		SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
		if (request.getParameter(GlobalConstants.INVITE_EMAIL_ID) != null) {
			sessionStrategy.setAttribute(new ServletWebRequest(request), GlobalConstants.INVITE_EMAIL_ID,
					request.getParameter(GlobalConstants.INVITE_EMAIL_ID));
			return true;
		}

		if (sessionStrategy.getAttribute(new ServletWebRequest(request), GlobalConstants.INVITE_EMAIL_ID) != null)
			return true;

		return false;
	}


	public static String getTenant() throws RecruizException {
		String tenantName = TenantContextHolder.getTenant();

		if (tenantName == null) {
			throw new RecruizWarnException("Tenant information missing for current session",
					ErrorHandler.TENANT_NOT_PRESENT);
		}
		return tenantName;
	}
}
