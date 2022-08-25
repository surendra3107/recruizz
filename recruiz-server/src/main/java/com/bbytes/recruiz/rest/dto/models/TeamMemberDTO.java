package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class TeamMemberDTO implements Serializable {

	private static final long serialVersionUID = -745950146273475420L;

	private Long id;

	private Long teamId;

	private Long targetAmount = 0L;

	private Long targetPositionOpeningClosure = 0L;

	private String email;

	private String role;

}
