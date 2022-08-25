package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class CustomerDTO implements Serializable {

	private static final long serialVersionUID = -6340858056087253543L;

	private String orgName;

	private String orgId;

	private String stripeAccnId;

	private double billingAmount;

	private Date amountUpdatedTimeStamp;

	private Date validTill;

	private String deleteStatus;

	private String deleteReason;

	private boolean disableStatus = false;

	private String disableReason;

	private String trialPeriod;

	// private Interval trialPeriodInterval;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date startTrialPeriodDate;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date endTrialPeriodDate;

	private Object supportPeriod;

	private Object supportPeriodInterval;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date startSupportPeriodDate;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date endSupportPeriodDate;

	private String discountApplyPeriod;

	private Object discountApplyPeriodInterval;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date startDiscountApplyPeriodDate;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date endDiscountApplyPeriodDate;

	private Double discountPercentage;

	private Double discountAmount;

	private Date creationDate;

	private PricingPlanDTO pricingPlan;

	private String discountApplyPeriodCycle;

	private Object contactInfo;

	private Object affiliateReseller;

	private Object paymentHistoryList;

	private Object promoCodeDTOs;

	private String currency;
}
