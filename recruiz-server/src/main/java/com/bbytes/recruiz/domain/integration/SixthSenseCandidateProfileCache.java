package com.bbytes.recruiz.domain.integration;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * This Domain is used to cache the candidate profile html from sixth sense
 * 
 * @author akshay
 *
 */

@Data
@Document(collection = "ss_candidate_profile_cache")
public class SixthSenseCandidateProfileCache implements Serializable {

	private static final long serialVersionUID = -5114356307901480534L;

	@Id
	private String id;

	@Field("resume_id")
	@Indexed
	private String resumeId;

	@Field("tenant_id")
	@Indexed
	private String tenantId;

	@Field("source")
	private String source;

	@Field("profile_url")
	private String profileUrl;
	
	@Field("profile_data")
	private String profileData;

	@Field("html_profile")
	private String htmlProfile;
	
	@Field("html_profile_data")
	private String htmlProfileData;

	@CreatedDate
	private Date creationDate = new Date();

	@LastModifiedDate
	private Date lastModified = new Date();
}
