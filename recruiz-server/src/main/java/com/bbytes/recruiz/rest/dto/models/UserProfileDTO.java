package com.bbytes.recruiz.rest.dto.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfileDTO extends UserDTO {

	private static final long serialVersionUID = 259655037328459988L;

	private String updatedXAuthToken;

}
