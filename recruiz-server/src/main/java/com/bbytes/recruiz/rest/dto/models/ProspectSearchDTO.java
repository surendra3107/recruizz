package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProspectSearchDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String[] nameList;

	private String[] locationList;
	
	private String[] statusList;

}
