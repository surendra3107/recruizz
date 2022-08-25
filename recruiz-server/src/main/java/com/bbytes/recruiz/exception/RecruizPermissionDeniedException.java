package com.bbytes.recruiz.exception;

public class RecruizPermissionDeniedException extends RuntimeException {

	private static final long serialVersionUID = -3272059847266046969L;

	protected static String errConstant = "recruiz_api_permission_denied";

	public RecruizPermissionDeniedException() {
		super(errConstant);
	}

	/**
	 * Constructs an <code>AccessDeniedException</code> with the specified
	 * message.
	 *
	 * @param msg
	 *            the detail message
	 */
	public RecruizPermissionDeniedException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an <code>AccessDeniedException</code> with the specified
	 * message and root cause.
	 *
	 * @param msg
	 *            the detail message
	 * @param t
	 *            root cause
	 */
	public RecruizPermissionDeniedException(String msg, Throwable t) {
		super(msg, t);
	}

	public String getErrConstant() {
		return errConstant;
	}

}
