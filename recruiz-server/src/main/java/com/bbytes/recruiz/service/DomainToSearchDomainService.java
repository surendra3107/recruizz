package com.bbytes.recruiz.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.ClientSearch;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;
import com.bbytes.recruiz.search.domain.PositionSearch;
import com.bbytes.recruiz.search.domain.ProspectSearch;
import com.bbytes.recruiz.search.domain.SuggestSearch;
import com.bbytes.recruiz.search.domain.UserSearch;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class DomainToSearchDomainService {

	@Autowired
	private PositionService positionService;

	public List<com.bbytes.recruiz.search.domain.ClientSearch> convertClients(List<Client> clients, String tenantName) {
		List<com.bbytes.recruiz.search.domain.ClientSearch> searchClients = new ArrayList<>();
		if (clients != null) {
			for (Client client : clients) {
				searchClients.add(convertClient(client, tenantName));
			}
		}

		return searchClients;
	}

	public com.bbytes.recruiz.search.domain.ClientSearch convertClient(Client client, String tenantName) {
		if (client == null)
			return null;

		com.bbytes.recruiz.search.domain.ClientSearch clientSearch = new com.bbytes.recruiz.search.domain.ClientSearch();
		clientSearch.setId(client.getId(), tenantName);
		clientSearch.setAddress(client.getAddress());
		clientSearch.setClientLocation(client.getClientLocation());
		clientSearch.setClientName(client.getClientName());
		clientSearch.setCreationDate(client.getCreationDate());
		clientSearch.setModificationDate(client.getModificationDate());
		clientSearch.setNotes(client.getNotes());
		clientSearch.setWebsite(client.getWebsite());
		clientSearch.setStatus(client.getStatus());

		return clientSearch;

	}

	public List<SuggestSearch> convertPositionsForSuggest(List<Position> positions, String tenantName) {
		List<SuggestSearch> suggestPositions = new ArrayList<>();
		if (positions != null) {
			for (Position position : positions) {
				suggestPositions.addAll(convertPositionForSuggest(position, tenantName));
			}
		}

		return suggestPositions;
	}

	public List<SuggestSearch> convertPositionForSuggest(Position position, String tenantName) {
		if (position == null)
			return null;

		List<SuggestSearch> finalSet = new ArrayList<>();

		SuggestSearch suggestSearch = new SuggestSearch();

		suggestSearch.setId(PositionSearch.INDEX_NAME, position.getId(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setPositionLocation(position.getLocation());
		suggestSearch.setPositionTitle(position.getTitle());
		finalSet.add(suggestSearch);

		for (String reqSkillSet : position.getReqSkillSet()) {
			SuggestSearch suggestSearchReqSkillSet = new SuggestSearch();
			suggestSearchReqSkillSet.setId(PositionSearch.INDEX_NAME + "-" + reqSkillSet, position.getId(), tenantName);
			suggestSearchReqSkillSet.setPositionReqSkillSet(reqSkillSet);
			finalSet.add(suggestSearchReqSkillSet);
		}

		for (String goodSkillSet : position.getGoodSkillSet()) {
			SuggestSearch suggestSearchGoodSkillSet = new SuggestSearch();
			suggestSearchGoodSkillSet.setId(PositionSearch.INDEX_NAME + "-" + goodSkillSet, position.getId(), tenantName);
			suggestSearchGoodSkillSet.setPositionGoodSkillSet(goodSkillSet);
			finalSet.add(suggestSearchGoodSkillSet);
		}

		return finalSet;

	}

	public List<SuggestSearch> convertProspectForSuggest(List<Prospect> prospects, String tenantName) {

		List<SuggestSearch> suggestProspects = new ArrayList<>();
		if (prospects != null) {
			for (Prospect prospect : prospects) {
				suggestProspects.addAll(convertProspectForSuggest(prospect, tenantName));
			}
		}

		return suggestProspects;
	}

	public List<SuggestSearch> convertProspectForSuggest(Prospect prospect, String tenantName) {
		if (prospect == null)
			return null;

		List<SuggestSearch> finalSet = new ArrayList<>();

		SuggestSearch suggestSearch = new SuggestSearch();
		suggestSearch.setId(ProspectSearch.INDEX_NAME, prospect.getProspectId(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setProspectCompanyName(prospect.getCompanyName());
		suggestSearch.setProspectEmail(prospect.getEmail());
		suggestSearch.setProspectLocation(prospect.getLocation());
		suggestSearch.setProspectOwner(prospect.getOwner());
		finalSet.add(suggestSearch);

		return finalSet;

	}

	public List<SuggestSearch> convertPositionRequestsForSuggest(List<PositionRequest> positionRequests, String tenantName) {
		List<SuggestSearch> suggestPositionRequests = new ArrayList<>();
		if (positionRequests != null) {
			for (PositionRequest positionRequest : positionRequests) {
				suggestPositionRequests.addAll(convertPositionRequestForSuggest(positionRequest, tenantName));
			}
		}

		return suggestPositionRequests;
	}

	public List<SuggestSearch> convertPositionRequestForSuggest(PositionRequest positionRequest, String tenantName) {
		if (positionRequest == null)
			return null;

		List<SuggestSearch> finalSet = new ArrayList<>();

		SuggestSearch suggestSearch = new SuggestSearch();
		suggestSearch.setId(PositionRequestSearch.INDEX_NAME, positionRequest.getId(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setPositionRequestLocation(positionRequest.getLocation());
		suggestSearch.setPositionRequestTitle(positionRequest.getTitle());
		finalSet.add(suggestSearch);

		for (String reqSkillSet : positionRequest.getReqSkillSet()) {
			SuggestSearch suggestSearchReqSkillSet = new SuggestSearch();
			suggestSearchReqSkillSet.setId(PositionRequestSearch.INDEX_NAME + "-" + reqSkillSet, positionRequest.getId(), tenantName);
			suggestSearchReqSkillSet.setPositionRequestReqSkillSet(reqSkillSet);
			finalSet.add(suggestSearchReqSkillSet);
		}

		for (String goodSkillSet : positionRequest.getGoodSkillSet()) {
			SuggestSearch suggestSearchGoodSkillSet = new SuggestSearch();
			suggestSearchGoodSkillSet.setId(PositionRequestSearch.INDEX_NAME + "-" + goodSkillSet, positionRequest.getId(), tenantName);
			suggestSearchGoodSkillSet.setPositionRequestGoodSkillSet(goodSkillSet);
			finalSet.add(suggestSearchGoodSkillSet);
		}

		return finalSet;

	}

	public List<SuggestSearch> convertClientsForSuggest(List<Client> clients, String tenantName) {
		List<SuggestSearch> suggestClients = new ArrayList<>();
		if (suggestClients != null) {
			for (Client client : clients) {
				suggestClients.add(convertClientForSuggest(client, tenantName));
			}
		}

		return suggestClients;
	}

	public SuggestSearch convertClientForSuggest(Client client, String tenantName) {
		if (client == null)
			return null;

		SuggestSearch suggestSearch = new SuggestSearch();
		suggestSearch.setId(ClientSearch.INDEX_NAME, client.getId(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setClientLocation(client.getClientLocation());
		suggestSearch.setClientName(client.getClientName());

		return suggestSearch;

	}

	public List<SuggestSearch> convertUsersForSuggest(List<User> users, String tenantName) {
		List<SuggestSearch> suggestUsers = new ArrayList<>();
		if (suggestUsers != null) {
			for (User user : users) {
				suggestUsers.add(convertUserForSuggest(user, tenantName));
			}
		}

		return suggestUsers;
	}

	public SuggestSearch convertUserForSuggest(User user, String tenantName) {
		if (user == null)
			return null;

		SuggestSearch suggestSearch = new SuggestSearch();
		suggestSearch.setId(UserSearch.INDEX_NAME, user.getUserId(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setUserAppEmail(user.getEmail());
		suggestSearch.setUserAppName(user.getName());

		return suggestSearch;

	}

	public List<com.bbytes.recruiz.search.domain.CandidateSearch> convertCandidates(List<Candidate> candidates, String tenantName) {
		List<com.bbytes.recruiz.search.domain.CandidateSearch> searchCandidates = new ArrayList<>();
		if (candidates != null) {
			for (Candidate candidate : candidates) {
				searchCandidates.add(convertCandidate(candidate, tenantName));
			}
		}

		return searchCandidates;
	}

	public com.bbytes.recruiz.search.domain.CandidateSearch convertCandidate(Candidate candidate, String tenantName) {
		if (candidate == null)
			return null;

		com.bbytes.recruiz.search.domain.CandidateSearch candidateSearch = new com.bbytes.recruiz.search.domain.CandidateSearch();
		candidateSearch.setId(candidate.getCid(), tenantName);

		candidateSearch.setComments(candidate.getComments());
		candidateSearch.setCandidateId(candidate.getCandidateRandomId());
		candidateSearch.setCommunication(candidate.getCommunication());
		candidateSearch.setCreationDate(candidate.getCreationDate());
		candidateSearch.setCtcUnit(candidate.getCtcUnit());
		candidateSearch.setCurrentCompany(candidate.getCurrentCompany());
		candidateSearch.setPreviousCompany(candidate.getPreviousEmployment());
		candidateSearch.setCurrentCtc(candidate.getCurrentCtc());
		candidateSearch.setCurrentLocation(candidate.getCurrentLocation());
		candidateSearch.setCurrentTitle(candidate.getCurrentTitle());
		candidateSearch.setDob(candidate.getDob());
		candidateSearch.setEmail(candidate.getEmail());
		candidateSearch.setEmploymentType(candidate.getEmploymentType());
		candidateSearch.setExpectedCtc(candidate.getExpectedCtc());
		candidateSearch.setFacebookProf(candidate.getFacebookProf());
		candidateSearch.setFullName(candidate.getFullName());
		candidateSearch.setGender(candidate.getGender());
		candidateSearch.setGithubProf(candidate.getGithubProf());
		candidateSearch.setHighestQual(candidate.getHighestQual());
		candidateSearch.setKeySkills(candidate.getKeySkills());
		candidateSearch.setLastWorkingDay(candidate.getLastWorkingDay());
		candidateSearch.setLinkedinProf(candidate.getLinkedinProf());
		candidateSearch.setMobile(candidate.getMobile());
		candidateSearch.setModificationDate(candidate.getModificationDate());
		candidateSearch.setSourcedOnDate(candidate.getSourcedOnDate());
		candidateSearch.setNoticePeriod(candidate.getNoticePeriod());
		candidateSearch.setNoticeStatus(candidate.isNoticeStatus());
		candidateSearch.setPreferredLocation(candidate.getPreferredLocation());
		candidateSearch.setResumeLink(candidate.getResumeLink());
		candidateSearch.setSource(candidate.getSource());
		candidateSearch.setStatus(candidate.getStatus());
		candidateSearch.setTotalExp(candidate.getTotalExp());
		candidateSearch.setTwitterProf(candidate.getTwitterProf());

		candidateSearch.setOwnerEmail(candidate.getOwner());
		candidateSearch.setSourceEmail(candidate.getSourceEmail());
		candidateSearch.setActualSource(candidate.getSource());

		for (CandidateEducationDetails educationDetails : candidate.getEducationDetails()) {
			if (StringUtils.isValid(educationDetails.getCollege()))
				candidateSearch.getEducationalInstitute().add(educationDetails.getCollege());
			if (StringUtils.isValid(educationDetails.getDegree()))
				candidateSearch.getEducationalQualification().add(educationDetails.getDegree());
		}

		if (candidate.getCustomField() != null && !candidate.getCustomField().isEmpty()) {
			for (String customFieldKey : candidate.getCustomField().keySet()) {
				candidateSearch.getCustomField().add(customFieldKey +" : " + candidate.getCustomField().get(customFieldKey));
			}
		}

			if (candidate.getResumeLink() != null) {
				File resume = new File(candidate.getResumeLink());
				if (resume.exists()) {
					try {
						String resumeContent = FileUtils.readPdfText(resume);
						candidateSearch.setResumeContent(resumeContent);
					} catch (Exception e) {
						// do nothing
					}

				}
			}

		if (candidate.getSourcedOnDate() == null) {
			candidateSearch.setSourcedOnDate(candidate.getCreationDate());
		} else {
			candidateSearch.setSourcedOnDate(candidate.getSourcedOnDate());
		}

		if (candidate.getResumeLink() != null) {
			File resume = new File(candidate.getResumeLink());
			if (resume.exists()) {
				try {
					String resumeContent = FileUtils.readPdfText(resume);
					candidateSearch.setResumeContent(resumeContent);
				} catch (Exception e) {
					// do nothing
				}

			}
		}

		return candidateSearch;

	}

	public List<SuggestSearch> convertCandidatesForSuggest(List<Candidate> candidates, String tenantName) {
		List<SuggestSearch> suggestCandidates = new ArrayList<>();
		if (candidates != null) {
			for (Candidate candidate : candidates) {
				suggestCandidates.addAll(convertCandidateForSuggest(candidate, tenantName));
			}
		}

		return suggestCandidates;
	}

	public List<SuggestSearch> convertCandidateForSuggest(Candidate candidate, String tenantName) {
		if (candidate == null)
			return null;

		List<SuggestSearch> finalSet = new ArrayList<>();

		SuggestSearch suggestSearch = new SuggestSearch();
		suggestSearch.setId(CandidateSearch.INDEX_NAME, candidate.getCid(), tenantName);
		suggestSearch.setTenantName(tenantName);
		suggestSearch.setCandidateCurrentCompany(candidate.getCurrentCompany());
		suggestSearch.setCandidateCurrentLocation(candidate.getCurrentLocation());
		suggestSearch.setCandidateEmail(candidate.getEmail());
		suggestSearch.setCandidateFullName(candidate.getFullName());
		suggestSearch.setCandidatePreferredLocation(candidate.getPreferredLocation());
		finalSet.add(suggestSearch);

		for (String skillSet : candidate.getKeySkills()) {
			SuggestSearch suggestCandidateSkillSet = new SuggestSearch();
			suggestCandidateSkillSet.setId(CandidateSearch.INDEX_NAME + "-" + skillSet, candidate.getCid(), tenantName);
			suggestCandidateSkillSet.setCandidateSkillSuggest(skillSet);
			finalSet.add(suggestCandidateSkillSet);
		}

		for (CandidateEducationDetails candidateEducationDetails : candidate.getEducationDetails()) {
			SuggestSearch suggestCandidateEducation = new SuggestSearch();
			suggestCandidateEducation.setId(CandidateSearch.INDEX_NAME + "-" + candidateEducationDetails.getCollege(), candidate.getCid(),
					tenantName);
			suggestCandidateEducation.setCandidateEducationalInstitute(candidateEducationDetails.getCollege());
			suggestCandidateEducation.setCandidateEducationalQualification(candidateEducationDetails.getDegree());
			finalSet.add(suggestCandidateEducation);
		}

		return finalSet;

	}

	public List<com.bbytes.recruiz.search.domain.PositionSearch> convertPositions(List<Position> positions, String tenantName) {
		List<com.bbytes.recruiz.search.domain.PositionSearch> searchPositions = new ArrayList<>();
		if (positions != null) {
			for (Position position : positions) {
				searchPositions.add(convertPosition(position, tenantName));
			}
		}

		return searchPositions;
	}

	public com.bbytes.recruiz.search.domain.PositionSearch convertPosition(Position position, String tenantName) {
		if (position == null)
			return null;

		com.bbytes.recruiz.search.domain.PositionSearch positionSearch = new com.bbytes.recruiz.search.domain.PositionSearch();
		positionSearch.setId(position.getId(), tenantName);
		positionSearch.setCloseByDate(position.getCloseByDate());
		positionSearch.setCreationDate(position.getCreationDate());
		positionSearch.setDescription(position.getDescription());
		positionSearch.setGoodSkillSet(position.getGoodSkillSet());
		positionSearch.setReqSkillSet(position.getReqSkillSet());
		positionSearch.setLocation(position.getLocation());
		positionSearch.setMaxSal(position.getMaxSal());
		positionSearch.setMinSal(position.getMinSal());
		positionSearch.setMinExp(position.getMinExp());
		positionSearch.setMaxExp(position.getMaxExp());
		positionSearch.setModificationDate(position.getModificationDate());
		positionSearch.setNotes(position.getNotes());
		positionSearch.setOpenedDate(position.getOpenedDate());
		positionSearch.setPositionCode(position.getPositionCode());
		positionSearch.setPositionUrl(position.getPositionUrl());
		positionSearch.setRemoteWork(position.isRemoteWork());
		positionSearch.setStatus(position.getStatus());
		positionSearch.setTitle(position.getTitle());
		positionSearch.setTotalPosition(position.getTotalPosition());
		positionSearch.setType(position.getType());
		positionSearch.addVendorEmails(position.getVendors());

		positionService.calculateFinalStatusForPosition(position);
		positionSearch.setFinalStatus(position.getFinalStatus());

		return positionSearch;

	}

	public List<com.bbytes.recruiz.search.domain.ProspectSearch> convertProspects(List<Prospect> prospects, String tenantName) {
		List<com.bbytes.recruiz.search.domain.ProspectSearch> searchProspects = new ArrayList<>();
		if (prospects != null) {
			for (Prospect prospect : prospects) {
				searchProspects.add(convertProspect(prospect, tenantName));
			}
		}

		return searchProspects;
	}

	public com.bbytes.recruiz.search.domain.ProspectSearch convertProspect(Prospect prospect, String tenantName) {
		if (prospect == null)
			return null;

		com.bbytes.recruiz.search.domain.ProspectSearch prospectSearch = new com.bbytes.recruiz.search.domain.ProspectSearch();
		prospectSearch.setId(prospect.getProspectId(), tenantName);
		prospectSearch.setCreationDate(prospect.getCreationDate());

		prospectSearch.setAddress(prospect.getAddress());
		prospectSearch.setCategory(prospect.getCategory());
		prospectSearch.setCompanyName(prospect.getCompanyName());
		prospectSearch.setCurrency(prospect.getCurrency());
		prospectSearch.setDealSize(prospect.getDealSize());
		prospectSearch.setDesignation(prospect.getDesignation());
		prospectSearch.setEmail(prospect.getEmail());
		prospectSearch.setIndustry(prospect.getIndustry());
		prospectSearch.setLocation(prospect.getLocation());
		prospectSearch.setMobile(prospect.getMobile());
		prospectSearch.setMode(prospect.getMode());
		prospectSearch.setName(prospect.getName());
		prospectSearch.setOwner(prospect.getOwner());
		prospectSearch.setPercentage(prospect.getPercentage());
		prospectSearch.setProspectRating(prospect.getProspectRating());
		prospectSearch.setSource(prospect.getSource());
		prospectSearch.setStatus(prospect.getStatus());
		prospectSearch.setValue(prospect.getValue());
		prospectSearch.setWebsite(prospect.getWebsite());

		return prospectSearch;

	}

	public List<com.bbytes.recruiz.search.domain.PositionRequestSearch> convertPositionRequests(List<PositionRequest> positionRequests,
			String tenantName) {
		List<com.bbytes.recruiz.search.domain.PositionRequestSearch> searchPositionRequests = new ArrayList<>();
		if (positionRequests != null) {
			for (PositionRequest positionRequest : positionRequests) {
				searchPositionRequests.add(convertPositionRequest(positionRequest, tenantName));
			}
		}

		return searchPositionRequests;
	}

	public com.bbytes.recruiz.search.domain.PositionRequestSearch convertPositionRequest(PositionRequest positionRequest,
			String tenantName) {
		if (positionRequest == null)
			return null;

		com.bbytes.recruiz.search.domain.PositionRequestSearch positionRequestSearch = new com.bbytes.recruiz.search.domain.PositionRequestSearch();
		positionRequestSearch.setId(positionRequest.getId(), tenantName);
		positionRequestSearch.setCloseByDate(positionRequest.getCloseByDate());
		positionRequestSearch.setCreationDate(positionRequest.getCreationDate());
		positionRequestSearch.setDescription(positionRequest.getDescription());
		positionRequestSearch.setPositionRequestGoodSkillSet(positionRequest.getGoodSkillSet());
		positionRequestSearch.setPositionRequestReqSkillSet(positionRequest.getReqSkillSet());
		positionRequestSearch.setPositionRequestLocation(positionRequest.getLocation());
		positionRequestSearch.setMaxSal(positionRequest.getMaxSal());
		positionRequestSearch.setModificationDate(positionRequest.getModificationDate());
		positionRequestSearch.setNotes(positionRequest.getNotes());
		positionRequestSearch.setOpenedDate(positionRequest.getOpenedDate());
		positionRequestSearch.setPositionCode(positionRequest.getPositionCode());
		positionRequestSearch.setPositionUrl(positionRequest.getPositionUrl());
		positionRequestSearch.setRemoteWork(positionRequest.isRemoteWork());
		positionRequestSearch.setStatus(positionRequest.getStatus());
		positionRequestSearch.setPositionRequestTitle(positionRequest.getTitle());
		positionRequestSearch.setTotalPosition(positionRequest.getTotalPosition());
		positionRequestSearch.setType(positionRequest.getType());

		return positionRequestSearch;

	}

	public List<com.bbytes.recruiz.search.domain.UserSearch> convertUsers(List<User> users, String tenantName) {
		List<com.bbytes.recruiz.search.domain.UserSearch> searchUsers = new ArrayList<>();
		if (searchUsers != null) {
			for (User user : users) {
				searchUsers.add(convertUser(user, tenantName));
			}
		}

		return searchUsers;
	}

	public com.bbytes.recruiz.search.domain.UserSearch convertUser(User user, String tenantName) {

		com.bbytes.recruiz.search.domain.UserSearch userSearch = new com.bbytes.recruiz.search.domain.UserSearch();

		if (user == null)
			return null;

		userSearch.setId(user.getUserId(), tenantName);
		userSearch.setAccountStatus(user.getAccountStatus());
		userSearch.setCreationDate(user.getCreationDate());
		userSearch.setEmail(user.getEmail());
		userSearch.setJoinedDate(user.getJoinedDate());
		userSearch.setJoinedStatus(user.getJoinedStatus());
		userSearch.setModificationDate(user.getModificationDate());
		userSearch.setName(user.getName());
		userSearch.setProfileUrl(user.getProfileUrl());

		return userSearch;

	}

	public AdvancedSearchQueryEntity convertToPersistEntity(AdvancedSearchQueryEntity searchQuery) {
		searchQuery.setNoticePeriod(StringUtils.arrayToString(searchQuery.getNoticePeriodList()));
		searchQuery.setCurrLocation(StringUtils.arrayToString(searchQuery.getCurrLocationList()));
		searchQuery.setPrefLocation(StringUtils.arrayToString(searchQuery.getPrefLocationList()));
		searchQuery.setSource(StringUtils.arrayToString(searchQuery.getPortalSourceList()));
		searchQuery.setPostPGDegree(StringUtils.arrayToString(searchQuery.getPostPGDegreeList()));
		searchQuery.setPostPGDegreeSpecialization(StringUtils.arrayToString(searchQuery.getPostPGDegreeSpecList()));
		searchQuery.setPostPGDegreeType(StringUtils.arrayToString(searchQuery.getPostPGDegreeTypeList()));
		searchQuery.setPgDegree(StringUtils.arrayToString(searchQuery.getPgDegreeList()));
		searchQuery.setPgDegreeSpecialization(StringUtils.arrayToString(searchQuery.getPgDegreeSpecList()));
		searchQuery.setPgDegreeType(StringUtils.arrayToString(searchQuery.getPgDegreeTypeList()));
		searchQuery.setUgDegree(StringUtils.arrayToString(searchQuery.getUgDegreeList()));
		searchQuery.setUgDegreeSpecialization(StringUtils.arrayToString(searchQuery.getUgDegreeSpecList()));
		searchQuery.setUgDegreeType(StringUtils.arrayToString(searchQuery.getUgDegreeTypeList()));
		searchQuery.setUniversityDegree(StringUtils.arrayToString(searchQuery.getUniversityDegreeList()));
		searchQuery.setIndustry(StringUtils.arrayToString(searchQuery.getIndustryList()));
		searchQuery.setFunctionalArea(StringUtils.arrayToString(searchQuery.getFunctionalAreaList()));
		searchQuery.setFuncRole(StringUtils.arrayToString(searchQuery.getFuncRoleList()));
		searchQuery.setPassYearDegree(StringUtils.arrayToString(searchQuery.getPassYearDegreeList()));
		return searchQuery;
	}

	public AdvancedSearchQueryEntity convertFromPersistEntity(AdvancedSearchQueryEntity searchQuery) {
		searchQuery.setNoticePeriodList(StringUtils.stringToArray(searchQuery.getNoticePeriod()));
		searchQuery.setCurrLocationList(StringUtils.stringToArray(searchQuery.getCurrLocation()));
		searchQuery.setPrefLocationList(StringUtils.stringToArray(searchQuery.getPrefLocation()));
		searchQuery.setPortalSourceList(StringUtils.stringToArray(searchQuery.getSource()));
		searchQuery.setPostPGDegreeList(StringUtils.stringToArray(searchQuery.getPostPGDegree()));
		searchQuery.setPostPGDegreeSpecList(StringUtils.stringToArray(searchQuery.getPostPGDegreeSpecialization()));
		searchQuery.setPostPGDegreeTypeList(StringUtils.stringToArray(searchQuery.getPostPGDegreeType()));
		searchQuery.setPgDegreeList(StringUtils.stringToArray(searchQuery.getPgDegree()));
		searchQuery.setPgDegreeSpecList(StringUtils.stringToArray(searchQuery.getPgDegreeSpecialization()));
		searchQuery.setPgDegreeTypeList(StringUtils.stringToArray(searchQuery.getPgDegreeType()));
		searchQuery.setUgDegreeList(StringUtils.stringToArray(searchQuery.getUgDegree()));
		searchQuery.setUgDegreeSpecList(StringUtils.stringToArray(searchQuery.getUgDegreeSpecialization()));
		searchQuery.setUgDegreeTypeList(StringUtils.stringToArray(searchQuery.getUgDegreeType()));
		searchQuery.setUniversityDegreeList(StringUtils.stringToArray(searchQuery.getUniversityDegree()));
		searchQuery.setIndustryList(StringUtils.stringToArray(searchQuery.getIndustry()));
		searchQuery.setFunctionalAreaList(StringUtils.stringToArray(searchQuery.getFunctionalArea()));
		searchQuery.setFuncRoleList(StringUtils.stringToArray(searchQuery.getFuncRole()));
		searchQuery.setPassYearDegreeList(StringUtils.stringToArray(searchQuery.getPassYearDegree()));
		return searchQuery;
	}

}
