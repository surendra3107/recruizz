package com.bbytes.recruiz.exception;

public class RecruizException extends Exception {

	private static final long serialVersionUID = -2317729349964250645L;

	protected String errConstant;

	public RecruizException(Throwable ex) {
		super(ex);
	}
	public RecruizException(String message) {
		super(message);
	}
	
	public RecruizException(String message, String errConstant, Throwable ex) {
		super(message, ex);
		this.errConstant = errConstant;
	}

	public RecruizException(String message, Throwable ex) {
		super(message, ex);
	}

	public RecruizException(String message, String errConstant) {
		super(message);
		this.errConstant = errConstant;
	}
	
	public String getErrConstant() {
		return errConstant;
	}

	public void setErrConstant(String errConstant) {
		this.errConstant = errConstant;
	}
}
