package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString()
public class TeamResponseDTO extends TeamSimpleResponseDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	protected Collection<UserDTO> members = new ArrayList<>();

	protected TeamResponseDTO parentTeam;

	protected Collection<TeamResponseDTO> childrenTeams = new ArrayList<>();

}
