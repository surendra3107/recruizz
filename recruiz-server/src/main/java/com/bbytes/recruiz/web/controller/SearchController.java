package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.CustomFieldDetails;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.AdvancedSearchType;
import com.bbytes.recruiz.enums.CloseByDateRange;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.ExpectedCTCRange;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.NoticePeriodRange;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.AdvancedSearchDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateSearchDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.ClientSearchDTO;
import com.bbytes.recruiz.rest.dto.models.PositionSearchDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectSearchDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.SkillSuggestDTO;
import com.bbytes.recruiz.search.builder.CandidateSearchBuilder;
import com.bbytes.recruiz.search.builder.ClientSearchBuilder;
import com.bbytes.recruiz.search.builder.PositionRequestSearchBuilder;
import com.bbytes.recruiz.search.builder.PositionSearchBuilder;
import com.bbytes.recruiz.search.builder.ProspectSearchBuilder;
import com.bbytes.recruiz.search.domain.GlobalSearchResult;
import com.bbytes.recruiz.service.AdvancedSearchService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DomainToSearchDomainService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.SearchService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Search Controller, It will communicate with elastic search service.
 * 
 * @author akshay
 *
 */
@RestController
public class SearchController {

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private SearchService searchService;
	
	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private AdvancedSearchService advancedSearchService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private UserService userService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	/**
	 * API to get possible full global search of clients.
	 * 
	 * @param queryString
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/client/global", method = RequestMethod.GET)
	public RestResponse getGlobalSearchResultForClient(
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetGlobalSearchResultForClient.name());*/

		if (searchText == null || searchText.isEmpty())
			return null;

		Pageable pageable = pageableService.getSearchPageRequestObject(pageNo, sortField);
		Page<ClientOpeningCountDTO> result = searchService.globalSearchClient(searchText, pageable);

