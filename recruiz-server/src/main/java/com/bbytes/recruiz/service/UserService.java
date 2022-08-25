package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.enums.UserType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseSearchService;
import com.bbytes.recruiz.repository.OrganizationRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseDeleteUserDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.JPAUtil;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.UserFileHeaderConstant;
import com.bbytes.recruiz.utils.ValidateEmailDomain;

import net.fortuna.ical4j.data.ParserException;

@Service
public class UserService extends AbstractService<User, Long> {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	private UserRepository userRepository;

	@Autowired
	private PasswordHashService passwordHashService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private TaskItemService taskItemService;

	@Autowired
	private TaskFolderService taskFolderService;

	@Autowired
	private FolderService folderService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private SixthSenseSearchService sixthSenseSearchService;

	@Autowired
	private AdvancedSearchService advancedSearchService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private RoundCandidateService roundCandidateService;
	
	@Autowired
	EmailAccountDetailService emailAccountDetailService;
	
	@Autowired
	CandidateActivityService candidateActivityService;
	
	@Autowired
	PositionActivityService positionActivityService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${email.inviteToJoin.subject}")
	private String inviteToJoinSubject;

	@Value("${email.inviteToSignup.subject}")
	private String inviteToSignupSubject;

	@Autowired
	public UserService(UserRepository userRepository) {
		super(userRepository);
		this.userRepository = userRepository;
	}

	/**
	 * This method will return current logged in User's email address from the
	 * JWT Token
	 * 
	 * @return
	 */
	public String getLoggedInUserEmail() {
		try {
			final String email = SecurityContextHolder.getContext().getAuthentication().getName();
			return email;
		} catch (Exception ex) {
			// TODO log exception here
			return null;
		}
	}

