package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;

import lombok.Data;

@Data
public class ClientDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;

	private Long id;
	
	private String clientName;
	
	private String address;
	
	private String website;
	
	private String empSize;
	
	private String clientLocation;
	
	private String turnOvr;
	
	private String notes;
	
	private Set<ClientDecisionMaker> clientDecisionMaker;
	
	private Set<ClientInterviewerPanel> clientInterviewerPanel;
	
	private boolean dummy=false;
	
	private Map<String, String> customField = new HashMap<>();
}
