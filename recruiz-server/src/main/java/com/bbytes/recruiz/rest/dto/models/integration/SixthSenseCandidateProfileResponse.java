package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This is combine object of search DTO and few additional attributes
 * 
 * @author akshay
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class SixthSenseCandidateProfileResponse extends SixthSenseAbstractResolve implements Serializable {

	private static final long serialVersionUID = 4397419983561620442L;

	private String profileHtml;
	
	private String profileData;

}
