package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class RoundResponseDTO implements Serializable {

	private static final long serialVersionUID = 3088462175334711832L;

	String name;
	
	String type;
	
	String roundId;
	
	int orderNo;
	
	List<RoundCandidateDTO> candidateList = new LinkedList<RoundCandidateDTO>();

}
