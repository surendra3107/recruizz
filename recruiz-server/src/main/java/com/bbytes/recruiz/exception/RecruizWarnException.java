package com.bbytes.recruiz.exception;

/**
 * The exception to be logged as warning and thrown to ui as warning message
 * too.
 */
public class RecruizWarnException extends RecruizException {

	private static final long serialVersionUID = -2317729349964250645L;

	public RecruizWarnException(String message, Throwable ex) {
		super(message, ex);
	}

	public RecruizWarnException(String message, String errConstant) {
		super(message, errConstant);
	}

}
