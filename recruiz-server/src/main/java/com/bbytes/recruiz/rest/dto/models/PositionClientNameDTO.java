package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class PositionClientNameDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String positionName;

	private String positionCode;
	
	private String clientName;
	
	private String location;
	
}
