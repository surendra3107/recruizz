package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class DashboardDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String positionName;

	private String positionCount;

	private String clientName;
	
	private String candidateNames;
	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	private Date scheduleDate;
}
