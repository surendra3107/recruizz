package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class CampaignCandidateDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2771354763317209381L;

	private String memberEmailId;

	private String memberName;

}
