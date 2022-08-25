package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class PricingPlanDTO implements Serializable {

	private static final long serialVersionUID = -2522748618036012956L;

	private String pricingPlanId;
	private String planName;
	private String description;
	private String monthlyPrice;
	private String annualPrice;
	private String currency;
	private String appProfile;
	private String maxDepartmentUsers;
	private String maxAppUsers;
	private String maxVendorUsers;
	private String maxDeparments;
	private String maxPositions;
	private String maxCandidates;
	private String maxResumeParserCount;
	private String maxVendors;
	private String vendorFeature;
	private String maxDepartmentHeadUser;
	private String postionSocialSharing;
	private String advancedSearch;
	private String featureMap;
	private String publicPricingPlan;

	private String subscriptionMode;
}
