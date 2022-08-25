package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RecruiterPerformanceReportDTO implements Serializable {

	private static final long serialVersionUID = -219012943789015471L;
	
	private String recruiterName;
	private String recruiterEmail;
	private String candidatesSourced;
	private List<BaseDTO> statusCandidates;
	
}
