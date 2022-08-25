package com.bbytes.recruiz.service;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.NotificationRepository;
import com.bbytes.recruiz.rest.dto.models.ClientCountHRMDTO;
import com.bbytes.recruiz.rest.dto.models.DashboardDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class QueryService {

	private final Logger logger = LoggerFactory.getLogger(QueryService.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private TaskItemService taskService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private InterviewScheduleService scheduleService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private ForwardProfileService forwardProfileService;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private ReportService reportService;

	protected Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	/**
	 * getting data for HR Executive Dashboard
	 *
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	public Map<String, Object> getDashboardData() throws ParseException, RecruizException {
		Map<String, Object> counts = new HashMap<String, Object>();
		counts.put("positionCount", "0");
		counts.put("clientCount", "0");
		counts.put("candidateCount", "0");
		counts.put("taskCount", "0");
		counts.put("totalOpenedPosition", "0");
		List<Long> teamIds = new ArrayList<>();
		teamIds.add(0L);
		List<Team> teams = teamService.getAllTeamsForCurrentUser();
		for (Team team : teams) {
			teamIds = getAllChildrenTeamIds(team, teamIds);
		}

		counts.put("positionCount", positionService.getPositionCountForLoggedInUserManager());
		counts.put("totalOpenedPosition", positionService.getTotalPositionCountForLoggedInUserManager());
		counts.put("clientCount", clientService.getClientCountForLoggedInUserManager());
		if (checkUserPermission.hasViewAllCandidatesPermission())
			counts.put("candidateCount", candidateService.count());
		else
			counts.put("candidateCount", candidateService.getCandidateCountForLoggedInUser());

		counts.put("taskCount", taskService.findTaskItemCountForUser(userService.getLoggedInUserObject()));
		List<DashboardDTO> scheduleDetails = scheduleService.getInterviewSchedule();
		counts.put("schedues", scheduleDetails);

		/*
		 * Session session = getCurrentSession();
		 *
		 * // total Client for logged in user along with owned by and working on
		 * Set<Long> clientForLoggedinUser = new HashSet<>();
		 *
		 * // setting criteria to get clients which is owned by HR executive
		 * DetachedCriteria onlyClientQuery = DetachedCriteria.forClass(Client.class,
		 * "client") .add(Restrictions.eq("client.owner",
		 * userService.getLoggedInUserEmail()));
		 *
		 * @SuppressWarnings("unchecked") List<Client> onlyClientCount =
		 * onlyClientQuery.getExecutableCriteria(session).list(); if (onlyClientCount !=
		 * null && !onlyClientCount.isEmpty()) { for (Client client : onlyClientCount) {
		 * clientForLoggedinUser.add(client.getId()); } }
		 *
		 * counts.put("clientCount", clientForLoggedinUser.size());
		 *
		 * // Setting criteria for Position // it will return the positions owned by a
		 * HR and on which one is // working Set<Position> positionForLoggedinUser = new
		 * HashSet<>(); List<Long> userId = new ArrayList<>();
		 * userId.add(userService.getLoggedInUserObject().getUserId()); Long[] userIds =
		 * userId.toArray(new Long[userId.size()]); DetachedCriteria positionQuery =
		 * DetachedCriteria.forClass(Position.class, "position")
		 * .createAlias("position.hrExecutives", "user").add(Restrictions.in("user.id",
		 * userIds));
		 *
		 * @SuppressWarnings("unchecked") List<Position> positionList =
		 * positionQuery.getExecutableCriteria(session).list(); if (positionList != null
		 * && !positionList.isEmpty()) { for (Position position : positionList) {
		 * positionForLoggedinUser.add(position); } }
		 *
		 * DetachedCriteria positionQueryByOwner =
		 * DetachedCriteria.forClass(Position.class, "position")
		 * .add(Restrictions.eq("position.owner", userService.getLoggedInUserEmail()));
		 *
		 * @SuppressWarnings("unchecked") List<Position> positionListByOwner =
		 * positionQueryByOwner.getExecutableCriteria(session).list(); if
		 * (positionListByOwner != null && !positionListByOwner.isEmpty()) { for
		 * (Position position : positionListByOwner) {
		 * positionForLoggedinUser.add(position); } }
		 *
		 * // gettiing Team positions List<Position> teamPositions =
		 * positionService.getPositionByHrExecutive(); if (teamPositions != null &&
		 * !teamPositions.isEmpty()) { for (Position position : teamPositions) {
		 * positionForLoggedinUser.add(position); } }
		 *
		 * List<String> positonCodes = new ArrayList<String>(); Set<Long> pids = new
		 * HashSet<>(); List<Long> positionIds = new ArrayList<Long>(); if
		 * (positionForLoggedinUser != null && !positionForLoggedinUser.isEmpty()) { for
		 * (Position position : positionForLoggedinUser) {
		 * positonCodes.add(position.getPositionCode()); pids.add(position.getId()); }
		 *
		 * positionIds.addAll(pids);
		 *
		 * // Setting criteria for client count // this will return all client which is
		 * owned by HR and on who's // position hr is working Long[] ids =
		 * positionIds.toArray(new Long[positionIds.size()]); DetachedCriteria
		 * clientQuery = DetachedCriteria.forClass(Client.class, "client")
		 * .createAlias("client.positions", "position") .setProjection(
		 * Projections.distinct(Projections.projectionList().add(Projections.
		 * property("id"), "id"))) .add(Restrictions.or(Restrictions.eq("client.owner",
		 * userService.getLoggedInUserEmail()), Restrictions.in("position.id", ids)));
		 *
		 * @SuppressWarnings("unchecked") List<Long> clientCount =
		 * clientQuery.getExecutableCriteria(session).list();
		 *
		 * if (clientCount != null && !clientCount.isEmpty()) { for (Long clientId :
		 * clientCount) { clientForLoggedinUser.add(clientId); } }
		 *
		 * List<Long> teamIds = new ArrayList<>(); List<Team> teams =
		 * teamService.getAllTeamsForCurrentUser(); if (teams != null &&
		 * !teams.isEmpty()) { for (Team team : teams) { teamIds.add(team.getId()); } }
		 * else { teamIds.add(0L); }
		 *
		 * counts.put("positionCount",
		 * positionService.getHrPositionCount(userService.getLoggedInUserEmail() ,
		 * teamIds, userService.getLoggedInUserObject().getUserId()) + "");
		 *
		 * if (null != positionIds && !positionIds.isEmpty()) {
		 * counts.put("totalOpenedPosition",
		 * positionService.getTotalPositionCountForHR(positionIds)); }
		 *
		 * if (clientForLoggedinUser.size() > 0) counts.put("clientCount",
		 * clientForLoggedinUser.size() + "");
		 *
		 * List<DashboardDTO> scheduleDetails =
		 * getInterviewScheduleForLoggedInUser(session); counts.put("schedues",
		 * scheduleDetails);
		 *
		 * }
		 *
		 * // checking user has view all candidate permission int candidateCount = 0; if
		 * (permissionService.hasViewAllCandidatesPermission()) { candidateCount =
		 * candidateService.getActiveCandidateCount(); } else candidateCount = (int)
		 * candidateService.candidateCountByOwner(userService. getLoggedInUserEmail());
		 *
		 * if (candidateCount > 0) counts.put("candidateCount", candidateCount + "");
		 *
		 * Long count = taskService.findTaskItemCountForUser(userService.
		 * getLoggedInUserObject()); if (null == count) { count = 0L; }
		 * counts.put("taskCount", count);
		 */

		return counts;
	}

	/**
	 * get interview schedule for logged in user along with candidate name, total
	 * schedule count per client (interviewScheduleCount/client)
	 *
	 * @param session
	 * @return
	 * @throws RecruizException
	 */
	private List<DashboardDTO> getInterviewScheduleForLoggedInUser() throws RecruizException {
		List<DashboardDTO> scheduleDetails = scheduleService.getInterviewSchedule();
		return scheduleDetails;
	}

	/**
	 * getting dashboard client position counts (Opened/Closed/OnHold/Near Closure /
	 * Post Closure)
	 *
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@SuppressWarnings("null")
	@Transactional(readOnly = true)
	public List<ClientCountHRMDTO> getDashboardDataCount() throws ParseException, RecruizException {

		List<ClientCountHRMDTO> clientCountDTOList = new ArrayList<ClientCountHRMDTO>();

		Set<Long> cliendIds = new HashSet<>();

		List<Client> clients = clientService.getClientsByOwnerAndPositionHRs();
		if (clients != null || !clients.isEmpty()) {
			for (Client client : clients) {
				cliendIds.add(client.getId());
			}
		}

		if (cliendIds != null) {
			for (Long id : cliendIds) {
				Client client = clientService.findOne(id);
				ClientCountHRMDTO clientCountDTO = new ClientCountHRMDTO();
				clientCountDTO.setClientName(client.getClientName());
				clientCountDTO.setLocation(client.getClientLocation());
				clientCountDTO.setClosedPositionCount(
						positionService.getStatusCountByHRClient(id, Status.Closed.getDisplayName()));
				clientCountDTO.setOpenedPositionCount(
						positionService.getStatusCountByHRClient(id, Status.Active.getDisplayName()));
				clientCountDTO
						.setOnHoldPositionCount(positionService.getStatusCountByHRClient(id, Status.OnHold.toString()));
				clientCountDTO.setPostClosedDatePositionCount(positionService.getPostClosurePosition(id));
				clientCountDTO.setNearClosurePositionCount(positionService.getNearClosurePosition(id));
				clientCountDTOList.add(clientCountDTO);
			}
		}
		return clientCountDTOList;
	}

	/**
	 * getting data for HR along with client name he/she is working on
	 *
	 * @return
	 * @throws RecruizException
	 */
	public List<Object> hrData() throws RecruizException {
		List<Object> hrData = new ArrayList<>();
		List<BigInteger> hrList = new ArrayList<>();
		hrList = positionService.getAllPositionHrListForHrManager();

		if (hrList != null && !hrList.isEmpty()) {
			List<Long> clients = clientService.getClientIdsForHrExecutive(userService.getLoggedInUserObject());
			if (clients != null && !clients.isEmpty()) {
				for (BigInteger hrId : hrList) {
					Map<String, Object> hrDetails = new HashMap<String, Object>();
					Set<User> userSet = new HashSet<>();
					User user = userService.findOne(hrId.longValue());
					if (!user.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
						userSet.add(user);
						List<Long> clientIdForHr = clientService.getClientIdsForHrExecutive(user);
						List<Long> hrClientIds = new ArrayList<>();
						if (clientIdForHr != null && !clientIdForHr.isEmpty()) {
							for (Long hrClientId : clientIdForHr) {
								if (clients.contains(hrClientId)) {
									hrClientIds.add(hrClientId);
								}
							}
							if (hrClientIds != null && !hrClientIds.isEmpty()) {
								List<String> clientNames = clientService.getClientNameByIds(hrClientIds);
								hrDetails.put(user.getName(), clientNames);
								hrData.add(hrDetails);
							} else {
								hrDetails.put(user.getName(), "No Clients");
								hrData.add(hrDetails);
							}

						}
					}
				}
			}
		}
		return hrData;
	}

	/**
	 * get hrData For super admin
	 *
	 * @return
	 */
	public List<Object> hrDataForSuperAdmin() {
		List<Object> hrData = new ArrayList<>();
		List<BigInteger> hrList = new ArrayList<>();
		hrList = positionService.getAllPositionHrList();

		if (hrList != null && !hrList.isEmpty()) {
			List<Long> clients = clientService.getClientIds();
			if (clients != null && !clients.isEmpty()) {
				for (BigInteger hrId : hrList) {
					Map<String, Object> hrDetails = new HashMap<String, Object>();
					Set<User> userSet = new HashSet<>();
					User user = userService.findOne(hrId.longValue());
					if (!user.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
						userSet.add(user);
						List<Long> clientIdForHr = clientService.getClientIdsForHrExecutive(user);
						List<Long> hrClientIds = new ArrayList<>();
						if (clientIdForHr != null && !clientIdForHr.isEmpty()) {
							for (Long hrClientId : clientIdForHr) {
								if (clients.contains(hrClientId)) {
									hrClientIds.add(hrClientId);
								}
							}
							if (hrClientIds != null && !hrClientIds.isEmpty()) {
								List<String> clientNames = clientService.getClientNameByIds(hrClientIds);
								hrDetails.put(user.getName(), clientNames);
								hrData.add(hrDetails);
							} else {
								hrDetails.put(user.getName(), "No Clients");
								hrData.add(hrDetails);
							}

						}
					}
				}
			}
		}
		return hrData;
	}

	/**
	 * getting complete map along with Client position count, Hr details with
	 * working on client, interview scheduled for HR Manager
	 *
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	public Map<String, Object> getDashboardDataForHRM() throws ParseException, RecruizException {
		Map<String, Object> counts = new HashMap<String, Object>();
		List<ClientCountHRMDTO> countData = getDashboardDataCount();
		List<Object> hrData = hrData();

		List<DashboardDTO> scheduleDetails = getInterviewScheduleForLoggedInUser();
		counts.put("clients", countData);
		counts.put("hrData", hrData);
		counts.put("scheduleDetails", scheduleDetails);
		return counts;
	}

	public Map<String, Object> getDashboardDataForHRExec() throws ParseException, RecruizException {
		Map<String, Object> counts = new HashMap<String, Object>();
		List<DashboardDTO> scheduleDetails = getInterviewScheduleForLoggedInUser();
		counts.put("scheduleDetails", scheduleDetails);
		return counts;
	}

	/**
	 * Get the user count , client count , candidate count and position count
	 *
	 * @return
	 */
	public String getOrgStatsCount() {
		String userCount = userService.count() + "";
		String clientCount = clientService.count() + "";
		String candidateCount = candidateService.count() + "";
		String positionCount = positionService.count() + "";

		String countString = "Organization Name : " + organizationService.getCurrentOrganization().getOrgName()
				+ "\nTotal User Count : " + userCount + " , Total Client Count : " + clientCount
				+ " , Total Position Count : " + positionCount + " , Total Candidate Count : " + candidateCount;

		return countString;
	}

	public List<String> getOrgUserEmailList() {
		return userService.findAllEmails();
	}

	/**
	 * Get entity count map for plutus stat
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getOrgStatForPlutus() {

		long clientCount = clientService.count();
		long positionCount = positionService.count();
		long candidateCount = candidateService.count();
		long taskCount = taskService.count();
		long interviewScheduleCount = scheduleService.count();
		long feedbackRecievedCount = feedbackService.getGivenFeedbackCount();
		long forwardedProfileCount = forwardProfileService.count();

		Map<String, Object> plutusStatMap = new HashMap<>();
		plutusStatMap.put("clientCount", clientCount);
		plutusStatMap.put("positionCount", positionCount);
		plutusStatMap.put("candidateCount", candidateCount);
		plutusStatMap.put("taskCount", taskCount);
		plutusStatMap.put("interviewScheduleCount", interviewScheduleCount);
		plutusStatMap.put("feedbackRecievedCount", feedbackRecievedCount);
		plutusStatMap.put("forwardedProfileCount", forwardedProfileCount);
		plutusStatMap.put("users", userService.findAll());
		plutusStatMap.put("bulkEmailUsedCount",
				organizationService.getCurrentOrganization().getOrganizationConfiguration().getBulkEmailUsed());
		return plutusStatMap;
	}

	/**
	 * to get notification count of different object
	 *
	 * @return
	 */
	public Map<String, Long> getNotificationCount() {

		Map<String, Long> countMap = new HashMap<>();
		countMap.put("clientCount", notificationRepository.countByViewStateAndUserAndClientIdGreaterThan(false,
				userService.getLoggedInUserEmail(), 0));
		countMap.put("positionCount", notificationRepository.countByViewStateAndUserAndPositionCodeIsNotNull(false,
				userService.getLoggedInUserEmail()));
		countMap.put("candidateCount", notificationRepository.countByViewStateAndUserAndCandidateIdGreaterThan(false,
				userService.getLoggedInUserEmail(), 0));

		countMap.put("roundcandidateCount", notificationRepository
				.countByViewStateAndUserAndRoundCandidateIdGreaterThan(false, userService.getLoggedInUserEmail(), 0));
		countMap.put("roundCount", notificationRepository.countByViewStateAndUserAndRoundIdGreaterThan(false,
				userService.getLoggedInUserEmail(), 0));
		countMap.put("interviewScheduleCount",
				notificationRepository.countByViewStateAndUserAndInterviewScheduleIdGreaterThan(false,
						userService.getLoggedInUserEmail(), 0));
		countMap.put("requestedPositionCount",
				notificationRepository.countByViewStateAndUserAndRequestedPositionIdGreaterThan(false,
						userService.getLoggedInUserEmail(), 0));

		return countMap;
	}

	/**
	 * to get notification list for given client list
	 *
	 * @param clientIds
	 * @return
	 */
	public Map<Long, List<Notification>> getNotificationCountMapForClient(List<Long> clientIds) {
		Map<Long, List<Notification>> countMap = new HashMap<>();
		if (clientIds != null && !clientIds.isEmpty()) {
			for (Long clientId : clientIds) {
				List<Notification> clientNotification = notificationRepository
						.findByUserAndClientIdAndViewStateFalse(userService.getLoggedInUserEmail(), clientId);
				countMap.put(clientId, clientNotification);
			}
		}
		return countMap;
	}

	/**
	 * to get list of notification for given candidate
	 *
	 * @param candidateIds
	 * @return
	 */
	public Map<Long, List<Notification>> getNotificationCountMapForCandidate(List<Long> candidateIds) {
		Map<Long, List<Notification>> countMap = new HashMap<>();
		if (candidateIds != null && !candidateIds.isEmpty()) {
			for (Long candidateId : candidateIds) {
				List<Notification> candidateNotification = notificationRepository
						.findByViewStateAndUserAndCandidateId(false, userService.getLoggedInUserEmail(), candidateId);
				countMap.put(candidateId, candidateNotification);
			}
		}
		return countMap;
	}

	/**
	 * to get list of notification for given round
	 *
	 * @param roundIds
	 * @return
	 */
	public Map<Long, List<Notification>> getNotificationCountMapForRound(List<Long> roundIds) {
		Map<Long, List<Notification>> countMap = new HashMap<>();
		if (roundIds != null && !roundIds.isEmpty()) {
			for (Long roundId : roundIds) {
				List<Notification> roundNotification = notificationRepository
						.findByViewStateAndUserAndCandidateId(false, userService.getLoggedInUserEmail(), roundId);
				countMap.put(roundId, roundNotification);
			}
		}
		return countMap;
	}

	/**
	 * to get list of notification for given round candidate (candidate card in
	 * board)
	 *
	 * @param roundCandidateIds
	 * @return
	 */
	public Map<Long, List<Notification>> getNotificationCountMapForRoundCandidate(List<Long> roundCandidateIds) {
		Map<Long, List<Notification>> countMap = new HashMap<>();
		if (roundCandidateIds != null && !roundCandidateIds.isEmpty()) {
			for (Long roundCandidateId : roundCandidateIds) {
				List<Notification> roundCandidateNotification = notificationRepository
						.findByViewStateAndUserAndCandidateId(false, userService.getLoggedInUserEmail(),
								roundCandidateId);
				countMap.put(roundCandidateId, roundCandidateNotification);
			}
		}
		return countMap;
	}

	/**
	 * to get list of notification for given position
	 *
	 * @param positionCodes
	 * @return
	 */
	public Map<String, List<Notification>> getNotificationCountMapForPosittion(List<String> positionCodes) {
		Map<String, List<Notification>> countMap = new HashMap<>();
		if (positionCodes != null && !positionCodes.isEmpty()) {
			for (String positionCode : positionCodes) {
				List<Notification> roundCandidateNotification = notificationRepository
						.findByViewStateAndUserAndPositionCode(false, userService.getLoggedInUserEmail(), positionCode);
				countMap.put(positionCode, roundCandidateNotification);
			}
		}
		return countMap;
	}

	// this will return total count of client/position/candidate and all
	// interview schedule of the day
	public Map<String, Object> getDashboardDataForSuperAdmin() throws ParseException, RecruizException {
		Map<String, Object> counts = new HashMap<String, Object>();
		//List<ClientCountHRMDTO> countData = getDashboardDataCountForSuperAdmin();
		//List<Object> hrData = hrDataForSuperAdmin();

		List<DashboardDTO> scheduleDetails = scheduleService.getInterviewScheduleForSuperAdmin();
		//counts.put("clients", countData);
		//counts.put("hrData", hrData);
		counts.put("scheduleDetails", scheduleDetails);
		return counts;
	}

	@SuppressWarnings("null")
	@Transactional(readOnly = true)
	public List<ClientCountHRMDTO> getDashboardDataCountForSuperAdmin() throws ParseException {
		List<ClientCountHRMDTO> clientCountDTOList = new ArrayList<ClientCountHRMDTO>();
		Set<Long> cliendIds = new HashSet<>(clientService.getClientIds());

		if (cliendIds != null) {
			for (Long id : cliendIds) {
				Client client = clientService.findOne(id);
				ClientCountHRMDTO clientCountDTO = new ClientCountHRMDTO();
				clientCountDTO.setClientName(client.getClientName());
				clientCountDTO.setLocation(client.getClientLocation());
				clientCountDTO.setClosedPositionCount(
						positionService.getStatusCountByClient(id, Status.Closed.getDisplayName()));
				clientCountDTO.setOpenedPositionCount(
						positionService.getStatusCountByClient(id, Status.Active.getDisplayName()));
				clientCountDTO
						.setOnHoldPositionCount(positionService.getStatusCountByClient(id, Status.OnHold.toString()));
				clientCountDTO.setPostClosedDatePositionCount(positionService.getPostClosurePositionForSuperAdmin(id));
				clientCountDTO.setNearClosurePositionCount(positionService.getNearClosurePositionForSuperAdmin(id));
				clientCountDTOList.add(clientCountDTO);
			}
		}
		return clientCountDTOList;
	}

	public Map<String, Object> getRecruiterProfileCount(long clientId, User hrExecutive, Date startDate, Date endDate,
			String status) {

		Map<String, Object> profileMap = new HashMap<>();

		Map<String, Object> clientCountMap = new HashMap<>();
		Client client = null;
		int count = 0;
		if (clientId == 0) {
			List<Long> hrClients = clientService.getClientIdsForHrExecutive(hrExecutive);
			for (Long cid : hrClients) {
				client = clientService.findOne(cid);
				count = getClientPositionStatMapForRecruiter(hrExecutive, startDate, endDate, clientCountMap, client,
						count, status);
			}
		} else {
			client = clientService.findOne(clientId);
			count = getClientPositionStatMapForRecruiter(hrExecutive, startDate, endDate, clientCountMap, client, count,
					status);
		}
		profileMap.put("gridData", clientCountMap);
		profileMap.put("positionCount", count);
		return profileMap;
	}
	
	//@author - Sajin (added to show custom statuses in Recruiter performance table)
	public Map<String, Object> getRecruiterProfileCountCustom(long clientId, User hrExecutive, Date startDate, Date endDate,
			String status) {

		Map<String, Object> profileMap = new HashMap<>();

		Map<String, Object> clientCountMap = new HashMap<>();
		Client client = null;
		int count = 0;
		if (clientId == 0) {
			List<Long> hrClients = clientService.getClientIdsForHrExecutive(hrExecutive);
			for (Long cid : hrClients) {
				client = clientService.findOne(cid);
				count = getClientPositionStatMapForRecruiterCustom(hrExecutive, startDate, endDate, clientCountMap, client,
						count, status);
			}
		} else {
			client = clientService.findOne(clientId);
			count = getClientPositionStatMapForRecruiterCustom(hrExecutive, startDate, endDate, clientCountMap, client, count,
					status);
		}
		profileMap.put("gridData", clientCountMap);
		profileMap.put("positionCount", count);
		return profileMap;
	}

	public int getClientPositionStatMapForRecruiter(User hrExecutive, Date startDate, Date endDate,
			Map<String, Object> clientCountMap, Client client, int count, String status) {

		Set<User> positionHrs = new HashSet<>();
		positionHrs.add(hrExecutive);

		List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, positionHrs, status);

		if (hrPositions != null && !hrPositions.isEmpty()) {
			Map<String, Object> responseMap = new HashMap<>();
			for (Position position : hrPositions) {
				Object countResponse = null;
				try {
					countResponse = reportService.getCandidateCountStatusWiseRecruiterProgressReport(startDate, endDate,
							hrExecutive.getEmail(), position.getPositionCode());
				} catch (RecruizException e) {
					logger.error(e.getMessage(), e);
				}
				responseMap.put(position.getTitle(), countResponse);
				count++;
			}
			clientCountMap.put(client.getClientName(), responseMap);
		}
		return count;
	}
	
	//@author - Sajin (added to show custom statuses in Recruiter performance table)
	public int getClientPositionStatMapForRecruiterCustom(User hrExecutive, Date startDate, Date endDate,
			Map<String, Object> clientCountMap, Client client, int count, String status) {

		Set<User> positionHrs = new HashSet<>();
		positionHrs.add(hrExecutive);

		List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, positionHrs, status);

		if (hrPositions != null && !hrPositions.isEmpty()) {
			Map<String, Object> responseMap = new HashMap<>();
			for (Position position : hrPositions) {
				Object countResponse = null;
				try {
					countResponse = reportService.getCandidateCountStatusWiseCustom(startDate, endDate,
							hrExecutive.getEmail(), position.getPositionCode());
				} catch (RecruizException e) {
					logger.error(e.getMessage(), e);
				}
				responseMap.put(position.getTitle(), countResponse);
				count++;
			}
			clientCountMap.put(client.getClientName(), responseMap);
		}
		return count;
	}

	/**
	 * To get different count like total sourced info, position closed count,
	 * Interview Schedule Count, Profile forwarded info etc
	 *
	 * @param hrExecutive
	 * @return
	 */
	public Map<String, Object> getRecruiterEntiryStat(User hrExecutive, String clientName, String clientId,
			Date startDate, Date endDate) {
		Map<String, Object> clientCountMap = new HashMap<>();
		try {
			String positionCodes = "";
			if (!clientId.equalsIgnoreCase("0")) {
				Set<User> hrExecutives = new HashSet<>();
				hrExecutives.add(hrExecutive);
				Client client = clientService.findOne(Long.parseLong(clientId));
				if (clientName == null || clientName.trim().isEmpty()) {
					clientName = client.getClientName();
				}
				List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, hrExecutives, null);
				if (null != hrPositions && !hrPositions.isEmpty()) {
					for (Position position : hrPositions) {
						if (positionCodes.isEmpty()) {
							positionCodes = "'" + position.getPositionCode() + "'";
						} else {
							positionCodes = positionCodes + "," + "'" + position.getPositionCode() + "'";
						}
					}
				}
			}
			clientCountMap = reportService.getStatForRecruitmentHr(hrExecutive.getEmail(), hrExecutive.getUserId() + "",
					clientName, clientId, startDate, endDate, positionCodes);
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return clientCountMap;
	}

	public List<Long> getPerformaceReportCandidate(User hrExecutive, String clientName, String clientId, String status,
			Date startDate, Date endDate) {
		List<String> posCodes = new ArrayList<String>();
		if (!clientId.equalsIgnoreCase("0")) {
			Set<User> hrExecutives = new HashSet<>();
			hrExecutives.add(hrExecutive);
			Client client = clientService.findOne(Long.parseLong(clientId));
			List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, hrExecutives, null);
			if (null != hrPositions && !hrPositions.isEmpty()) {
				for (Position position : hrPositions) {
					posCodes.add(position.getPositionCode());
				}
			}
		}
		List<String> statusList = new ArrayList<String>();
		List<Long> candidateIds = new ArrayList<Long>();
		if (status.equals(GlobalConstants.SOURCED)) {
			List<Long> candidateIdsFromDB = candidateService
					.findCandidateIdsBySourcebyBetweenDate(hrExecutive.getEmail(), startDate, endDate);
			if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
				for (Object obj : candidateIdsFromDB) {
					candidateIds.add(((BigInteger) obj).longValue());
				}
			}
		} else if (status.equals(GlobalConstants.SOURCED_TO_BOARD)) {
			candidateIds = roundCandidateService.getCandidateIds(posCodes, hrExecutive.getEmail(), statusList,
					startDate, endDate);
		} else {
			// offer status means all together (Joined, Offered, Offer Accepted
			// and
			// Offer Rejected)
			if (status.equals(BoardStatus.Offered.toString())) {
				String[] statuses = { BoardStatus.Offered.toString(), BoardStatus.OfferAccepted.toString(),
						BoardStatus.OfferDeclined.toString(), BoardStatus.Joined.toString() };
				statusList.addAll(Arrays.asList(statuses));
			} else {
				statusList.add(status);
			}
			candidateIds = roundCandidateService.getCandidateIds(posCodes, hrExecutive.getEmail(), statusList,
					startDate, endDate);
		}
		return candidateIds;
	}

	public Map<String, Object> getPositionStatMapForTeam(List<Long> teamIds, Date startDate, Date endDate) {
		List<Position> teamPositions = positionService.getAllPositionByTeamsInAndModifiationDateBetween(teamIds,
				startDate, endDate);

		Map<String, Object> responseMap = new LinkedHashMap<>();

		if (teamPositions != null && !teamPositions.isEmpty()) {
			for (Position position : teamPositions) {
				Map<String, Object> countResponse = null;
				try {
					countResponse = reportService.getCandidateCountStatusWiseForTeam(startDate, endDate,
							position.getPositionCode());
					countResponse.put("client", position.getClient().getClientName());
				} catch (RecruizException e) {
					logger.error(e.getMessage(), e);
				}
				responseMap.put(position.getTitle(), countResponse);
			}
		}
		return responseMap;
	}

	public Map<String, Object> getTeamEntityStat(List<Long> teamIds, List<String> teamMemberEmails, Date startDate,
			Date endDate) {
		Map<String, Object> clientCountMap = new HashMap<>();
		try {
			String positionCodes = "";
			List<Position> teamPosition = positionService.getAllPositionByTeamsInAndModifiationDateBetween(teamIds,
					startDate, endDate);
			if (null != teamPosition && !teamPosition.isEmpty()) {
				for (Position position : teamPosition) {
					if (positionCodes.isEmpty()) {
						positionCodes = "'" + position.getPositionCode() + "'";
					} else {
						positionCodes = positionCodes + "," + "'" + position.getPositionCode() + "'";
					}
				}
			}
			clientCountMap = reportService.getStatForTeam(teamIds, StringUtils.commaSeparate(teamMemberEmails),
					startDate, endDate, positionCodes);
			clientCountMap.put("positionCount", teamPosition.size());
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}
		return clientCountMap;
	}

	private List<Long> getAllChildrenTeamIds(Team team, List<Long> teamIds) {
		if (team.getChildren() != null && !team.getChildren().isEmpty()) {
			for (Team subTeam : team.getChildren()) {
				getAllChildrenTeamIds(subTeam, teamIds);
			}
		} else {
			teamIds.add(team.getId());
		}
		return teamIds;
	}

}
