package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ComposeEmailDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	List<String> reciepent;
	List<String> cc;
	List<String> bcc;
	String body;
	String subject;

	//Map<String,String> fileMap = new HashMap<String,String>();
	
	Object fileMap;
	
	Long uid;
	
	String folderName;
}
