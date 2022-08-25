package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class InviteUser implements Serializable {

	private static final long serialVersionUID = 583276480449140065L;

	private String userName;
	
	private String email;
	
	private String roleId;

}
