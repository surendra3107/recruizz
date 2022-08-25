package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.bbytes.recruiz.domain.Position;

import lombok.Data;

@Data
public class CareerSiteDTO implements Serializable {

	private static final long serialVersionUID = 909228259372068381L;

	private List<Position> positionList;
	
	private Map<String, List<Position>> positionByDept;

	private List<String> positionLocationList;

	private List<String> positionIndustryList;

}
