package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CampaignRunDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private Long campaignId;

	private List<Long> campaignMemberIds;

	// this will contain test ids and other details
	private Map<String, String> campaignOtherDetails;

}
