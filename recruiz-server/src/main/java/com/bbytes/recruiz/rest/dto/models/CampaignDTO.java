package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class CampaignDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private long id;

	private String name;

	private String type;

	private String campaignRenderedTemplate;

	private String campaignRenderedSubject;

	private String positionCode;

	private Long clientId;

	private List<CampaignCandidateDTO> campaignCandidates;

	private Set<String> campaignHrMembersEmail = new HashSet<>();

	private Date campaignStartDate;

	/**********************
	 * below will be used if campaign type is of levelbar
	 **************/
	private String testId;

	private String qnLink;

	private Boolean random = true;

	private String questionSetId;

	private String recruizTenant;

	private List<String> recruizNotifyMailIds = new ArrayList<>();

}
