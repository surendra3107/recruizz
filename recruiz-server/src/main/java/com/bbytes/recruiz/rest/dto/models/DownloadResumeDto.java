package com.bbytes.recruiz.rest.dto.models;

import java.io.File;

import lombok.Data;

@Data
public class DownloadResumeDto {

	String message;
	
	int responseCode;
	
	File file;
	
}
