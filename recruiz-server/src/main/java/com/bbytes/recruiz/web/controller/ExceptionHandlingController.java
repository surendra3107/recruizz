package com.bbytes.recruiz.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;

@ControllerAdvice
public class ExceptionHandlingController {

	public final Logger logger = LoggerFactory.getLogger(ExceptionHandlingController.class);

	@Autowired
	private UserService userService;

	@ExceptionHandler(RecruizException.class)
	@ResponseBody
	public ResponseEntity<Object> handleError(HttpServletRequest req, RecruizException ex) {
		logError(req, ex);
		RestResponse errorResponse = new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
		return new ResponseEntity<Object>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(RecruizAuthException.class)
	@ResponseBody
	public ResponseEntity<Object> handleAuthError(HttpServletRequest req, RecruizException ex) {
		logError(req, ex);
		RestResponse errorResponse = new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
		return new ResponseEntity<Object>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseBody
	public ResponseEntity<Object> handleAuthenticationError(HttpServletRequest req, RecruizException ex) {
		logError(req, ex);
		RestResponse errorResponse = new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
		return new ResponseEntity<Object>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(RecruizPermissionDeniedException.class)
	@ResponseBody
	public ResponseEntity<Object> handleAccessDeniedExceptionError(HttpServletRequest req, RecruizPermissionDeniedException ex) {
		logError(req, ex);
		RestResponse errorResponse = new RestResponse(RestResponse.FAILED, ex.getMessage(), ex.getErrConstant());
		return new ResponseEntity<Object>(errorResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<Object> handleError(HttpServletRequest req, Exception ex) {
		logError(req, ex);
		RestResponse errorResponse = new RestResponse(RestResponse.FAILED, ex.getMessage(), ErrorHandler.SERVER_ERROR);
		return new ResponseEntity<Object>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * If the exception is anything other then recruiz warn exception then log
	 * to file as error else as warning message to log file
	 * 
	 * @param req
	 * @param ex
	 */
	private void logError(HttpServletRequest req, Exception ex) {

		String erroMessage = "Request: " + req.getRequestURL() + " raised - " + ex.getMessage() + getUserInfo();
		if (ex instanceof RecruizWarnException) {
			if (req != null) {
				logger.warn(erroMessage);
			}
			logger.warn(ex.getMessage() + getUserInfo(), ex);
		} else {
			if (req != null) {
				logger.error(erroMessage);
			}
			logger.error(ex.getMessage() + getUserInfo(), ex);
		}

	}

	/**
	 * Return string with loggedIn user info
	 * 
	 * @author Akshay
	 * @return
	 */
	private String getUserInfo() {

		User user = userService.getLoggedInUserObject();

		if (user == null)
			return "";

		String userInfo = " - by Logged in User - " + user.getName() + " with Email : " + user.getEmail() + " of "
				+ user.getOrganization().getOrgName() + " - organization";

		return userInfo;
	}

}