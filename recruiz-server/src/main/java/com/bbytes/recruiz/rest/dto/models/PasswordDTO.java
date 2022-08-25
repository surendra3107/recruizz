package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

/**
 * Password DTO Object
 * 
 * @author akshay
 *
 */
@Data
public class PasswordDTO implements Serializable {

	private static final long serialVersionUID = -8038110478828131503L;

	private String oldPassword;

	private String newPassword;

}
