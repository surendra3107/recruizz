package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class KnowlarityCallLogsActivity  implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;
	
	private String initiatedBy;
	
    private String callerMobileNumber;

    private String callerAltMobileNumber;
    
	private String candidateEmail;
	
	private String callStatus;
	
	private String durationOfCall;
	
	private String callRecordingUrl;
	
	private String callLogStatus;
	
	private String candidateMobile;
	
}

