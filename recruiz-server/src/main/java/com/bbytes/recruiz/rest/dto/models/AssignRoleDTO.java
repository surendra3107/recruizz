package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AssignRoleDTO implements Serializable {

	private static final long serialVersionUID = -3773709200581702893L;

	private List<String> userEmailList = new ArrayList<String>();

	private String roleId;

}
