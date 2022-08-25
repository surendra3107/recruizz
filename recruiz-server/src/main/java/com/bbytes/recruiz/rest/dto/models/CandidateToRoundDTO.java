package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class CandidateToRoundDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	List<String> candidateEmailList = new ArrayList<String>();

	String roundId = null;

	String status = null;

	String positionCode = null;

	String rejectReason = null;
	
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	Date joiningDate = null;

	String joinedBy = null;
	
}
