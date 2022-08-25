package com.bbytes.recruiz.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.InterviewerTimeSlot;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectPosition;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.enums.ProspectActivityType;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.ClientDecisionMakerRepository;
import com.bbytes.recruiz.repository.ClientInterviewPanelRepository;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewPanelDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.ActivityMessageConstants;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class ClientService extends AbstractService<Client, Long> {

	private ClientRepository clientRepository;

	@Autowired
	private PositionService positionService;

	@Autowired
	private DecisionMakerService decisionMakerService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private ClientDecisionMakerRepository decisionMakerRepository;

	@Autowired
	private ClientInterviewPanelRepository interviewerRepository;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private FileService fileService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private GenericDecisionMakerService genericDecisionMakerService;

	@Autowired
	private GenericInterviewerService genericInterviewerService;

	@Autowired
	private ClientActivityService clientActivityService;

	@Autowired
	private ProspectService prospectService;

	@Autowired
	private ProspectPositionService prospectPositionService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private TeamService teamService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	public ClientService(ClientRepository clientRepository) {
		super(clientRepository);
		this.clientRepository = clientRepository;
	}

	@Transactional(readOnly = true)
	public boolean clientExist(String clientName) {
		boolean state = clientRepository.findOneByClientName(clientName) == null ? false : true;
		return state;
	}

	@Transactional
	public boolean clientExist(long clientId) {
		boolean state = clientRepository.findOne(clientId) == null ? false : true;
		return state;
	}

	/**
	 * <code> addClient </code> method is used to add client along with decision
	 * makers and interview panel.
	 *
	 * @param clientDTO
	 * @throws RecruizException
	 */
	@Transactional
	public Client addClient(ClientDTO clientDTO) throws RecruizException {

		Client client = addClientToDB(clientDTO);

		// adding client creation activity
		ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.CLIENT_ADDED.getDisplayName(),
				ActivityMessageConstants.getCreatedByMsg(userService.getLoggedInUserObject()), new Date(), client.getId());
		clientActivityService.addActivity(activity);

		return client;
	}

	@Transactional
	public Client addClientToDB(ClientDTO clientDTO) throws RecruizWarnException {
		if (clientExist(clientDTO.getClientName()))
			throw new RecruizWarnException(ErrorHandler.CLIENT_EXISTS, ErrorHandler.DUPLICATE_CLIENT);

		Client client = new Client();

		Set<ClientDecisionMaker> decisionMakerList = clientDTO.getClientDecisionMaker();
		// need not to be checked during add time so commenting the code
		/*
		 * for (ClientDecisionMaker decisionMaker : decisionMakerList) { if
		 * (decisionMakerService.decisionMakerExists(decisionMaker.getEmail()))
		 * throw new RecruizException(ErrorHandler.DECISION_MAKER_EXIST +
		 * decisionMaker.getEmail() + " exists", ErrorHandler.EMAIL_EXISTS); }
		 */

		Set<ClientInterviewerPanel> clientInterviewerPanelList = clientDTO.getClientInterviewerPanel();
		// need not to be checked during add time so commenting the code
		/*
		 * for (ClientInterviewerPanel clientInterviewerPanel :
		 * clientInterviewerPanelList) { if
		 * (interviewPanelService.interviewerExists(clientInterviewerPanel.
		 * getEmail())) throw new RecruizException(
		 * ErrorHandler.INTERVIEW_PANEL_EXIST +
		 * clientInterviewerPanel.getEmail() + " exists",
		 * ErrorHandler.EMAIL_EXISTS); }
		 */
		if (clientInterviewerPanelList != null && !clientInterviewerPanelList.isEmpty()) {
			for (ClientInterviewerPanel clientInterviewerPanel : clientInterviewerPanelList) {
				clientInterviewerPanel.addInterviewerTimeSlot(clientInterviewerPanel.getInterviewerTimeSlots());
			}
		}

		client.setClientName(clientDTO.getClientName());
		client.setAddress(clientDTO.getAddress());
		client.setWebsite(clientDTO.getWebsite());
		client.setEmpSize(clientDTO.getEmpSize());
		client.setStatus(Status.Active.toString());
		client.setClientLocation(clientDTO.getClientLocation());
		client.setTurnOvr(clientDTO.getTurnOvr());
		client.setNotes(clientDTO.getNotes());
		client.addClientDecisionMaker(decisionMakerList);
		client.addClientInterviewerPanel(clientInterviewerPanelList);
		client.setOwner(userService.getLoggedInUserEmail());
		client.setDummy(clientDTO.isDummy());
		client.setCustomeField(clientDTO.getCustomField());

		client = clientRepository.save(client);

		// if these decision makers are not part of generic list then adding it
		genericDecisionMakerService.addDMsFromClient(decisionMakerList);

		// if interviewer are not part of generic list then adding it
		genericInterviewerService.addGenericInterviewerFromClientOrPosition(clientInterviewerPanelList);

		return client;
	}

	/**
	 * <code> updateClient </code> method is used to update client along with
	 * decision makers and interview panel.
	 *
	 * @param clientId
	 * @param clientDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Client updateClient(String clientId, ClientDTO clientDTO) throws RecruizException {

		if (!clientExist(Long.valueOf(clientId)))
			throw new RecruizWarnException(ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.INVALID_CLIENT);
		Client clientFromDB = clientRepository.findOne(Long.valueOf(clientId));

		Prospect prospect = prospectService.getProspectByClient(clientFromDB);
		if (prospect != null && prospect.getStatus().equals(ProspectActivityType.Converted.getDisplayName())) {
			for (ProspectPosition prospectPosition : prospectPositionService.getByClientName(clientFromDB.getClientName())) {
				prospectPosition.setClientName(clientDTO.getClientName());
				prospectPositionService.save(prospectPosition);
			}
			prospect.setClient(clientFromDB);
			prospect.setCompanyName(clientDTO.getClientName());
			prospectService.save(prospect);
		}

		boolean updatable = false;
		if (clientFromDB.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkUserPermission.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkUserPermission.hasAddEditClientPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		clientFromDB.setClientName(clientDTO.getClientName());
		clientFromDB.setAddress(clientDTO.getAddress());
		clientFromDB.setClientLocation(clientDTO.getClientLocation());
		clientFromDB.setStatus(Status.Active.toString());
		clientFromDB.setWebsite(clientDTO.getWebsite());
		clientFromDB.setEmpSize(clientDTO.getEmpSize());
		clientFromDB.setTurnOvr(clientDTO.getTurnOvr());
		clientFromDB.setNotes(clientDTO.getNotes());
		clientFromDB.setCustomeField(clientDTO.getCustomField());

		/**
		 * if decision maker already exists then fetch that from db and update
		 * the details if decision maker does not exist then add new record to
		 * db
		 */

		Set<ClientDecisionMaker> decisionMakerListFromDB = new HashSet<ClientDecisionMaker>();
		ClientDecisionMaker decisionMaker;
		for (ClientDecisionMaker decisionMakerFromUI : clientDTO.getClientDecisionMaker()) {
			if (decisionMakerFromUI.getId() != null && !decisionMakerFromUI.getId().equals("")
					&& decisionMakerService.decisionMakerExists(decisionMakerFromUI.getId())) {
				decisionMaker = decisionMakerRepository.findOne(decisionMakerFromUI.getId());
				decisionMaker.setName(decisionMakerFromUI.getName());
				decisionMaker.setEmail(decisionMakerFromUI.getEmail());
				decisionMaker.setMobile(decisionMakerFromUI.getMobile());
				decisionMakerListFromDB.add(decisionMaker);
			} else {
				decisionMakerListFromDB.add(decisionMakerFromUI);
			}
		}
		clientFromDB.getClientDecisionMaker().clear();
		clientFromDB.addClientDecisionMaker(decisionMakerListFromDB);

		/**
		 * getting interviewer and removing them from position if they are
		 * removed from ui
		 */

		Set<ClientInterviewerPanel> existingInterviewer = clientFromDB.getClientInterviewerPanel();
		if (existingInterviewer != null) {
			for (ClientInterviewerPanel clientInterviewerPanel : existingInterviewer) {
				boolean exists = false;
				for (ClientInterviewerPanel clientInterviewerPanelFromUI : clientDTO.getClientInterviewerPanel()) {
					if (clientInterviewerPanelFromUI.getEmail().equalsIgnoreCase(clientInterviewerPanel.getEmail())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					List<Position> positions = positionService.getPositionListByInterviewerAndClient(clientInterviewerPanel, clientFromDB);
					if (positions != null && !positions.isEmpty()) {
						for (Position position : positions) {
							position.getInterviewers().remove(clientInterviewerPanel);
						}
					}
				}
			}
		}

		/**
		 * if interviewer already exists then fetch that from db and update the
		 * details if interviewer does not exist then add new record to db
		 */

		Set<ClientInterviewerPanel> interviewerPanelListFromDB = new LinkedHashSet<ClientInterviewerPanel>();

		ClientInterviewerPanel interviewerFromDB;

		Set<InterviewerTimeSlot> timeSlotOfExistingInterviewerList;

		for (ClientInterviewerPanel clientInterviewerPanelFromUI : clientDTO.getClientInterviewerPanel()) {
			if (clientInterviewerPanelFromUI.getId() != null && !clientInterviewerPanelFromUI.equals("")
					&& interviewPanelService.interviewerExists(clientInterviewerPanelFromUI.getId())) {
				interviewerFromDB = interviewerRepository.findOne(clientInterviewerPanelFromUI.getId());
				interviewerFromDB.setEmail(clientInterviewerPanelFromUI.getEmail());
				interviewerFromDB.setMobile(clientInterviewerPanelFromUI.getMobile());
				interviewerFromDB.setName(clientInterviewerPanelFromUI.getName());
				interviewerFromDB.getInterviewerTimeSlots().clear();

				timeSlotOfExistingInterviewerList = clientInterviewerPanelFromUI.getInterviewerTimeSlots();
				interviewerFromDB.addInterviewerTimeSlot(timeSlotOfExistingInterviewerList);
				interviewerPanelListFromDB.add(interviewerFromDB);
			} else {
				interviewerFromDB = clientInterviewerPanelFromUI;
				interviewerFromDB.setEmail(clientInterviewerPanelFromUI.getEmail());
				interviewerFromDB.setMobile(clientInterviewerPanelFromUI.getMobile());
				interviewerFromDB.setName(clientInterviewerPanelFromUI.getName());

				timeSlotOfExistingInterviewerList = clientInterviewerPanelFromUI.getInterviewerTimeSlots();
				interviewerFromDB.addInterviewerTimeSlot(timeSlotOfExistingInterviewerList);
				interviewerPanelListFromDB.add(clientInterviewerPanelFromUI);
			}
		}
		clientFromDB.getClientInterviewerPanel().clear();
		clientFromDB.addClientInterviewerPanel(interviewerPanelListFromDB);
		clientFromDB = clientRepository.save(clientFromDB);

		// if these decision makers are not part of generic list then adding it
		genericDecisionMakerService.addDMsFromClient(clientFromDB.getClientDecisionMaker());

		// if interviewer are not part of generic list then adding it
		genericInterviewerService.addGenericInterviewerFromClientOrPosition(clientFromDB.getClientInterviewerPanel());

		// adding client modification activity
		ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.CLIENT_MODIFIED.getDisplayName(),
				ActivityMessageConstants.getUpdatedByMsg(userService.getLoggedInUserObject()), new Date(), clientFromDB.getId());
		clientActivityService.addActivity(activity);

		clientOnUpdateNotification(clientId, clientFromDB, NotificationEvent.CLIENT_MODIFIED.getDisplayName(), "Client details updated ");
		return clientFromDB;
	}

	/**
	 * Notification sent to HR executives working on the clients position on
	 * client update
	 *
	 * @param clientId
	 * @param clientFromDB
	 * @throws RecruizException
	 */
	private void clientOnUpdateNotification(String clientId, Client clientFromDB, String notificationType, String message)
			throws RecruizException {

		List<Position> positionList = positionService.getAllPositionByClient(clientFromDB);
		Set<User> clientHRs = new HashSet<User>();
		if (positionList != null && !positionList.isEmpty()) {
			for (Position position : positionList) {
				// sending notification to removed HRs of the position
				Set<User> positionHr = position.getHrExecutives();
				if (positionHr != null && !positionHr.isEmpty()) {
					for (User hr : positionHr) {
						clientHRs.add(hr);
					}
				}
			}
		}

		clientHRs.add(userService.getUserByEmail(clientFromDB.getOwner()));

		if (clientHRs != null && !clientHRs.isEmpty()) {
			List<String> clientAssociatedUsers = new ArrayList<>();
			for (User hr : clientHRs) {
				// if the user is not the logged in user
				if (!hr.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
					clientAssociatedUsers.add(hr.getEmail());
					notificationService.sendNotification(new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
							userService.getLoggedInUserObject().getName(), notificationType,
							notificationService.getMessageClientDetailsUpdated(Long.parseLong(clientId)), new Date(), null,
							clientFromDB.getId(), 0, 0, null));
				}
			}

			// sending emails to hrs and owner of the client -- REZQA-18
			sendEmailToDeptAssociatedUser(clientFromDB, clientAssociatedUsers);
		}
	}

	/**
	 * This will send modification email to all users who is associated to this
	 * dept
	 *
	 * @param clientFromDB
	 * @param clientAssociatedUsers
	 * @throws RecruizException
	 */
	private void sendEmailToDeptAssociatedUser(Client clientFromDB, List<String> clientAssociatedUsers) throws RecruizException {
		// adding owner as part of email list
		if (!clientAssociatedUsers.contains(clientFromDB.getOwner())) {
			clientAssociatedUsers.add(clientFromDB.getOwner());
		}

		Map<String, Object> emailBodyVariableMap = new HashMap<>();
		emailBodyVariableMap.put(GlobalConstants.CLIENT_NAME, clientFromDB.getClientName());
		String clientLabel = "Department";
		if ("Agency".equalsIgnoreCase(organizationService.getCurrentOrganization().getOrgType())) {
			clientLabel = "Client";
		}

		emailBodyVariableMap.put(GlobalConstants.CLIENT_LABEL, clientLabel);
		String templateFileName = "email-template-client-modified.html";

		String emailLink = fileService.getBaseUrl() + "/web/client-details?cid=" + clientFromDB.getId();

		emailTemplateDataService.initEmailBodyDefaultVariables(emailBodyVariableMap);
		String templateFromFile = emailTemplateDataService.getHtmlContentFromFile(emailBodyVariableMap, templateFileName);
		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateFromFile, emailLink,
				"View " + clientLabel + " Details");

		String emailSubject = clientFromDB.getClientName() + " details updated ";
		emailService.sendEmail(clientAssociatedUsers, renderedMasterTemplate, emailSubject, true);
	}

	/**
	 * <code>updateClientStatus</code> method is used to change the status of
	 * client by clientId.
	 *
	 * @param clientId
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Client updateClientStatus(long clientId, String status) throws RecruizException {
		Client client = clientRepository.findOne(clientId);

		boolean updatable = false;
		if (client.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkUserPermission.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkUserPermission.hasAddEditClientPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		String oldStatus = client.getStatus();

		client.setStatus(status);
		List<Position> positionList = positionService.getAllPositionByClient(client);
		for (Position position : positionList) {
			position.setClientStatus(status);
			position.getBoard().setClientStatus(status);
		}

		client = clientRepository.save(client);

		// adding client status change activity
		ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.CLIENT_STATUS_CHANGED.getDisplayName(),
				ActivityMessageConstants.getStatusChangedMsg(userService.getLoggedInUserObject(), oldStatus, status), new Date(), clientId);
		clientActivityService.addActivity(activity);

		clientOnUpdateNotification(clientId + "", client, NotificationEvent.CLIENT_STATUS_CHANGED.getDisplayName(),
				"Client status updated to " + status + " ");
		return client;
	}

	/**
	 * <code>getAllClient</code> method return the list of all clients if the
	 * use belong s to HR executive group then his/her respective client will be
	 * returned
	 *
	 * @return
	 * @throws RecruizException
	 */
	// @Transactional(readOnly = true)
	// public List<ClientOpeningCountDTO> getAllClient() throws RecruizException
	// {
	// if (checkUserPermission.hasNormalRole())
	// throw new RecruizException(ErrorHandler.NORMAL_USER_NOT_ALLOWED,
	// ErrorHandler.NORMAL_USER);
	//
	// if
	// (checkUserPermission.belongsToHrGroup(userService.getLoggedInUserObject().getUserRole()))
	// return getAllClientForHr();
	//
	// // this will return list of all client except HR executive. needs to
	// // implement view_permission for all role otherwise we cannot control
	// // view of any entity
	// List<Long> clientIds = new ArrayList<Long>();
	// clientIds = clientRepository.getClientIds();
	// if (clientIds.isEmpty())
	// return null;
	// List<ClientOpeningCountDTO> clientOpeningCountDTO =
	// clientRepository.clientListWithTotalOpening(clientIds);
	//
	// // this count of total opening should be replaced when working with
	// // finding total opened openings, total closed openings of each position
	// // by client
	// if (clientOpeningCountDTO != null && !clientOpeningCountDTO.isEmpty()) {
	// for (ClientOpeningCountDTO clientOpeningCount : clientOpeningCountDTO) {
	// clientOpeningCount.setTotalOpenings(
	// positionService.getTotalOpeningByClient(clientOpeningCount.getClient().getId()));
	// }
	// return clientOpeningCountDTO;
	// }
	// return null;
	// }

	/**
	 * All clients with pagination
	 *
	 * @param pageable
	 * @return
	 * @throws RecruizException
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Page<ClientOpeningCountDTO> getAllClient(Pageable pageable, String sortField, String sortOrder) throws RecruizException {

		User loggedInUser = userService.getLoggedInUserObject();
		if (checkUserPermission.hasNormalRole())
			throw new RecruizWarnException(ErrorHandler.NORMAL_USER_NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		List<Long> clientIds = new ArrayList<Long>();
		if (checkUserPermission.isSuperAdmin()) {
			clientIds = getClientIds();
		} else if (checkUserPermission.belongsToHrManagerGroup(userService.getLoggedInUserObject().getUserRole())) {
			clientIds = getClientIdsForLoggedInUserManager();
		} else if (checkUserPermission.belongsToHrExecGroup(userService.getLoggedInUserObject().getUserRole())) {
			clientIds = getAllClientForHr(loggedInUser);
		} else {
			throw new RecruizException(ErrorHandler.DOES_NOT_HAVE_PERMISSION, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		if (clientIds != null && !clientIds.isEmpty()) {
			// below create hibernate query added for order by clause because
			// sort order wont work in named query
			String hql = "SELECT NEW com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO(c, COUNT(p))"
					+ "FROM client c LEFT JOIN  c.positions p where c.id IN :ids GROUP BY c ORDER BY c." + sortField + " " + sortOrder;
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
		return null;
	}

	/**
	 * all client user wise
	 *
	 * @return
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public List<Long> getAllClientForHr(User loggedInUser) throws RecruizException {
		// List<Long> teamIds = new ArrayList<>();
		// List<Team> teams = teamService.getAllTeamsForUser(loggedInUser);
		// if (null != teams && !teams.isEmpty()) {
		// for (Team team : teams) {
		// teamIds.add(team.getId());
		// }
		// } else {
		// teamIds.add(0L);
		// }

		List<Long> clientIds = new ArrayList<Long>();
		List<BigInteger> ids = clientRepository.findByClientAndPositions(loggedInUser.getEmail(), userService.loggedInUserSet(),
				loggedInUser.getEmail());
		for (BigInteger bigInt : ids) {
			clientIds.add(bigInt.longValue());
		}

		if (clientIds.isEmpty())
			return null;

		return clientIds;
	}

	/**
	 * all client user wise
	 *
	 * @return
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public List<Long> getAllClientForHrByIds(User loggedInUser, List<Long> cIds) throws RecruizException {

		List<Long> clientIds = new ArrayList<Long>();
		if (!cIds.isEmpty()) {
			List<BigInteger> ids = clientRepository.findByClientAndPositionsAndIdIn(loggedInUser.getEmail(), userService.loggedInUserSet(),
					loggedInUser.getEmail(), cIds);
			for (BigInteger bigInt : ids) {
				clientIds.add(bigInt.longValue());
			}
		}

		return clientIds;
	}

	/**
	 * <code>getClient</code> method return the client object
	 *
	 * @param clientId
	 * @return
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public ClientOpeningCountDTO getClient(long clientId) throws RecruizException {
		return clientRepository.clientWithTotalOpening(clientId);
	}

	/**
	 * <code>getClientByName</code> Method will return client object by passing
	 * client name as argument.
	 *
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 */
//	@Transactional(readOnly = true)
	public Client getClientByName(String clientName) throws RecruizException {
		return clientRepository.findOneByClientName(clientName);
	}

