package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class PositionOfferCostDTO implements Serializable  {

	private static final long serialVersionUID = -2190129400799015471L;
	
	/*private long id;

	private long positionId;

	private String approvedCost;

	private String hourlyBilling;

	private String fixCostRates;

	private String status;*/
	
	private long id;

	private long positionId;

	private String billRate;

	private String billHours;

	private String billingDate;

	private String projectDuration;
	
	private String oneTimeCost;
	
	private String headHunting;
	
}
