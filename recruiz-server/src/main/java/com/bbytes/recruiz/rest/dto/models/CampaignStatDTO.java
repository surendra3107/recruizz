package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class CampaignStatDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String totalCandidate = "0";

	private String openRate = "0%";

	private String profileUpdateRate = "0%";

	private String notInterested = "0%";

	private String avgTimeToOpen = "N/A";

	private String linkClick = "0%";

	private String interestedClick = "0%";

}
