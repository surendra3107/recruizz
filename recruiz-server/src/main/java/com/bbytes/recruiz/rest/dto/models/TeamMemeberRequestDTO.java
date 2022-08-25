package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;

@Data
public class TeamMemeberRequestDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private Long teamId;

	private Collection<String> memberEmails;

}
