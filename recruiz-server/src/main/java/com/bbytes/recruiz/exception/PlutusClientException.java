package com.bbytes.recruiz.exception;

public class PlutusClientException extends Exception {

	private static final long serialVersionUID = -4084444984163796577L;

	public PlutusClientException(String msg) {
		super(msg);
	}

	public PlutusClientException(Throwable ex) {
		super(ex);
	}

	public PlutusClientException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
