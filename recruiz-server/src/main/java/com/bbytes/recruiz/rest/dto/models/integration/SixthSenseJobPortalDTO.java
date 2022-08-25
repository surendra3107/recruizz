
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.bbytes.recruiz.enums.ViewUsageType;

import lombok.Data;

@Data
public class SixthSenseJobPortalDTO implements Serializable {

	private static final long serialVersionUID = 6409933581448324607L;

	private String email;
	
	private Set<String> sources = new HashSet<String>();

	private String usageType = ViewUsageType.UNLIMITED_VIEW.toString();

	private int viewCount = -1;

}
