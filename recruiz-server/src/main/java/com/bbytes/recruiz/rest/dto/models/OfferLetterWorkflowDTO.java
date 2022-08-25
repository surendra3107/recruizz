package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class OfferLetterWorkflowDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;
	
	private long id;
	
	private String workflowName;
	
    private String lowerMargin;
	
	private String upperMargin;
	
	private String lowerMarginOperator;
	
	private String approverName;
	
	private String upperMarginOperator;
	
	private String approverEmail;

	private String otherLoop;
	
	private String otherLoopName;
	
	private String status;
	
	
}
