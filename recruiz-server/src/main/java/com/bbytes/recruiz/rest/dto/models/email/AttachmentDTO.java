package com.bbytes.recruiz.rest.dto.models.email;

import java.io.InputStream;
import java.io.Serializable;

import lombok.Data;

@Data
public class AttachmentDTO implements Serializable {

	private static final long serialVersionUID = -462298551626365070L;

	private String contentType;
	
	private String fileName;
	
	private InputStream stream;
	
	private byte[] fileBytes;
	
	private String base64EnocdedString;
	
	

}
