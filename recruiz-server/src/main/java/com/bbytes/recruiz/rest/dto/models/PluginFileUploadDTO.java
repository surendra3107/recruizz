package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PluginFileUploadDTO extends PluginUploadDTO implements Serializable {
	
	private static final long serialVersionUID = 8716381724880795879L;
	
	private MultipartFile file;

}
