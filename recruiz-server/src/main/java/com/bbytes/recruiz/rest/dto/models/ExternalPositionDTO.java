package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
public class ExternalPositionDTO implements Serializable {

	private static final long serialVersionUID = 1223889429606908737L;

	private long id;
	
	private String clientName;

	private String positionCode;

	private String title;

	private String location;

	private Date openedDate;

	private Set<String> reqSkillSet = new HashSet<String>();

	private Set<String> goodSkillSet = new HashSet<String>();
	
	private Set<String> educationalQualification = new HashSet<String>();

	private String type; // Payroll or onContract basis
	
	private String typeDisplayName;

	private String remoteWork;

	private String notes;

	private String description;
	
	private int totalPosition;

	private Date closeByDate;

	private String positionUrl;

	private String experienceRange;
	
	private double maxSal;
	
	private double minSal;

	private String salUnit;

	private String industry;
	
	private String functionalArea;

	private String status;
	
	private String finalStatus;

	private String clientStatus = Status.Active.toString();

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getOpenedDate() {
		return openedDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setOpenedDate(Date openedDate) {
		this.openedDate = openedDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCloseByDate() {
		return closeByDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCloseByDate(Date closeByDate) {
		this.closeByDate = closeByDate;
	}
	
}
