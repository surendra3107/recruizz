package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class TeamDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private Long teamId;
	
	private Long parentTeamId;
	
	private List<Long> childrenTeamIds;

	private String teamName;

	private String teamDesc;
	
	private Boolean rootTeam = false;

	private Long teamTargetAmount = 0L;

	private Long teamTargetPositionOpeningClosure = 0L;

	private String teamTargetAmountCurrency;

	private List<TeamMemberDTO> members;

}
