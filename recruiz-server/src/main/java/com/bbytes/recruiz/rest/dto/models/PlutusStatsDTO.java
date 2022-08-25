package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class PlutusStatsDTO implements Serializable{
	
	private static final long serialVersionUID = 4624053334607633922L;
	
	 private Date entryDate;
	 
	 private Map<String, Object> stats = new HashMap<String, Object>();
	 
}
