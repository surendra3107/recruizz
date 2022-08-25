package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InviteUserDTO implements Serializable {

	private static final long serialVersionUID = 583276480449140065L;

	List<InviteUser> inviteUsers = new ArrayList<InviteUser>();
}