		return new RestResponse(RestResponse.SUCCESS, result);

	}

	/**
	 * API to get possible full global search of clients.
	 * 
	 * @param queryString
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/prospect/global", method = RequestMethod.GET)
	public RestResponse getGlobalSearchResultForProspect(
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetGlobalSearchResultForProspect.name());*/

		if (searchText == null || searchText.isEmpty())
			return null;

		Pageable pageable = pageableService.getSearchPageRequestObject(pageNo, sortField);
		Page<Prospect> result = searchService.globalSearchProspect(searchText, pageable);

		return new RestResponse(RestResponse.SUCCESS, result);

	}

	/**
	 * API to get possible full global search of positions.
	 * 
	 * @param queryString
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position/global", method = RequestMethod.GET)
	public RestResponse getGlobalSearchResultForPosition(
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetGlobalSearchResultForPosition.name());*/

		if (searchText == null || searchText.isEmpty())
			return null;

		Pageable pageable = pageableService.getSearchPageRequestObject(pageNo, sortField);
		Page<Position> result = searchService.globalSearchPosition(searchText, pageable);

		if (result.getContent() != null && !result.getContent().isEmpty()) {
			for (Position position : result) {
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getVendors().size();
				// adding board candidate count here
				Map<String, String> boardCandidateCount = roundCandidateService
						.getBoardCandidateCount(position.getPositionCode());
				position.setBoardCandidateCount(boardCandidateCount);
			}
		}

		positionService.calculateFinalStatusForPositions(result.getContent());

		return new RestResponse(RestResponse.SUCCESS, result);

	}

	/**
	 * API to get possible full global search of positions.
	 * 
	 * @param queryString
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate/global", method = RequestMethod.GET)
	public RestResponse getGlobalSearchResultForCandidate(
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetGlobalSearchResultForCandidate.name());*/

		if (searchText == null || searchText.isEmpty())
			return null;

		Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField);
		Page<Candidate> result = searchService.globalSearchCandidate(searchText, pageable);
		candidateService.attachCurrentPosition(result.getContent());

		return new RestResponse(RestResponse.SUCCESS, result);

	}

	/**
	 * API for query result action for select all in global search
	 * 
	 * @param queryString
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/search/candidate/global/queryresult", method = RequestMethod.POST)
	public RestResponse selectAllQueryActionForCandidateGlobalSearch(
			@RequestPart("json") @Valid EmailActivity emailActivity,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "sourceMode", required = false) String sourceMode,
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "action") String action) throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllQueryActionForCandidateGlobalSearch.name());*/

		if (searchText == null || searchText.isEmpty())
			return null;

		Set<String> candidateEmailList = new HashSet<String>();
		List<Candidate> candidateList = searchService.globalSearchCandidate(searchText);

		for (Candidate candidate : candidateList) {
			candidateEmailList.add(candidate.getEmail());
		}

		if (GlobalConstants.SEND_EMAIL.equals(action)) {

			emailActivityService.sendBulkEmailActivity(emailActivity, new ArrayList<String>(candidateEmailList), file,
					fileName, false);

			return new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
		} else if (GlobalConstants.ADD_TO_POSITION.equals(action)) {

			CandidateToRoundDTO candidateToRoundDTO = new CandidateToRoundDTO();
			candidateToRoundDTO.setCandidateEmailList(new ArrayList<String>(candidateEmailList));
			candidateToRoundDTO.setPositionCode(positionCode);

			roundCandidateService.addCandidateToPosition(candidateToRoundDTO, sourceMode);
			return new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CANDIDATE_ADDED);
		}

		return null;
	}

	/**
	 * API to get possible full global search of clients, candidates and
	 * positions.
	 * 
	 * @param queryString
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/global", method = RequestMethod.GET)
	public RestResponse getGlobalSearchResult(@RequestParam(value = "searchText", required = false) String searchText)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetGlobalSearchResult.name());

		if (searchText == null || searchText.isEmpty())
			return null;

		GlobalSearchResult globalSearchResult = new GlobalSearchResult();
		int searchCount = 0;

		try {
			globalSearchResult = searchService.globalSearch(searchText);

			// Need for lazy loading
			for (Candidate candidate : globalSearchResult.getCandidates()) {
				candidate.getKeySkills().size();
				candidate.getEducationDetails().size();
			}

			// to set the public profile url property
			candidateService.setCandidatePublicProfileUrl(globalSearchResult.getCandidates());
			for (Position position : globalSearchResult.getPositions()) {
				// need to load lazy
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getClient().getClientName();
				position.getEducationalQualification().size();
				position.getClient().getClientName();
				position.getVendors().size();
				// adding board candidate count here
				Map<String, String> boardCandidateCount = roundCandidateService
						.getBoardCandidateCount(position.getPositionCode());
				position.setBoardCandidateCount(boardCandidateCount);
			}

			searchCount = globalSearchResult.getCandidates().size() + globalSearchResult.getPositions().size()
					+ globalSearchResult.getClients().size();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		Map<String, Object> searchMap = new LinkedHashMap<String, Object>();
		searchMap.put(GlobalConstants.SEARCH_COUNT, searchCount);
		searchMap.put(GlobalConstants.SEARCH_RESULT, globalSearchResult);

		return new RestResponse(RestResponse.SUCCESS, searchMap);

	}

	/**
	 * API to suggest possible location of position.
	 * 
	 * @param location
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position/location", method = RequestMethod.GET)
	public RestResponse getSuggestedPositionLocation(
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "type", required = false) String type) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedPositionLocation.name());*/

		if (location == null || location.isEmpty())
			return null;
		List<String> locationList = new ArrayList<String>();
		if (type.equalsIgnoreCase("position")) {
			locationList = searchService.suggestPostionLocation(location);
		} else if (type.equalsIgnoreCase("positionRequest")) {
			locationList = searchService.suggestPostionRequestLocation(location);
		}
		List<Map<String, String>> locationResponse = new ArrayList<Map<String, String>>();
		if (locationList != null && !locationList.isEmpty()) {
			for (String loc : locationList) {
				Map<String, String> locMap = new HashMap<String, String>(1);
				locMap.put("name", loc);
				locationResponse.add(locMap);
			}

			return new RestResponse(RestResponse.SUCCESS, locationResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible title of position.
	 * 
	 * @param title
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position/title", method = RequestMethod.GET)
	public RestResponse getSuggestedPositionTitle(@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "type", required = false) String type) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedPositionTitle.name());*/

		if (title == null || title.isEmpty())
			return null;
		List<String> titleList = new ArrayList<String>();
		if (type.equalsIgnoreCase("position")) {
			titleList = searchService.suggestPositionTitle(title);
		} else if (type.equalsIgnoreCase("positionRequest")) {
			titleList = searchService.suggestPositionRequestTitle(title);
		}
		List<Map<String, String>> titleResponse = new ArrayList<Map<String, String>>();
		if (titleList != null && !titleList.isEmpty()) {
			for (String titleFromList : titleList) {
				Map<String, String> titleMap = new HashMap<String, String>(1);
				titleMap.put("name", titleFromList);
				titleResponse.add(titleMap);
			}

			return new RestResponse(RestResponse.SUCCESS, titleResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible skills of position.
	 * 
	 * @param skill
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position/skill", method = RequestMethod.GET)
	public RestResponse getSuggestedPositionSkill(@RequestParam(value = "skill", required = false) String skill,
			@RequestParam(value = "type", required = false) String type) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedPositionSkill.name());*/

		if (skill == null || skill.isEmpty())
			return null;
		List<String> skillList = new ArrayList<String>();
		if (type.equalsIgnoreCase("position")) {
			skillList = searchService.suggestPositionSkills(skill);
		} else if (type.equalsIgnoreCase("positionRequest")) {
			skillList = searchService.suggestPositionRequestSkills(skill);
		}
		List<Map<String, String>> skillsResponse = new ArrayList<Map<String, String>>();
		if (skillList != null && !skillList.isEmpty()) {
			for (String skillFromList : skillList) {
				Map<String, String> skillMap = new HashMap<String, String>(1);
				skillMap.put("name", skillFromList);
				skillsResponse.add(skillMap);
			}

			return new RestResponse(RestResponse.SUCCESS, skillsResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * Get recruiz db user name based on name text suggest
	 * 
	 * @param fullName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/suggest/user/name", method = RequestMethod.GET)
	public RestResponse suggestUserName(@RequestParam(value = "name", required = false) String name)
			throws RecruizException {

		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedUserName.name());
*/
		if (name == null || name.isEmpty())
			return null;
		List<String> names = searchService.suggestAppUserName(name);
		List<Map<String, String>> nameResponse = new ArrayList<Map<String, String>>();
		if (names != null && !names.isEmpty()) {
			for (String nameFromList : names) {
				Map<String, String> nameMap = new HashMap<String, String>(1);
				nameMap.put("name", nameFromList);
				nameResponse.add(nameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, nameResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);

	}

	/**
	 * Get recruiz db user email based on name text suggest
	 * 
	 * @param fullName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/suggest/user/email", method = RequestMethod.GET)
	public RestResponse suggestUserEmail(@RequestParam(value = "email", required = false) String email)
			throws RecruizException {

		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedUserEmail.name());*/

		if (email == null || email.isEmpty())
			return null;
		List<String> emails = searchService.suggestAppUserEmail(email);
		List<Map<String, String>> emailResponse = new ArrayList<Map<String, String>>();
		if (emails != null && !emails.isEmpty()) {
			for (String emailFromList : emails) {
				Map<String, String> emailMap = new HashMap<String, String>(1);
				emailMap.put("name", emailFromList);
				emailResponse.add(emailMap);
			}

			return new RestResponse(RestResponse.SUCCESS, emailResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);

	}

	/**
	 * Get candidate skills based on key skills text suggest
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/suggest/candidate/skills", method = RequestMethod.GET)
	public RestResponse suggestCandidateSkills(@RequestParam(value = "skillText", required = false) String skillText)
			throws RecruizException {

		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidateSkillSet.name());*/

		if (skillText == null || skillText.isEmpty())
			return null;
		List<String> skills = searchService.suggestCandidateSkills(skillText);
		List<Map<String, String>> skillsResponse = new ArrayList<Map<String, String>>();
		if (skills != null && !skills.isEmpty()) {
			for (String skillFromList : skills) {
				Map<String, String> skillMap = new HashMap<String, String>(1);
				skillMap.put("name", skillFromList);
				skillsResponse.add(skillMap);
			}

			return new RestResponse(RestResponse.SUCCESS, skillsResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);

	}

	/**
	 * Get candidate full name based on name text suggest
	 * 
	 * @param fullName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/suggest/candidate/fullName", method = RequestMethod.GET)
	public RestResponse suggestCandidateFullName(@RequestParam(value = "fullName", required = false) String fullName)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidateName.name());*/

		if (fullName == null || fullName.isEmpty())
			return null;
		List<String> fullNames = searchService.suggestCandidateFullName(fullName);
		List<Map<String, String>> fullNameResponse = new ArrayList<Map<String, String>>();
		if (fullNames != null && !fullNames.isEmpty()) {
			for (String fullNameFromList : fullNames) {
				Map<String, String> fullNameMap = new HashMap<String, String>(1);
				fullNameMap.put("name", fullNameFromList);
				fullNameResponse.add(fullNameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, fullNameResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);

	}

	/**
	 * Get candidate email based on email text suggest
	 * 
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/suggest/candidate/email", method = RequestMethod.GET)
	public RestResponse suggestCandidateEmail(@RequestParam(value = "email", required = false) String email)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidateEmailAddress.name());*/

		if (email == null || email.isEmpty())
			return null;
		List<String> emails = searchService.suggestCandidateEmail(email);
		List<Map<String, String>> emailResponse = new ArrayList<Map<String, String>>();
		if (emails != null && !emails.isEmpty()) {
			for (String emailFromList : emails) {
				Map<String, String> emailMap = new HashMap<String, String>(1);
				emailMap.put("name", emailFromList);
				emailResponse.add(emailMap);
			}

			return new RestResponse(RestResponse.SUCCESS, emailResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);

	}

	/**
	 * API to suggest possible skill of candidate.
	 * 
	 * @param skill
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate/skill", method = RequestMethod.GET)
	public RestResponse getSuggestedCandidateSkill(@RequestParam(value = "skill", required = false) String skill)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidatesSkillSet.name());*/

		if (skill == null || skill.isEmpty())
			return null;
		List<String> skillList = searchService.suggestCandidateSkills(skill);
		List<Map<String, String>> response = new ArrayList<Map<String, String>>();
		if (skillList != null && !skillList.isEmpty()) {
			for (String skills : skillList) {
				Map<String, String> skillSet = new HashMap<String, String>(1);
				skillSet.put("skill", skills);
				response.add(skillSet);
			}

			return new RestResponse(RestResponse.SUCCESS, response);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	@RequestMapping(value = "/api/v1/search/candidate/advancesearch/skill", method = RequestMethod.GET)
	public RestResponse getSuggestedCandidateSkillForAdvSearch(@RequestParam(value = "skill", required = false) String skill)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidatesSkillSet.name());*/

		if (skill == null || skill.isEmpty())
			return null;

		List<SkillSuggestDTO> skillSuggestList = searchService.suggestCandidateSkillsForAdvanceSearch(skill);
		return new RestResponse(RestResponse.SUCCESS, skillSuggestList);
	}

	/**
	 * API to suggest possible preferred location of candidate.
	 * 
	 * @param prefLocation
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate/preflocation", method = RequestMethod.GET)
	public RestResponse getSuggestedCandidatePreferredLocation(
			@RequestParam(value = "prefLocation", required = false) String prefLocation) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidatePreferedLocation.name());*/

		if (prefLocation == null || prefLocation.isEmpty())
			return null;
		List<String> locationList = searchService.suggestCandidatePreferredLocation(prefLocation);
		List<Map<String, String>> response = new ArrayList<Map<String, String>>();
		if (locationList != null && !locationList.isEmpty()) {
			for (String location : locationList) {
				Map<String, String> loc = new HashMap<String, String>(1);
				loc.put("prefLocation", location);
				response.add(loc);
			}

			return new RestResponse(RestResponse.SUCCESS, response);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible preferred location of candidate.
	 * 
	 * @param currentLocation
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate/currentlocation", method = RequestMethod.GET)
	public RestResponse getSuggestedCandidateCurrentLocation(
			@RequestParam(value = "currentLocation", required = false) String currentLocation) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidateCurrentLocation.name());*/

		if (currentLocation == null || currentLocation.isEmpty())
			return null;
		List<String> locationList = searchService.suggestCandidateCurrentLocation(currentLocation);
		List<Map<String, String>> locationResponse = new ArrayList<Map<String, String>>();
		if (locationList != null && !locationList.isEmpty()) {
			for (String location : locationList) {
				Map<String, String> locMap = new HashMap<String, String>(1);
				locMap.put("name", location);
				locationResponse.add(locMap);
			}

			return new RestResponse(RestResponse.SUCCESS, locationResponse);
		}
		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible current company of candidate.
	 * 
	 * @param company
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate/company", method = RequestMethod.GET)
	public RestResponse getSuggestedCandidateCurrentCompany(
			@RequestParam(value = "company", required = false) String company) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedCandidateCurrentCompany.name());*/

		if (company == null || company.isEmpty())
			return null;
		List<String> companyList = searchService.suggestCurrentCompany(company);
		List<Map<String, String>> comapnyResponse = new ArrayList<Map<String, String>>();
		if (companyList != null && !companyList.isEmpty()) {
			for (String companyFromList : companyList) {
				Map<String, String> companyMap = new HashMap<String, String>(1);
				companyMap.put("name", companyFromList);
				comapnyResponse.add(companyMap);
			}

			return new RestResponse(RestResponse.SUCCESS, comapnyResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible location of client.
	 * 
	 * @param location
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/client/location", method = RequestMethod.GET)
	public RestResponse getSuggestedClientLocation(@RequestParam(value = "location", required = false) String location)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedClientLocation.name());*/

		if (location == null || location.isEmpty())
			return null;
		List<String> locationList = searchService.suggestClientLocation(location);
		List<Map<String, String>> locationResponse = new ArrayList<Map<String, String>>();
		if (locationList != null && !locationList.isEmpty()) {
			for (String loc : locationList) {
				Map<String, String> locMap = new HashMap<String, String>(1);
				locMap.put("name", loc);
				locationResponse.add(locMap);
			}

			return new RestResponse(RestResponse.SUCCESS, locationResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible location of client.
	 * 
	 * @param location
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/prospect/location", method = RequestMethod.GET)
	public RestResponse getSuggestedProspectLocation(
			@RequestParam(value = "location", required = false) String location) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedProspectLocation.name());*/
		if (location == null || location.isEmpty())
			return null;
		List<String> locationList = searchService.suggestProspectLocation(location);
		List<Map<String, String>> locationResponse = new ArrayList<Map<String, String>>();
		if (locationList != null && !locationList.isEmpty()) {
			for (String loc : locationList) {
				Map<String, String> locMap = new HashMap<String, String>(1);
				locMap.put("name", loc);
				locationResponse.add(locMap);
			}

			return new RestResponse(RestResponse.SUCCESS, locationResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible names of client.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/client/name", method = RequestMethod.GET)
	public RestResponse getSuggestedClientName(@RequestParam(value = "name", required = false) String name)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedClientName.name());
*/
		if (name == null || name.isEmpty())
			return null;
		List<String> nameList = searchService.suggestClientName(name);
		List<Map<String, String>> nameResponse = new ArrayList<Map<String, String>>();
		if (nameList != null && !nameList.isEmpty()) {
			for (String nameFromList : nameList) {
				Map<String, String> nameMap = new HashMap<String, String>(1);
				nameMap.put("name", nameFromList);
				nameResponse.add(nameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, nameResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible names of client.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/prospect/name", method = RequestMethod.GET)
	public RestResponse getSuggestedProspectCompanyName(@RequestParam(value = "name", required = false) String name)
			throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedProspectCompany.name());*/

		if (name == null || name.isEmpty())
			return null;
		List<String> nameList = searchService.suggestProspectCompanyName(name);
		List<Map<String, String>> nameResponse = new ArrayList<Map<String, String>>();
		if (nameList != null && !nameList.isEmpty()) {
			for (String nameFromList : nameList) {
				Map<String, String> nameMap = new HashMap<String, String>(1);
				nameMap.put("name", nameFromList);
				nameResponse.add(nameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, nameResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible names of educational qualification.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/educational/qualification", method = RequestMethod.GET)
	public RestResponse getSuggestedEducationalQualification(
			@RequestParam(value = "qualification", required = false) String qualification) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedEducationQualification.name());*/

		if (qualification == null || qualification.isEmpty())
			return null;
		List<String> qualificationList = searchService.suggestCandidateEducationalQualification(qualification);
		List<Map<String, String>> qualificationResponse = new ArrayList<Map<String, String>>();
		if (qualificationList != null && !qualificationList.isEmpty()) {
			for (String qualificationFromList : qualificationList) {
				Map<String, String> nameMap = new HashMap<String, String>(1);
				nameMap.put("qualification", qualificationFromList);
				qualificationResponse.add(nameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, qualificationResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to suggest possible names of institutions.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/educational/institution", method = RequestMethod.GET)
	public RestResponse getSuggestedInstitution(
			@RequestParam(value = "institution", required = false) String institution) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSuggestedInstitute.name());*/

		if (institution == null || institution.isEmpty())
			return null;
		List<String> institutionList = searchService.suggestCandidateEducationalInstitute(institution);
		List<Map<String, String>> institutionResponse = new ArrayList<Map<String, String>>();
		if (institutionList != null && !institutionList.isEmpty()) {
			for (String institutionFromList : institutionList) {
				Map<String, String> nameMap = new HashMap<String, String>(1);
				nameMap.put("institution", institutionFromList);
				institutionResponse.add(nameMap);
			}

			return new RestResponse(RestResponse.SUCCESS, institutionResponse);
		}

		return new RestResponse(RestResponse.SUCCESS, null);
	}

	/**
	 * API to get client search result.
	 * 
	 * @param clientSearchDTO
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	@RequestMapping(value = "/api/v1/search/client", method = RequestMethod.POST)
	public RestResponse getClientSearch(@RequestBody ClientSearchDTO clientSearchDTO,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
					throws RecruizException, RecruizEmptySearchCriteriaException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetClientSearch.name());

		if (permissionService.hasITAdminPermission() || permissionService.hasNormalRole())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		ClientSearchBuilder clientSearchBuilder = new ClientSearchBuilder();

		if (clientSearchDTO.getLocationList() != null && clientSearchDTO.getLocationList().length > 0
				&& clientSearchDTO.getLocationList()[0] != "" && clientSearchDTO.getLocationList()[0] != null) {
			clientSearchBuilder.withLocation(clientSearchDTO.getLocationList());
		}

		if (clientSearchDTO.getNameList() != null && clientSearchDTO.getNameList().length > 0
				&& clientSearchDTO.getNameList()[0] != "" && clientSearchDTO.getNameList()[0] != null) {
			clientSearchBuilder.withName(clientSearchDTO.getNameList());
		}

		if (clientSearchDTO.getStatusList() != null && clientSearchDTO.getStatusList().length > 0
				&& clientSearchDTO.getStatusList()[0] != "" && clientSearchDTO.getStatusList()[0] != null) {
			clientSearchBuilder.withStatus(clientSearchDTO.getStatusList());
		}

		Page<ClientOpeningCountDTO> clientList = searchService.getClientResultAsNativeModel(clientSearchBuilder,
				userService.getLoggedInUserObject(),
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				sortField, getSortOrder(sortOrder));
		return new RestResponse(RestResponse.SUCCESS, clientList);
	}

	/**
	 * API to get client search result.
	 * 
	 * @param prospectSearchDTO
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	@RequestMapping(value = "/api/v1/search/prospect", method = RequestMethod.POST)
	public RestResponse getProspectSearch(@RequestBody ProspectSearchDTO prospectSearchDTO,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
					throws RecruizException, RecruizEmptySearchCriteriaException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetProspectSearch.name());

		if (permissionService.hasITAdminPermission() || permissionService.hasNormalRole())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		ProspectSearchBuilder prospectSearchBuilder = new ProspectSearchBuilder();

		if (prospectSearchDTO.getLocationList() != null && prospectSearchDTO.getLocationList().length > 0
				&& prospectSearchDTO.getLocationList()[0] != "" && prospectSearchDTO.getLocationList()[0] != null) {
			prospectSearchBuilder.withLocation(prospectSearchDTO.getLocationList());
		}

		if (prospectSearchDTO.getNameList() != null && prospectSearchDTO.getNameList().length > 0
				&& prospectSearchDTO.getNameList()[0] != "" && prospectSearchDTO.getNameList()[0] != null) {
			prospectSearchBuilder.withCompanyName(prospectSearchDTO.getNameList());
		}

		if (prospectSearchDTO.getStatusList() != null && prospectSearchDTO.getStatusList().length > 0
				&& prospectSearchDTO.getStatusList()[0] != "" && prospectSearchDTO.getStatusList()[0] != null) {
			prospectSearchBuilder.withStatus(prospectSearchDTO.getStatusList());
		}

		Page<Prospect> prospectList = searchService.getProspectResultAsNativeModel(prospectSearchBuilder,
				userService.getLoggedInUserObject(),
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				sortField, getSortOrder(sortOrder));
		return new RestResponse(RestResponse.SUCCESS, prospectList);
	}

	/**
	 * API to get position search result.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position", method = RequestMethod.POST)
	public RestResponse getPositionSearch(@RequestBody PositionSearchDTO positionSearchDTO,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder,
			@RequestParam(value = "clientName", required = false) String clientName) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionSearch.name());

		if (permissionService.hasITAdminPermission() || permissionService.hasNormalRole())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		PositionSearchBuilder positionSearchBuilder = new PositionSearchBuilder();
		List<CloseByDateRange> dateList = new LinkedList<CloseByDateRange>();

		Integer closeDaysFrom = null;
		Integer closeDaysTo = null;

		if (positionSearchDTO.getCloseByDate() != null && positionSearchDTO.getCloseByDate().length > 0) {
			for (String posdate : positionSearchDTO.getCloseByDate()) {
				if (posdate != null && posdate != "")
					dateList.add(CloseByDateRange.valueOf(posdate));
			}
		}

		if (positionSearchDTO.getLocationList() != null && positionSearchDTO.getLocationList().length > 0
				&& !positionSearchDTO.getLocationList()[0].equals(""))
			positionSearchBuilder.withLocation(positionSearchDTO.getLocationList());

		if (positionSearchDTO.getNameList() != null && positionSearchDTO.getNameList().length > 0
				&& !positionSearchDTO.getNameList()[0].equals(""))
			positionSearchBuilder.withName(positionSearchDTO.getNameList());

		if (positionSearchDTO.getSkills() != null && positionSearchDTO.getSkills().length > 0
				&& !positionSearchDTO.getSkills()[0].equals(""))
			positionSearchBuilder.withSkillSet(positionSearchDTO.getSkills());

		if (positionSearchDTO.getTypeList() != null && positionSearchDTO.getTypeList().length > 0
				&& !positionSearchDTO.getTypeList()[0].equals(""))
			positionSearchBuilder.withType(positionSearchDTO.getTypeList());

		if (positionSearchDTO.getStatusList() != null && positionSearchDTO.getStatusList().length > 0
				&& !positionSearchDTO.getStatusList()[0].equals(""))
			positionSearchBuilder.withStatus(positionSearchDTO.getStatusList());

		if (dateList != null && !dateList.isEmpty()) {
			for (CloseByDateRange expromList : dateList) {
				closeDaysFrom = expromList.getCloseDaysFrom();
				closeDaysTo = expromList.getCloseDaysTo();
				positionSearchBuilder.withCloseDaysRange(closeDaysFrom, closeDaysTo);
			}
		}
		Client client = null;
		Page<Position> positionList = new PageImpl<Position>(new ArrayList<Position>());
		if (clientName != null && !clientName.isEmpty())
			client = clientService.getClientByName(clientName);
		if (client != null) {
			positionList = searchService.getPositionResultAsNativeModelByClient(
					positionSearchBuilder, userService.getLoggedInUserObject(), client, pageableService
					.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
					sortField, getSortOrder(sortOrder));
		} else {

			User user = userService.getLoggedInUserObject();
			if(checkPermissionService.isSuperAdmin()){
				positionList = searchService.getPositionResultAsNativeModel(positionSearchDTO,
						positionSearchBuilder, userService.getLoggedInUserObject(), pageableService
						.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
						sortField, getSortOrder(sortOrder));
			}else{
				positionList = searchService.getPositionResultAsNativeModelForNonAdmin(positionSearchDTO,
						positionSearchBuilder, userService.getLoggedInUserObject(), pageableService
						.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
						sortField, getSortOrder(sortOrder));
			}
		}

		if(positionList!=null){
			for (Position position : positionList) {
				// need to load lazy
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
				position.getClient().getClientName();

				// adding board candidate count here   
				Map<String, String> boardCandidateCount = roundCandidateService
						.getBoardCandidateCount(position.getPositionCode());
				position.setBoardCandidateCount(boardCandidateCount);
			}
		}else{
			positionList = new PageImpl<Position>(new ArrayList<Position>());
		}

		return new RestResponse(RestResponse.SUCCESS, positionList);
	}

	/**
	 * API to get position request search result.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/position/request", method = RequestMethod.POST)
	public RestResponse getPositionRequestSearch(@RequestBody PositionSearchDTO positionSearchDTO,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder,
			@RequestParam(value = "clientName", required = false) String clientName) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetPositionRequestSearch.name());
		if (permissionService.hasITAdminPermission())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		PositionRequestSearchBuilder positionRequestSearchBuilder = new PositionRequestSearchBuilder();
		List<CloseByDateRange> dateList = new LinkedList<CloseByDateRange>();

		Integer closeDaysFrom = null;
		Integer closeDaysTo = null;

		if (positionSearchDTO.getCloseByDate() != null && positionSearchDTO.getCloseByDate().length > 0) {
			for (String posdate : positionSearchDTO.getCloseByDate()) {
				if (posdate != null && posdate != "")
					dateList.add(CloseByDateRange.valueOf(posdate));
			}
		}

		if (positionSearchDTO.getLocationList() != null && positionSearchDTO.getLocationList().length > 0
				&& !positionSearchDTO.getLocationList()[0].equals(""))
			positionRequestSearchBuilder.withLocation(positionSearchDTO.getLocationList());

		if (positionSearchDTO.getNameList() != null && positionSearchDTO.getNameList().length > 0
				&& !positionSearchDTO.getNameList()[0].equals(""))
			positionRequestSearchBuilder.withName(positionSearchDTO.getNameList());

		if (positionSearchDTO.getSkills() != null && positionSearchDTO.getSkills().length > 0
				&& !positionSearchDTO.getSkills()[0].equals(""))
			positionRequestSearchBuilder.withSkillSet(positionSearchDTO.getSkills());

		if (positionSearchDTO.getTypeList() != null && positionSearchDTO.getTypeList().length > 0
				&& !positionSearchDTO.getTypeList()[0].equals(""))
			positionRequestSearchBuilder.withType(positionSearchDTO.getTypeList());

		if (positionSearchDTO.getStatusList() != null && positionSearchDTO.getStatusList().length > 0
				&& !positionSearchDTO.getStatusList()[0].equals(""))
			positionRequestSearchBuilder.withStatus(positionSearchDTO.getStatusList());

		if (dateList != null && !dateList.isEmpty()) {
			for (CloseByDateRange expromList : dateList) {
				closeDaysFrom = expromList.getCloseDaysFrom();
				closeDaysTo = expromList.getCloseDaysTo();
				positionRequestSearchBuilder.withCloseDaysRange(closeDaysFrom, closeDaysTo);
			}
		}
		Client client = null;
		Page<PositionRequest> positionRequestList = new PageImpl<PositionRequest>(new ArrayList<PositionRequest>());
		if (clientName != null && !clientName.isEmpty())
			client = clientService.getClientByName(clientName);
		if (client != null) {
			positionRequestList = searchService.getPositionRequestResultAsNativeModelByClient(
					positionRequestSearchBuilder, client.getClientName(),
					pageableService.getPageRequestObject(pageNo, sortField), sortField, getSortOrder(sortOrder));
		} else {
			positionRequestList = searchService.getPositionRequestResultAsNativeModel(positionRequestSearchBuilder,
					pageableService.getPageRequestObject(pageNo, sortField), sortField, getSortOrder(sortOrder));
		}

		return new RestResponse(RestResponse.SUCCESS, positionRequestList);
	}

	/**
	 * API to get Candidate search result.
	 * 
	 * @param candidateSearchDTO
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/search/candidate", method = RequestMethod.POST)
	public RestResponse getCandidateSearch(@RequestBody CandidateSearchDTO candidateSearchDTO,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidateSearch.name());

		if (permissionService.hasITAdminPermission() || permissionService.hasNormalRole())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		List<EmploymentType> typeList = new LinkedList<EmploymentType>();
		List<ExpectedCTCRange> ctcList = new LinkedList<ExpectedCTCRange>();
		List<ExperinceRange> expList = new LinkedList<ExperinceRange>();
		List<NoticePeriodRange> noticePeriodList = new LinkedList<NoticePeriodRange>();
		List<Status> statusList = new LinkedList<Status>();
		boolean isServingNotice = Boolean.parseBoolean(candidateSearchDTO.getIsServingNotice());
		CandidateSearchBuilder candidateSearchBuilder = new CandidateSearchBuilder();

		double fromCTC = 0;
		double toCTC = 0;

		Double fromExp = null;
		Double toExp = null;

		Integer daysFrom = null;
		Integer daysTo = null;

		if (candidateSearchDTO.getExpectedCTC() != null && candidateSearchDTO.getExpectedCTC().length > 0) {
			{
				for (String ctc : candidateSearchDTO.getExpectedCTC()) {
					if (ctc != null && !ctc.isEmpty())
						ctcList.add(ExpectedCTCRange.valueOf(ctc));
				}
			}
		}

		if (candidateSearchDTO.getTotalExp() != null && candidateSearchDTO.getTotalExp().length > 0) {
			{
				for (String exp : candidateSearchDTO.getTotalExp()) {
					if (exp != null && !exp.isEmpty())
						expList.add(ExperinceRange.valueOf(exp));
				}
			}
		}

		if (candidateSearchDTO.getNoticePeriod() != null && candidateSearchDTO.getNoticePeriod().length > 0) {
			{
				for (String notice : candidateSearchDTO.getNoticePeriod()) {
					if (notice != null && !notice.isEmpty())
						noticePeriodList.add(NoticePeriodRange.valueOf(notice));
				}
			}
		}
		if (candidateSearchDTO.getEmpTypeList() != null && candidateSearchDTO.getEmpTypeList().length > 0) {
			for (String type : candidateSearchDTO.getEmpTypeList()) {
				if (type != null && !type.isEmpty())
					typeList.add(EmploymentType.valueOf(type));
			}
		}
		if (candidateSearchDTO.getStatusList() != null && candidateSearchDTO.getStatusList().length > 0) {
			for (String status : candidateSearchDTO.getStatusList()) {
				if (status != null && !status.isEmpty())
					statusList.add(Status.valueOf(status));
			}
		}

		if (candidateSearchDTO.getCurrentLocationList() != null
				&& candidateSearchDTO.getCurrentLocationList().length > 0
				&& !candidateSearchDTO.getCurrentLocationList().equals("")) {
			candidateSearchBuilder.withCurrentLocation(candidateSearchDTO.getCurrentLocationList());
		}

		if (candidateSearchDTO.getPreferredLocationList() != null
				&& candidateSearchDTO.getPreferredLocationList().length > 0
				&& candidateSearchDTO.getPreferredLocationList()[0] != null
				&& candidateSearchDTO.getPreferredLocationList()[0] != "") {
			candidateSearchBuilder.withPreferredLocation(candidateSearchDTO.getPreferredLocationList());
		}

		if (candidateSearchDTO.getActualSourceList() != null
				&& candidateSearchDTO.getActualSourceList().length > 0
				&& !candidateSearchDTO.getActualSourceList().equals("")) {
			candidateSearchBuilder.withActualSource(candidateSearchDTO.getActualSourceList());
		}

		if (candidateSearchDTO.getSkills() != null && candidateSearchDTO.getSkills().length > 0
				&& candidateSearchDTO.getSkills()[0] != null && candidateSearchDTO.getSkills()[0] != "") {
			candidateSearchBuilder.withSkillSet(candidateSearchDTO.getSkills());
		}

		if (typeList != null && !typeList.isEmpty())
			candidateSearchBuilder.withEmploymentType(typeList);
		if (statusList != null && !statusList.isEmpty())
			candidateSearchBuilder.withStatus(statusList);
		if (ctcList != null && !ctcList.isEmpty()) {
			for (ExpectedCTCRange ctcFromList : ctcList) {
				fromCTC = ctcFromList.getFromCTC();
				toCTC = ctcFromList.getToCTC();
				candidateSearchBuilder.withCTCRange(fromCTC, toCTC);
			}
		}
		if (expList != null && !expList.isEmpty()) {
			for (ExperinceRange expFromList : expList) {
				fromExp = expFromList.getFromExp();
				toExp = expFromList.getToExp();
				candidateSearchBuilder.withTotalExp(fromExp, toExp);
			}
		}
		if (noticePeriodList != null && !noticePeriodList.isEmpty()) {
			for (NoticePeriodRange noticeFromList : noticePeriodList) {
				daysFrom = noticeFromList.getDaysFrom();
				daysTo = noticeFromList.getDaysTo();
				candidateSearchBuilder.withNoticePeriodRange(daysFrom, daysTo);
			}
		}

		if (isServingNotice) {
			candidateSearchBuilder.withIsServingNoticePeriod(isServingNotice);
		}

		Page<Candidate> candidateList = searchService.getCandidateResultAsNativeModel(candidateSearchBuilder,
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
				sortField, getSortOrder(sortOrder));
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		}
		// to set the public profile url property
		candidateService.setCandidatePublicProfileUrl(candidateList.getContent());
		candidateService.attachCurrentPosition(candidateList.getContent());
		return new RestResponse(RestResponse.SUCCESS, candidateList);
	}

	/**
	 * API to AdvancedSearch.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/advancedsearch/candidates", method = RequestMethod.POST)
	public RestResponse getCandidatesForAdvancedSearch(@RequestBody AdvancedSearchQueryEntity advancedSearchQuery,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "anySearchKey", required = false) String anySearchKey,
			@RequestParam(value = "allSearchKey", required = false) String allSearchKey)
					throws RecruizException, UnknownHostException, IOException, ParseException {


		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.AdvanceSearchCandidate.name());

		if (checkAppSettingsService.isValidityExpired()) {
			return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
		}

		if (!checkAppSettingsService.isAdvancedSearchFeatureEnabled()) {
			return new RestResponse(false, ErrorHandler.FEATURE_NOT_ENABLED, ErrorHandler.FEATURE_NOT_ALLOWED);
		}

		RestResponse candidateResultResponse = null;
		try {

			if (AdvancedSearchType.basicSearch.toString().equals(advancedSearchQuery.getSearchType())) {
				Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField);
				Page<Candidate> candidateList = searchService
						.globalSearchCandidate(advancedSearchQuery.getBasicSearch(), pageable);
				// to set the public profile url property
				candidateService.attachCurrentPosition(candidateList.getContent());
				candidateService.setCandidatePublicProfileUrl(candidateList.getContent());
				candidateResultResponse = new RestResponse(RestResponse.SUCCESS, candidateList);
			} else if(advancedSearchQuery.getRecruizQuickSearch()!=null && !advancedSearchQuery.getRecruizQuickSearch().equals("")){ 
			
				List<Candidate> candidateListData = new ArrayList<>();
				if(StringUtils.isNumeric(advancedSearchQuery.getRecruizQuickSearch().trim())){
					candidateListData = candidateService.findByMobileNo(advancedSearchQuery.getRecruizQuickSearch().trim());
				}else if(advancedSearchQuery.getRecruizQuickSearch().contains("@")){
					Candidate candidate = candidateService.getCandidateByEmail(advancedSearchQuery.getRecruizQuickSearch());
					candidateListData.add(candidate);
				}else{
					Candidate candidate = candidateService.getCandidateByEmail(advancedSearchQuery.getRecruizQuickSearch());
					List<Candidate> cadidateList = candidateService.findAll();
					
					if(cadidateList==null)
						new RestResponse(RestResponse.FAILED,"Unable to find all candidates. Please try again.");
					
					for (Candidate can : cadidateList) {
						
						String[] spiltedName = can.getFullName().split(" ");
						
						for (String name : spiltedName) {
							if(name.equalsIgnoreCase(advancedSearchQuery.getRecruizQuickSearch())){
								candidateListData.add(can);
								continue;
							}
						}
							
					}
					
					if(candidate!=null){
						candidateListData.add(candidate);
					}
					
				}
				
				Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField);
				int start = pageable.getOffset();
				int end = (start + pageable.getPageSize()) > candidateListData.size() ? candidateListData.size() : (start + pageable.getPageSize());
				final Page<Candidate> candidatesList = new PageImpl<Candidate>(candidateListData.subList(start, end), pageable,
						candidateListData.size());			

				candidateService.attachCurrentPosition(candidatesList.getContent());

				// to set the public profile url property
				candidateService.setCandidatePublicProfileUrl(candidatesList.getContent());

				return new RestResponse(RestResponse.SUCCESS, candidatesList);
				
				
			}else {
				if(anySearchKey != null && !anySearchKey.isEmpty()) {
					if(advancedSearchQuery.getAnyKeyword() != null && !advancedSearchQuery.getAnyKeyword().isEmpty()) {
						advancedSearchQuery.setAnyKeyword(advancedSearchQuery.getAnyKeyword() + " or " + anySearchKey);    
					}else {
						advancedSearchQuery.setAnyKeyword(anySearchKey);
					}
				}

				if(allSearchKey != null && !allSearchKey.isEmpty()) {
					if(advancedSearchQuery.getAllKeyword() != null && !advancedSearchQuery.getAllKeyword().isEmpty()) {
						advancedSearchQuery.setAllKeyword(advancedSearchQuery.getAllKeyword() + " and " + allSearchKey);    
					}else {
						advancedSearchQuery.setAllKeyword(allSearchKey);
					}
				}
				List<Candidate> candidateList = searchService.advanceSearchCandidateNativeModel(advancedSearchQuery,
						pageableService.getPageRequestObject(pageNo, sortField));


				//Custom field search
				if(advancedSearchQuery.getCustomFieldList()!=null && advancedSearchQuery.getCustomFieldList().size()>0 
						&& !advancedSearchQuery.getCustomFieldList().isEmpty()){

					boolean status = searchService.getAdvancedSearchQueryStatus(advancedSearchQuery);

					if(status){
						candidateList = new ArrayList<>();
					}

					Collection<Long> finalCustomCandidates = new HashSet<>();				
					Set<Long> totalCustomFieldCandidate = new HashSet<>();
					for (CustomFieldDetails custom : advancedSearchQuery.getCustomFieldList()) {
						if(custom!=null){
							String fieldName = custom.getFieldName();
							String fieldvalue = custom.getFieldValue();
							if(!fieldvalue.isEmpty() && !fieldvalue.equalsIgnoreCase("") && fieldvalue.length()>0 && fieldvalue!=null){
								List<BigInteger> candidateByCustomField = candidateService.findCandidateByCustomFields(fieldName,fieldvalue);
								for (BigInteger id : candidateByCustomField) {
									totalCustomFieldCandidate.add(id.longValue());
								}
							}
						}
					}

					if(totalCustomFieldCandidate.size()>0){
						for (Long cutomCandidateId : totalCustomFieldCandidate) {
							int x=0;
							if(candidateList.size()>0)
								for (Candidate candidate : candidateList) {	

									if(candidate.getCid() == cutomCandidateId){
										x=1;
										break;
									}
								}
							if(x==0){
								finalCustomCandidates.add(cutomCandidateId);
							}
						}
						List<Candidate> customCandidateList = candidateService.findByCidIn(finalCustomCandidates);					
						candidateList.addAll(customCandidateList);
					}
				}			

				if(advancedSearchQuery.getSources().size()>0 && advancedSearchQuery.getSources()!=null || advancedSearchQuery.getLanguage()!=null &&
						!advancedSearchQuery.getLanguage().equals("") || advancedSearchQuery.getNationality()!=null && !advancedSearchQuery.getNationality().equals("")){
					
					List<Candidate> listData = searchService.filterCandidateByLanguageAndNationality(candidateList, advancedSearchQuery);
					
					candidateList = new ArrayList<>();
					
					candidateList.addAll(listData);
				}
				
				
				Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField);
				int start = pageable.getOffset();
				int end = (start + pageable.getPageSize()) > candidateList.size() ? candidateList.size() : (start + pageable.getPageSize());
				final Page<Candidate> candidatesList = new PageImpl<Candidate>(candidateList.subList(start, end), pageable,
						candidateList.size());			

				candidateService.attachCurrentPosition(candidatesList.getContent());

				// to set the public profile url property
				candidateService.setCandidatePublicProfileUrl(candidatesList.getContent());

				candidateResultResponse = new RestResponse(RestResponse.SUCCESS, candidatesList);
			}

		} catch (Exception e) {
			candidateResultResponse = new RestResponse(RestResponse.FAILED, new PageImpl<>(new ArrayList<>()),
					"Query execution failed");
			if (e.getCause() instanceof QueryParsingException) {
				candidateResultResponse = new RestResponse(RestResponse.FAILED, new PageImpl<>(new ArrayList<>()),
						"Syntax error - Incorrect query string");
			}
		}

		return candidateResultResponse;
	}

	/**
	 * API for query result action for select all in advanced search
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/advancedsearch/candidates/overall/queryresult", method = RequestMethod.POST)
	public RestResponse selectAllQueryActionForAdvancedSearch(
			@RequestPart("json") @Valid AdvancedSearchDTO advancedSearchDTO,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "action") String action)
					throws RecruizException, UnknownHostException, IOException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetAllQueryActionForAdvanceSearch.name());
		if (checkAppSettingsService.isValidityExpired()) {
			return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
		}

		if (!checkAppSettingsService.isAdvancedSearchFeatureEnabled()) {
			return new RestResponse(false, ErrorHandler.FEATURE_NOT_ENABLED, ErrorHandler.FEATURE_NOT_ALLOWED);
		}

		RestResponse candidateResultResponse = null;
		try {
			Set<String> candidateEmailList = new HashSet<String>();
			List<Candidate> candidateList = searchService
					.advanceSearchCandidateNativeModel(advancedSearchDTO.getAdvancedSearchQuery());

			for (Candidate candidate : candidateList) {
				candidateEmailList.add(candidate.getEmail());
			}

			if (GlobalConstants.SEND_EMAIL.equals(action)) {

				emailActivityService.sendBulkEmailActivity(advancedSearchDTO.getEmailActivity(),
						new ArrayList<String>(candidateEmailList), file, advancedSearchDTO.getFileName(), false);

				candidateResultResponse = new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
			} else if (GlobalConstants.ADD_TO_POSITION.equals(action)) {

				CandidateToRoundDTO candidateToRoundDTO = new CandidateToRoundDTO();
				candidateToRoundDTO.setCandidateEmailList(new ArrayList<String>(candidateEmailList));
				candidateToRoundDTO.setPositionCode(advancedSearchDTO.getPositionCode());

				roundCandidateService.addCandidateToPosition(candidateToRoundDTO, advancedSearchDTO.getSourceMode());
				candidateResultResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CANDIDATE_ADDED);
			}

		} catch (Exception e) {
			candidateResultResponse = new RestResponse(RestResponse.FAILED, new ArrayList<>(),
					"Query execution failed");
			if (e.getCause() instanceof QueryParsingException) {
				candidateResultResponse = new RestResponse(RestResponse.FAILED, new ArrayList<>(),
						"Syntax error - Incorrect query string");
			}
		}

		return candidateResultResponse;
	}

	/**
	 * API to get all saved search queries for user.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/savedqueries", method = RequestMethod.GET)
	public RestResponse getSavedSearchQueries() throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSavedSearchedQuery.name());*/

		List<AdvancedSearchQueryEntity> searchQueryList = new ArrayList<AdvancedSearchQueryEntity>();
		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, searchQueryList);
		try {
			List<AdvancedSearchQueryEntity> searchQueries = advancedSearchService.getAllQueries();
			for (AdvancedSearchQueryEntity query : searchQueries) {
				query.setOwner(null);
				searchQueryList.add(domainToSearchDomainService.convertFromPersistEntity(query));
			}
		} catch (Exception e) {
			logger.error("\n\n\n\n\n"+e.getMessage(),e);
			restResponse = new RestResponse(RestResponse.FAILED, "Failed to fetch records");
		}
		return restResponse;
	}

	/**
	 * API to Save AdvancedSearch Query.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/advancedsearch", method = RequestMethod.POST)
	public RestResponse saveAdvancedSearchQuery(@RequestBody AdvancedSearchQueryEntity advancedSearchQuery,@RequestParam(required=false) Long id)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.SaveAdvanceSerachQuery.name());

		AdvancedSearchQueryEntity savedAdvancedSearchQuery = null;
		RestResponse candidateResultResponse = new RestResponse(RestResponse.SUCCESS, savedAdvancedSearchQuery);
		try {
			if(null != id && id > 0) {
				savedAdvancedSearchQuery = advancedSearchService.updateSavedQuery(advancedSearchQuery, id);
				savedAdvancedSearchQuery = domainToSearchDomainService
						.convertFromPersistEntity(savedAdvancedSearchQuery);
				savedAdvancedSearchQuery.setOwner(null);
				candidateResultResponse.setData(savedAdvancedSearchQuery);
			}else if (!advancedSearchService.isSearchQueryExists(advancedSearchQuery.getQueryName())) {
				savedAdvancedSearchQuery = advancedSearchService.saveSearchQuery(advancedSearchQuery);
				savedAdvancedSearchQuery = domainToSearchDomainService
						.convertFromPersistEntity(savedAdvancedSearchQuery);
				savedAdvancedSearchQuery.setOwner(null);
				candidateResultResponse.setData(savedAdvancedSearchQuery);
			} else {
				candidateResultResponse = new RestResponse(RestResponse.FAILED, "Query already exists with name: "
						+ advancedSearchQuery.getQueryName() + ". Try giving a new name for this query", "DUPLICATE");
			}
		} catch (Exception e) {
			candidateResultResponse = new RestResponse(RestResponse.FAILED, new PageImpl<>(new ArrayList<>()),
					"Failed to save Search Query");
		}

		return candidateResultResponse;
	}

	/**
	 * API to delete AdvancedSearch Query.
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/advancedsearch/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteSearchQueryById(@PathVariable String id) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteSearchQuery.name());

		RestResponse candidateResultResponse = new RestResponse(RestResponse.SUCCESS, "Deleted Search Query");
		try {
			advancedSearchService.deleteSearchQueryById(Long.parseLong(id));
		} catch (Exception e) {
			candidateResultResponse = new RestResponse(RestResponse.FAILED, new PageImpl<>(new ArrayList<>()),
					"Failed to delete Search Query");
		}

		return candidateResultResponse;
	}

	/**
	 * Check sort order from UI and matches with enum then gives appropriate
	 * sort order requires for elastic search
	 * 
	 * @author Akshay
	 * @param sortOrder
	 * @return
	 */
	private SortOrder getSortOrder(String sortOrder) {
		if (sortOrder != null && !sortOrder.isEmpty()) {
			if (SortOrder.DESC.toString().equals(sortOrder))
				return SortOrder.DESC;
			else if (SortOrder.ASC.toString().equals(sortOrder))
				return SortOrder.ASC;
		}
		return SortOrder.DESC;
	}

	
	
	
	@RequestMapping(value = "/api/v1/search/email/getEmailList", method = RequestMethod.GET)
	public RestResponse getEmailList(@RequestParam("type") String type, @RequestParam("searchText") String searchText) throws Exception {

	if(type==null || type.equalsIgnoreCase("") || type.isEmpty())
		return new RestResponse(RestResponse.FAILED, "Search type required !!", null);
	
	if(searchText==null || searchText.isEmpty())
		return new RestResponse(RestResponse.FAILED, "searchText required !!", null);
		
		return searchService.getEmailList(type, searchText);
	}
	
	
	
}
