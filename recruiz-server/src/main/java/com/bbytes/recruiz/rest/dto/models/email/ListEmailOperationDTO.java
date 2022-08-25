package com.bbytes.recruiz.rest.dto.models.email;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ListEmailOperationDTO implements Serializable {

	private static final long serialVersionUID = -462298551626365070L;

	private List<Long> messageUids;
	
	private String folderName;
	
	private Long emailClientId;
	
	private String status;  // read or unread
	
	
}
