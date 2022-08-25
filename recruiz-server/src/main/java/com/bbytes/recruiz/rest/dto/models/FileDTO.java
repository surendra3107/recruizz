package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Candidate resume uploaded as file is sent using this dto to be added to
 * candidate database or position
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String fileContent;

	private String fileName;

	private String fileType;
}
