package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RecruiterCandidatesDTO implements Serializable {

	private static final long serialVersionUID = -219012943789015471L;

	List<BaseDTO> statusList ;
	List<RecruiterPerformanceReportDTO> recruiterData ;

}
