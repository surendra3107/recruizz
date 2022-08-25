package com.bbytes.recruiz.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.search.repository.CandidateSearchRepo;
import com.bbytes.recruiz.search.repository.ClientSearchRepo;
import com.bbytes.recruiz.search.repository.PositionRequestSearchRepo;
import com.bbytes.recruiz.search.repository.PositionSearchRepo;
import com.bbytes.recruiz.search.repository.ProspectSearchRepo;
import com.bbytes.recruiz.search.repository.SuggestSearchRepo;
import com.bbytes.recruiz.search.repository.UserSearchRepo;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DomainToSearchDomainService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionRequestService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.ProspectService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class ElasticsearchReIndexPerTenantService {

	private static Logger logger = LoggerFactory.getLogger(ElasticsearchReIndexPerTenantService.class);

	@Autowired
	private ClientService clientService;

	@Autowired
	private ProspectService prospectService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private UserService userService;

	@Autowired
	private PositionRequestService positionRequestService;

	@Autowired
	private UserSearchRepo userSearchRepo;

	@Autowired
	private SuggestSearchRepo suggestSearchRepo;

	@Autowired
	private ClientSearchRepo clientSearchRepo;

	@Autowired
	private ProspectSearchRepo prospectSearchRepo;

	@Autowired
	private CandidateSearchRepo candidateSearchRepo;

	@Autowired
	private PositionSearchRepo positionSearchRepo;

	@Autowired
	private PositionRequestSearchRepo positionRequestSearchRepo;

	@Autowired
	private DomainToSearchDomainService domainToSearchDomainService;

	@Autowired
	private PageableService pageableService;

	@Async
	public void runIndexForTenant(String tenant) {
		try {
			TenantContextHolder.setTenant(tenant);
			logger.info("Started reindexing ..... tenant : " + tenant);
			indexTenanDocumentsFromDB(tenant);
			logger.info("Done reindex for tenant id : " + tenant);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			TenantContextHolder.clearContext();
		}
	}

	@Transactional(readOnly = true)
	private void indexTenanDocumentsFromDB(String tenantId) {

		int pageSize = 1000;

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			Page<Client> clients = clientService.getAllClientsForSearchIndex(pageable);
			if (clients != null && clients.hasContent()) {
				clientSearchRepo.save(domainToSearchDomainService.convertClients(clients.getContent(), tenantId));
				suggestSearchRepo.save(domainToSearchDomainService.convertClientsForSuggest(clients.getContent(), tenantId));
			} else {
				break;
			}
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			Page<Prospect> prospects = prospectService.getAllProspectForSearchIndex(pageable);
			if (prospects != null && prospects.hasContent()) {
				prospectSearchRepo.save(domainToSearchDomainService.convertProspects(prospects.getContent(), tenantId));
				suggestSearchRepo.save(domainToSearchDomainService.convertProspectForSuggest(prospects.getContent(), tenantId));
			} else {
				break;
			}
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			List<Position> positions = positionService.getAllPositionForSearchIndex(pageable);
			if (positions != null && !positions.isEmpty()) {
				for (Position position : positions) {
					positionSearchRepo.save(domainToSearchDomainService.convertPosition(position, tenantId));
					suggestSearchRepo.save(domainToSearchDomainService.convertPositionForSuggest(position, tenantId));
				}
			} else {
				break;
			}
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			List<PositionRequest> positionRequests = positionRequestService.getAllPositionRequestForSearchIndex(pageable);
			if (positionRequests != null && !positionRequests.isEmpty()) {
				positionRequestSearchRepo.save(domainToSearchDomainService.convertPositionRequests(positionRequests, tenantId));
				suggestSearchRepo.save(domainToSearchDomainService.convertPositionRequestsForSuggest(positionRequests, tenantId));
			} else {
				break;
			}
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {

			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			Page<User> users = userService.getAllUsersForSearchIndex(pageable);
			if (users != null && users.hasContent()) {
				userSearchRepo.save(domainToSearchDomainService.convertUsers(users.getContent(), tenantId));
				suggestSearchRepo.save(domainToSearchDomainService.convertUsersForSuggest(users.getContent(), tenantId));
			} else {
				break;
			}
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {

			Pageable pageable = pageableService.defaultPageRequest(i, pageSize);
			List<Candidate> candidates = candidateService.getAllCandidateForSearchIndex(pageable);
			if (candidates != null && !candidates.isEmpty()) {
				candidateSearchRepo.save(domainToSearchDomainService.convertCandidates(candidates, tenantId));
				suggestSearchRepo.save(domainToSearchDomainService.convertCandidatesForSuggest(candidates, tenantId));
			} else {
				break;
			}
		}

	}

}
