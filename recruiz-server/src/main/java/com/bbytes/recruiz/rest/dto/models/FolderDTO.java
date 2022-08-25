package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.PositionFolderLink;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FolderType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderDTO implements Serializable {

	private static final long serialVersionUID = -2190129403499015471L;

	private long id;

	private String folderName;

	private String folderDesc;
	
	private String folderOwner;

	private boolean folderPublic;

	private String folderType = FolderType.CANDIDATE_FOLDER.toString();

	private Integer sharedUserCount = 0;

	private Integer candidateCount = 0;

	private Integer positionCount = 0;

}
