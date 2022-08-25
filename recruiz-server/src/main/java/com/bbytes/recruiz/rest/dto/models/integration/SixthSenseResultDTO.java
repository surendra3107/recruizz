
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SixthSenseResultDTO implements Serializable {

	private static final long serialVersionUID = -3996937280864592915L;

	@JsonProperty("date")
	public String date;

	@JsonProperty("education")
	public String education;

	@JsonProperty("materialtype-list")
	public List<String> materialtypeList = null;

	@JsonProperty("viewkey")
	public String viewkey;

	@JsonProperty("source")
	public String source;

	@JsonProperty("experience")
	public String experience;

	@JsonProperty("title")
	public String title;

	@JsonProperty("simrescnt")
	public Integer simrescnt;

	@JsonProperty("year-list")
	public List<String> yearList = null;

	@JsonProperty("sourceURL")
	public String sourceURL;

	@JsonProperty("date-obj")
	public String dateObj;

	@JsonProperty("simres")
	public String simres;

	@JsonProperty("skill")
	public String skill;

	@JsonProperty("company")
	public String company;

	@JsonProperty("id")
	public SixthSenseIdDTO id;

	@JsonProperty("emailkey")
	public String emailkey;

	@JsonProperty("prevcompany")
	public String prevcompany;

	@JsonProperty("material-type")
	public String materialType;

	@JsonProperty("candimg")
	public String candimg;

	@JsonProperty("active")
	public String active;

	@JsonProperty("simresURL")
	public String simresURL;

	@JsonProperty("url")
	public String url;

	@JsonProperty("resumeid")
	public String resumeid;

	@JsonProperty("pageno")
	public Integer pageno;

	@JsonProperty("name")
	public String name;

	@JsonProperty("designation")
	public String designation;

	@JsonProperty("curlocation")
	public String curlocation;

	// Recruiz ATS candidate domain
	public Candidate candidate;

	public List<String> keySkills = new ArrayList<String>();

	public String currentLocation;

	public String prefLocation;

	public String totalExperince;

	public String currentCtc;

	public String currentDesignation;

	public String previousDesignation;

	public List<String> getKeySkills() {
		if (getSkill() != null && !getSkill().isEmpty()) {
			return StringUtils.commaSeparateStringToList(getSkill());
		}
		return keySkills;
	}

	public String getCurrentLocation() {
		if (getCurlocation() != null && !getCurlocation().isEmpty()) {
			// pipe separated location 0th index is current location
			List<String> locationList = StringUtils.pipeSeparateStringToList(getCurlocation());
			return locationList.size() > 0 ? locationList.get(0).trim() : null;
		}
		return null;
	}

	public String getPrefLocation() {
		if (getSkill() != null && !getSkill().isEmpty()) {
			// pipe separated location first index is preferred location
			List<String> locationList = StringUtils.pipeSeparateStringToList(getCurlocation());
			return locationList.size() > 1 ? locationList.get(1).trim() : null;
		}
		return null;
	}

	public String getTotalExperince() {
		if (getExperience() != null && !getExperience().isEmpty()) {
			// pipe separated experience 0th index is total experience
			List<String> list = StringUtils.pipeSeparateStringToList(getExperience());
			return list.size() > 0 ? list.get(0).replaceAll("Exp:", "").trim() : null;
		}
		return null;
	}

	public String getCurrentCtc() {
		if (getExperience() != null && !getExperience().isEmpty()) {
			// pipe separated experience 1st index is current ctc
			List<String> list = StringUtils.pipeSeparateStringToList(getExperience());
			return list.size() > 1 ? list.get(1).replaceAll("CTC:", "").trim() : null;
		}
		return null;
	}

	public String getCurrentDesignation() {
		if (getDesignation() != null && !getDesignation().isEmpty()) {
			// pipe separated designation 0th index is current designation
			List<String> designationList = StringUtils.pipeSeparateStringToList(getDesignation());
			return (designationList.size() > 0 && !designationList.get(0).trim().isEmpty()) ? (designationList.get(0).trim()) : "N/A";
		}
		return null;
	}

	public String getPreviousDesignation() {
		if (getDesignation() != null && !getDesignation().isEmpty()) {
			// pipe separated designation 1st index is preferred designation
			List<String> designationList = StringUtils.pipeSeparateStringToList(getDesignation());
			return (designationList.size() > 1 && !designationList.get(1).trim().isEmpty()) ? (designationList.get(1).trim()) : "N/A";
		}
		return null;
	}
}
