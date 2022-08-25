package com.bbytes.recruiz.service;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.math.IntRange;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.SendingEmailList;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.AdvancedSearchIn;
import com.bbytes.recruiz.enums.AdvancedSearchTabs;
import com.bbytes.recruiz.enums.BooleanSearchType;
import com.bbytes.recruiz.enums.ResumeFreshness;
import com.bbytes.recruiz.enums.SearchMatch;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.integration.AdvancedSearchNoticePeriod;
import com.bbytes.recruiz.enums.integration.SixthSenseDesignationType;
import com.bbytes.recruiz.enums.integration.SixthSenseIncludeCompanyType;
import com.bbytes.recruiz.enums.integration.SixthSenseJobStatus;
import com.bbytes.recruiz.enums.integration.SixthSenseJobType;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseCityService;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.PositionRequestRepository;
import com.bbytes.recruiz.repository.ProspectRepository;
import com.bbytes.recruiz.repository.SendingEmailListRepository;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.PositionSearchDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.SkillSuggestDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultDTO;
import com.bbytes.recruiz.search.builder.CandidateAdvanceSearchBuilder;
import com.bbytes.recruiz.search.builder.CandidateDuplicateSearchBuilder;
import com.bbytes.recruiz.search.builder.CandidateSearchBuilder;
import com.bbytes.recruiz.search.builder.ClientSearchBuilder;
import com.bbytes.recruiz.search.builder.PositionRequestSearchBuilder;
import com.bbytes.recruiz.search.builder.PositionSearchBuilder;
import com.bbytes.recruiz.search.builder.ProspectSearchBuilder;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.ClientSearch;
import com.bbytes.recruiz.search.domain.GlobalSearchResult;
import com.bbytes.recruiz.search.domain.IdWithScore;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;
import com.bbytes.recruiz.search.domain.PositionSearch;
import com.bbytes.recruiz.search.domain.ProspectSearch;
import com.bbytes.recruiz.search.repository.CandidateSearchRepo;
import com.bbytes.recruiz.search.repository.ClientSearchRepo;
import com.bbytes.recruiz.search.repository.PositionRequestSearchRepo;
import com.bbytes.recruiz.search.repository.PositionSearchRepo;
import com.bbytes.recruiz.search.repository.ProspectSearchRepo;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.MultiTenantUtils;
import com.bbytes.recruiz.utils.SearchUtil;
import com.bbytes.recruiz.utils.SortUtil;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class SearchService {

	private static Logger logger = LoggerFactory.getLogger(SearchService.class);

	@Autowired
	private PageableService pageableService;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private ClientSearchRepo clientSearchRepo;

	@Autowired
	private CandidateSearchRepo candidateSearchRepo;

	@Autowired
	private PositionSearchRepo positionSearchRepo;

	@Autowired
	private ProspectSearchRepo prospectSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private PositionRequestSearchRepo positionRequestSearchRepo;

	@Autowired
	private PositionRequestRepository positionRequestRepository;

	@Autowired
	private ClientRepository clientOrDepartmentRepository;

	@Autowired
	private ProspectRepository prospectRepository;

	@Autowired
	private ClientService clientService;
	
	@Autowired
	private SendingEmailListRepository sendingEmailListRepository;

	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamService teamService;

	@Autowired
	protected SpringProfileService springProfileService;

	@Autowired
	private SixthSenseCityService sixthSenseCityService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Search full elastic search , it does global search and return matching
	 * candidates, positions and clients
	 * 
	 * @param queryText
	 * @return
	 */
	public GlobalSearchResult globalSearch(String queryText) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		final Map<String, String> indexNameToClassNameMap = new HashMap<>();
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(ClientSearch.class).getIndexName(),
				ClientSearch.class.getSimpleName());
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(PositionSearch.class).getIndexName(),
				PositionSearch.class.getSimpleName());
		// indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(CandidateSearch.class).getIndexName(),
		// CandidateSearch.class.getSimpleName());

		String[] indexNames = indexNameToClassNameMap.keySet().toArray(new String[indexNameToClassNameMap.values().size()]);

		// add tenant filter to global search
		BoolQueryBuilder boolQueryBuilder = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
		applyAdditionalFilter(boolQueryBuilder);

		// queryText = SearchUtil.cleanQueryText(queryText);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(queryText)).withFilter(boolQueryBuilder)
				.withIndices(indexNames).withFields("docId").build();

		logger.debug("Global ElasticSearch Query JSON :- \n" + searchQuery.getQuery().toString());

		GlobalSearchResult globalSearchResult = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<GlobalSearchResult>() {

			@Override
			public GlobalSearchResult extract(SearchResponse response) {
				LinkedList<Long> clientIds = new LinkedList<Long>();
				LinkedList<Long> positionIds = new LinkedList<Long>();

				for (SearchHit hit : response.getHits()) {
					String indexClassName = indexNameToClassNameMap.get(hit.getIndex());
					Long id = ((Number) hit.field("docId").getValue()).longValue();
					if (indexClassName.equals(ClientSearch.class.getSimpleName())) {
						clientIds.add(id);
					} else if (indexClassName.equals(PositionSearch.class.getSimpleName())) {
						positionIds.add(id);
					}

				}

				List<Position> positions = positionRepository.findAll(positionIds);
				List<ClientOpeningCountDTO> clients = new ArrayList<ClientOpeningCountDTO>();
				if (!clientIds.isEmpty() && clientIds != null)
					clients = clientOrDepartmentRepository.clientListWithTotalOpening(clientIds);

				return new GlobalSearchResult(clients, positions);
			}
		});

		// use advance query to get the candidate result as the user can
		// enter a
		// boolean query in global search
		AdvancedSearchQueryEntity advancedSearchQuery = new AdvancedSearchQueryEntity();
		advancedSearchQuery.setBooleanQuery(queryText);
		advancedSearchQuery.setSearchIn(String.valueOf(AdvancedSearchIn.All.getCode()));
		advancedSearchQuery.setTab(AdvancedSearchTabs.BOOLEAN.toString());
		List<Candidate> candidates = advanceSearchCandidateNativeModel(advancedSearchQuery);

		globalSearchResult.setCandidates(candidates);

		logger.debug("Global ElasticSearch Query result  :- \n" + globalSearchResult);
		return globalSearchResult;
	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public Page<ClientOpeningCountDTO> globalSearchClient(String queryText, final Pageable pageable) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		final Map<String, String> indexNameToClassNameMap = new HashMap<>();
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(ClientSearch.class).getIndexName(),
				ClientSearch.class.getSimpleName());

		String[] indexNames = indexNameToClassNameMap.keySet().toArray(new String[indexNameToClassNameMap.values().size()]);

		// add tenant filter to global search
		BoolQueryBuilder boolQueryBuilder = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
		applyAdditionalFilter(boolQueryBuilder);

		// queryText = SearchUtil.cleanQueryText(queryText);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(queryText)).withFilter(boolQueryBuilder)
				.withIndices(indexNames).withFields("docId").withPageable(pageable).build();

		logger.debug("Global ElasticSearch Query JSON :- \n" + searchQuery.getQuery().toString());

		Page<ClientOpeningCountDTO> clientOpeningCountDTOs = elasticsearchTemplate.query(searchQuery,
				new ResultsExtractor<Page<ClientOpeningCountDTO>>() {

			@Override
			public Page<ClientOpeningCountDTO> extract(SearchResponse response) {
				Set<Long> clientIds = new LinkedHashSet<Long>();

				for (SearchHit hit : response.getHits()) {
					Long id = ((Number) hit.field("docId").getValue()).longValue();
					clientIds.add(id);
				}

				List<ClientOpeningCountDTO> clients = new ArrayList<ClientOpeningCountDTO>();
				if (!clientIds.isEmpty() && clientIds != null)
					clients = clientOrDepartmentRepository.clientListWithTotalOpening(new LinkedList<>(clientIds));

				return new PageImpl<ClientOpeningCountDTO>(new LinkedList<>(clients), pageable, response.getHits().totalHits());
			}
		});

		return clientOpeningCountDTOs;

	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public Page<Prospect> globalSearchProspect(String queryText, final Pageable pageable) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		final Map<String, String> indexNameToClassNameMap = new HashMap<>();
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(ProspectSearch.class).getIndexName(),
				ProspectSearch.class.getSimpleName());

		String[] indexNames = indexNameToClassNameMap.keySet().toArray(new String[indexNameToClassNameMap.values().size()]);

		// add tenant filter to global search
		BoolQueryBuilder boolQueryBuilder = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
		applyAdditionalFilter(boolQueryBuilder);

		queryText = SearchUtil.cleanQueryText(queryText);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(queryText)).withFilter(boolQueryBuilder)
				.withIndices(indexNames).withFields("docId").withPageable(pageable).build();

		logger.debug("Global ElasticSearch Query JSON :- \n" + searchQuery.getQuery().toString());

		Page<Prospect> prospects = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Page<Prospect>>() {

			@Override
			public Page<Prospect> extract(SearchResponse response) {
				Set<Long> prospectIds = new LinkedHashSet<Long>();

				for (SearchHit hit : response.getHits()) {
					Long id = ((Number) hit.field("docId").getValue()).longValue();
					prospectIds.add(id);
				}

				List<Prospect> prospectList = new ArrayList<Prospect>();
				if (!prospectIds.isEmpty() && prospectIds != null)
					prospectList = prospectRepository.findAll(new LinkedList<>(prospectIds));

				return new PageImpl<Prospect>(new LinkedList<>(prospectList), pageable, response.getHits().totalHits());
			}
		});

		return prospects;

	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public Page<Position> globalSearchPosition(String queryText, final Pageable pageable) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		final Map<String, String> indexNameToClassNameMap = new HashMap<>();
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(PositionSearch.class).getIndexName(),
				ClientSearch.class.getSimpleName());

		String[] indexNames = indexNameToClassNameMap.keySet().toArray(new String[indexNameToClassNameMap.values().size()]);

		// add tenant filter to global search
		BoolQueryBuilder boolQueryBuilder = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
		applyAdditionalFilter(boolQueryBuilder);

		// queryText = SearchUtil.cleanQueryText(queryText);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(queryText)).withFilter(boolQueryBuilder)
				.withIndices(indexNames).withFields("docId").withPageable(pageable).build();

		logger.debug("Global ElasticSearch Query JSON :- \n" + searchQuery.getQuery().toString());

		Page<Position> positions = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Page<Position>>() {

			@Override
			public Page<Position> extract(SearchResponse response) {
				Set<Long> positionIds = new LinkedHashSet<Long>();

				for (SearchHit hit : response.getHits()) {
					Long id = ((Number) hit.field("docId").getValue()).longValue();
					positionIds.add(id);
				}

				List<Position> positions = positionRepository.findAll(positionIds);

				return new PageImpl<Position>(new LinkedList<>(positions), pageable, response.getHits().totalHits());
			}
		});

		return positions;

	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public Page<Position> globalSearchPositionForCareerSite(String queryText, final Pageable pageable) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("Career site serach query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		final Map<String, String> indexNameToClassNameMap = new HashMap<>();
		indexNameToClassNameMap.put(elasticsearchTemplate.getPersistentEntityFor(PositionSearch.class).getIndexName(),
				ClientSearch.class.getSimpleName());

		String[] indexNames = indexNameToClassNameMap.keySet().toArray(new String[indexNameToClassNameMap.values().size()]);

		// add tenant filter to global search
		BoolQueryBuilder boolQueryBuilder = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
		applyAdditionalFilter(boolQueryBuilder);

		// queryText = SearchUtil.cleanQueryText(queryText);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(queryText)).withFilter(boolQueryBuilder)
				.withIndices(indexNames).withFields("docId").withPageable(pageable).build();

		logger.debug("Career Site Position ElasticSearch Query JSON :- \n" + searchQuery.getQuery().toString());

		Page<Position> positions = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Page<Position>>() {

			@Override
			public Page<Position> extract(SearchResponse response) {
				Set<Long> positionIds = new LinkedHashSet<Long>();

				for (SearchHit hit : response.getHits()) {
					Long id = ((Number) hit.field("docId").getValue()).longValue();
					positionIds.add(id);
				}

				List<Position> positions = positionService.getPositionByStatusAndPublishCareersite(Status.Active.toString(), positionIds);

				return new PageImpl<Position>(new LinkedList<>(positions), pageable, response.getHits().totalHits());
			}
		});

		return positions;

	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public Page<Candidate> globalSearchCandidate(String queryText, Pageable pageable) throws RecruizException {

		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		// queryText = SearchUtil.cleanQueryText(queryText);

		// use advance query to get the candidate result as the user can
		// enter a
		// boolean query in global search
		AdvancedSearchQueryEntity advancedSearchQuery = new AdvancedSearchQueryEntity();
		advancedSearchQuery.setBooleanQuery(queryText);
		advancedSearchQuery.setSearchIn(String.valueOf(AdvancedSearchIn.All.getCode()));
		advancedSearchQuery.setTab(AdvancedSearchTabs.BOOLEAN.toString());
		List<Candidate> candidates = advanceSearchCandidateNativeModel(advancedSearchQuery, pageable);

		/*Page<Candidate> candidatesList = new PageImpl<Candidate>(candidates, pageable,
				candidates.size());*/
		// TODO we have to fix later
		// this is added for wild card candidate id search (referred as
		// candidate random id)
		// AdvancedSearchQueryEntity advancedSearchQueryFromCandidateId = new
		// AdvancedSearchQueryEntity();
		// advancedSearchQueryFromCandidateId.setBooleanQuery("%" + queryText +
		// "%");
		// advancedSearchQueryFromCandidateId.setSearchIn(String.valueOf(AdvancedSearchIn.Candidate_Id.getCode()));
		// advancedSearchQueryFromCandidateId.setTab(AdvancedSearchTabs.BOOLEAN.toString());
		// Page<Candidate> candidatesFromCandidateId =
		// advanceSearchCandidateNativeModel(
		// advancedSearchQueryFromCandidateId, pageable);
		//
		// result.getContent().addAll(candidates.getContent());
		// result.getContent().addAll(candidatesFromCandidateId.getContent());

		
		int start = pageable.getOffset();
		int end = (start + pageable.getPageSize()) > candidates.size() ? candidates.size() : (start + pageable.getPageSize());
		final Page<Candidate> candidatesList = new PageImpl<Candidate>(candidates.subList(start, end), pageable,
				candidates.size());			
		
		return candidatesList;

	}

	/**
	 * Search full elastic search , it does global search and return matching
	 * clients
	 * 
	 * @param queryText
	 * @return
	 */
	public List<Candidate> globalSearchCandidate(String queryText) throws RecruizException {
		if (queryText == null || queryText.isEmpty())
			throw new RecruizException("GlobalSearch query text cannot be null or empty", ErrorHandler.ELASTICSEARCH_ERROR);

		// queryText = SearchUtil.cleanQueryText(queryText);

		// use advance query to get the candidate result as the user can
		// enter a
		// boolean query in global search
		AdvancedSearchQueryEntity advancedSearchQuery = new AdvancedSearchQueryEntity();
		advancedSearchQuery.setBooleanQuery(queryText);
		advancedSearchQuery.setSearchIn(String.valueOf(AdvancedSearchIn.All.getCode()));
		advancedSearchQuery.setTab(AdvancedSearchTabs.BOOLEAN.toString());
		List<Candidate> candidates = advanceSearchCandidateNativeModel(advancedSearchQuery);

		return candidates;

	}

	/**
	 * Apply additional filter before search
	 * 
	 * @param positionSearchBuilder
	 * @throws RecruizException
	 */
	private void applyAdditionalFilter(BoolQueryBuilder boolQueryBuilder) throws RecruizException {
		applyVendorEmailFilter(boolQueryBuilder);
	}

	/**
	 * This check is applied for vendor login to show only positions shared with
	 * vendor user and vendor team . Method withVendorEmails() does that filter
	 * . We add the vendor main email id to VendorEmails list to apply the
	 * filter
	 * 
	 * @param positionSearchBuilder
	 * @throws RecruizException
	 */
	private void applyVendorEmailFilter(BoolQueryBuilder boolQueryBuilder) throws RecruizException {
		if (userService.isLoggedInUserVendor()) {
			boolQueryBuilder.must(matchQuery("vendorEmails", userService.getVendorEmail()));
		}
	}

	/**
	 * Return the top matching ids of candidate for given position - This is
	 * triggered using match button in ui
	 * 
	 * @param positionCode
	 * @param pageable
	 * @return
	 * @throws RecruizException
	 */
	public Page<Candidate> getTopCandidateForPostion(String positionCode, Pageable pageable) throws RecruizException {
		if (positionCode != null && !positionCode.isEmpty()) {

			PositionSearchBuilder positionSearchBuilder = new PositionSearchBuilder();
			positionSearchBuilder.withPositionCode(positionCode);
			List<PositionSearch> positionSearchs = positionSearchRepo.getResult(positionSearchBuilder);
			if (positionSearchs != null && !positionSearchs.isEmpty()) {
				PositionSearch positionSearch = positionSearchs.get(0);
				CandidateSearchBuilder candidateSearchBuilderExcellent = getExcellentFitSearchBuilder(positionSearch);
				CandidateSearchBuilder candidateSearchBuilderGood = getGoodFitSearchBuilder(positionSearch);
				CandidateSearchBuilder candidateSearchBuilderModerate = getModerateFitSearchBuilder(positionSearch);

				try {
					List<Long> candidateListExcellentIds = candidateSearchRepo.getResultAsIds(candidateSearchBuilderExcellent);
					List<Long> candidateListGoodIds = candidateSearchRepo.getResultAsIds(candidateSearchBuilderGood);
					List<Long> candidateListModerateIds = candidateSearchRepo.getResultAsIds(candidateSearchBuilderModerate);

					List<Candidate> candidateListExcellent = new ArrayList<>();
					if (candidateListExcellentIds != null) {
						candidateListExcellentIds = new ArrayList<>(candidateListExcellentIds);
						candidateListExcellent = candidateService.getAllCandidate(candidateListExcellentIds);
					}

					// CS-918
					SortUtil.sortCandidateModificationDateDesc(candidateListExcellent);

					List<Candidate> candidateListGood = new ArrayList<>();
					if (candidateListExcellentIds != null && candidateListGoodIds != null) {
						// remove same ids in different list so that we have
						// unique list ..excellent is given more pref than good
						// fit etc
						candidateListGoodIds = new ArrayList<>(candidateListGoodIds);
						candidateListGoodIds.removeAll(candidateListExcellentIds);
						candidateListGood = candidateService.getAllCandidate(candidateListGoodIds);
					}

					// CS-918
					SortUtil.sortCandidateModificationDateDesc(candidateListGood);

					List<Candidate> candidateListModerate = new ArrayList<>();
					if (candidateListModerateIds != null && candidateListExcellentIds != null && candidateListGoodIds != null) {
						// remove same ids in different list so that we have
						// unique
						// list ..excellent is given more pref than good fit etc
						candidateListModerateIds = new ArrayList<>(candidateListModerateIds);
						candidateListModerateIds.removeAll(candidateListExcellentIds);
						candidateListModerateIds.removeAll(candidateListGoodIds);
						candidateListModerate = candidateService.getAllCandidate(candidateListModerateIds);
					}

					// CS-918
					SortUtil.sortCandidateModificationDateDesc(candidateListModerate);

					// since candidates are desc order in score and applying
					// pageable after that (Hibernate pageable working on
					// modification date but match API want sort on score)
					// List<Candidate> candidateListExcellent =
					// candidateSearchRepo.getResultAsNativeModel(candidateSearchBuilder);

					List<Candidate> orderedCandidateList = new LinkedList<>();
					if (candidateListExcellent != null) {
						for (Candidate candidate : candidateListExcellent) {
							candidate.setSearchMatch(SearchMatch.Excellent_fit.getDisplayText());
							orderedCandidateList.add(candidate);
						}
					}

					if (candidateListGood != null) {
						for (Candidate candidate : candidateListGood) {
							candidate.setSearchMatch(SearchMatch.Good_fit.getDisplayText());
							orderedCandidateList.add(candidate);
						}
					}

					if (candidateListModerate != null) {
						for (Candidate candidate : candidateListModerate) {
							candidate.setSearchMatch(SearchMatch.Moderate_fit.getDisplayText());
							orderedCandidateList.add(candidate);
						}
					}

					final Page<Candidate> candidatesPage = getPageableCandidates(pageable, orderedCandidateList);
					return candidatesPage;
				} catch (Exception e) {
					// do nothing
					logger.error(e.getMessage(), e);
				}
			}
		}

		return new PageImpl<Candidate>(new ArrayList<Candidate>());
	}

	/**
	 * The search criteria to get excellent auto match for candidate for given
	 * position
	 * 
	 * @param positionSearch
	 * @return
	 */
	private CandidateSearchBuilder getExcellentFitSearchBuilder(PositionSearch positionSearch) {
		//		CandidateSearchBuilder candidateSearchBuilder = new CandidateSearchBuilder();
		//		candidateSearchBuilder.withBooleanSearchType(BooleanSearchType.AND);
		//		if (positionSearch.getMinSal() != 0D && positionSearch.getMaxSal() != 0D)
		//			candidateSearchBuilder.withCTCRange(BooleanSearchType.AND, positionSearch.getMinSal(), positionSearch.getMaxSal());
		//
		//		candidateSearchBuilder.withCurrentLocation(BooleanSearchType.AND, StringUtils.getLocation(positionSearch.getLocation()));
		//		candidateSearchBuilder.withPreferredLocation(BooleanSearchType.OR, StringUtils.getLocation(positionSearch.getLocation()));
		//		candidateSearchBuilder.withCurrentDesignation(BooleanSearchType.OR, positionSearch.getTitle());
		//
		//		Set<String> allSkils = new HashSet<>();
		//		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		//		allSkils.addAll(positionSearch.getPositionGoodSkillSet());
		//		candidateSearchBuilder.withSkillSet(BooleanSearchType.OR, allSkils);
		//
		//		// resumes added in last 60 days
		//		candidateSearchBuilder.withResumeFreshnessOnRange(BooleanSearchType.OR, 60, 0);
		//
		//		candidateSearchBuilder.withMandatoryStatus(BooleanSearchType.AND, Status.Active);
		//
		//		// candidateSearchBuilder.withIsServingNoticePeriod(true);
		//		candidateSearchBuilder.withTotalExp(BooleanSearchType.AND, positionSearch.getMinExp(), positionSearch.getMaxExp());
		//		return candidateSearchBuilder;

		Set<String> allSkils = new HashSet<>();
		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		allSkils.addAll(positionSearch.getPositionGoodSkillSet());


		CandidateSearchBuilder candidateSearchBuilder = CandidateSearchBuilder.getInstance()

				.andCurrentLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.andCurrentDesignation(positionSearch.getTitle())
				.andSkillSet(allSkils)
				.andStatus(Status.Active)
				.andTotalExp(positionSearch.getMinExp(), positionSearch.getMaxExp())
				.orPreferredLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.orCTCRange(positionSearch.getMinSal(), positionSearch.getMaxSal())
				.orResumeFreshnessOnRange(new IntRange(0, 60));

		return candidateSearchBuilder;

	}

	/**
	 * The search criteria to get good auto match for candidate for given
	 * position
	 * 
	 * @param positionSearch
	 * @return
	 */
	private CandidateSearchBuilder getGoodFitSearchBuilder(PositionSearch positionSearch) {
		//		CandidateSearchBuilder candidateSearchBuilder = new CandidateSearchBuilder();
		//		candidateSearchBuilder.withBooleanSearchType(BooleanSearchType.AND);
		//		if (positionSearch.getMinSal() != 0D && positionSearch.getMaxSal() != 0D)
		//			candidateSearchBuilder.withCTCRange(BooleanSearchType.OR, positionSearch.getMinSal(), positionSearch.getMaxSal());
		//
		//		candidateSearchBuilder.withCurrentLocation(BooleanSearchType.OR, StringUtils.getLocation(positionSearch.getLocation()));
		//		candidateSearchBuilder.withPreferredLocation(BooleanSearchType.OR, StringUtils.getLocation(positionSearch.getLocation()));
		//
		//		Set<String> allSkils = new HashSet<>();
		//		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		//		allSkils.addAll(positionSearch.getPositionGoodSkillSet());
		//		candidateSearchBuilder.withSkillSet(BooleanSearchType.OR, allSkils);
		//
		//		candidateSearchBuilder.withMandatoryStatus(BooleanSearchType.AND, Status.Active);
		//
		//		// candidateSearchBuilder.withIsServingNoticePeriod(true);
		//		candidateSearchBuilder.withTotalExp(BooleanSearchType.AND, positionSearch.getMinExp(), positionSearch.getMaxExp());
		//		return candidateSearchBuilder;

		Set<String> allSkils = new HashSet<>();
		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		allSkils.addAll(positionSearch.getPositionGoodSkillSet());


		CandidateSearchBuilder candidateSearchBuilder = CandidateSearchBuilder.getInstance()

				.andCurrentLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.andSkillSet(allSkils)
				.andStatus(Status.Active)
				.andTotalExp(positionSearch.getMinExp(), positionSearch.getMaxExp())
				.orPreferredLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.orCTCRange(positionSearch.getMinSal(), positionSearch.getMaxSal())
				.orCurrentDesignation(positionSearch.getTitle())
				.orResumeFreshnessOnRange(new IntRange(0, 365));

		return candidateSearchBuilder;
	}

	/**
	 * The search criteria to get moderate auto match for candidate for given
	 * position
	 * 
	 * @param positionSearch
	 * @return
	 */
	private CandidateSearchBuilder getModerateFitSearchBuilder(PositionSearch positionSearch) {
		//		CandidateSearchBuilder candidateSearchBuilder = new CandidateSearchBuilder();
		//		candidateSearchBuilder.withBooleanSearchType(BooleanSearchType.OR);
		//		if (positionSearch.getMinSal() != 0D && positionSearch.getMaxSal() != 0D)
		//			candidateSearchBuilder.withCTCRange(BooleanSearchType.OR, positionSearch.getMinSal(), positionSearch.getMaxSal());
		//
		//		candidateSearchBuilder.withCurrentLocation(BooleanSearchType.OR, StringUtils.getLocation(positionSearch.getLocation()));
		//		candidateSearchBuilder.withPreferredLocation(BooleanSearchType.OR, StringUtils.getLocation(positionSearch.getLocation()));
		//
		//		Set<String> allSkils = new HashSet<>();
		//		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		//		allSkils.addAll(positionSearch.getPositionGoodSkillSet());
		//		candidateSearchBuilder.withSkillSet(BooleanSearchType.OR, allSkils);
		//
		//		candidateSearchBuilder.withMandatoryStatus(BooleanSearchType.AND, Status.Active);
		//
		//		return candidateSearchBuilder;

		Set<String> allSkils = new HashSet<>();
		allSkils.addAll(positionSearch.getPositionReqSkillSet());
		allSkils.addAll(positionSearch.getPositionGoodSkillSet());


		CandidateSearchBuilder candidateSearchBuilder = CandidateSearchBuilder.getInstance()

				.andSkillSet(allSkils)
				.andStatus(Status.Active)
				.orCurrentLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.orTotalExp(positionSearch.getMinExp(), positionSearch.getMaxExp())
				.orPreferredLocation(StringUtils.getLocation(positionSearch.getLocation()))
				.orCTCRange(positionSearch.getMinSal(), positionSearch.getMaxSal())
				.orCurrentDesignation(positionSearch.getTitle());


		return candidateSearchBuilder;
	}

	/**
	 * Get the possible string suggest result for the text typed by user . The
	 * text typed by user is passed as arg . For eg for Bangalore the user would
	 * type 'Ban' and the method would return 'Bangalore', 'Bandipur' etc
	 * 
	 * @param location
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPostionLocation(String location) throws RecruizException {
		if (location == null || location.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionSuggestLocation(location);
	}

	/**
	 * Get the possible string suggest result for the text typed by user . The
	 * text typed by user is passed as arg . For eg for Bangalore the user would
	 * type 'Ban' and the method would return 'Bangalore', 'Bandipur' etc
	 * 
	 * @param location
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPostionRequestLocation(String location) throws RecruizException {
		if (location == null || location.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionRequestSuggestLocation(location);
	}

	/**
	 * Get the possible string suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param title
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPositionTitle(String title) throws RecruizException {
		if (title == null || title.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionSuggestTitle(title);
	}

	/**
	 * Get the possible string suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param title
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPositionRequestTitle(String title) throws RecruizException {
		if (title == null || title.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionRequestSuggestTitle(title);
	}

	/**
	 * Get the possible skill suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param skill
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPositionSkills(String skill) throws RecruizException {
		if (skill == null || skill.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionSuggestSkills(skill);
	}

	/**
	 * Get the possible skill suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param skill
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestPositionRequestSkills(String skill) throws RecruizException {
		if (skill == null || skill.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.positionRequestSuggestSkills(skill);
	}

	/**
	 * Get the possible full name for the text typed by user . The text typed by
	 * user is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateFullName(String fullName) throws RecruizException {
		if (fullName == null || fullName.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestFullName(fullName);
	}

	/**
	 * Get the possible email for the text typed by user . The text typed by
	 * user is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateEmail(String email) throws RecruizException {
		if (email == null || email.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestEmail(email);
	}

	/**
	 * Get the possible name for the text typed by user . The text typed by user
	 * is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestAppUserName(String name) throws RecruizException {
		if (name == null || name.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.userAppSuggestName(name);
	}

	/**
	 * Get the possible email for the text typed by user . The text typed by
	 * user is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestAppUserEmail(String email) throws RecruizException {
		if (email == null || email.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.userAppSuggestEmail(email);
	}

	/**
	 * Get the possible skill suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateSkills(String skillText) throws RecruizException {
		if (skillText == null || skillText.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestSkills(skillText);
	}

	/**
	 * Get the possible skill suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param skillText
	 * @return
	 * @throws RecruizException
	 */
	public List<SkillSuggestDTO> suggestCandidateSkillsForAdvanceSearch(String skillText) throws RecruizException {
		List<SkillSuggestDTO> skillSetSuggestList = new ArrayList<>();
		if (skillText == null || skillText.isEmpty())
			return skillSetSuggestList;

		suggestSearchRepo.refresh();

		List<String> resultSkills = suggestSearchRepo.candidateSuggestSkillsForAdvanceSearch(skillText);
		for (String skill : resultSkills) {
			if (skill != null && !skill.isEmpty()) {
				SkillSuggestDTO skillSuggestDTO= new SkillSuggestDTO();
				skillSuggestDTO.setId(skill.hashCode());
				skillSuggestDTO.setSkill(skill);
				skillSetSuggestList.add(skillSuggestDTO);
			}
		}

		return skillSetSuggestList;
	}

	/**
	 * Get the possible location suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param prefLocation
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidatePreferredLocation(String prefLocation) throws RecruizException {
		if (prefLocation == null || prefLocation.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestPreferredLocation(prefLocation);
	}

	/**
	 * Get the possible location suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param currLocation
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateCurrentLocation(String currLocation) throws RecruizException {
		if (currLocation == null || currLocation.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestCurrentLocation(currLocation);
	}

	/**
	 * Get the possible educational qualification suggest result for the text
	 * typed by user . The text typed by user is passed as arg .
	 * 
	 * @param prefLocation
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateEducationalQualification(String educationalQualification) throws RecruizException {
		if (educationalQualification == null || educationalQualification.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestEducationalQualification(educationalQualification);
	}

	/**
	 * Get the possible educational institute suggest result for the text typed
	 * by user . The text typed by user is passed as arg .
	 * 
	 * @param prefLocation
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCandidateEducationalInstitute(String educationalInstitute) throws RecruizException {
		if (educationalInstitute == null || educationalInstitute.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestEducationalInstitute(educationalInstitute);
	}

	/**
	 * Get the possible company suggest result for the text typed by user . The
	 * text typed by user is passed as arg .
	 * 
	 * @param currCompany
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestCurrentCompany(String currCompany) throws RecruizException {
		if (currCompany == null || currCompany.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.candidateSuggestCurrentCompany(currCompany);
	}

	/**
	 * Get the possible client name suggest result for the text typed by user .
	 * The text typed by user is passed as arg .
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestClientLocation(String location) throws RecruizException {
		if (location == null || location.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.clientSuggestLocation(location);
	}

	/**
	 * Get the possible prospect name suggest result for the text typed by user
	 * . The text typed by user is passed as arg .
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestProspectLocation(String location) throws RecruizException {
		if (location == null || location.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.prospectSuggestLocation(location);
	}

	/**
	 * Get the possible client name suggest result for the text typed by user .
	 * The text typed by user is passed as arg .
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestClientName(String name) throws RecruizException {
		if (name == null || name.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.clientSuggestName(name);
	}

	/**
	 * Get the possible prospect company name suggest result for the text typed
	 * by user . The text typed by user is passed as arg .
	 * 
	 * @param name
	 * @return
	 * @throws RecruizException
	 */
	public List<String> suggestProspectCompanyName(String name) throws RecruizException {
		if (name == null || name.isEmpty())
			return new ArrayList<>();

		suggestSearchRepo.refresh();

		return suggestSearchRepo.prospectSuggestCompanyName(name);
	}

	/**
	 * Get result for candidate page filter
	 * 
	 * @param candidateSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<CandidateSearch> getCandidateResult(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {
		if (candidateSearchBuilder == null)
			throw new RecruizException("CandidateSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		candidateSearchRepo.refresh();

		return candidateSearchRepo.getResult(candidateSearchBuilder);
	}

	/**
	 * Get result for candidate page filter
	 * 
	 * @param candidateSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<CandidateSearch> getCandidateResult(CandidateDuplicateSearchBuilder candidateDuplicateSearchBuilder)
			throws RecruizException {
		if (candidateDuplicateSearchBuilder == null)
			throw new RecruizException("CandidateDuplicateSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		candidateSearchRepo.refresh();

		return candidateSearchRepo.getResult(candidateDuplicateSearchBuilder);
	}

	/**
	 * Get result only ids for candidate page filter
	 * 
	 * @param candidateSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<IdWithScore> getCandidateResultAsIds(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {
		if (candidateSearchBuilder == null)
			throw new RecruizException("CandidateSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);
		try {
			candidateSearchRepo.refresh();

			return candidateSearchRepo.getResultAsIdsWithScore(candidateSearchBuilder);
		} catch (RecruizEmptySearchCriteriaException e) {
			return new ArrayList<IdWithScore>();
		}

	}

	/**
	 * Get result as Candidate RDBMS Model object instead of CandidateSearch
	 * elasticsearch object for candidate page filter
	 * 
	 * @param candidateSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<Candidate> getCandidateResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {

		if (candidateSearchBuilder == null)
			throw new RecruizException("CandidateSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);
		try {
			candidateSearchRepo.refresh();
			return candidateSearchRepo.getResultAsNativeModel(candidateSearchBuilder);
		} catch (RecruizEmptySearchCriteriaException e) {
			return new ArrayList<Candidate>();
		}

	}

	/**
	 * Get result as Candidate RDBMS Model object instead of CandidateSearch
	 * elasticsearch object for candidate page filter with pagination
	 * 
	 * @param candidateSearchBuilder
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<Candidate> getCandidateResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException {

		if (candidateSearchBuilder == null)
			throw new RecruizException("CandidateSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);
		try {
			candidateSearchRepo.refresh();
			return candidateSearchRepo.getResultAsNativeModel(candidateSearchBuilder, pageable, sortFieldName, order);
		} catch (RecruizEmptySearchCriteriaException e) {
			// checking user has view all candidate permission
			if (checkPermissionService.isSuperAdmin() || checkPermissionService.hasViewAllCandidatesPermission()) {
				return candidateService.findAll(pageable);
			} else {
				return candidateService.getAllCandidateByOwner(pageable, userService.getLoggedInUserEmail());
			}
		}
	}

	/**
	 * Get result as Candidate RDBMS Model object instead of advanced
	 * CandidateSearch elasticsearch object for candidate page filter
	 * 
	 * @param advancedSearchQuery
	 * @param pageable
	 * @return
	 * @throws RecruizException
	 */
	public List<Candidate> advanceSearchCandidateNativeModel(AdvancedSearchQueryEntity advancedSearchQuery, Pageable pageable)
			throws RecruizException {

		CandidateAdvanceSearchBuilder candidateAdvanceSearchBuilder = buildAdvanceQuery(advancedSearchQuery);
		List<Candidate> candidatesFromDB = new ArrayList<>();
		try {
			candidateSearchRepo.refresh();
			// since candidates are desc order in score and applying
			// pageable after that (Hibernate pageable working on
			// modification date it should sort on score)
			candidatesFromDB = candidateSearchRepo.getResultAsNativeModel(candidateAdvanceSearchBuilder);

			//	final Page<Candidate> candidatesList = getPageableCandidates(pageable, candidatesFromDB);

			return candidatesFromDB;

		} catch (RecruizEmptySearchCriteriaException e) {
			//return new PageImpl<Candidate>(new ArrayList<Candidate>());
			return candidatesFromDB;
		}

	}

	private Page<Candidate> getPageableCandidates(Pageable pageable, List<Candidate> candidatesFromDB) {
		int start = pageable.getOffset();
		int end = (start + pageable.getPageSize()) > candidatesFromDB.size() ? candidatesFromDB.size() : (start + pageable.getPageSize());
		final Page<Candidate> candidatesList = new PageImpl<Candidate>(candidatesFromDB.subList(start, end), pageable,
				candidatesFromDB.size());
		return candidatesList;
	}

	/**
	 * Get result as Candidate RDBMS Model object instead of CandidateSearch
	 * elasticsearch object for candidate filter
	 * 
	 * @param candidateSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<Candidate> advanceSearchCandidateNativeModel(AdvancedSearchQueryEntity advancedSearchQueryEntity) throws RecruizException {

		CandidateAdvanceSearchBuilder candidateAdvanceSearchBuilder = buildAdvanceQuery(advancedSearchQueryEntity);
		try {
			candidateSearchRepo.refresh();
			return candidateSearchRepo.getResultAsNativeModel(candidateAdvanceSearchBuilder);
		} catch (RecruizEmptySearchCriteriaException e) {
			return new ArrayList<Candidate>();
		}
	}

	private CandidateAdvanceSearchBuilder buildAdvanceQuery(AdvancedSearchQueryEntity advancedSearchQueryEntity) throws RecruizException {
		if (advancedSearchQueryEntity == null)
			throw new RecruizException("Advanced search query cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		CandidateAdvanceSearchBuilder candidateAdvanceSearchBuilder = new CandidateAdvanceSearchBuilder();

		List<String> currentLocList = sixthSenseCityService.findCityNameByCodes(advancedSearchQueryEntity.getCurrLocationList());

		List<String> prefLocList = sixthSenseCityService.findCityNameByCodes(advancedSearchQueryEntity.getPrefLocationList());

		if (StringUtils.isValid(advancedSearchQueryEntity.getAllKeyword()))
			candidateAdvanceSearchBuilder.withAllKeyword(advancedSearchQueryEntity.getAllKeyword());

		if (StringUtils.isValid(advancedSearchQueryEntity.getAnyKeyword()))
			candidateAdvanceSearchBuilder.withAnyKeyword(advancedSearchQueryEntity.getAnyKeyword());

		if (StringUtils.isValid(advancedSearchQueryEntity.getExcludeKeyword()))
			candidateAdvanceSearchBuilder.withExcludeKeyword(advancedSearchQueryEntity.getExcludeKeyword());

		if (StringUtils.isValid(advancedSearchQueryEntity.getBooleanQuery()))
			candidateAdvanceSearchBuilder.withBooleanQuery(advancedSearchQueryEntity.getBooleanQuery());

		if (StringUtils.isValid(advancedSearchQueryEntity.getSearchIn())) {
			AdvancedSearchIn searchIn = AdvancedSearchIn.valueOf(Integer.valueOf(advancedSearchQueryEntity.getSearchIn()));
			candidateAdvanceSearchBuilder.withAdvancedSearchIn(searchIn);
		}

		if (StringUtils.isValid(advancedSearchQueryEntity.getJobStatus()) && StringUtils.isValid(advancedSearchQueryEntity.getJobType())) {

			SixthSenseJobType jobType = SixthSenseJobType.valueOf(Integer.valueOf(advancedSearchQueryEntity.getJobType()));

			SixthSenseJobStatus jobStatus = SixthSenseJobStatus.valueOf(Integer.valueOf(advancedSearchQueryEntity.getJobStatus()));

			SixthSenseJobStatus employmentTypes = SixthSenseJobStatus.valueOf(Integer.valueOf(advancedSearchQueryEntity.getEmploymentTypes()));

			List<String> jobTypeList = new ArrayList<String>();
			if (!SixthSenseJobType.Any.equals(jobType)) {
				jobTypeList.add(jobType.toString());
			}
			if (!SixthSenseJobStatus.Any.equals(jobStatus)) {
				jobTypeList.add(jobStatus.toString());
			}
			if (!SixthSenseJobStatus.Any.equals(employmentTypes)) {
				jobTypeList.add(employmentTypes.toString());
			}

			if (jobTypeList != null && !jobTypeList.isEmpty())
				candidateAdvanceSearchBuilder.withJobType(jobTypeList);

		}

		if (advancedSearchQueryEntity.getMinExp() != 0 || advancedSearchQueryEntity.getMaxExp() != 0)
			candidateAdvanceSearchBuilder.withTotalExp(advancedSearchQueryEntity.getMinExp(), advancedSearchQueryEntity.getMaxExp());

		if (advancedSearchQueryEntity.getMinSalary() != 0 || advancedSearchQueryEntity.getMaxSalary() != 0)
			candidateAdvanceSearchBuilder.withCTCRange(advancedSearchQueryEntity.getMinSalary() * 100000,
					advancedSearchQueryEntity.getMaxSalary() * 100000);

		if (advancedSearchQueryEntity.getMinAge() != 0 || advancedSearchQueryEntity.getMaxAge() != 0)
			candidateAdvanceSearchBuilder.withAgeOnRange(advancedSearchQueryEntity.getMinAge(), advancedSearchQueryEntity.getMaxAge());

		List<AdvancedSearchNoticePeriod> noticePeriodRange = new ArrayList<AdvancedSearchNoticePeriod>();
		for (String noticePeriod : advancedSearchQueryEntity.getNoticePeriodList()) {

			AdvancedSearchNoticePeriod noticePeriodValue = AdvancedSearchNoticePeriod.valueOf(Integer.valueOf(noticePeriod));
			if (AdvancedSearchNoticePeriod.Currently_Serving_Notice_Period.toString().equals(noticePeriodValue.toString())) {
				candidateAdvanceSearchBuilder.withIsServingNoticePeriod(true);
			} else {
				noticePeriodRange.add(noticePeriodValue);
			}
		}

		if (noticePeriodRange != null && !noticePeriodRange.isEmpty()) {
			candidateAdvanceSearchBuilder.withNoticePeriodRange(BooleanSearchType.OR, noticePeriodRange);
		}

		for (String currLocation : currentLocList) {
			candidateAdvanceSearchBuilder.withCurrentLocation(currLocation);
		}

		for (String prefLocation : prefLocList) {
			candidateAdvanceSearchBuilder.withPreferredLocation(prefLocation);
		}

		if (StringUtils.isValid(advancedSearchQueryEntity.getResumeFreshness())) {
			ResumeFreshness resume = ResumeFreshness.getValueof(advancedSearchQueryEntity.getResumeFreshness());
			candidateAdvanceSearchBuilder.withResumeFreshnessOnRange(resume);
		}

		// check if only female candidate select from advance search
		if (null != advancedSearchQueryEntity.getFemaleCandidate() && advancedSearchQueryEntity.getFemaleCandidate())
			candidateAdvanceSearchBuilder.withGender(advancedSearchQueryEntity.getFemaleCandidate());

		// only current designation searching
		for (SixthSenseDesignationType designationType : SixthSenseDesignationType.values()) {
			if (String.valueOf(designationType.getCode()).equals(advancedSearchQueryEntity.getDesignationType())
					&& !advancedSearchQueryEntity.getDesignationType().equals(SixthSenseDesignationType.Previous.getCode())) {
				if (StringUtils.isValid(advancedSearchQueryEntity.getDesignation())) {
					candidateAdvanceSearchBuilder.withCurrentDesignation(advancedSearchQueryEntity.getDesignation());
				}
			}
		}

		for (SixthSenseIncludeCompanyType includeCompanyType : SixthSenseIncludeCompanyType.values()) {
			if (String.valueOf(includeCompanyType.getCode()).equals(advancedSearchQueryEntity.getIncludeCompanyType())) {
				if (StringUtils.isValid(advancedSearchQueryEntity.getIncludeCompany())) {

					switch (includeCompanyType) {
					case Current:
						candidateAdvanceSearchBuilder.withCurrentCompany(advancedSearchQueryEntity.getIncludeCompany());
						break;
					case Previous:
						candidateAdvanceSearchBuilder.withPreviousCompany(advancedSearchQueryEntity.getIncludeCompany());
						break;
					case Current_Previous:
						candidateAdvanceSearchBuilder.withCurrentCompany(BooleanSearchType.OR,
								advancedSearchQueryEntity.getIncludeCompany());
						candidateAdvanceSearchBuilder.withPreviousCompany(BooleanSearchType.OR,
								advancedSearchQueryEntity.getIncludeCompany());
						break;

					}
				}
			}
		}

		if (StringUtils.isValid(advancedSearchQueryEntity.getUniversity()))
			candidateAdvanceSearchBuilder.withEducationalInstitute(advancedSearchQueryEntity.getUniversity());

		return candidateAdvanceSearchBuilder;
	}

	/**
	 * Get result for client page filter
	 * 
	 * @param clientSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<ClientSearch> getClientResult(ClientSearchBuilder clientSearchBuilder) throws RecruizException {
		if (clientSearchBuilder == null)
			throw new RecruizException("ClientSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		clientSearchRepo.refresh();

		return clientSearchRepo.getResult(clientSearchBuilder);
	}

	/**
	 * Get result only ids for client page filter
	 * 
	 * @param clientSearchBuilder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	public List<Long> getClientResultAsIds(ClientSearchBuilder clientSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		if (clientSearchBuilder == null)
			throw new RecruizException("ClientSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		clientSearchRepo.refresh();

		return clientSearchRepo.getResultAsIds(clientSearchBuilder);
	}

	/**
	 * Get result as Client RDBMS Model object instead of ClientSearch
	 * elasticsearch object for client page filter
	 * 
	 * @param clientSearchBuilder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	public List<ClientOpeningCountDTO> getClientResultAsNativeModel(ClientSearchBuilder clientSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		if (clientSearchBuilder == null)
			throw new RecruizException("ClientSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		clientSearchRepo.refresh();

		return clientSearchRepo.getResultAsNativeModel(clientSearchBuilder);
	}

	/**
	 * Get result as Client RDBMS Model object instead of ClientSearch
	 * elasticsearch object for client page filter
	 * 
	 * @param clientSearchBuilder
	 * @param loggedInUser
	 * @param pageable
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws RecruizException
	 */
	@SuppressWarnings("unchecked")
	public Page<ClientOpeningCountDTO> getClientResultAsNativeModel(ClientSearchBuilder clientSearchBuilder, User loggedInUser,
			Pageable pageable, String sortField, SortOrder sortOrder) throws RecruizException {
		if (clientSearchBuilder == null)
			throw new RecruizException("ClientSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		List<Long> clientIds = new ArrayList<Long>();


		if(checkPermissionService.isSuperAdmin()) {
			try {
				clientIds = clientSearchRepo.getResultAsIds(clientSearchBuilder, null, sortField, sortOrder);
			} catch (RecruizEmptySearchCriteriaException e) {

				clientIds = clientService.getAllClientForHr(loggedInUser);		

			}
		} else if (checkPermissionService.belongsToHrExecGroup(userService.getLoggedInUserObject().getUserRole())
				|| checkPermissionService.belongsToHrManagerGroup (userService.getLoggedInUserObject().getUserRole())) {

			try {
				clientIds = clientService.getAllClientForHrByIds(loggedInUser, clientSearchRepo.getResultAsIds(clientSearchBuilder, null, sortField, sortOrder));
			} catch (Exception e) {
				clientIds = clientService.getAllClientForHr(loggedInUser);
			}

		} else {
			throw new RecruizException(ErrorHandler.DOES_NOT_HAVE_PERMISSION, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		if (clientIds != null && !clientIds.isEmpty()) {
			// below create hibernate query added for order by clause because
			// sort order wont work in named query
			String hql = "SELECT NEW com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO(c, COUNT(p))"
					+ "FROM client c LEFT JOIN  c.positions p where c.id IN :ids GROUP BY c ORDER BY c." + sortField + " "
					+ sortOrder.toString();
			Query query = entityManager.createQuery(hql);
			query.setParameter("ids", clientIds);
			List<ClientOpeningCountDTO> clientOpeningCountDTOs = query.getResultList();

			int start = pageable.getOffset();
			int end = (start + pageable.getPageSize()) > clientOpeningCountDTOs.size() ? clientOpeningCountDTOs.size()
					: (start + pageable.getPageSize());
			final Page<ClientOpeningCountDTO> page = new PageImpl<ClientOpeningCountDTO>(clientOpeningCountDTOs.subList(start, end),
					pageable, clientOpeningCountDTOs.size());
			return page;
		}

		return new PageImpl<ClientOpeningCountDTO>(new ArrayList<ClientOpeningCountDTO>());
	}

	/**
	 * Get result for Position page filter
	 * 
	 * @param positionSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<PositionSearch> getPositionResult(PositionSearchBuilder positionSearchBuilder) throws RecruizException {
		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionSearchRepo.refresh();

		return positionSearchRepo.getResult(positionSearchBuilder);
	}

	/**
	 * Get result for Position page filter
	 * 
	 * @param positionSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<ProspectSearch> getProspectResult(ProspectSearchBuilder prospectSearchBuilder) throws RecruizException {
		if (prospectSearchBuilder == null)
			throw new RecruizException("ProspectSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		prospectSearchRepo.refresh();

		return prospectSearchRepo.getResult(prospectSearchBuilder);
	}

	/**
	 * Get result for Position page filter
	 * 
	 * @param positionRequestSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<PositionRequestSearch> getPositionRequestResult(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException {
		if (positionRequestSearchBuilder == null)
			throw new RecruizException("PositionRequestSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionRequestSearchRepo.refresh();

		return positionRequestSearchRepo.getResult(positionRequestSearchBuilder);
	}

	/**
	 * Get result only ids for Position page filter
	 * 
	 * @param positionSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<Long> getPositionResultAsIds(PositionSearchBuilder positionSearchBuilder) throws RecruizException {
		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		try {
			positionSearchRepo.refresh();
			return positionSearchRepo.getResultAsIds(positionSearchBuilder);
		} catch (RecruizEmptySearchCriteriaException e) {
			return new ArrayList<Long>();
		}

	}

	/**
	 * Get result only ids for Position page filter
	 * 
	 * @param positionSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<Long> getProspectResultAsIds(ProspectSearchBuilder prospectSearchBuilder) throws RecruizException {
		if (prospectSearchBuilder == null)
			throw new RecruizException("ProspectSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		try {
			prospectSearchRepo.refresh();
			return prospectSearchRepo.getResultAsIds(prospectSearchBuilder);
		} catch (RecruizEmptySearchCriteriaException e) {
			return new ArrayList<Long>();
		}

	}

	/**
	 * Get result only ids for Position page filter
	 * 
	 * @param positionRequestSearchBuilder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	public List<Long> getPositionRequestResultAsIds(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		if (positionRequestSearchBuilder == null)
			throw new RecruizException("PositionRequestSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionRequestSearchRepo.refresh();

		return positionRequestSearchRepo.getResultAsIds(positionRequestSearchBuilder);
	}

	/**
	 * Get the result as Position RDBMS Model object instead of PositionSearch
	 * elasticsearch object for position page filter
	 * 
	 * @param positionSearchBuilder
	 * @return
	 * @throws RecruizException
	 */
	public List<Position> getPositionResultAsNativeModel(PositionSearchBuilder positionSearchBuilder, User loggedInUser)
			throws RecruizException {

		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionSearchRepo.refresh();

		if (checkPermissionService.belongsToHrExecGroup(loggedInUser.getUserRole())) {
			try {
				return positionSearchRepo.getResultAsNativeModelOwnerOrHrExec(positionSearchBuilder, loggedInUser,
						userService.loggedInUserSet());
			} catch (RecruizEmptySearchCriteriaException e) {
				return new ArrayList<Position>();
			}
		} else {
			try {
				return positionSearchRepo.getAllResultAsNativeModel(positionSearchBuilder);
			} catch (RecruizEmptySearchCriteriaException e) {
				return new ArrayList<Position>();
			}

		}
	}

	/**
	 * Get the result as Prospect RDBMS Model object instead of ProspectSearch
	 * 
	 * @param prospectSearchBuilder
	 * @param loggedInUser
	 * @return
	 * @throws RecruizException
	 */
	public List<Prospect> getProspectResultAsNativeModel(ProspectSearchBuilder prospectSearchBuilder, User loggedInUser)
			throws RecruizException {

		if (prospectSearchBuilder == null)
			throw new RecruizException("ProspectSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		prospectSearchRepo.refresh();

		if (checkPermissionService.belongsToHrExecGroup(loggedInUser.getUserRole())) {
			try {
				return prospectSearchRepo.getResultAsNativeModelOwner(prospectSearchBuilder, loggedInUser);
			} catch (RecruizEmptySearchCriteriaException e) {
				return new ArrayList<Prospect>();
			}
		} else {
			try {
				return prospectSearchRepo.getAllResultAsNativeModel(prospectSearchBuilder);
			} catch (RecruizEmptySearchCriteriaException e) {
				return new ArrayList<Prospect>();
			}

		}
	}

	/**
	 * Get the result as Position RDBMS Model object instead of PositionSearch
	 * elasticsearch object for position page filter using pageable
	 * 
	 * @param positionSearchBuilder
	 * @param loggedInUser
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<Position> getPositionResultAsNativeModel(PositionSearchDTO positionSearchDTO,PositionSearchBuilder positionSearchBuilder, User loggedInUser, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException {

		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionSearchRepo.refresh();

		// as per new requirement only respective positions should be displayed
		// for HR Manager and HR executive
		// if
		// (checkPermissionService.belongsToHrExecGroup(loggedInUser.getUserRole()))
		// {
		if (userService.isLoggedInUserVendor()) {
			try {
				Set<Vendor> vendors = new HashSet<>();
				vendors.add(userService.getLoggedInUserVendor());
				return positionSearchRepo.getResultAsNativeModelOwnerOrVendor(positionSearchBuilder, loggedInUser, vendors, pageable,
						sortFieldName, order);
				/*List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				List<Position> posiList = positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams);
				Page<Position> posiListData = getRefinePositionList(positionSearchDTO, posiList,pageable);
				return posiListData;*/

			} catch (Exception e) {
				List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				return positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams, pageable);

			}
		} else
			try {
				return positionSearchRepo.getResultAsNativeModelOwnerOrHrExec(positionSearchBuilder, loggedInUser,
						userService.loggedInUserSet(), pageable, sortFieldName, order);
				/*List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				List<Position> posiListData = positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams);
				Page<Position> posiList = getRefinePositionList(positionSearchDTO, posiListData,pageable);
				return posiList;*/

			} catch (Exception e) {
				List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				return positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams, pageable);
			}

		// } else {
		// try {
		// return
		// positionSearchRepo.getAllResultAsNativeModel(positionSearchBuilder,
		// pageable);
		// } catch (Exception e) {
		// return positionRepository.findAll(pageable);
		// }
		// }
	}

	public Page<Position> getPositionResultAsNativeModelForNonAdmin(PositionSearchDTO positionSearchDTO,PositionSearchBuilder positionSearchBuilder, User loggedInUser, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException {

		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionSearchRepo.refresh();

		if (userService.isLoggedInUserVendor()) {
			try {
				Set<Vendor> vendors = new HashSet<>();
				vendors.add(userService.getLoggedInUserVendor());
				return positionSearchRepo.getResultAsNativeModelOwnerOrVendor(positionSearchBuilder, loggedInUser, vendors, pageable,
						sortFieldName, order);

			} catch (Exception e) {
				List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				return positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams, pageable);

			}
		} else
			try {
				return positionSearchRepo.getResultAsNativeModelOwnerOrHrExecForNonAdmin(positionSearchBuilder, loggedInUser,
						userService.loggedInUserSet(), pageable, sortFieldName, order);
			} catch (Exception e) {
				List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
				return positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
						userService.loggedInUserSet(), teams, pageable);
			}
	}


	/*	private Page<Position> getRefinePositionList(PositionSearchDTO positionSearchDTO, List<Position> posiListData, Pageable pageable) {


		Page<Position> positionList = new PageImpl<Position>(new ArrayList<Position>());
		List<Position> positionListData = new ArrayList<>();
		int filterData = 0;


		boolean CloseByDate = true;
		boolean LocationList = true;
		boolean NameList = true;
		boolean Skills = true;
		boolean TypeList = true;
		boolean StatusList = true;

		for (Position position : posiListData) {
			int x=0;
			if (positionSearchDTO.getCloseByDate() != null && positionSearchDTO.getCloseByDate().length > 0) {		
				filterData = 1; 
				CloseByDate = false;
				Date today = new Date();
				for (String posdate : positionSearchDTO.getCloseByDate()) {

					if (posdate != null && posdate != ""){

						if(posdate.equalsIgnoreCase("Today")){

							if(position.getCloseByDate().after(today) && position.getCloseByDate().before(today)){
								CloseByDate = true;
								x=1;
							}
							if(x==1)
								break;
						}

						if(posdate.equalsIgnoreCase("Next_7_Days")){
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.DAY_OF_YEAR, 7);
							Date positionDate = calendar.getTime();
							if(position.getCloseByDate().after(today) && position.getCloseByDate().before(positionDate)){
								CloseByDate = true;
								x=1;
							}
							if(x==1)
								break;

						}

						if(posdate.equalsIgnoreCase("Next_14_Days")){
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.DAY_OF_YEAR, 14);
							Date positionDate = calendar.getTime();
							if(position.getCloseByDate().after(today) && position.getCloseByDate().before(positionDate)){
								CloseByDate = true;
								x=1;
							}
							if(x==1)
								break;
						}

						if(posdate.equalsIgnoreCase("Next_30_Days")){
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.DAY_OF_YEAR, 30);
							Date positionDate = calendar.getTime();
							if(position.getCloseByDate().after(today) && position.getCloseByDate().before(positionDate)){
								CloseByDate = true;
								x=1;
							}
							if(x==1)
								break;
						}

						if(posdate.equalsIgnoreCase("More_Than_30_Days")){
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.DAY_OF_YEAR, 30);
							Date positionDate = calendar.getTime();
							if(position.getCloseByDate().after(positionDate)){
								CloseByDate = true;
								x=1;
							}
							if(x==1)
								break;
						}
					}

				}

			}

			if (positionSearchDTO.getLocationList() != null && positionSearchDTO.getLocationList().length > 0
					&& !positionSearchDTO.getLocationList()[0].equals("")){
				filterData = 1;
				LocationList = false;
				Set<String> positionListOfLocation = new  HashSet<String>();

				for(String location:positionSearchDTO.getLocationList()){
					positionListOfLocation.add(location);
				}

				for(String location:positionListOfLocation){
					if(position.getLocation().equalsIgnoreCase(location)){
						LocationList = true;
						x=1;
					}
					if(x==1)
						break;
				}

			}


			if (positionSearchDTO.getNameList() != null && positionSearchDTO.getNameList().length > 0
					&& !positionSearchDTO.getNameList()[0].equals("")){
				filterData = 1;
				NameList = false;
				Set<String> positionListOfName = new  HashSet<String>();

				for(String name:positionSearchDTO.getNameList()){
					positionListOfName.add(name);
				}

				for(String name:positionListOfName){

					if(position.getTitle().equalsIgnoreCase(name)){
						NameList = true;
						x=1;
					}
					if(x==1)
						break;
				}

			}


			if (positionSearchDTO.getSkills() != null && positionSearchDTO.getSkills().length > 0
					&& !positionSearchDTO.getSkills()[0].equals("")){
				filterData = 1;
				Skills = false;
				Set<String> positionListOfSkills = new  HashSet<String>();
				Set<String> goodSkills = position.getGoodSkillSet();
				Set<String> reqSkills = position.getReqSkillSet();
				goodSkills.addAll(reqSkills);

				for(String skill:positionSearchDTO.getSkills()){
					positionListOfSkills.add(skill);
				}

				for(String type:positionListOfSkills){

					for (String skill : goodSkills) {
						if(skill.equalsIgnoreCase(type)){
							Skills = true;
							x=1;
						}
						if(x==1)
							break;
					}
					if(x==1)
						break;
				}

			}


			if (positionSearchDTO.getTypeList() != null && positionSearchDTO.getTypeList().length > 0
					&& !positionSearchDTO.getTypeList()[0].equals("")){
				filterData = 1;
				TypeList = false;
				Set<String> positionListOfType = new  HashSet<String>();

				for(String type:positionSearchDTO.getTypeList()){
					positionListOfType.add(type);
				}

				for(String type:positionListOfType){

					if(position.getType().equalsIgnoreCase(type)){
						TypeList = true;
						x=1;
					}
					if(x==1)
						break;
				}

			}


			if (positionSearchDTO.getStatusList() != null && positionSearchDTO.getStatusList().length > 0
					&& !positionSearchDTO.getStatusList()[0].equals("")){
				filterData = 1;
				StatusList = false;
				Set<String> positionListOfStatus = new  HashSet<String>();

				for(String status:positionSearchDTO.getStatusList()){
					positionListOfStatus.add(status);
				}

				for(String status:positionListOfStatus){

					if(position.getStatus().equalsIgnoreCase(status)){
						StatusList = true;
						x=1;
					}
					if(x==1)
						break;
				}	

			}

			if(filterData==1){
				if(CloseByDate && LocationList && NameList && Skills && TypeList && StatusList){
					positionListData.add(position);
				}
			}

		}

		List<Position> dataByPageable = new ArrayList<>();
		if(positionListData.size()>0){			
			int startData = pageable.getPageNumber()*10;
			int endData = startData + 10;

			if(endData<=positionListData.size()){
				for (;startData<endData;startData++) {
					Position newData = positionListData.get(startData);
					if(newData!=null){
						dataByPageable.add(newData);
					}
				}
			}else{	
				endData = positionListData.size();

				for (;startData<endData;startData++) {
					Position newData = positionListData.get(startData);
					if(newData!=null){
						dataByPageable.add(newData);
					}
				}
			}
		}
		if(filterData==1){
		positionList = new PageImpl<>(dataByPageable, pageable, positionListData.size());
		}else{
			positionList = new PageImpl<>(posiListData, pageable, posiListData.size());
		}
		return positionList;
	}
	 */
	/**
	 * Get the result as Prospect RDBMS Model object instead of ProspectSearch
	 * 
	 * @param prospectSearchBuilder
	 * @param loggedInUser
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<Prospect> getProspectResultAsNativeModel(ProspectSearchBuilder prospectSearchBuilder, User loggedInUser, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException {

		if (prospectSearchBuilder == null)
			throw new RecruizException("ProspectSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		prospectSearchRepo.refresh();
		try {
			return prospectSearchRepo.getResultAsNativeModelOwner(prospectSearchBuilder, loggedInUser, pageable, sortFieldName, order);
		} catch (Exception e) {
			return prospectRepository.findDistinctByOwner(loggedInUser.getEmail(), pageable);
		}
	}

	/**
	 * Get the result as Position RDBMS Model object instead of PositionSearch
	 * elasticsearch object for position page filter using pageable by client
	 * 
	 * @param positionSearchBuilder
	 * @param loggedInUser
	 * @param client
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<Position> getPositionResultAsNativeModelByClient(PositionSearchBuilder positionSearchBuilder, User loggedInUser,
			Client client, Pageable pageable, String sortFieldName, SortOrder order) throws RecruizException {

		if (positionSearchBuilder == null)
			throw new RecruizException("PositionSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionSearchRepo.refresh();

		// as per new requirement only respective positions should be displayed
		// for HR Manager and HR executive
		// if
		// (checkPermissionService.belongsToHrExecGroup(loggedInUser.getUserRole()))
		// {
		try {
			return positionSearchRepo.getResultAsNativeModelOwnerOrHrExecByClient(positionSearchBuilder, loggedInUser,
					userService.loggedInUserSet(), client, pageable, sortFieldName, order);
		} catch (Exception e) {

			// commenting this code as page object with is giving wrong count
			// when and || or pertaor is used
			// return
			// positionRepository.findDistinctByClientAndOwnerOrClientAndHrExecutivesIn(client,
			// loggedInUser.getEmail(), client, userService.loggedInUserSet(),
			// pageable);

			// finding all position for client and then making it as page object
			/*List<Position> allPosition = positionRepository.findDistinctByClientAndOwnerOrClientAndHrExecutivesIn(client,
					userService.getLoggedInUserEmail(), client, userService.loggedInUserSet());*/

			Page<Position> allPosition = positionService.getAllPositionByClient(client,pageable,sortFieldName);

			if (allPosition != null) {
				// making page object of fetched client position
				return allPosition;
			}
			return null;
		}

		// } else {
		// try {
		// return
		// positionSearchRepo.getAllResultAsNativeModelByClient(positionSearchBuilder,
		// client, pageable);
		// } catch (Exception e) {
		// return positionRepository.findByClient(client, pageable);
		// }
		// }
	}

	/**
	 * Get the result as Prospect RDBMS Model object instead of ProspectSearch
	 * 
	 * @param prospectSearchBuilder
	 * @param loggedInUser
	 * @param client
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<Prospect> getProspectResultAsNativeModelByClient(ProspectSearchBuilder prospectSearchBuilder, User loggedInUser,
			Pageable pageable, String sortFieldName, SortOrder order) throws RecruizException {

		if (prospectSearchBuilder == null)
			throw new RecruizException("ProspectSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		prospectSearchRepo.refresh();

		try {
			return prospectSearchRepo.getResultAsNativeModelOwner(prospectSearchBuilder, loggedInUser, pageable, sortFieldName, order);
		} catch (Exception e) {

			// finding all Prospect for client and then making it as page object
			List<Prospect> allProspect = prospectRepository.findDistinctByOwner(userService.getLoggedInUserEmail());
			if (allProspect != null && !allProspect.isEmpty()) {
				// making page object of fetched client position
				return new PageImpl<>(allProspect, pageable, allProspect.size());
			}
			return null;
		}
	}

	/**
	 * Get the result as Position RDBMS Model object instead of PositionSearch
	 * elasticsearch object for position page filter
	 * 
	 * @param positionRequestSearchBuilder
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	public List<PositionRequest> getPositionRequestResultAsNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		if (positionRequestSearchBuilder == null)
			throw new RecruizException("PositionRequestSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		positionRequestSearchRepo.refresh();

		return positionRequestSearchRepo.getResultAsNativeModel(positionRequestSearchBuilder);
	}

	/**
	 * Get the result as Position RDBMS Model object instead of
	 * PositionRequestSearch elasticsearch object for position request page
	 * filter using pageabl
	 * 
	 * @param positionRequestSearchBuilder
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<PositionRequest> getPositionRequestResultAsNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder,
			Pageable pageable, String sortFieldName, SortOrder order) throws RecruizException {
		if (positionRequestSearchBuilder == null)
			throw new RecruizException("PositionRequestSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		try {
			positionRequestSearchRepo.refresh();
			return positionRequestSearchRepo.getResultAstNativeModel(positionRequestSearchBuilder, pageable, sortFieldName, order);
		} catch (Exception e) {
			return positionRequestRepository.findAll(pageable);
		}
	}

	/**
	 * Get the result as Position RDBMS Model object instead of
	 * PositionRequestSearch elasticsearch object for position request page
	 * filter using pageable by client
	 * 
	 * @param positionRequestSearchBuilder
	 * @param clientName
	 * @param pageable
	 * @param sortFieldName
	 * @param order
	 * @return
	 * @throws RecruizException
	 */
	public Page<PositionRequest> getPositionRequestResultAsNativeModelByClient(PositionRequestSearchBuilder positionRequestSearchBuilder,
			String clientName, Pageable pageable, String sortFieldName, SortOrder order) throws RecruizException {
		if (positionRequestSearchBuilder == null)
			throw new RecruizException("PositionRequestSearchBuilder cannot be null", ErrorHandler.ELASTICSEARCH_ERROR);

		try {
			positionRequestSearchRepo.refresh();
			return positionRequestSearchRepo.getResultAsNativeModelByClient(positionRequestSearchBuilder, clientName, pageable,
					sortFieldName, order);
		} catch (Exception e) {
			return positionRequestRepository.findByClientName(clientName, pageable);
		}
	}

	protected String getCurrentTenant() throws RecruizException {
		String tenantId = MultiTenantUtils.getTenant();
		if (springProfileService.isSaasMode() && (tenantId == null || tenantId.isEmpty()))
			throw new RecruizException("Tenant id cannot be missing for elastic search query", ErrorHandler.ELASTICSEARCH_ERROR);

		return tenantId;
	}

	/**
	 * This method is used to check duplicate candidate into local ATS DB and
	 * Sixth Sense
	 * 
	 * @param searchResultMap
	 * @return
	 * @throws RecruizException
	 */
	public Map<String, Page<SixthSenseResultDTO>> checkDuplicateCandidate(Map<String, Page<SixthSenseResultDTO>> searchResultMap)
			throws RecruizException {
		if (searchResultMap == null) {
			return searchResultMap;
		}

		for (String source : searchResultMap.keySet()) {
			Page<SixthSenseResultDTO> sixthSenseResultDTOList = searchResultMap.get(source);

			List<String> candidateExternalAppIds = new ArrayList<>();
			List<String> candidateSha1HashList = new ArrayList<>();

			for (SixthSenseResultDTO sixthSenseSearchDTO : sixthSenseResultDTOList.getContent()) {
				candidateExternalAppIds.add(sixthSenseSearchDTO.getResumeid());
				candidateSha1HashList.add(SearchUtil.candidateHash(sixthSenseSearchDTO.getName(), sixthSenseSearchDTO.getCompany()));
			}

			Map<String, Candidate> resultExternalAppCIdMap = new HashMap<>();
			Map<String, Candidate> resultCandidateSha1HashLMap = new HashMap<>();

			List<Candidate> candidateListFromExternalAppCid = candidateService.getByExternalAppCandidateIdIn(candidateExternalAppIds);
			List<Candidate> candidateListFromSha1Hash = candidateService.getByCandidateSha1HashIn(candidateSha1HashList);

			for (Candidate candidate : candidateListFromExternalAppCid) {
				resultExternalAppCIdMap.put(candidate.getExternalAppCandidateId(), candidate);
			}

			for (Candidate candidate : candidateListFromSha1Hash) {
				resultCandidateSha1HashLMap.put(candidate.getCandidateSha1Hash(), candidate);
			}

			for (SixthSenseResultDTO sixthSenseSearchDTO : sixthSenseResultDTOList.getContent()) {
				Candidate candidate = resultExternalAppCIdMap.get(sixthSenseSearchDTO.getResumeid());
				candidateService.attachCurrentPosition(candidate);

				sixthSenseSearchDTO.setCandidate(candidate);
				if (sixthSenseSearchDTO.getCandidate() == null) {
					sixthSenseSearchDTO.setCandidate(resultCandidateSha1HashLMap
							.get(SearchUtil.candidateHash(sixthSenseSearchDTO.getName(), sixthSenseSearchDTO.getCompany())));
				}
			}

			searchResultMap.put(source, sixthSenseResultDTOList);

		}

		return searchResultMap;
	}

	public boolean getAdvancedSearchQueryStatus(AdvancedSearchQueryEntity advancedSearchQuery) {

		int x = 0;

		if(advancedSearchQuery.getAllKeyword()!=null && !advancedSearchQuery.getAllKeyword().equalsIgnoreCase("") && !advancedSearchQuery.getAllKeyword().isEmpty())
			x=1;
		if(advancedSearchQuery.getAnyKeyword()!=null && !advancedSearchQuery.getAnyKeyword().equalsIgnoreCase("") && !advancedSearchQuery.getAnyKeyword().isEmpty())
			x=1;
		if(advancedSearchQuery.getBasicSearch()!=null && !advancedSearchQuery.getBasicSearch().equalsIgnoreCase("") && !advancedSearchQuery.getBasicSearch().isEmpty())
			x=1;
		if(advancedSearchQuery.getCurrLocationList()!=null && advancedSearchQuery.getCurrLocationList().size()>0)
			x=1;
		if(advancedSearchQuery.getExcludeKeyword()!=null && !advancedSearchQuery.getExcludeKeyword().equalsIgnoreCase("") && !advancedSearchQuery.getExcludeKeyword().isEmpty())
			x=1;
		if(advancedSearchQuery.getFuncRoleList()!=null && advancedSearchQuery.getFuncRoleList().size()>0)
			x=1;
		if(advancedSearchQuery.getFunctionalAreaList()!=null && advancedSearchQuery.getFunctionalAreaList().size()>0)
			x=1;
		if(advancedSearchQuery.getIndustryList()!=null && advancedSearchQuery.getIndustryList().size()>0)
			x=1;
		if(advancedSearchQuery.getNoticePeriodList()!=null && advancedSearchQuery.getNoticePeriodList().size()>0)
			x=1;
		if(advancedSearchQuery.getPassYearDegreeList()!=null && advancedSearchQuery.getPassYearDegreeList().size()>0)
			x=1;
		if(advancedSearchQuery.getPgDegreeList()!=null && advancedSearchQuery.getPgDegreeList().size()>0)
			x=1;
		if(advancedSearchQuery.getPgDegreeSpecList()!=null && advancedSearchQuery.getPgDegreeSpecList().size()>0)
			x=1;
		if(advancedSearchQuery.getPgDegreeTypeList()!=null && advancedSearchQuery.getPgDegreeTypeList().size()>0)
			x=1;
		if(advancedSearchQuery.getPostPGDegreeList()!=null && advancedSearchQuery.getPostPGDegreeList().size()>0)
			x=1;
		if(advancedSearchQuery.getPostPGDegreeSpecList()!=null && advancedSearchQuery.getPostPGDegreeSpecList().size()>0)
			x=1;
		if(advancedSearchQuery.getPostPGDegreeTypeList()!=null && advancedSearchQuery.getPostPGDegreeTypeList().size()>0)
			x=1;
		if(advancedSearchQuery.getPrefLocationList()!=null && advancedSearchQuery.getPrefLocationList().size()>0)
			x=1;
		if(advancedSearchQuery.getUgDegreeList()!=null && advancedSearchQuery.getUgDegreeList().size()>0)
			x=1;
		if(advancedSearchQuery.getUgDegreeSpecList()!=null && advancedSearchQuery.getUgDegreeSpecList().size()>0)
			x=1;
		if(advancedSearchQuery.getUgDegreeTypeList()!=null && advancedSearchQuery.getUgDegreeTypeList().size()>0)
			x=1;


		if(x==1)
			return false;
		else
			return true;
	}

	public List<Candidate> filterCandidateByLanguageAndNationality(List<Candidate> candidateList,
			AdvancedSearchQueryEntity advancedSearchQuery) {

		List<Candidate> newList = new ArrayList<>();
		
		for (Candidate candidate : candidateList) {
			int i=1,j=1,k=1;
			if(advancedSearchQuery.getSources().size()>0 && advancedSearchQuery.getSources()!=null){ 
				i=0;
				
				for(String source:advancedSearchQuery.getSources()){
					
					if(candidate.getSource().equalsIgnoreCase(source)){
						i=1;
					}
				}
				
			} 
			if(advancedSearchQuery.getLanguage()!=null && !advancedSearchQuery.getLanguage().equals("")){
				j=0;
				if(!candidate.getLanguages().equalsIgnoreCase("N/A")){
				
					String[] spilited = candidate.getLanguages().split(",");
					for (String lang : spilited) {
						if(lang.equalsIgnoreCase(advancedSearchQuery.getLanguage())){
							j=1;
						}
					}
					
				}
			}
			if(advancedSearchQuery.getNationality()!=null && !advancedSearchQuery.getNationality().equals("")){
				k=0;
				if(!candidate.getNationality().equalsIgnoreCase("N/A")){
					
					if(candidate.getNationality().equalsIgnoreCase(advancedSearchQuery.getNationality())){
						k=1;
					}
				}
			}

			if(i==1 && j==1 && k==1){
				newList.add(candidate);
			}
		}
		
		
		return newList;
	}

	public RestResponse getEmailList(String type, String searchText) {
		
		
		Set<String> emailList = new HashSet<>();
		
		try{
		List<SendingEmailList> lists = sendingEmailListRepository.findAllByType(type);
 		
		for (SendingEmailList sendingEmailList : lists) {
			
			if(sendingEmailList.getEmail_id().toLowerCase().contains(searchText.toLowerCase()))
				emailList.add(sendingEmailList.getEmail_id());
			
		}
		}catch(Exception e){
			logger.error("getting Error details = "+e);
		}
		return new RestResponse(RestResponse.SUCCESS, emailList);
	}

}
