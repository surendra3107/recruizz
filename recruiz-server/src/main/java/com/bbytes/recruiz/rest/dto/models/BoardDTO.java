package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class BoardDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String positionName;

	private String positionCode;
	
	private String positionId;

	private String clientName;
	
	private String clientId;

	private String boardId;
	
	private String positionStatus;
	
	private String clientStatus;

	private Date createdDate;
	
	private List<RoundResponseDTO> rounds = new ArrayList<RoundResponseDTO>();

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCreatedDate() {
		return createdDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

}
