package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class YetToOnBoardEmployeeeDTO implements Serializable {

	private static final long serialVersionUID = 3088462175334711832L;

	Long roundCanddiateId;

	String name;

	Map<String, String> currentlyOfferedPosition = new HashMap<>();
	
	Map<String,String> offeredPositionStatus = new HashMap<>();
	
	Map<String,String> offeredPositionRoundMap = new HashMap<>();
	
	String emailID;
	
	int offeredInPositionCount;

}
