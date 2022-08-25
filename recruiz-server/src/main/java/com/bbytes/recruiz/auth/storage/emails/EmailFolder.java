package com.bbytes.recruiz.auth.storage.emails;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Document
public class EmailFolder implements Serializable {
	
	private static final long serialVersionUID = -462298551626365070L;
	
	@Id
	private String uniqKey;
	
	@Field
	private String folderName;
	
	@Field
	private int noOfEmails;
	
	@Field
	private String accountEmail;
	
	@Field
	private int unreadCount;
	
}

