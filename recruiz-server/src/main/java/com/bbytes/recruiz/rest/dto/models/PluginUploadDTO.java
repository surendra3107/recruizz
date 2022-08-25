package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import com.bbytes.recruiz.utils.StringUtils;

import lombok.Data;

@Data
public class PluginUploadDTO implements Serializable {

	private static final long serialVersionUID = -219012943789015471L;

	private String source;
	private String positionCode;
	private String candidateEmail;
	private String candidateMobile;
	private String candidateName;

	private String currentExp;
	private String currentLoc;
	private String prefLoc;
	private String industry;
	private String noticePeriod;
	private double cctc = 0;
	private String language;
	private String address;
	private String dob;
	private String gender;
	private String alternateMobile;
	private String prevEmployment;
	private String currentTitle;

	private String fileHtmlContent;
	private String fileName;
	private String pluginName;
	private String keySkills;
	private String totalExp;
	
	public int getNoticePeriod() {
		return StringUtils.parseNoticePeriodString(noticePeriod);
	}

}
