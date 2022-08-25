package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "email_activity")
public class EmailActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(columnDefinition="longtext")
	private String subject;

	@Column(columnDefinition="longtext")
	private String body;

	@JsonProperty(access=Access.WRITE_ONLY)
	@Column(columnDefinition="longtext")
	private String emailTo;
	
	@JsonProperty(access=Access.WRITE_ONLY)
	@Column(columnDefinition="longtext")
	private String cc;

	private String emailFrom;

	@Column
	private Date date;
	
	@JsonProperty(access=Access.WRITE_ONLY)
	@Column(columnDefinition="longtext")
	private String attachmentLink;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private List<String> toEmails;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private List<String> ccEmails;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private List<String> attachments;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private String pcode;
	
	public void setToEmails(String toEmails) {
	    if(null != toEmails) {
		this.toEmails = StringUtils.commaSeparateStringToList(toEmails);
	    }
	}
	
	public void setCcEmails(String ccEmails) {
	    if(null != ccEmails) {
		this.ccEmails = StringUtils.commaSeparateStringToList(ccEmails);
	    }
	}
	
	public void setAttachments(String attachments) {
	    if(null != attachments) {
		this.attachments = StringUtils.commaSeparateStringToList(attachments);
	    }
	}

}
