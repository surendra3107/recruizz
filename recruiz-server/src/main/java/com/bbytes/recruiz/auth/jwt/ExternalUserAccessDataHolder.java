package com.bbytes.recruiz.auth.jwt;

import lombok.Data;

@Data
public final class ExternalUserAccessDataHolder {

	// External user info
	private String extenalUserEmail;

	private String extenalUserName;

	private String extenalUserMobile;

}
