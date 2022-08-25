package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PlutusOrgDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String orgId;

	private String orgName;
	
	private String orgType;

	private Date updatedDate;

	private String subscryptionId;

	private String subscryptionKey;

	private String planId;

	private String orgEmail;

	private String featureMapString;

	private String stripeAccnId;

	private String planName;

	private String subscriptionMode;

	private Boolean disableStatus;

	private String disableReason;
	
	private String registeredMobile;
}
