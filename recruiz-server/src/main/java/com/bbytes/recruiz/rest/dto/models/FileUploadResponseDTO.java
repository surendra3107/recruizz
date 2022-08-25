package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class FileUploadResponseDTO implements Serializable {

	private static final long serialVersionUID = -219012943789015471L;

	private String fileName="N/A";

	private boolean success;

	private String message;
	
	private CandidateExistsResponseDTO candidateExistsResponseDTO;

	public void setResult(boolean success,String message){
		this.success=success;
		this.message=message;
	}

}
