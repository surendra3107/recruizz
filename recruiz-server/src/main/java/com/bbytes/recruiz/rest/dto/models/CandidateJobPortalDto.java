package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CandidateJobPortalDto implements Serializable {

	 public String jobTitle;
	 public String name; 
	 public Double totalExperience;
	 public String tenant;
     public String email;
	 public String mobile;
	 public MultipartFile resume;
	 public Double currentCtc;
	 public String location;
	 public String pastExperience;
	 public Integer noticePeriod;
	 public String education;
	 public String jobPortal;
     public String keySkills;
	
}