	/**
	 * This method will return current logged in User Object from the JWT Token
	 * 
	 * @return
	 */
	public User getLoggedInUserObject() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext == null)
			return null;
		Authentication auth = securityContext.getAuthentication();
		if (auth == null)
			return null;

		final String email = auth.getName();
		if (email == null || email.isEmpty())
			return null;
		User user = getUserByEmail(email);
		return user;
	}

	/**
	 * This method will return current logged in User TimeZone from the JWT
	 * Token
	 * 
	 * @return
	 */
	public String getLoggedInUserTimeZone() {
		final String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = getUserByEmail(email);
		if (user != null)
			return user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata";

			return "Asia/Kolkata";
	}

	/**
	 * This method will return current logged in User Object from the JWT Token
	 * 
	 * @return
	 */
	public String getLoggedInUserName() {
		final String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = getUserByEmail(email);
		if (user != null)
			return user.getName();

		return "N/A";
	}

	/**
	 * This method will return current logged in User timezone from the JWT
	 * Token
	 * 
	 * @return
	 */
	public DateTimeZone getLoggedInUserTimezone() {
		final MultiTenantAuthenticationToken token = (MultiTenantAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return token.getUserTimeZone();
	}

	/**
	 * This method will return current logged in User locale from the JWT Token
	 * 
	 * @return
	 */
	public Locale getLoggedInUserLocale() {
		final MultiTenantAuthenticationToken token = (MultiTenantAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return token.getUserLocale();
	}

	/**
	 * This method will return external User Object from the access Token.
	 * 
	 * @return
	 */
	public ExternalUserAccessDataHolder getExternalUserObject() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		MultiTenantAuthenticationToken authToken = (MultiTenantAuthenticationToken) auth;
		ExternalUserAccessDataHolder externalUserAccessDataHolder = authToken.getExternalUserAccessDataHolder();
		return externalUserAccessDataHolder;
	}

	@Transactional(readOnly = true)
	public List<String> findAllEmails() {
		return userRepository.findAllEmails();
	}

	@Transactional(readOnly = true)
	public boolean userEmailExist(String email) {
		boolean state = userRepository.findOneByEmail(email) == null ? false : true;
		return state;
	}


	public User getUserByEmail(String email) {
		return userRepository.findOneByEmail(email);
	}

	@Transactional(readOnly = true)
	public Collection<User> getUsersByEmails(Collection<String> emails) {
		Set<String> emailList = new HashSet<>();
		emailList.addAll(emails);
		return userRepository.findByEmailIn(emailList);
	}

	@Transactional(readOnly = true)
	public Iterable<User> getAllUsers() {
		return userRepository.findAll(new Sort("email"));
	}

	@Transactional(readOnly = true)
	public List<User> getAllAppUsers() {
		return userRepository.findByUserType(GlobalConstants.USER_TYPE_APP);
	}

	@Transactional(readOnly = true)
	public List<User> getAllActiveAppUsers() {
		return userRepository.findByUserTypeAndAccountStatus(GlobalConstants.USER_TYPE_APP, true);
	}

	@Transactional(readOnly = true)
	public List<User> getAllActiveAppAndNonPendingUsers() {
		return userRepository.findByUserTypeAndAccountStatusAndJoinedStatus(GlobalConstants.USER_TYPE_APP, true, true);
	}

	@Transactional(readOnly = true)
	public List<User> getAllUsersByType(String userType) {
		return userRepository.findByUserType(userType);
	}

	@Transactional(readOnly = true)
	public List<User> getAllVendroUser() {
		return userRepository.findByUserType(GlobalConstants.USER_TYPE_VENDOR);
	}

	@Transactional(readOnly = true)
	public List<User> getAllUserByVendor(String vendorId) {
		return userRepository.findByUserTypeAndVendorIdAndAccountStatus(GlobalConstants.USER_TYPE_VENDOR, vendorId, true);
	}

	@Transactional
	public void delete(User user, String newOwnerEmail)
			throws IOException, ParserException, MessagingException, ParseException, RecruizException {
		if (!checkPermissionService.hasOrgAdminPermission() && !checkPermissionService.hasDeleteUserPermission()
				&& !checkPermissionService.hasAdminSettingPermission())
			try {
				throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
			} catch (RecruizException e) {
				e.printStackTrace();
			}

		// changing position ownership and removing the user from HR executive
		// for the position
	/*	List<Position> position = positionService.getPositionListByUser(user);
		if (position != null && !position.isEmpty()) {
			for (Position pos : position) {
				pos.getHrExecutives().remove(user);
				if (pos.getOwner().equalsIgnoreCase(user.getEmail())) {
					pos.setOwner(newOwnerEmail);
				}
				positionService.save(pos);
			}
		}*/


		List<Candidate> candidateList = candidateService.getAllCandidateByOwner(user.getEmail());

		for (Candidate candidate : candidateList) {
			if(candidate.getOwner().equalsIgnoreCase(user.getEmail())){
				candidate.setOwner(newOwnerEmail);
				candidateService.save(candidate);
				
				candidateActivityService.addActivity("Owner ship moved from this "+user.getEmail()+" to this "+newOwnerEmail,
						this.getLoggedInUserEmail() + "(" + this.getLoggedInUserObject().getName() + ")",
						candidate.getCid() + "", CandidateActivityType.OWNERSHIP_CHANGED.getDisplayName());		

			}
		}


		List<RoundCandidate> roundList = roundCandidateService.getCandidateSourcedBy(user.getEmail());

		for (RoundCandidate roundCandidate : roundList) {
			if(roundCandidate.getSourcedBy().equalsIgnoreCase(user.getEmail())){
				roundCandidate.setSourcedBy(newOwnerEmail);
				roundCandidateService.save(roundCandidate);
				
				candidateActivityService.addActivity("Owner ship moved from this "+user.getEmail()+" to this "+newOwnerEmail,
						this.getLoggedInUserEmail() + "(" + this.getLoggedInUserObject().getName() + ")",
						roundCandidate.getCandidate().getCid() + "", CandidateActivityType.OWNERSHIP_CHANGED.getDisplayName());		
				
				
			}
		}

		
		try{
			List<String> pCodeList = positionService.getPositionHrExecutivesIn(user);
			for (String pCode : pCodeList) {
				Position positionData =	positionService.getPositionByCode(pCode);
				
				Set<User> userList = new HashSet<>();

				for(User userData : positionData.getHrExecutives()){

					if(userData.getEmail().equalsIgnoreCase(user.getEmail())){
						userList.add(user);
					}else{
						userList.add(userData);
					}
				}

				positionData.setHrExecutives(userList);

				positionService.save(positionData);

			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		List<Position> position = positionService.getPositionListByUser(user);
		if (position != null && !position.isEmpty()) {
			for (Position pos : position) {
				pos.getHrExecutives().remove(user);
				if (pos.getOwner().equalsIgnoreCase(user.getEmail())) {
					pos.setOwner(newOwnerEmail);
				}
				positionService.save(pos);
				
				PositionActivity positionActivity = new PositionActivity(this.getLoggedInUserEmail(), this.getLoggedInUserName(),
						"Owner ship moved from this "+user.getEmail()+" to this "+newOwnerEmail,
						"Owner ship moved from this "+user.getEmail()+" to this "+newOwnerEmail, new Date(), pos.getPositionCode(),
						pos.getTeam());
				positionActivityService.addActivity(positionActivity);
				
			}
		}
		
		
		// changing client ownership
		List<Client> clientsForUser = clientService.getClientsByOwner(user.getEmail());
		if (clientsForUser != null && !clientsForUser.isEmpty()) {
			for (Client client : clientsForUser) {
				client.setOwner(newOwnerEmail);
			}
			clientService.save(clientsForUser);
		}

		// changing adv search query save ownership
		User newOwner = getUserByEmail(newOwnerEmail);
		if (newOwner != null) {
			List<AdvancedSearchQueryEntity> advancedSearchQueryEntities = advancedSearchService.getByOwner(user);
			if (advancedSearchQueryEntities != null && !advancedSearchQueryEntities.isEmpty()) {
				for (AdvancedSearchQueryEntity advancedSearchQueryEntity : advancedSearchQueryEntities) {
					advancedSearchQueryEntity.setOwner(newOwner);
				}
				advancedSearchService.save(advancedSearchQueryEntities);
			}
		}

		// changing the interview scheduled ownership
		List<InterviewSchedule> schedulesForUser = interviewScheduleService.getCalenderByOwner(user.getEmail());
		if (schedulesForUser != null && !schedulesForUser.isEmpty()) {
			for (InterviewSchedule interviewSchedule : schedulesForUser) {
				interviewSchedule.setInterviewSchedulerEmail(newOwnerEmail);
			}
			interviewScheduleService.save(schedulesForUser);
		}

		// removing the interview scheduled hr executive
		List<InterviewSchedule> userInterviewSchedule = interviewScheduleService.getInterviewScheduleByHr(user.getUserId() + "");
		if (userInterviewSchedule != null && !userInterviewSchedule.isEmpty()) {
			for (InterviewSchedule interviewSchedule : userInterviewSchedule) {
				interviewSchedule.getScheduleHrExecutives().remove(user);
				interviewScheduleService.save(interviewSchedule);
			}
		}

		// if user is part of any task then remove the user and update the task
		List<User> taskMemberUser = new ArrayList<>();
		taskMemberUser.add(user);
		List<TaskItem> userOfTaskItems = taskItemService.findByUsersIn(taskMemberUser);
		if (userOfTaskItems != null && !userOfTaskItems.isEmpty()) {
			for (TaskItem taskItem : userOfTaskItems) {
				taskItem.getUsers().remove(user);
				taskItemService.save(taskItem);
			}
		}

		// if user has any task item then it will cancel the schedule and then
		// it will delete the task
		List<TaskItem> userTaskItems = taskItemService.findByOwner(user);
		if (userTaskItems != null && !userTaskItems.isEmpty()) {
			for (TaskItem taskItem : userTaskItems) {
				taskItemService.deleteTaskItem(taskItem.getId(), user);
			}
		}

		// if user has created any task folder then delete it also
		List<TaskFolder> taskFolders = taskFolderService.findByOwner(user);
		if (taskFolders != null && !taskFolders.isEmpty()) {
			taskFolderService.delete(taskFolders);
		}

		// if user has shared folder in their account then remove
		Collection<User> users = new ArrayList<>();
		users.add(user);
		Set<Folder> folders = folderService.findBySharedUserListIn(users);
		if (folders != null && !folders.isEmpty()) {
			for (Folder folder : folders) {
				folder.getSharedUserList().remove(user);
			}
		}
		// if user owner of folder switch user
		Set<Folder> ownerFolders = folderService.findByOwner(user.getEmail());
		if (ownerFolders != null && !ownerFolders.isEmpty()) {
			for (Folder folder : ownerFolders) {
				folder.setOwner(newOwnerEmail);
			}
		}

		teamService.removeUser(user);

		// remove sixth sense user
		try {
			SixthSenseDeleteUserDTO sixthSenseDeleteUserDTO = new SixthSenseDeleteUserDTO();
			sixthSenseDeleteUserDTO.getUsers().add(user.getEmail());
			sixthSenseSearchService.deleteSixthSenseUser(sixthSenseDeleteUserDTO);
		} catch (Exception e) {
			// if it fails ignore
		}

		folderService.save(folders);
		folderService.save(ownerFolders);

		
		emailAccountDetailService.deleteByUser(user);
		
		
		userRepository.delete(user);
	}

	@Transactional
	public void deleteAll() {
		userRepository.deleteAll();
	}

	@Transactional
	public User create(String email, String name, String password, Organization org, UserRole userRole, String timezone, String locale,
			String userType, String mobile) throws RecruizException {

		User user = new User(name, email);
		user.setOrganization(org);
		user.setAccountStatus(true);
		user.setJoinedStatus(false);
		user.setPassword(passwordHashService.encodePassword(password));
		user.setUserRole(userRole);
		user.setUserType(userType);
		user.setMobile(mobile);
		save(user);
		return user;
	}

	@Transactional
	public User createForSocialSignUp(String email, String name, Organization org, UserRole userRole) throws RecruizException {
		if (tenantResolverService.userExistsForOrg(email, org.getOrgId()))
			throw new RecruizWarnException(ErrorHandler.INVITATION_FAILED, ErrorHandler.EMAIL_NOT_UNIQUE);

		User user = new User(name, email);
		user.setPassword(null);
		user.setOrganization(org);
		user.setAccountStatus(true);
		user.setJoinedStatus(true);
		user.setUserRole(userRole);
		save(user);
		return user;
	}

	@Transactional
	public boolean updatePassword(String newPassword, String userEmail) throws RecruizException {
		User user = tenantResolverService.getUserByEmail(userEmail);
		return updatePassword(newPassword, user);
	}

	@Transactional
	public boolean change(String newPassword, String oldPassword, String userEmail) throws RecruizException {
		User user = tenantResolverService.getUserByEmail(userEmail);
		//		if (!(passwordHashService.passwordMatches(oldPassword, user.getPassword())))
		//			throw new RecruizWarnException(GlobalConstants.PASSWORD_WRONG, ErrorHandler.PASSWORD_MISMATCH);
		return updatePassword(newPassword, user);
	}

	@Transactional
	public boolean updatePassword(String password, User user) throws RecruizException {
		if (user != null && password != null) {
			return tenantResolverService.resetPassword(user.getEmail(), password);
		} else {
			throw new RecruizWarnException(GlobalConstants.PASSWORD_IS_NULL, ErrorHandler.BAD_CREDENTIALS);
		}
	}

	@Transactional
	public User update(String name, String timezone, String mobile, String designation, String profileSignature) throws RecruizException {
		User user = getLoggedInUserObject();
		user.setName(name);
		user.setTimezone(timezone);
		user.setMobile(mobile);
		user.setDesignation(designation);
		user.setProfileSignature(profileSignature);
		user = userRepository.save(user);
		return user;
	}

	@Transactional
	public User updateJoinedStatus(String userEmail) throws RecruizException {
		if (userEmail == null || userRepository.findOneByEmail(userEmail) == null)
			throw new RecruizWarnException(ErrorHandler.ACTIVATION_FAILED, ErrorHandler.USER_NOT_FOUND);
		User user = userRepository.findOneByEmail(userEmail);
		user.setJoinedStatus(true);
		user = saveAndFlush(user);
		return user;
	}

	//@Transactional
	public User updateLoggedOnTime(String userEmail) {
		User user = userRepository.findOneByEmail(userEmail);
		user.setLoggedOn(DateTime.now().toDate());
		user = saveAndFlush(user);
		return user;
	}

	@Transactional
	public User updateUserAccountStatus(String userEmail, boolean status) throws RecruizException {
		if (!checkPermissionService.hasOrgAdminPermission() && !checkPermissionService.hasAddEditUserPermission()
				&& !checkPermissionService.hasAdminSettingPermission())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		if (userEmail != null && !userEmail.isEmpty()) {
			if (userRepository.exists(userRepository.findOneByEmail(userEmail).getUserId())) {
				User user = userRepository.findOneByEmail(userEmail);
				user.setAccountStatus(status);
				user = save(user);
				return user;
			}
		}
		return null;
	}

	@Transactional
	public void assignUserRole(String email, String roleName) throws RecruizException {
		if (!checkPermissionService.hasOrgAdminPermission() && !checkPermissionService.hasAddEditUserPermission()
				&& !checkPermissionService.hasAdminSettingPermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		if (email != null && !email.isEmpty()) {
			if (roleName != null && !roleName.isEmpty()) {
				if (userRepository.exists(userRepository.findOneByEmail(email).getUserId())) {
					UserRole orgAdmin = userRoleService.getRolesByName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
					User user = userRepository.findOneByEmail(email);

					if (roleName.equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE)
							&& (!user.getAccountStatus() || !user.getJoinedStatus())) {
						throw new RecruizWarnException(ErrorHandler.CAN_NOT_ASSIGN_SUPER_ADMIN_ROLE_FOR_INACTIVE_USER,
								ErrorHandler.NO_ROLE_CHANGE_FOR_INACTIVE_USER);
					}

					UserRole roleToChange = userRoleService.getRolesByName(roleName);
					Set<User> users = getAllUserByRole(orgAdmin);
					if (users.size() < 2 && orgAdmin.equals(user.getUserRole()))
						throw new RecruizWarnException(ErrorHandler.ONE_ORG_ADMIN_REQUIRED, ErrorHandler.NO_ORG_ADMIN);
					user.setUserRole(roleToChange);
					save(user);

					// role change notification entry
					notificationService.sendNotification(new Notification(user.getEmail(), getLoggedInUserEmail(),
							getLoggedInUserObject().getName(), NotificationEvent.USER_ROLE_CHANGED.getDisplayName(),
							"Your role changed to : " + roleName, new Date(), null, 0, 0, 0, null));
				}
			}
		}
	}

	@Transactional
	public List<User> getAllUsersForOrganization(String orgID) {
		Organization organization = orgRepo.findByOrgId(orgID);
		return userRepository.findByOrganization(organization);
	}

	@Transactional
	public List<User> getAllUserByJoinedStatus(boolean status) {
		return userRepository.findByJoinedStatus(status);
	}

	@Transactional
	public boolean isAccountActive(String email, boolean status) {
		boolean state = userRepository.findOneByEmailAndAccountStatus(email, status) == null ? false : true;
		return state;
	}

	@Transactional
	public boolean isUserJoined(String email, boolean status) {
		boolean state = userRepository.findOneByEmailAndJoinedStatus(email, status) == null ? false : true;
		return state;
	}

	@Transactional
	public User getUserToLogin(String email, String password) {
		return userRepository.findOneByEmailAndPassword(email, password);
	}

	@Transactional
	public List<UserRole> getAllRoles() throws RecruizException {
		return userRoleService.getAllRoles();
	}

	@Transactional(readOnly = true)
	public Set<User> getAllUserByRole(UserRole role) throws RecruizException {
		return userRepository.findByUserRole(role);
	}

	@Transactional(readOnly = true)
	public Set<User> getAllActiveUserByRole(UserRole role) throws RecruizException {
		return userRepository.findByJoinedStatusAndUserRole(true, role);
	}

	@Transactional(readOnly = true)
	public List<User> getJoinedAppUsers() throws RecruizException {
		return userRepository.findByUserTypeAndJoinedStatusIsTrue(UserType.APP.getDisplayName());
	}

	/**
	 * <code>getHrList</code> method will return all HR list.
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public List<User> getHrList() throws RecruizException {
		List<User> users = userRepository.findByUserType(UserType.APP.getDisplayName());

		List<User> hrList = new LinkedList<User>();
		for (User user : users) {
			if ((user.getJoinedStatus() == true && user.getAccountStatus() == true)
					&& (checkPermissionService.belongsToHrExecGroup(user.getUserRole())
							|| checkPermissionService.belongsToHrManagerGroup(user.getUserRole())))
				hrList.add(user);
		}

		if (hrList != null && !hrList.isEmpty()) {
			Collections.sort(hrList, new Comparator<User>() {
				public int compare(User hr1, User hr2) {

					int res = String.CASE_INSENSITIVE_ORDER.compare(hr1.getName(), hr2.getName());
					if (res == 0) {
						res = hr1.getName().compareTo(hr2.getName());
					}
					return res;
				}
			});
			return hrList;
		}
		return hrList;
	}

	@Transactional
	public Map<String, Object> getRoleWithUserCount() {
		Map<String, Object> roleMap = new HashMap<String, Object>();
		List<User> roleCount = userRepository.getUserCountGroupedByRole();
		for (int i = 0; i < roleCount.size(); i++) {
			Map<String, Object> result = new HashMap<String, Object>();
			Object usr = roleCount.get(i);
			Object[] objects = (Object[]) usr;
			long roleId = (Long) objects[0];
			String role = (String) objects[1];
			long count = (Long) objects[2];
			if (!role.equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE) && !role.equalsIgnoreCase(GlobalConstants.VENDOR_ROLE)
					&& !role.equalsIgnoreCase(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE)
					&& !role.equalsIgnoreCase(GlobalConstants.IT_ADMIN_USER_ROLE)) {
				result.put("id", roleId);
				result.put("role", role);
				result.put("count", count + "");
				roleMap.put(role, result);
			}
		}
		return roleMap;
	}

	/*
	 * this method will return list of user email who are not asssigned with the
	 * role name passed as argument
	 */

	@Transactional
	public Map<String, Object> getUserList() {
		Map<String, Object> emailList = new HashMap<String, Object>();
		List<User> userList = userRepository.findAll();
		Map<String, String> userMap = null;

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			if (!(user.getUserRole().getRoleName().equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE))) {
				userMap = new HashMap<String, String>();
				userMap.put("name", user.getName());
				userMap.put("email", user.getEmail());
				userMap.put("role", user.getUserRole().getRoleName());
				emailList.put(i + "", userMap);
			}
		}
		return emailList;
	}

	/**
	 * Get list of app user to change role
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getUserListToChangeRole() {
		Map<String, Object> emailList = new HashMap<String, Object>();
		List<User> userList = userRepository.findByUserType(UserType.APP.getDisplayName());
		Map<String, String> userMap = null;

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			if (!(user.getUserRole().getRoleName().equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE))) {
				userMap = new HashMap<String, String>();
				userMap.put("name", user.getName());
				userMap.put("email", user.getEmail());
				userMap.put("role", user.getUserRole().getRoleName());
				emailList.put(i + "", userMap);
			}
		}
		return emailList;
	}

	public boolean isUserHasMultipleOrg() {
		List<String> tenantIds = tenantResolverService.findTenantIdListForUserEmail(getLoggedInUserEmail());
		if (tenantIds.size() > 1)
			return true;
		else
			return false;
	}

	public void forgotPassword(String email) throws RecruizException {
		final String template = GlobalConstants.RESET_PASSWORD_TEMPLATE;
		List<String> tenants = tenantResolverService.findAllTenantsForUserId(email);
		if (tenants != null && !tenants.isEmpty()) {
			TenantContextHolder.setTenant(tenants.get(0));
			User user = tenantResolverService.getUserByEmail(email);
			String tempAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(email, tenants.get(0), WebMode.DASHBOARD, 48,
					TimeZone.getDefault().getID().toString(), "en");

			Map<String, Object> emailBody = new HashMap<>();
			emailBody.put(GlobalConstants.USER_NAME, user.getName());
			emailBody.put(GlobalConstants.USER_EMAIL, email);
			emailBody.put(GlobalConstants.ACTIVATION_LINK,
					baseUrl + GlobalConstants.RESET_PASSWORD + GlobalConstants.PASSKEY + tempAuthToken);

			List<String> emailList = new ArrayList<String>();
			emailList.add(email);
			emailService.sendEmail(emailList, emailBody, "Reset Recruiz Password", template);

		} else {
			throw new RecruizWarnException(ErrorHandler.USER_NOT_FOUND, ErrorHandler.USER_NOT_FOUND);
		}
	}

	public String inviteUserToSignup(final String emailTemplate, final String emailSubject, String email, String name, String orgId)
			throws RecruizException {

		String key = email + ":" + orgId + ":" + GlobalConstants.SIGNUP_MODE_INVITED;

		String encryptedKey = Base64.encodeBase64URLSafeString(key.getBytes());

		List<String> emailList = new ArrayList<String>();
		emailList.add(email);
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, name);
		emailBody.put(GlobalConstants.ORG_NAME, orgId);
		emailBody.put(GlobalConstants.ACTIVATION_LINK,
				baseUrl + GlobalConstants.INVITE_SIGNUP_URL + GlobalConstants.PASSKEY + encryptedKey);

		emailBody.put(GlobalConstants.USER_EMAIL, email);
		emailBody.put(GlobalConstants.FIRST_PASSWORD, GlobalConstants.DEFAULT_PASSWORD);

		emailBody = emailTemplateDataService.addHeaderFooterVariable(emailBody);

		emailService.sendEmail(emailTemplate, emailList, emailSubject, emailBody);
		return email;
	}

	// transactional bcoz it will save email activity

	@Transactional
	public void inviteUser(final String emailTemplate, final String emailSubject, List<User> successFullyInvitedEmails, String email,
			String name) throws RecruizException {

		String org_id = getLoggedInUserObject().getOrganization().getOrgId();
		User user = getLoggedInUserObject();
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(email, org_id, WebMode.DASHBOARD, 48, user.getTimezone(),
				user.getLocale());

		List<String> emailList = new ArrayList<String>();
		emailList.add(email);
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, name);
		emailBody.put(GlobalConstants.ACTIVATION_LINK, baseUrl + GlobalConstants.ACTIVATION_URL + GlobalConstants.PASSKEY + xauthToken);

		emailBody = emailTemplateDataService.addHeaderFooterVariable(emailBody);
		emailService.sendEmail(emailList, emailBody, emailSubject, emailTemplate);

		if (successFullyInvitedEmails != null)
			successFullyInvitedEmails.add(getUserByEmail(email));
	}

	@Transactional
	public String generateNewApiToken(String email, String tenantId) {
		return tokenAuthenticationProvider.getNewApiToken(email, tenantId);
	}

	public String getApiToken(User loggedInUser) {

		if (loggedInUser == null)
			return "N/A";

		String apiToken = tokenAuthenticationProvider.getApiToken(loggedInUser.getEmail());
		if (apiToken == null || apiToken.trim().isEmpty()) {
			apiToken = generateNewApiToken(loggedInUser.getEmail(), loggedInUser.getOrganization().getOrgId());
		}

		return apiToken;
	}

	/**
	 * will be used when HR set needs to be used (mostly to get position list
	 * where HR executive IN)
	 * 
	 * @return
	 */
	public Set<User> loggedInUserSet() {
		Set<User> userSet = new HashSet<User>();
		userSet.add(getLoggedInUserObject());
		return userSet;
	}

	@Transactional
	public List<User> getAllMarkedForDeleteUser() {
		return userRepository.findByMarkForDelete(true);
	}

	@Transactional(readOnly = true)
	public List<User> getAllByRoleName(String roleName) throws RecruizException {
		UserRole role = userRoleService.getRolesByName(roleName);
		return userRepository.findOneByUserRole(role);
	}

	@Transactional(readOnly = true)
	public Collection<User> getAllByEmails(Collection<String> emails) throws RecruizException {
		return userRepository.findByEmailIn(emails);
	}


	public Vendor getLoggedInUserVendor() throws RecruizException {
		final String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = getUserByEmail(email);
		Vendor vendor = null;

		if (user.getVendorId() != null)
			vendor = vendorService.findOne(Long.parseLong(user.getVendorId()));

		if (vendor == null)
			throw new RecruizWarnException(ErrorHandler.USER_IS_NOT_VENDOR, ErrorHandler.NOT_A_VENDOR);

		return vendor;
	}


	public boolean isLoggedInUserVendor() {
		try {
			return getLoggedInUserVendor() != null ? true : false;
		} catch (RecruizException e) {
			return false;
		}
	}

	@Transactional(readOnly = true)
	public String getVendorEmail() {
		Vendor vendor = null;
		try {
			vendor = getLoggedInUserVendor();
			return vendor.getEmail();
		} catch (RecruizException e) {
			// not a vendor
			return null;
		}
	}

	/**
	 * is vendor user exists for current tenant
	 * 
	 * 
	 * @param email
	 */
	@Transactional
	public boolean isVendorUserExists(String email) {
		User vendorUser = userRepository.findByEmailAndUserType(email, GlobalConstants.USER_TYPE_VENDOR);
		if (vendorUser != null)
			return true;
		return false;
	}

	/**
	 * is vendor user exists for current tenant
	 * 
	 * 
	 * @param email
	 */
	@Transactional
	public boolean getVendorUser(String email) {
		User vendorUser = userRepository.findByEmailAndUserType(email, GlobalConstants.USER_TYPE_VENDOR);
		if (vendorUser != null)
			return true;
		return false;
	}

	@Transactional
	public boolean isVendorAcitve(String email) {
		User vendorUser = userRepository.findByEmailAndUserType(email, GlobalConstants.USER_TYPE_VENDOR);
		Vendor vendor = vendorService.findOne(Long.parseLong(vendorUser.getVendorId()));
		if (vendor.getStatus())
			return true;
		return false;
	}

	/**
	 * Return bulk uploaded users using excel file
	 * 
	 * @param importFile
	 * @param headerMap
	 * @param loggedInUser
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public List<User> bulkUploadUsers(File importFile, Map<String, String> headerMap, User loggedInUser) throws RecruizException {

		final String inviteToSignup = GlobalConstants.INVITE_USER_TO_SIGNUP_EMAIL_TEMPLATE;
		final String inviteToJoinTemplate = GlobalConstants.INVITE_USER_TO_JOIN_EMAIL_TEMPLATE;

		List<User> bulkUserList = new LinkedList<User>();
		InternetAddress emailAddress;

		try {
			Workbook workbook;
			FileInputStream excelFile = new FileInputStream(importFile);

			String fileExtension = FilenameUtils.getExtension(importFile.getName());

			if ("xls".equals(fileExtension)) {
				// Get the workbook instance for XLS file
				workbook = new HSSFWorkbook(excelFile);
			} else {
				// Get the workbook instance for XLSX file
				workbook = new XSSFWorkbook(excelFile);
			}

			// Get first sheet from the workbook
			Sheet workSheet = workbook.getSheetAt(0);

			// Iterate through each rows from first sheet
			Iterator<Row> iterator = workSheet.iterator();

			Map<Integer, String> columnIndexMap = new LinkedHashMap<Integer, String>();
			// Get first row
			Row firstRow = workSheet.getRow(0);
			// get the first column index for a row
			short minColIx = firstRow.getFirstCellNum();
			// get the last column index for a row
			short maxColIx = firstRow.getLastCellNum();
			// loop from first to last index
			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				// get the cell
				Cell cell = firstRow.getCell(colIx);
				// add the cell index and cell contents (name of column) to the
				// map
				columnIndexMap.put(cell.getColumnIndex(), cell.getStringCellValue());
			}

			while (iterator.hasNext()) {
				Map<String, String> rowAsMap = new LinkedHashMap<String, String>();
				Row row = iterator.next();
				if (row.getRowNum() == 0) {
					continue;// skip first row, as it contains column names
				}

				// For each row, iterate through each columns
				for (int i = 0; i < row.getLastCellNum(); i++) {

					Cell cell = row.getCell(i);

					if (cell == null) {
						rowAsMap.put(columnIndexMap.get(i), null);
					} else {
						if (CellType.STRING == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), cell.getStringCellValue());
						} else if (CellType.NUMERIC == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), String.valueOf(cell.getNumericCellValue()));
						} else if (CellType.BOOLEAN == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), String.valueOf(cell.getBooleanCellValue()));
						} else if (CellType.BLANK == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), cell.getStringCellValue());
						}
					}
				}

				String userName = null;
				String email = rowAsMap.get(headerMap.get(UserFileHeaderConstant.Email_Address));
				if(email==null || email.isEmpty())
					continue;

				email = email.trim().toLowerCase();
				if (rowAsMap.get(headerMap.get(UserFileHeaderConstant.Name)) != null
						&& !rowAsMap.get(headerMap.get(UserFileHeaderConstant.Name)).isEmpty())
					userName = rowAsMap.get(headerMap.get(UserFileHeaderConstant.Name));
				UserRole userRole = null;
				if (rowAsMap.get(headerMap.get(UserFileHeaderConstant.Role_Name)) != null
						&& !rowAsMap.get(headerMap.get(UserFileHeaderConstant.Role_Name)).isEmpty()) {
					String roleName = rowAsMap.get(headerMap.get(UserFileHeaderConstant.Role_Name));
					userRole = userRoleService.getRolesByName(roleName);
				}
				// if no role present for id, assigning to normal user
				if (userRole == null || GlobalConstants.DEPARTMENT_HEAD_USER_ROLE.equals(userRole.getRoleName())
						|| GlobalConstants.VENDOR_ROLE.equals(userRole.getRoleName()))
					userRole = userRoleService.getRolesByName(GlobalConstants.NORMAL_USER_ROLE);
				try {
					boolean result = true;
					try {
						// checking email valid or not
						emailAddress = new InternetAddress(email);
						emailAddress.validate();
						// checking email domain is valid or not
						if (ValidateEmailDomain.isEmailDomainNotValid(email))
							result = false;
					} catch (AddressException e) {
						result = false;
					}
					if (result) {

						User userFromDB = getUserByEmail(email);
						User createdUser = null;
						String name;
						boolean isUserRegisteredWithRecruiz = tenantResolverService.emailExist(email);
						// below condition to check whether invite for
						// signup or
						// join
						if (isUserRegisteredWithRecruiz && userFromDB == null) {
							int index = email.indexOf("@");
							// below condition to check if name present]
							if (userName != null)
								name = userName;
							else
								name = email.substring(0, index);
							User existingUser = tenantResolverService.getUserByEmail(email);
							createdUser = create(email, existingUser.getName(), GlobalConstants.DEFAULT_PASSWORD,
									loggedInUser.getOrganization(), userRole, loggedInUser.getTimezone(), loggedInUser.getLocale(),
									UserType.APP.getDisplayName(), null);
							tenantResolverService.saveTenantResolverForUser(createdUser);
							inviteUser(inviteToJoinTemplate, inviteToJoinSubject, null, email, name);
						} else if (userFromDB == null) {
							int index = email.indexOf("@");
							// below condition to check if name present
							if (userName != null)
								name = userName;
							else
								name = email.substring(0, index);
							createdUser = create(email, name, GlobalConstants.DEFAULT_PASSWORD, loggedInUser.getOrganization(), userRole,
									loggedInUser.getTimezone(), loggedInUser.getLocale(), UserType.APP.getDisplayName(), null);
							inviteUserToSignup(inviteToSignup, inviteToSignupSubject, email, name,
									loggedInUser.getOrganization().getOrgId());
						}
						if (createdUser != null)
							bulkUserList.add(createdUser);
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}

			workbook.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.BULD_UPLAOD_FAILED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.BULD_UPLAOD_FAILED);
		}

		return bulkUserList;
	}

	@Transactional(readOnly = true)
	public long getUserCountByType(String userType) {
		return userRepository.countByUserType(userType);
	}

	@Transactional(readOnly = true)
	public long getVendorUserCount(String vendorId) {
		return userRepository.countByVendorId(vendorId);
	}

	@Transactional(readOnly = true)
	public long getVendorCount() {
		return userRepository.getVendorCount();
	}

	@Transactional(readOnly = true)
	public boolean isLicenceExpired() throws UnknownHostException, IOException, ParseException {
		return checkAppSettingsService.isValidityExpired();
	}

	@Transactional(readOnly = true)
	public boolean isUserExists(String email, String type) {
		User user = userRepository.findByEmailAndUserType(email, type);
		if (user == null)
			return false;
		return true;
	}

	/**
	 * This will return the list of venor user email ids, useful when sending
	 * email to all the vendor user
	 * 
	 * @param vendorId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<String> getAllUserEmailByVendor(String vendorId) {
		List<User> vendorUser = userRepository.findByUserTypeAndVendorIdAndAccountStatus(GlobalConstants.USER_TYPE_VENDOR, vendorId, true);
		List<String> vendorUserEmailIds = new ArrayList<>();
		if (vendorUser != null && !vendorUser.isEmpty()) {
			for (User usr : vendorUser) {
				vendorUserEmailIds.add(usr.getEmail());
			}
		}
		return vendorUserEmailIds;
	}

	/**
	 * check the user is active means joinedStatus, Acconunt status should be
	 * true and mark for delete should be false
	 * 
	 * @param user
	 * @return
	 */
	public boolean isActiveUser(User user) {
		if (user.getJoinedStatus() && user.getAccountStatus() && !user.getMarkForDelete())
			return true;
		else
			return false;
	}

	@Transactional(readOnly = true)
	public long getUserCountByJoinedStatus(boolean joinedStatus) {
		return userRepository.countByJoinedStatus(joinedStatus);
	}

	@Transactional(readOnly = true)
	public long getUserCountByAccountStatus(boolean accountStatus) {
		return userRepository.countByAccountStatus(accountStatus);
	}

	@Transactional(readOnly = true)
	public Collection<User> searchTextInNameOrEmailOrMobile(String searchText) {
		return userRepository.findAll(Specifications.where(JPAUtil.userContainsTextInNameOrEmailOrMobile(searchText)));
	}

	@Transactional(readOnly = true)
	public Page<User> getAllUsersForSearchIndex(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Set<User> getAllUserByPermissionName(String permissionName) throws RecruizException {
		Set<UserRole> permissionRoles = getAllRolesByPermissionName(permissionName);
		permissionRoles.add(userRoleService.getRolesByName(GlobalConstants.SUPER_ADMIN_USER_ROLE));
		Set<User> usersListByRoles = userRepository.findByUserRoleIn(permissionRoles);
		return usersListByRoles;
	}

	/**
	 * To get all roles using permission name
	 * 
	 * @param permissionName
	 * @throws RecruizException
	 */
	public Set<UserRole> getAllRolesByPermissionName(String permissionName) throws RecruizException {
		List<UserRole> roles = userRoleService.getAllRoles();
		Set<UserRole> permissionRoles = new HashSet<>();
		if (roles != null && !roles.isEmpty()) {
			for (UserRole userRole : roles) {
				for (Permission permission : userRole.getPermissions()) {
					if (permission.getPermissionName().equalsIgnoreCase(permissionName)) {
						permissionRoles.add(userRole);
						break;
					}
				}
			}
		}
		return permissionRoles;
	}

	public void resetUserPasswordByAdmin(String email, String password) throws RecruizException {
		try{
			User user = userRepository.findOneByEmail(email);
			User tenentUser = tenantResolverService.getUserByEmail(email);

			if(user!=null && tenentUser!=null){
				updatePassword(password, tenentUser);
				tenentUser = tenantResolverService.getUserByEmail(email);
				user.setPassword(tenentUser.getPassword());
				userRepository.save(user);

			}

		}catch(Exception e){
			logger.error("In resetUserPasswordByAdmin = "+e);
		}

	}

}