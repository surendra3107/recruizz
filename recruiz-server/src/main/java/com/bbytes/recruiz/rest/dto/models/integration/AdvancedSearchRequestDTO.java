package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AdvancedSearchRequestDTO implements Serializable {

	private static final long serialVersionUID = -5597832627556109085L;

	private String queryName;

	private String tab;

	private String searchType;

	private String source;

	private String basicSearch;

	private String booleanQuery;

	private String allKeyword;

	private String anyKeyword;

	private String excludeKeyword;

	private String searchIn;

	private String show;

	private String jobType;

	private String jobStatus;

	private String resumeFreshness;

	private String sortBy;

	private double minExp;

	private double maxExp;

	private double minSalary;

	private double maxSalary;

	private boolean includeZeroSalary;

	private String currLocation;

	private String prefLocation;

	private String currentPrefLocJoinType;

	private boolean exactPrefLocation;

	private String postPGDegree;

	private String postPGDegreeSpecialization;

	private String postPGDegreeType;

	private String pgPostPGJoinType;

	private String pgDegree;

	private String pgDegreeSpecialization;

	private String pgDegreeType;

	private String ugPGJoinType;

	private String ugDegree;

	private String ugDegreeSpecialization;

	private String ugDegreeType;

	private boolean highestDegree;

	private String university;

	private String universityDegree;

	private double passYearFrom;

	private double passYearTo;

	private String passYearDegree;

	private String industry;

	private String functionalArea;

	private String funcRole;

	private String designation;

	private String designationType;

	private String includeCompany;

	private String includeCompanyType;

	private String excludeCompany;

	private String excludeCompanyType;

	private String noticePeriod;

	private int minAge;

	private int maxAge;

	private boolean femaleCandidate;

	private boolean specialAbility;

	private boolean preminumResume;

	private boolean smsEnable;

	private boolean verifiedMobile;

	private boolean verifiedEmail;

	private boolean attachResume;

	private boolean resumeNotViewed;

	private boolean profileWithPhoto;

	private boolean excludeConfidentialResume;

	private List<String> noticePeriodList = new ArrayList<String>();

	private List<String> currLocationList = new ArrayList<String>();

	private List<String> prefLocationList = new ArrayList<String>();

	private List<String> portalSourceList = new ArrayList<String>();

	private List<String> postPGDegreeList = new ArrayList<String>();

	private List<String> postPGDegreeSpecList = new ArrayList<String>();

	private List<String> postPGDegreeTypeList = new ArrayList<String>();

	private List<String> pgDegreeList = new ArrayList<String>();

	private List<String> pgDegreeSpecList = new ArrayList<String>();

	private List<String> pgDegreeTypeList = new ArrayList<String>();

	private List<String> ugDegreeList = new ArrayList<String>();

	private List<String> ugDegreeSpecList = new ArrayList<String>();

	private List<String> ugDegreeTypeList = new ArrayList<String>();

	private List<String> universityDegreeList = new ArrayList<String>();

	private List<String> industryList = new ArrayList<String>();

	private List<String> functionalAreaList = new ArrayList<String>();

	private List<String> funcRoleList = new ArrayList<String>();

	private List<String> passYearDegreeList = new ArrayList<String>();

	public List<String> getNoticePeriodList() {
		if (noticePeriodList == null)
			noticePeriodList = new ArrayList<>();
		return noticePeriodList;
	}

	public List<String> getCurrLocationList() {
		if (currLocationList == null)
			currLocationList = new ArrayList<>();
		return currLocationList;
	}

	public List<String> getPrefLocationList() {
		if (prefLocationList == null)
			prefLocationList = new ArrayList<>();
		return prefLocationList;
	}

	public List<String> getPortalSourceList() {
		if (portalSourceList == null)
			portalSourceList = new ArrayList<>();
		return portalSourceList;
	}

	public void setNoticePeriodList(List<String> noticePeriodList) {
		this.noticePeriodList = noticePeriodList;
	}

	public void setCurrLocationList(List<String> currLocationList) {
		this.currLocationList = currLocationList;
	}

	public void setPrefLocationList(List<String> prefLocationList) {
		this.prefLocationList = prefLocationList;
	}

	public void setPortalSourceList(List<String> portalSourceList) {
		this.portalSourceList = portalSourceList;
	}

	public String getCurrentPrefLocJoinType() {
		if (("And").equals(currentPrefLocJoinType))
			return currentPrefLocJoinType = "1";
		else
			return currentPrefLocJoinType = "2";
	}

	public String getPgPostPGJoinType() {
		if (("And").equals(pgPostPGJoinType))
			return pgPostPGJoinType = "1";
		else
			return pgPostPGJoinType = "2";
	}

	public String getUgPGJoinType() {
		if (("And").equals(ugPGJoinType))
			return ugPGJoinType = "1";
		else
			return ugPGJoinType = "2";
	}

}
