package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;

@Data
public class FolderRequestDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String folderName;

	private Collection<Long> candidateIds;
	
	private Collection<String> userEmails;
	
	private Collection<String> folderNames;
	
	private Long candidateId;

}
