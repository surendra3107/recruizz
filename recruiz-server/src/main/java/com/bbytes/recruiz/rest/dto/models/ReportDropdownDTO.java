package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ReportDropdownDTO implements Serializable {

	private static final long serialVersionUID = 1223889429606908737L;

	private String timePeriod;

	private Date startDate;

	private Date endDate;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getStartDate() {
		return startDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getEndDate() {
		return endDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCloseByDate(Date endDate) {
		this.endDate = endDate;
	}

}
