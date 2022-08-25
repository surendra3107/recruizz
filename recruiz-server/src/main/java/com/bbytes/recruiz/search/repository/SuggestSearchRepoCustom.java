package com.bbytes.recruiz.search.repository;

import java.util.List;

import com.bbytes.recruiz.exception.RecruizException;

public interface SuggestSearchRepoCustom {
	
	public List<String> userAppSuggestName(String name) throws RecruizException;

	public List<String> userAppSuggestEmail(String email) throws RecruizException;

	public List<String> clientSuggestLocation(String location) throws RecruizException;

	public List<String> clientSuggestName(String name) throws RecruizException;

	public List<String> candidateSuggestFullName(String fullName) throws RecruizException;

	public List<String> candidateSuggestEmail(String email) throws RecruizException;

	public List<String> candidateSuggestSkills(String skillText) throws RecruizException;
	
	public List<String> candidateSuggestSkillsForAdvanceSearch(String skillText) throws RecruizException;

	public List<String> candidateSuggestPreferredLocation(String prefLocation) throws RecruizException;

	public List<String> candidateSuggestCurrentLocation(String currLocation) throws RecruizException;

	public List<String> candidateSuggestCurrentCompany(String currCompany) throws RecruizException;

	public List<String> candidateSuggestEducationalQualification(String educationalQualification) throws RecruizException;

	public List<String> candidateSuggestEducationalInstitute(String educationalInstitute) throws RecruizException;

	public List<String> positionSuggestLocation(String location) throws RecruizException;

	public List<String> positionSuggestTitle(String title) throws RecruizException;

	public List<String> positionSuggestSkills(String skill) throws RecruizException;

	public List<String> positionRequestSuggestLocation(String location) throws RecruizException;

	public List<String> positionRequestSuggestTitle(String title) throws RecruizException;

	public List<String> positionRequestSuggestSkills(String skill) throws RecruizException;
	
	public List<String> prospectSuggestLocation(String location) throws RecruizException;
	
	public List<String> prospectSuggestCompanyName(String name) throws RecruizException;
	

}