//	@Transactional(readOnly = true)
	public List<Client> getClientByNameIn(List<String> clientNames) throws RecruizException {
		return clientRepository.findByClientNameIn(clientNames);
	}

	/**
	 * @param clientName
	 * @return
	 * @throws RecruizException
	 */
	public Client getClientByClientName(String clientName) throws RecruizException {
		return clientRepository.findByClientName(clientName);
	}

	/**
	 * <code>getClientByName</code> Method returns list of client names.
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<String> getAllClientName() {
		return clientRepository.getClientNames();
	}

	/**
	 * <code>deleteClient</code> method is used to delete client by clientId
	 *
	 * @param clientId
	 * @throws RecruizException
	 */
	@Transactional
	public void deleteClient(long clientId) throws RecruizException {

		Client client = clientRepository.getOne(clientId);
		// breaking relation mapping client and prospect
		Prospect prospect = prospectService.getProspectByClient(client);
		if (prospect != null) {
			prospect.setClient(null);
			prospectService.save(prospect);
		}

		User owner = userService.getUserByEmail(client.getOwner());

		boolean deletable = false;
		if (client.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail()) || checkUserPermission.hasGlobalDeletePermission()) {
			deletable = true;
		} else if (!deletable && !checkUserPermission.hasDeleteClientPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		List<Position> clientPositions = positionService.getAllPositionByClient(clientRepository.findOne(clientId));
		if (clientPositions != null && !clientPositions.isEmpty()) {
			throw new RecruizWarnException(ErrorHandler.POSITION_EXITS_FOR_CLIENT, ErrorHandler.DELETION_NOT_ALLOWED);
		}
		// position should not be delete from here, delete position first and
		// then delete the client
		// positionService.delete(clientPositions);
		delete(clientId);

		// sending notification to owner when client is deleted
		if (owner != null && !owner.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
			notificationService.sendNotification(new Notification(owner.getEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserObject().getName(), NotificationEvent.CLIENT_DELETED.getDisplayName(),
					"Client " + client.getClientName() + " is deleted.", new Date(), null, clientId, 0, 0, null));
		}
	}

	@Transactional(readOnly = true)
	public int getActiveClientCount() {
		return clientRepository.getActiveClientCount();
	}

	@Transactional(readOnly = true)
	public Page<Client> getAllClientsForSearchIndex(Pageable pageable) {
		return clientRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public List<Long> getClientIds() {
		return clientRepository.getClientIds();
	}

	/**
	 * saving new interviewer passed from interview schedule window
	 *
	 * @param scheduleDTO
	 * @throws Exception
	 */
	@Transactional
	public void saveNewInterviewer(InterviewScheduleDTO scheduleDTO) throws RecruizException {
		Position position = positionService.getPositionByCode(scheduleDTO.getPositionCode());
		Client client = clientRepository.findOne(position.getClient().getId());
		List<InterviewPanelDTO> interviewerList = scheduleDTO.getInterviewerList();

		Set<ClientInterviewerPanel> interviewersToBeAdded = new HashSet<>();

		for (InterviewPanelDTO interviewPanelDTO : interviewerList) {
			if (interviewPanelService.getInterviewerByEmailAndClient(interviewPanelDTO.getEmail(), client) != null)
				throw new RecruizWarnException("Interviewer exists with email : " + interviewPanelDTO.getEmail(),
						ErrorHandler.INVALID_SERVER_REQUEST);

			ClientInterviewerPanel clientInterviewerPanel = new ClientInterviewerPanel();
			clientInterviewerPanel.setName(interviewPanelDTO.getName());
			clientInterviewerPanel.setEmail(interviewPanelDTO.getEmail());
			clientInterviewerPanel.setMobile(interviewPanelDTO.getMobile());
			clientInterviewerPanel = interviewPanelService.addInterviewer(clientInterviewerPanel);
			client.addClientInterviewerPanel(clientInterviewerPanel);

			interviewersToBeAdded.add(clientInterviewerPanel);

			InterviewerTimeSlot interviewerTimeSlot = new InterviewerTimeSlot(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());
			clientInterviewerPanel.addInterviewerTimeSlot(interviewerTimeSlot);
			position.addInterviewerPanel(clientInterviewerPanel);

			// adding newly added email to interviewer list
			if (scheduleDTO.getInterviewerEmails() == null || scheduleDTO.getInterviewerEmails().isEmpty()) {
				Set<String> newInterviewer = new HashSet<>();
				newInterviewer.add(interviewPanelDTO.getEmail());
				scheduleDTO.setInterviewerEmails(newInterviewer);
			} else if (!scheduleDTO.getInterviewerEmails().contains(interviewPanelDTO.getEmail())) {
				scheduleDTO.getInterviewerEmails().add(interviewPanelDTO.getEmail());
			}
		}

		client = clientRepository.save(client);

		// adding interviewer to generic list on scheduling/rescheduling
		// interview
		if (null != interviewersToBeAdded && !interviewersToBeAdded.isEmpty()) {
			genericInterviewerService.addGenericInterviewerFromClientOrPosition(interviewersToBeAdded);
		}

		// adding new interviewer added activity
		ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), NotificationEvent.CLIENT_INTERVIEWER_ADDED.getDisplayName(),
				ActivityMessageConstants.getNewInterviewerAddedMsg(userService.getLoggedInUserObject()), new Date(), client.getId());

		clientActivityService.addActivity(activity);
	}

	@Transactional
	public List<Client> getClientsByOwner(String email) {
		return clientRepository.findByOwner(email);
	}

	/**
	 *
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public List<Client> getClientsByOwnerAndPositionHRs() throws RecruizException {
		List<Position> positions = positionService.getPositionByHrExecutive();
		List<Client> clients = clientRepository.findByPositionsInOrOwner(positions, userService.getLoggedInUserEmail());
		return clients;
	}

	@Transactional(readOnly = true)
	public List<String> getAllClientNameForHr(User hrExecutive) {

		Set<User> positionHrExecutives = userService.loggedInUserSet();
		positionHrExecutives.add(hrExecutive);

		List<String> clientName = clientRepository.findClientNameByClientAndPositions(userService.getLoggedInUserEmail(),
				positionHrExecutives, userService.getLoggedInUserEmail());

		if (clientName.isEmpty())
			return null;

		return clientName;
	}

	@Transactional(readOnly = true)
	public List<Long> findByClientAndPositionsByManagerClients(String loggedInUserEmail, Set<User> loggedInUserSet,
			String loggedInUserEmail1, List<Long> managerIds) {

		List<Long> clientIds = new ArrayList<Long>();
		List<BigInteger> ids = clientRepository.findByClientAndPositionsByManagerClients(loggedInUserEmail, loggedInUserSet,
				loggedInUserEmail1, managerIds);
		for (BigInteger bigInt : ids) {
			clientIds.add(bigInt.longValue());
		}

		if (clientIds.isEmpty())
			return null;

		return clientIds;
	}

	@Transactional(readOnly = true)
	public List<String> getClientNameByIds(List<Long> clientIds) {
		return clientRepository.findClientNameByClientIds(clientIds);
	}

	@Transactional(readOnly = true)
	public List<Long> getClientIdsForHrExecutive(User loggedInUser) {
		List<Long> clientIds = new ArrayList<Long>();
		List<BigInteger> ids = clientRepository.getClientIdForHrExecutive(loggedInUser.getUserId() + "");
		for (BigInteger bigInt : ids) {
			clientIds.add(bigInt.longValue());
		}
		return clientIds;
	}

	@Transactional(readOnly = true)
	public List<Client> getDummyClient() {
		return clientRepository.findByDummy(true);
	}

	/**
	 * to delete interviewer from client and position
	 *
	 * @param cid
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	public Set<ClientInterviewerPanel> deleteInterviewer(Long cid, Long id) throws RecruizException {
		Client client = clientRepository.findOne(cid);
		Set<ClientInterviewerPanel> clientInterviewPanelList = client.getClientInterviewerPanel();
		String interviewerDetails = "";
		if (null != clientInterviewPanelList && !clientInterviewPanelList.isEmpty()) {
			ClientInterviewerPanel interviewerToRemove = interviewPanelService.findOne(id);
			interviewerDetails = interviewerToRemove.getName() + " (" + interviewerToRemove.getEmail() + ")";

			if (clientInterviewPanelList.contains(interviewerToRemove)) {
				List<Position> clientPositions = positionService.getAllPositionByClient(client);
				if (null != clientPositions && !clientPositions.isEmpty()) {
					for (Position position : clientPositions) {
						positionService.removeInterviewer(position.getId(), id);
					}
				}

				clientInterviewPanelList.remove(interviewerToRemove);
				interviewPanelService.deleteInterviewer(interviewerToRemove);
				// client.getClientInterviewerPanel().clear();
				// client.addClientInterviewerPanel(clientInterviewPanelList);
				clientRepository.save(client);
			}

			// adding client interviewer removed activity
			ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserName(), NotificationEvent.CLIENT_INTERVIEWER_REMOVED.getDisplayName(),
					ActivityMessageConstants.getInterviewerRemovedMsg(userService.getLoggedInUserObject()), new Date(), client.getId());

			clientActivityService.addActivity(activity);
			clientActivityService.addActivity(activity);

		}

		return clientInterviewPanelList;
	}

	/**
	 * To Remove decision maker from client
	 *
	 * @param id
	 * @param cid
	 * @return
	 */
	public Set<ClientDecisionMaker> removeDecisionMakerFromClient(Long id, Long cid) {
		Client client = clientRepository.findOne(cid);
		Set<ClientDecisionMaker> decisionMakerList = client.getClientDecisionMaker();
		if (null != decisionMakerList && !decisionMakerList.isEmpty()) {

			ClientDecisionMaker dmToRemove = decisionMakerService.findOne(id);
			String dmDetails = dmToRemove.getName() + " (" + dmToRemove.getEmail() + ")";
			decisionMakerList.remove(dmToRemove);
			// client.getClientDecisionMaker().clear();
			// client.addClientDecisionMaker(decisionMakerList);
			clientRepository.save(client);

			// adding client DM removed activity
			ClientActivity activity = new ClientActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserName(), NotificationEvent.CLIENT_DECISIONMAKER_REMOVED.getDisplayName(),
					ActivityMessageConstants.getDMRemovedMsg(userService.getLoggedInUserObject()), new Date(), client.getId());
			clientActivityService.addActivity(activity);
		}
		return decisionMakerList;
	}

	public List<Client> getClientByInterviewPanelIn(Set<ClientInterviewerPanel> interviewPanelList) {
		return clientRepository.findByClientInterviewerPanelIn(interviewPanelList);
	}

	public List<Client> getClientByDecisionMakerIn(Set<ClientDecisionMaker> clientDMs) {
		return clientRepository.findByClientDecisionMakerIn(clientDMs);
	}

	// to delete interviewer from generic list
	public void deleteInterviewerFormClientAndPosition(Set<ClientInterviewerPanel> exisitingPanel) throws RecruizException {

		for (ClientInterviewerPanel panel : exisitingPanel) {
			Set<Position> interviewerPositions = panel.getPositions();
			for (Position position : interviewerPositions) {
				positionService.removeInterviewer(position.getId(), panel.getId());
			}

			Client clnt = panel.getClient();
			if (null != clnt) {
				deleteInterviewer(clnt.getId(), panel.getId());
			}

			// deleting feedback for this interviewer
			feedbackService.cancelFeedback(panel.getEmail());
			interviewScheduleService.untagInterviewer(panel.getEmail());

			// // adding client interviewer removed activity
			// ClientActivity activity = new
			// ClientActivity(userService.getLoggedInUserEmail(),
			// userService.getLoggedInUserEmail(),
			// userService.getLoggedInUserName(),
			// NotificationEvent.CLIENT_INTERVIEWER_REMOVED.getDisplayName(),
			// ActivityMessageConstants.getInterviewerRemovedMsg(userService.getLoggedInUserObject()),
			// new Date(), clnt.getId());
			// clientActivityService.save(activity);
		}

	}

	@Transactional(readOnly = true)
	public Object getClientNameIdsForHrExecutive(User loggedInUser) throws RecruizException {
		Set<Long> teamIds = teamService.getAllTeamsIdForUser(loggedInUser);
		List<Client> clients = null;
		if (teamIds == null || teamIds.isEmpty()) {
			clients = clientRepository.getClientForUserWithOutTeam(loggedInUser.getEmail(), loggedInUser.getEmail(),
					loggedInUser.getUserId());
		} else {
			clients = clientRepository.getClientForUserWithTeam(loggedInUser.getEmail(), teamService.getAllTeamsIdForUser(loggedInUser),
					loggedInUser.getEmail(), loggedInUser.getUserId());
		}

		Map<Object, Object> nameIdMap = new HashMap<>();
		for (Client client : clients) {
			nameIdMap.put(client.getId(), client.getClientName());
		}
		return nameIdMap;
	}

	@Transactional(readOnly = true)
	public Set<String> getClientLocations() {
		return clientRepository.getClientLocations();
	}

	public void deleteCustomFieldWithName(String name) {
		clientRepository.deleteCustomFieldWithName(name);
	}

	@Transactional(readOnly = true)
	public Client getClient(String clientName) {
		Long id = clientRepository.getIdByClient(clientName);
		return clientRepository.findOneByClientName(clientName);
	}

	public List<BigInteger> getClientIdsForUser(List<Long> teamIds) {
		return clientRepository.getClientIdsForUserWithTeam(userService.getLoggedInUserEmail(), teamIds, userService.getLoggedInUserEmail(),
				userService.getLoggedInUserObject().getUserId());
	}

	public List<Long> getClientIdsForLoggedInUserManager() throws RecruizException {
		Set<Long> teamIds = teamService.getAllTeamsIdForUser(userService.getLoggedInUserObject());
		List<Long> clientIds = new ArrayList<Long>();
		List<BigInteger> ids = new ArrayList<>();
		if (teamIds == null || teamIds.isEmpty()) {
			ids = clientRepository.getClientIdsForUserWithOutTeam(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserObject().getUserId());
		} else {
			ids = clientRepository.getClientIdsForUserWithTeam(userService.getLoggedInUserEmail(), teamIds,
					userService.getLoggedInUserEmail(), userService.getLoggedInUserObject().getUserId());
		}

		for (BigInteger bigInt : ids) {
			clientIds.add(bigInt.longValue());
		}

		return clientIds;
	}

	public List<String> getClientNamesForLoggedInUser() throws RecruizException {
		Set<Long> teamIds = teamService.getAllTeamsIdForUser(userService.getLoggedInUserObject());
		if (teamIds == null || teamIds.isEmpty()) {
			return clientRepository.getClientNamesForUserWithOutTeam(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserObject().getUserId());
		}
		return clientRepository.getClientNamesForUserWithTeam(userService.getLoggedInUserEmail(), teamIds,
				userService.getLoggedInUserEmail(), userService.getLoggedInUserObject().getUserId());
	}

	public Long getClientCountForUser(List<Long> teamIds) {
		return clientRepository.getClientCountForUserWithTeam(userService.getLoggedInUserEmail(), teamIds,
				userService.getLoggedInUserEmail(), userService.getLoggedInUserObject().getUserId());
	}

	public Long getClientCountForLoggedInUser() throws RecruizException {
		// Set<Long> teamIds =
		// teamService.getAllTeamsIdForUser(userService.getLoggedInUserObject());
		// if (teamIds == null || teamIds.isEmpty()) {
		return clientRepository.getClientCountForUserWithOutTeam(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserObject().getUserId());
		// }
		// return
		// clientRepository.getClientCountForUserWithTeam(userService.getLoggedInUserEmail(),
		// teamIds,
		// userService.getLoggedInUserEmail(),
		// userService.getLoggedInUserObject().getUserId());
	}

	public Long getClientCountForLoggedInUserManager() throws RecruizException {
		Set<Long> teamIds = teamService.getAllTeamsIdForUser(userService.getLoggedInUserObject());
		if (teamIds == null || teamIds.isEmpty()) {
			return clientRepository.getClientCountForUserWithOutTeam(userService.getLoggedInUserEmail(), userService.getLoggedInUserEmail(),
					userService.getLoggedInUserObject().getUserId());
		}
		return clientRepository.getClientCountForUserWithTeam(userService.getLoggedInUserEmail(), teamIds,
				userService.getLoggedInUserEmail(), userService.getLoggedInUserObject().getUserId());
	}
	
	///Return list of clients based on creation dates
	public List<Client> getClientbyDates(Date startdate, Date enddate) throws RecruizException {
		
		return clientRepository.findByCreationDateBetween(startdate, enddate);
	}
	
}
