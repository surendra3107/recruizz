package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class ClientCountHRMDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String clientName;

	private String location;

	private String closedPositionCount;
	
	private String openedPositionCount;
	
	private String onHoldPositionCount;
	
	private String nearClosurePositionCount;
	
	private String postClosedDatePositionCount;
	
}
