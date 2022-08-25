package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Set;

import com.bbytes.recruiz.domain.Permission;

import lombok.Data;

@Data
public class UserRoleDTO implements Serializable {

	private static final long serialVersionUID = -6592086195592802133L;

	private String id;
	
	private String roleName;

	private Set<Permission> permissions;

}
