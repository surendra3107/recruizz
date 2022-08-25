package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Candidate resume uploaded as file is sent using this dto to be added to
 * candidate database or position
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadRequestDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private byte[] filebytes;

	private String fileName;

	private String source;

	private String sourceDetails;

	private Date creationDate;

	private boolean overwrite = false;
	
	private boolean candidateUpdated = false;
}
