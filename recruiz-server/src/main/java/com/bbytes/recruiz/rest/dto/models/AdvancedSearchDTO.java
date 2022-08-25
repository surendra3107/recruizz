package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.EmailActivity;

import lombok.Data;

@Data
public class AdvancedSearchDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;

	private AdvancedSearchQueryEntity advancedSearchQuery;

	private EmailActivity emailActivity;

	private String fileName;

	private String positionCode;
	
	private String sourceMode;

}
