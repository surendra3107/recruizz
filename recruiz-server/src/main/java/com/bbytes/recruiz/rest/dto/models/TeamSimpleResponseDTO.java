package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class TeamSimpleResponseDTO implements Serializable {

	private static final long serialVersionUID = -2190129402899015471L;

	protected Long teamId;

	protected String teamName;

	protected String teamDesc;
	
	private Boolean rootTeam = false;

	protected int teamMemberCount;

	protected Long teamTargetAmount;

	protected Long teamTargetPositionOpeningClosure;

	protected String teamTargetAmountCurrency;

	protected Date creationDate;

	protected Date modificationDate;

}
