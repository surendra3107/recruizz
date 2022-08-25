package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "offerletter_approvals")
public class OfferLetterApprovals extends AbstractEntity {

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "client_id")
	private long client_id;
	
	@Column(name = "position_id")
	private long position_id;
	
	@Column(name = "position_code")
	private String position_code;
	
	@Column(name = "candidate_id")
	private long candidate_id;
	
	@Column(name = "workflow_id")
	private long workflow_id;
	
	@Column(name = "request_send_from_user")
	private long request_send_from_user;
	
	@Column(name = "approval_status")
	private String approval_status;
	
	@Column(name = "reject_reason")
	private String reject_reason;
	
	@Column(name = "deliveryLead")
	private String deliveryLead;
	
	@Column(name = "recruitersName")
	private String recruitersName;

	@Column(name = "accountManager")
	private String accountManager;
	
	@Column(name = "client")
	private String client;
	
	@Column(name = "clientPoC")
	private String clientPoC;
	
	@Column(name = "clientPhone")
	private String clientPhone;

	@Column(name = "clientEmail")
	private String clientEmail;
	
	@Column(name = "candidateName")
	private String candidateName;
	
	@Column(name = "designation")
	private String designation;
	
	@Column(name = "dateOfOffer")
	private Date dateOfOffer;

	@Column(name = "experience")
	private String experience;
	
	@Column(name = "doj")
	private Date doj;
	
	@Column(name = "billingDate")
	private Date billingDate;
	
	@Column(name = "compensationAnnual")
	private String compensationAnnual;

	@Column(name = "projectDurationMonths")
	private String projectDurationMonths;
	
	@Column(name = "billingHours")
	private String billingHours;
	
	@Column(name = "billRatePerHour")
	private String billRatePerHour;
	
	@Column(name = "subConCompensation")
	private String subConCompensation;

	@Column(name = "serviceTax")
	private String serviceTax;
	
	@Column(name = "oneTimeCost")
	private String oneTimeCost;
	
	@Column(name = "relocationCost")
	private String relocationCost;
	
	@Column(name = "noticePeriodCost")
	private String noticePeriodCost;
	
	@Column(name = "joiningBonusCost")
	private String joiningBonusCost;
	
	@Column(name = "headHunting")
	private String headHunting;
	
	@Column(name = "otherAssociatedCost")
	private String otherAssociatedCost;

	@Column(name = "monthlyCost")
	private String monthlyCost;
	
	@Column(name = "costProjectDuration")
	private String costProjectDuration;
	
	@Column(name = "billingMonthly")
	private String billingMonthly;
	
	@Column(name = "billingProjectDuration")
	private String billingProjectDuration;
	
	@Column(name = "monthlyDirectCost")
	private String monthlyDirectCost;
	
	@Column(name = "annualDirectCost")
	private String annualDirectCost;
	
	@Column(name = "montlyGrossMargin")
	private String montlyGrossMargin;
	
	@Column(name = "annualGorssMargin")
	private String annualGorssMargin;
	
	@Column(name = "grossMarginPercentage")
	private String grossMarginPercentage;
	
	@Column(name = "approvedDate")
	private Date approvedDate;
	
	@Column(name = "approvedBy")
	private String approvedBy;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "offerStatus")
	private String offerStatus;
	
	@Column(name = "iSJoinedOn")
	private Date iSJoinedOn;
	
	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;
	
	@Column(name = "field4")
	private String field4;
}
