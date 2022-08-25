package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestResponse implements Serializable {

	private static final long serialVersionUID = 7996664536603472824L;

	public static final String TOTAL_SIZE = "tsize";
	public static final String GRID_DATA = "gridData";
	public static final Boolean SUCCESS = true;
	public static final Boolean FAILED = false;

	private boolean success = false;

	private Object data;

	private String reason = null;

	/**
	 * Success response
	 * 
	 * @param success
	 * @param data
	 */
	public RestResponse(boolean success, Object data) {
		this.success = success;
		this.data = data;
	}

	/**
	 * Failure response
	 * 
	 * @param data
	 * @param reason
	 */
	public RestResponse(String data, String reason) {
		this.data = data;
		this.reason = reason;
	}

	/**
	 * Failure response with data
	 * 
	 * @param success
	 * @param data
	 * @param reason
	 */
	public RestResponse(boolean success, Object data, String reason) {
		this.success = success;
		this.data = data;
		this.reason = reason;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
