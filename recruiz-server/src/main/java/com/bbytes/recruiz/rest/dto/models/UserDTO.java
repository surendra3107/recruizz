package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private Long id;

	private String email;

	private String userName;

	private String mobile;

	private String timeZone;

	private String designation;

	private String locale;

	private String type;

	private String orgType;

	private Boolean accountStatus;

	private Boolean joinedStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
	private Date joinedDate;

	private BaseDTO userRole;

	private UserRole role;

	private String orgId;

	private String orgName;

	private Boolean orgMarkForDelete;

	private Date orgMarkDeleteDate;

	private Boolean isNotificationEnabled = true;

	private String profileSignature;

	private boolean jobPortalEnable;

	// used only when we show this dto for a team api
	private List<Long> teamIds = new ArrayList<>();

	// used only when we show this dto for a team api
	private Long teamMemberTargetAmount;

	protected Long teamTargetPositionOpeningClosure;

	// used only when we show this dto for a team api
	private String teamRole;

	// used for job portal view allowed
	private String usageType;

	// used for job portal view allowed
	private int viewCount;

	// used for listing the sources
	private Set<String> selectedSources = new HashSet<String>();
	
	//List of Teams user is part of 
	private HashMap<Long,String> userTeams;
	
	//Reporting Team
	private HashMap<Long,String> userReportingToTeams;
	

}
