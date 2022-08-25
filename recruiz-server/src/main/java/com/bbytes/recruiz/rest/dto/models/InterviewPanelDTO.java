package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class InterviewPanelDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;
	
	private Long id;

	private String name;

	private String email;

	private String mobile;
	
	private boolean selectedStatus;

}
