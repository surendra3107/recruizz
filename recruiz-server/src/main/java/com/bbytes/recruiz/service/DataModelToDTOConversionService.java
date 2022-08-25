package com.bbytes.recruiz.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.AgencyInvoicePaymentHistory;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateInvoice;
import com.bbytes.recruiz.domain.CandidateRating;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.OrganizationBankInfo;
import com.bbytes.recruiz.domain.OrganizationTaxDetails;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectContactInfo;
import com.bbytes.recruiz.domain.ProspectPosition;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.enums.ViewUsageType;
import com.bbytes.recruiz.enums.integration.SixthSenseSource;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TeamRepository;
import com.bbytes.recruiz.rest.dto.models.AgencyInvoicePaymentHistoryDTO;
import com.bbytes.recruiz.rest.dto.models.AgencyMultipleInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateInvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateRatingDTO;
import com.bbytes.recruiz.rest.dto.models.ExternalUserDTO;
import com.bbytes.recruiz.rest.dto.models.FolderDTO;
import com.bbytes.recruiz.rest.dto.models.HrExecutiveDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewPanelDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceSettingsDTO;
import com.bbytes.recruiz.rest.dto.models.OrganizationBankInfoDTO;
import com.bbytes.recruiz.rest.dto.models.OrganizationTaxDetailsDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectContactInfoDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectPostionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.TaskFolderDTO;
import com.bbytes.recruiz.rest.dto.models.TaskItemDTO;
import com.bbytes.recruiz.rest.dto.models.TaxCalculateDTO;
import com.bbytes.recruiz.rest.dto.models.TaxDTO;
import com.bbytes.recruiz.rest.dto.models.TaxRelatedDetailsDTO;
import com.bbytes.recruiz.rest.dto.models.TeamResponseDTO;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.rest.dto.models.UserProfileDTO;
import com.bbytes.recruiz.rest.dto.models.UserRoleDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseAdvanceSearchRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseDeleteUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalCredentialDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalManageResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalSourceDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserCredential;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class DataModelToDTOConversionService {

	private static Logger logger = LoggerFactory.getLogger(DataModelToDTOConversionService.class);

	private static final String SIXTH_SENSE_CHECK_VALUE = "on";

	@Autowired
	private BoardService boardService;

	@Autowired
	private FeedbackService feeedbackService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private AgencyInvoiceService agencyInvoiceService;

	@Autowired
	private ProspectPositionService prospectPositionService;

	@Autowired
	private TenantResolverService tenantResolverService;
	
	@Autowired
	private TeamRepository teamrepository;

	public BaseDTO convertToBaseDTO(String value) {
		BaseDTO baseDTO = new BaseDTO();
		baseDTO.setId(value);
		baseDTO.setValue(value);
		return baseDTO;
	}

	public BaseDTO convertToBaseDTOidValue(String value, String id) {
		BaseDTO baseDTO = new BaseDTO();
		baseDTO.setId(id);
		baseDTO.setValue(value);
		return baseDTO;
	}

	public List<BaseDTO> convertRolesToEntityDTOList(List<String> values) {
		List<BaseDTO> baseDTOList = new ArrayList<BaseDTO>();
		for (String value : values) {
			baseDTOList.add(convertToBaseDTO(value));
		}
		return baseDTOList;
	}

	/**
	 * <code>convertRolesToEntityDTOList</code> method returns id and value in
	 * list.
	 * 
	 * @param mapValues
	 * @return
	 */
	public List<BaseDTO> convertRolesToEntityDTOList(Map<String, String> mapValues) {
		List<BaseDTO> baseDTOList = new ArrayList<BaseDTO>();

		for (Map.Entry<String, String> entry : mapValues.entrySet()) {
			baseDTOList.add(convertToBaseDTOidValue(entry.getValue(), entry.getKey()));
		}
		return baseDTOList;
	}

	/**
	 * <code>convertUsers</code> method returns list of user which are converted
	 * by userDTO.
	 * 
	 * @param users
	 * @return
	 */
	public List<UserDTO> convertUsers(Collection<User> users) {
		List<UserDTO> userDTOList = new ArrayList<UserDTO>();
		if (users != null) {
			for (User user : users) {
				userDTOList.add(convertUser(user));
			}
		}
		return userDTOList;
	}

	/**
	 * <code>convertUsers</code> method returns list of user which are converted
	 * by userDTO.
	 * 
	 * @param users
	 * @return
	 */
	public Set<UserDTO> convertUsers(Set<User> users) {
		Set<UserDTO> userDTOList = new HashSet<UserDTO>();
		for (User user : users) {
			userDTOList.add(convertUser(user));
		}
		return userDTOList;
	}

	/**
	 * Convert user to dto for team members only
	 * 
	 * @param user
	 * @param teamTargetAmount
	 * @return
	 */
	public UserDTO convertTeamUser(User user, Long teamTargetAmount, Long teamTargetPositionOpeningClosure, String teamRole) {
		UserDTO userDto = convertUser(user);
		userDto.setTeamMemberTargetAmount(teamTargetAmount);
		userDto.setTeamTargetPositionOpeningClosure(teamTargetPositionOpeningClosure);
		userDto.setTeamRole(teamRole);
		return userDto;
	}

	/**
	 * <code>convertHrExecutive</code> method returns list of hr executive which
	 * are converted by userDTO.
	 * 
	 * @param users
	 * @return
	 */
	public List<HrExecutiveDTO> convertHrExecutives(Set<User> hrExecutive) {
		List<HrExecutiveDTO> hrExecutiveDTOList = new LinkedList<HrExecutiveDTO>();
		for (User user : hrExecutive) {
			hrExecutiveDTOList.add(convertHrExecutive(user));
		}
		// doing sorting here
		Collections.sort(hrExecutiveDTOList, new Comparator<HrExecutiveDTO>() {
			public int compare(HrExecutiveDTO dto1, HrExecutiveDTO dto2) {

				int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
				if (res == 0) {
					res = dto1.getName().compareTo(dto2.getName());
				}
				return res;
			}
		});
		return hrExecutiveDTOList;
	}

	/**
	 * <code>convertInterviewers</code> method returns interviewers which are
	 * converted by interviewerDTO.
	 * 
	 * @param user
	 * @return
	 */
	public List<InterviewPanelDTO> convertInterviewers(Set<ClientInterviewerPanel> interviewers) {
		List<InterviewPanelDTO> interviewPanelDTOList = new LinkedList<InterviewPanelDTO>();
		for (ClientInterviewerPanel interviewer : interviewers) {
			interviewPanelDTOList.add(convertInterviewer(interviewer));
		}

		// doing sorting here
		Collections.sort(interviewPanelDTOList, new Comparator<InterviewPanelDTO>() {
			public int compare(InterviewPanelDTO dto1, InterviewPanelDTO dto2) {

				int res = String.CASE_INSENSITIVE_ORDER.compare(dto1.getName(), dto2.getName());
				if (res == 0) {
					res = dto1.getName().compareTo(dto2.getName());
				}
				return res;
			}
		});

		return interviewPanelDTOList;
	}

	/**
	 * <code>convertHrExecutive</code> method returns hr executive which are
	 * converted by HrExecutiveDTO.
	 * 
	 * @param user
	 * @return
	 */
	public HrExecutiveDTO convertHrExecutive(User hrExecutive) {
		HrExecutiveDTO hrExecutiveDTO = new HrExecutiveDTO();
		hrExecutiveDTO.setId(hrExecutive.getUserId());
		hrExecutiveDTO.setName(hrExecutive.getName());
		hrExecutiveDTO.setEmail(hrExecutive.getEmail());
		hrExecutiveDTO.setMobile(hrExecutive.getMobile());
		hrExecutiveDTO.setSelectedStatus(true);
		return hrExecutiveDTO;
	}

	/**
	 * <code>convertExternalUser</code> method returns external user info which
	 * are converted by ExternalUserDTO.
	 * 
	 * @param user
	 * @return
	 */
	public ExternalUserDTO convertExternalUser(ExternalUserAccessDataHolder externalUser) {
		ExternalUserDTO externalUserDTO = new ExternalUserDTO();
		externalUserDTO.setUserName(externalUser.getExtenalUserName());
		externalUserDTO.setEmail(externalUser.getExtenalUserEmail());
		externalUserDTO.setMobile(externalUser.getExtenalUserMobile());
		return externalUserDTO;
	}

	/**
	 * <code>convertInterviewer</code> method returns interviewer which are
	 * converted by interviewerDTO.
	 * 
	 * @param user
	 * @return
	 */
	public InterviewPanelDTO convertInterviewer(ClientInterviewerPanel interviewer) {
		InterviewPanelDTO interviewPanelDTO = new InterviewPanelDTO();
		interviewPanelDTO.setId(interviewer.getId());
		interviewPanelDTO.setName(interviewer.getName());
		interviewPanelDTO.setEmail(interviewer.getEmail());
		interviewPanelDTO.setMobile(interviewer.getMobile());
		interviewPanelDTO.setSelectedStatus(true);
		return interviewPanelDTO;
	}

	public UserDTO convertUser(User user) {
		return convertUser(user, false);
	}

	/**
	 * <code>convertUser</code> method returns user which are converted by
	 * userDTO.
	 * 
	 * @param user
	 * @return
	 */
	public UserDTO convertUser(User user, boolean profileVersion) {
		UserDTO userDTO = new UserDTO();
		if (profileVersion) {
			userDTO = new UserProfileDTO();
		}
		userDTO.setId(user.getUserId());
		userDTO.setEmail(user.getEmail());
		userDTO.setUserName(user.getName());
		userDTO.setMobile(user.getMobile());
		try {
			List<Team> teams = teamService.getAllTeamsForUser(user);
			
			if (teams != null) {
				
				for (Team team : teams) {
					userDTO.getTeamIds().add(team.getId());
					
				}
				
			}
		} catch (RecruizException e1) {
			// do nothing
		}
		
		try {
			List<Team> ownteam = teamService.getOwnTeamOfUser(user);
			
			HashMap<Long,String> teamList=new HashMap<Long,String>();
			HashMap<Long,String> reportingToTeamList=new HashMap<Long,String>();
			

			if (teamList != null) {
				
				for (Team team : ownteam) {
					userDTO.getTeamIds().add(team.getId());
					teamList.put(team.getId(), team.getTeamName());
					
					if(team.getParent() != null)
					{	
						reportingToTeamList.put(team.getId(), team.getParent().getTeamName());
						
					}
				}
				
				userDTO.setUserTeams(teamList);	
				userDTO.setUserReportingToTeams(reportingToTeamList);
			}
			
		} catch (RecruizException e) {
			// do nothing
		}

		// checking if user is of type vendor then is vendor active or not
		if (GlobalConstants.USER_TYPE_VENDOR.equalsIgnoreCase(user.getUserType())) {
			try {
				if (userService.getLoggedInUserVendor().getStatus() && user.getAccountStatus()) {
					userDTO.setAccountStatus(true);
				} else {
					userDTO.setAccountStatus(false);
				}
			} catch (RecruizException e) {
				logger.error(e.getMessage(), e.getCause());
			}
		} else {
			userDTO.setAccountStatus(user.getAccountStatus());
		}
		userDTO.setJoinedStatus(user.getJoinedStatus());
		userDTO.setJoinedDate(user.getJoinedDate());
		userDTO.setRole(user.getUserRole());
		userDTO.setTimeZone(user.getTimezone());
		userDTO.setDesignation(user.getDesignation());
		userDTO.setLocale(user.getLocale());
		userDTO.setType(user.getUserType());
		userDTO.setOrgType(user.getOrganization().getOrgType());
		if (userDTO.getRole() != null)
			userDTO.getRole().getPermissions().size();
		userDTO.setOrgId(user.getOrganization().getOrgId());
		userDTO.setOrgName(user.getOrganization().getOrgName());
		userDTO.setOrgMarkForDelete(user.getOrganization().getMarkForDelete());
		userDTO.setProfileSignature(user.getProfileSignature());
		if (user.getOrganization().getMarkForDeleteDate() != null)
			userDTO.setOrgMarkDeleteDate(user.getOrganization().getMarkForDeleteDate());

		if (user.getUserRole() != null) {
			userDTO.setUserRole(convertToBaseDTOidValue(user.getUserRole().getRoleName(), user.getUserRole().getId() + ""));
		}
		userDTO.setIsNotificationEnabled(user.getIsNotificationOn());
		// checking sixth sense user configure or not
		SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(user.getEmail());
		userDTO.setJobPortalEnable(sixthSenseUser == null ? false : true);
		userDTO.setUsageType((sixthSenseUser != null && sixthSenseUser.getUsageType() != null) ? sixthSenseUser.getUsageType()
				: ViewUsageType.UNLIMITED_VIEW.toString());
		userDTO.setViewCount((sixthSenseUser != null && sixthSenseUser.getViewCount() > 0) ? sixthSenseUser.getViewCount()
				: ViewUsageType.UNLIMITED_VIEW.getNumberOfDays());
		userDTO.setSelectedSources(sixthSenseUser != null && sixthSenseUser.getSources() != null && !sixthSenseUser.getSources().isEmpty()
				? new HashSet<String>(StringUtils.commaSeparateStringToList(sixthSenseUser.getSources()))
				: new HashSet<String>());
		return userDTO;
	}

	public UserProfileDTO convertUser(User user, String updatedToken) {
		UserProfileDTO userProfileDTO = (UserProfileDTO) convertUser(user, true);
		userProfileDTO.setUpdatedXAuthToken(updatedToken);

		return userProfileDTO;
	}

	public Map<String, Object> getResponseMapWithGridDataAndUserStatusCount(List<User> users) {
		List<UserDTO> userDTOList = new ArrayList<UserDTO>();
		long joinedCount = 0;
		long pendingCount = 0;
		long activeUser = 0;
		long deactivatedUser = 0;
		for (User user : users) {
			if (user.getAccountStatus()) {
				activeUser++;
			} else {
				deactivatedUser++;
			}
			if (user.getJoinedStatus()) {
				joinedCount++;
			} else {
				pendingCount++;
			}
			userDTOList.add(convertUser(user));
			
			//List<Team> teamlist = teamrepository.findByMembers(user);
			
			//System.out.println("User is --> " +user.getEmail()   +"Team detail " +teamrepository.findByMembers(user));
			/*
			for (Team team : teamlist)
			{
				System.out.println("Team Name -- " +team.getTeamName());
				System.out.println("Parent Team Name --" +team.getParent());
				
			}
			*/
			
			
			
		}
		return getResponseMapWithGridDataAndUserStatusCount(activeUser, deactivatedUser, joinedCount, pendingCount, userDTOList);
	}

	public Map<String, Object> getAllUser(List<User> users) {
		List<UserDTO> userDTOList = new ArrayList<UserDTO>();
		for (User user : users) {
			userDTOList.add(convertUser(user));
		}
		Map<String, Object> responseData = new LinkedHashMap<String, Object>();
		responseData.put(RestResponse.GRID_DATA, userDTOList);
		return responseData;
	}

	private Map<String, Object> getResponseMapWithGridDataAndUserStatusCount(long activeUser, long deactivatedUser, long joinedCount,
			long pendingCount, List<UserDTO> gridData) {
		Map<String, Object> responseData = new LinkedHashMap<String, Object>();
		responseData.put(RestResponseConstant.ACTIVE_USER_COUNT, activeUser);
		responseData.put(RestResponseConstant.JOINED_USERS_COUNT, joinedCount);
		responseData.put(RestResponseConstant.PENDING_USERS_COUNT, pendingCount);
		responseData.put(RestResponseConstant.DEACTIVATED_USER, deactivatedUser);
		responseData.put(RestResponse.GRID_DATA, gridData);
		return responseData;
	}

	public UserRoleDTO convertUserRoleDTO(UserRole roles) {
		UserRoleDTO roleDTO = new UserRoleDTO();
		roleDTO.setId(roles.getId() + "");
		roleDTO.setRoleName(roles.getRoleName());
		roleDTO.setPermissions(roles.getPermissions());
		return roleDTO;
	}

	public Map<String, Object> getAllRoles(List<UserRole> userRoles) {
		List<UserRoleDTO> userRoleDTOs = new ArrayList<UserRoleDTO>();
		for (UserRole roles : userRoles) {
			if (!roles.getRoleName().equalsIgnoreCase(GlobalConstants.VENDOR_ROLE)
					&& !roles.getRoleName().equalsIgnoreCase(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE)) {
				userRoleDTOs.add(convertUserRoleDTO(roles));
			}
		}
		Map<String, Object> responseData = new LinkedHashMap<String, Object>();
		responseData.put(RestResponse.GRID_DATA, userRoleDTOs);
		return responseData;
	}

	public TaskItemDTO convertTaskItem(TaskItem item) {
		TaskItemDTO taskItemDTO = new TaskItemDTO();
		taskItemDTO.setId(item.getId());
		taskItemDTO.setName(item.getName());
		taskItemDTO.setNotes(item.getNotes());
		taskItemDTO.setOwner(convertUser(item.getOwner()));
		taskItemDTO.setDueDateTime(item.getDueDateTime());
		taskItemDTO.setReminderDateTime(item.getReminderDateTime());
		taskItemDTO.setReminderPeriodType(item.getReminderPeriodType());
		taskItemDTO.setReminderPeriod(item.getReminderPeriod());
		taskItemDTO.setState(item.getState());
		taskItemDTO.setUsers(convertUsers(item.getUsers()));

		TaskFolderDTO taskFolderDTO = new TaskFolderDTO();
		taskFolderDTO.setId(item.getTaskFolder().getId());
		taskFolderDTO.setName(item.getTaskFolder().getName());
		taskFolderDTO.setId(item.getTaskFolder().getId());
		taskItemDTO.setTaskFolder(taskFolderDTO);

		return taskItemDTO;
	}

	public Set<TaskItemDTO> convertTaskItem(Set<TaskItem> taskItems) {
		Set<TaskItemDTO> taskItemDTOs = new HashSet<>();
		for (TaskItem taskItem : taskItems) {
			taskItemDTOs.add(convertTaskItem(taskItem));
		}
		return taskItemDTOs;
	}

	public TaskFolderDTO convertTaskFolder(TaskFolder folder) {
		TaskFolderDTO taskFolderDTO = new TaskFolderDTO();
		taskFolderDTO.setId(folder.getId());
		taskFolderDTO.setName(folder.getName());
		taskFolderDTO.setOwner(convertUser(folder.getOwner()));
		taskFolderDTO.setTaskItems(convertTaskItem(folder.getTaskItems()));
		return taskFolderDTO;
	}

	public Set<TaskFolderDTO> convertTaskFolder(Set<TaskFolder> taskFolders) {
		Set<TaskFolderDTO> taskFolderDTOs = new HashSet<>();
		for (TaskFolder taskFolder : taskFolders) {
			taskFolderDTOs.add(convertTaskFolder(taskFolder));
		}

		return taskFolderDTOs;
	}

	/**
	 * This method will convert candidate information into candidateDTO which
	 * will need for board candidate card
	 * 
	 * @param roundCandidate
	 * @param round
	 * @return
	 */
	public RoundCandidateDTO convertRoundCandidate(RoundCandidate roundCandidate, Round round) {

		Candidate candidate = roundCandidate.getCandidate();

		RoundCandidateDTO candidateDTO = new RoundCandidateDTO();
		candidateDTO.setRoundCandidateId(roundCandidate.getId() + "");
		candidateDTO.setEmail(candidate.getEmail());
		candidateDTO.setId(candidate.getCid() + "");
		candidateDTO.setLocation(candidate.getCurrentLocation());
		candidateDTO.setName(candidate.getFullName());
		candidateDTO.setMobile(candidate.getMobile());
		candidateDTO.setResumeLink(candidate.getResumeLink());
		candidateDTO.setStatus(roundCandidate.getStatus());
		candidateDTO.setTotalExpRange(boardService.getExperinceRange(candidate.getTotalExp()));
		candidateDTO.setExpectedCtcRange(boardService.getExpectedCtcRange(candidate.getExpectedCtc()));
		candidateDTO.setNoticePeriodRange(boardService.getNoticePeriodRange(candidate.getNoticePeriod(), candidate.isNoticeStatus()));
		candidateDTO.setSourcedFrom(candidate.getSource());
		candidateDTO.setCardIndex(roundCandidate.getCardIndex());
		candidateDTO.setEmploymentType(candidate.getEmploymentType());
		// adding source information to card
		if (candidate.getSource().equalsIgnoreCase("Vendor")) {
			candidateDTO.setSourcedFrom(candidate.getSource() + " (" + candidate.getSourceName() + ")");
		}

		String roundId = String.valueOf(round.getId());
		String roundCandidateId = roundCandidate.getId() + "";
		String totalApproved = feeedbackService.getFeedbackCountByStatus(roundId, "Approved", roundCandidateId);
		candidateDTO.setTotalApproved(totalApproved);
		candidateDTO.setTotalExpectedFeedback(feeedbackService.getTotalFeedbackCount(roundId + "", roundCandidateId));
		candidateDTO.setTotalOnHold(feeedbackService.getFeedbackCountByStatus(roundId, "OnHold", roundCandidateId));
		candidateDTO.setTotalRejected(feeedbackService.getFeedbackCountByStatus(roundId, "Rejected", roundCandidateId));
		int totalFeedbackReceived = Integer.parseInt(candidateDTO.getTotalApproved()) + Integer.parseInt(candidateDTO.getTotalOnHold())
				+ Integer.parseInt(candidateDTO.getTotalRejected());
		candidateDTO.setTotalFeedbackReceived(String.valueOf(totalFeedbackReceived));

		InterviewSchedule schedule = interviewScheduleService.getScheduleByPositionCodeRoundEmail(roundCandidate.getPositionCode(),
				roundCandidate.getRound().getId() + "", candidate.getEmail());
		if (schedule != null && schedule.isActive()) {
			candidateDTO.setInterviewScheduled(true);
			candidateDTO.setScheduleDate(schedule.getStartsAt());
			candidateDTO.setScheduledBy(schedule.getInterviewSchedulerName());
			candidateDTO.setScheduledByEmail(schedule.getInterviewSchedulerEmail());
		} else {
			candidateDTO.setInterviewScheduled(false);
			candidateDTO.setScheduleDate(null);
		}
		return candidateDTO;
	}

	public ProspectDTO convertProspect(Prospect prospect) {

		ProspectDTO prospectDTO = new ProspectDTO();
		prospectDTO.setProspectId(prospect.getProspectId());
		prospectDTO.setCompanyName(prospect.getCompanyName());
		prospectDTO.setName(prospect.getName());
		prospectDTO.setMobile(prospect.getMobile());
		prospectDTO.setEmail(prospect.getEmail());
		prospectDTO.setOwner(prospect.getOwner());
		prospectDTO.setDesignation(prospect.getDesignation());
		prospectDTO.setLocation(prospect.getLocation());
		prospectDTO.setAddress(prospect.getAddress());
		prospectDTO.setSource(prospect.getSource());
		prospectDTO.setWebsite(prospect.getWebsite());
		prospectDTO.setStatus(prospect.getStatus());
		prospectDTO.setReason(prospect.getReason());
		prospectDTO.setIndustry(prospect.getIndustry());
		prospectDTO.setCategory(prospect.getCategory());
		prospectDTO.setProspectRating(prospect.getProspectRating());
		prospectDTO.setDealSize(prospect.getDealSize());
		prospectDTO.setPercentage(prospect.getPercentage());
		prospectDTO.setValue(prospect.getValue());
		prospectDTO.setCurrency(prospect.getCurrency());
		prospectDTO.setMode(prospect.getMode());
		prospectDTO.setCreationDate(prospect.getCreationDate());
		prospectDTO.setModificationDate(prospect.getModificationDate());

		Set<ProspectContactInfoDTO> prospectContactInfoDTOs = new HashSet<ProspectContactInfoDTO>();
		Set<ProspectPostionDTO> prospectPostionDTOs = new HashSet<ProspectPostionDTO>();

		if (prospect.getProspectContactInfo() != null) {

			for (ProspectContactInfo prospectContactInfo : prospect.getProspectContactInfo()) {
				ProspectContactInfoDTO prospectContactInfoDTO = new ProspectContactInfoDTO();

				prospectContactInfoDTO.setId(prospectContactInfo.getId());
				if (prospectContactInfo.getName() != null) {
					prospectContactInfoDTO.setName(prospectContactInfo.getName());
				}
				if (prospectContactInfo.getEmail() != null) {
					prospectContactInfoDTO.setEmail(prospectContactInfo.getEmail());
				}
				if (prospectContactInfo.getMobile() != null) {
					prospectContactInfoDTO.setMobile(prospectContactInfo.getMobile());
				}

				if (prospectContactInfo.getDesignation() != null) {
					prospectContactInfoDTO.setDesignation(prospectContactInfo.getDesignation());
				}

				prospectContactInfoDTOs.add(prospectContactInfoDTO);
			}

		}

		if (prospect.getProspectPositions() != null && !prospect.getProspectPositions().isEmpty()) {
			for (ProspectPosition prospectPosition : prospect.getProspectPositions()) {
				prospectPostionDTOs.add(convertProspectPosition(prospectPosition));
			}
		}

		if (prospect.getCustomField() != null && !prospect.getCustomField().isEmpty()) {
			prospectDTO.getCustomField().putAll(prospect.getCustomField());
		}

		if (!prospectPostionDTOs.isEmpty()) {
			prospectDTO.setProspectPostionDTOs(prospectPostionDTOs);
		}
		prospectDTO.setProspectContactInfo(prospectContactInfoDTOs);
		return prospectDTO;
	}

	public List<ProspectPostionDTO> convertProspectPosition(List<ProspectPosition> prospectPositions) {
		List<ProspectPostionDTO> prospectPostionDTOs = new ArrayList<ProspectPostionDTO>();
		for (ProspectPosition prospectPosition : prospectPositions) {
			prospectPostionDTOs.add(convertProspectPosition(prospectPosition));
		}
		return prospectPostionDTOs;
	}

	public ProspectPostionDTO convertProspectPosition(ProspectPosition prospectPosition) {
		ProspectPostionDTO prospectPostionDTO = new ProspectPostionDTO();
		if (prospectPosition != null) {
			if (prospectPosition.getPositionName() != null) {
				prospectPostionDTO.setPositionName(prospectPosition.getPositionName());
			}
			prospectPostionDTO.setPercentage(prospectPosition.getPercentage());
			prospectPostionDTO.setValue(prospectPosition.getValue());
			prospectPostionDTO.setPositionId(prospectPosition.getPositionId());

			if (prospectPosition.getKeySkills() != null && !prospectPosition.getKeySkills().isEmpty()) {
				prospectPostionDTO.setKeySkills(prospectPosition.getKeySkills());
			}

			if (prospectPosition.getLocation() != null && !prospectPosition.getLocation().isEmpty()) {
				prospectPostionDTO.setLocation(prospectPositionService.convertSetToString(prospectPosition.getLocation()));
			}

			if (prospectPosition.getEducationQualification() != null && !prospectPosition.getEducationQualification().isEmpty()) {
				prospectPostionDTO.setEducationQualification(prospectPosition.getEducationQualification());
			}

			if (prospectPosition.getClosureDate() != null) {
				prospectPostionDTO.setClosureDate(prospectPosition.getClosureDate());
			}

			prospectPostionDTO.setNumberOfOpenings(prospectPosition.getNumberOfOpenings());
			prospectPostionDTO.setMinExperience(prospectPosition.getMinExperience());
			prospectPostionDTO.setMaxExperience(prospectPosition.getMaxExperience());
			prospectPostionDTO.setType(prospectPosition.getType());
			prospectPostionDTO.setRemoteWork(prospectPosition.isRemoteWork());
			prospectPostionDTO.setMinSal(prospectPosition.getMinSal());
			prospectPostionDTO.setMaxSal(prospectPosition.getMaxSal());
			prospectPostionDTO.setIndustry(prospectPosition.getIndustry());
			prospectPostionDTO.setFunctionalArea(prospectPosition.getFunctionalArea());
			prospectPostionDTO.setClientName(prospectPosition.getClientName());
			prospectPostionDTO.setStatus(prospectPosition.getStatus());
			prospectPostionDTO.setCurrency(prospectPosition.getCurrency());
			prospectPostionDTO.setCreationDate(prospectPosition.getCreationDate());
			return prospectPostionDTO;
		} else {
			return null;
		}
	}

	public AgencyMultipleInvoiceDTO convertAgencyInvoice(AgencyInvoice agencyInvoice) throws RecruizException {

		AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO = new AgencyMultipleInvoiceDTO();
		agencyMultipleInvoiceDTO.setInvoiceId(agencyInvoice.getId());
		// it is for Ui
		agencyMultipleInvoiceDTO.setId(agencyInvoice.getInvoiceId());
		agencyMultipleInvoiceDTO.setInvoiceNumber(agencyInvoice.getInvoiceNumber());
		agencyMultipleInvoiceDTO.setClientName(agencyInvoice.getClientName());
		agencyMultipleInvoiceDTO.setClientId(agencyInvoice.getClientId());
		agencyMultipleInvoiceDTO.setInvoiceStatus(agencyInvoice.getInvoiceStatus());
		agencyMultipleInvoiceDTO.setDueDate(agencyInvoice.getDueDate());
		agencyMultipleInvoiceDTO.setCurrency(agencyInvoice.getCurrency());
		agencyMultipleInvoiceDTO.setDelayDay(agencyInvoice.getDelayDay());
		agencyMultipleInvoiceDTO.setSubTotal(agencyInvoice.getAmount());
		agencyMultipleInvoiceDTO.setPaymentReceived(agencyInvoice.getPaymentReceived());
		// agencyMultipleInvoiceDTO.setTotalAmountInWords(agencyInvoice.getTotalAmountInWords());
		agencyMultipleInvoiceDTO.setInvoiceGeneratedDate(agencyInvoice.getCreationDate());
		agencyMultipleInvoiceDTO.setPaymentReceivedDate(agencyInvoice.getPaymentReceivedDate());

		agencyMultipleInvoiceDTO.setDiscount(agencyInvoice.getDiscount());
		agencyMultipleInvoiceDTO.setDiscountValue(
				Double.parseDouble(new DecimalFormat("##.##").format(agencyInvoice.getAmount() * agencyInvoice.getDiscount() / 100)));
		agencyMultipleInvoiceDTO.setOrgLogoUrl(agencyInvoiceService.getOrgLogoUrl());
		agencyMultipleInvoiceDTO.setPendingAmount(agencyInvoice.getPendingAmount());
		agencyMultipleInvoiceDTO.setTotalAmount(agencyInvoice.getTotalAmount());
		agencyMultipleInvoiceDTO.setTotalAmountAfterDiscount(agencyInvoice.getTotalAmountAfterDiscount());
		agencyMultipleInvoiceDTO.setCandidateInvoices(convertCandidateInvoice(agencyInvoice.getCandidateInvoices()));
		agencyMultipleInvoiceDTO.setOrganizationName(agencyInvoice.getOrganizationName());
		agencyMultipleInvoiceDTO.setOrganization_address_1(agencyInvoice.getOrganization_address_1());
		agencyMultipleInvoiceDTO.setOrganization_address_2(agencyInvoice.getOrganization_address_2());
		agencyMultipleInvoiceDTO.setOrganizationCity(agencyInvoice.getOrganizationCity());
		agencyMultipleInvoiceDTO.setOrganizationState(agencyInvoice.getOrganizationState());
		agencyMultipleInvoiceDTO.setOrganizationCountry(agencyInvoice.getOrganizationCountry());
		agencyMultipleInvoiceDTO.setOrganizationPin(agencyInvoice.getOrganizationPin());
		agencyMultipleInvoiceDTO.setOrganizationPhone(agencyInvoice.getOrganizationPhone());
		agencyMultipleInvoiceDTO.setChequePayable(agencyInvoice.getChequePayable());
		agencyMultipleInvoiceDTO.setOrganizationAccountName(agencyInvoice.getOrganizationAccountName());
		agencyMultipleInvoiceDTO.setOrganizationAccountNumber(agencyInvoice.getOrganizationAccountNumber());
		agencyMultipleInvoiceDTO.setOrganizationBankName(agencyInvoice.getOrganizationBankName());
		agencyMultipleInvoiceDTO.setOrganizationBankBranchName(agencyInvoice.getOrganizationBankBranchName());
		agencyMultipleInvoiceDTO.setOrganizationBankIfsc(agencyInvoice.getOrganizationBankIfsc());
		agencyMultipleInvoiceDTO.setNote(agencyInvoice.getNote());
		agencyMultipleInvoiceDTO.setBillClientName(agencyInvoice.getBillClientName());
		agencyMultipleInvoiceDTO.setBillContactName(agencyInvoice.getBillContactName());
		agencyMultipleInvoiceDTO.setBill_address_1(agencyInvoice.getBill_address_1());
		agencyMultipleInvoiceDTO.setBill_address_2(agencyInvoice.getBill_address_2());
		agencyMultipleInvoiceDTO.setBillCity(agencyInvoice.getBillCity());
		agencyMultipleInvoiceDTO.setBillState(agencyInvoice.getBillState());
		agencyMultipleInvoiceDTO.setBillCountry(agencyInvoice.getBillCountry());
		agencyMultipleInvoiceDTO.setBillPin(agencyInvoice.getBillPin());
		agencyMultipleInvoiceDTO.setBillPhone(agencyInvoice.getBillPhone());
		// agencyMultipleInvoiceDTO.setTaxDetails(agencyInvoice.getTaxDetails());
		agencyMultipleInvoiceDTO.setTaxCalculateDTOs(convertTaxCalculate(agencyInvoice.getTaxDetails(), agencyInvoice.getAmount()));
		agencyMultipleInvoiceDTO.setGstAndPan(convertTaxRelatedDetails(agencyInvoice.getTaxRelatedDetails()));

		// clientId
		// positionCode
		// candidateId

		return agencyMultipleInvoiceDTO;

	}

	public List<CandidateInvoiceDTO> convertCandidateInvoice(Set<CandidateInvoice> candidateInvoices) {
		List<CandidateInvoiceDTO> candidateInvoiceDTOs = new ArrayList<CandidateInvoiceDTO>();
		for (CandidateInvoice candidateInvoice : candidateInvoices) {
			CandidateInvoiceDTO candidateInvoiceDTO = new CandidateInvoiceDTO(candidateInvoice.getId(), candidateInvoice.getCandidateName(),
					candidateInvoice.getCandidateEmail(), candidateInvoice.getPostionName(), candidateInvoice.getPositionCode(),
					candidateInvoice.getJoiningDate());
			// candidateInvoiceDTO.setPercentage(candidateInvoice.getPercentage());
			// candidateInvoiceDTO.setValue(candidateInvoice.getValue());
			candidateInvoiceDTO.setAmount(candidateInvoice.getAmount());
			candidateInvoiceDTO.setClientId(candidateInvoice.getClientId());
			candidateInvoiceDTO.setClientName(candidateInvoice.getClientName());
			candidateInvoiceDTOs.add(candidateInvoiceDTO);
		}
		return candidateInvoiceDTOs;
	}

	public List<AgencyMultipleInvoiceDTO> convertAgencyInvoice(List<AgencyInvoice> agencyInvoices) throws RecruizException {
		List<AgencyMultipleInvoiceDTO> agencyInvoiceDTOs = new ArrayList<AgencyMultipleInvoiceDTO>();
		for (AgencyInvoice agencyInvoice : agencyInvoices) {
			AgencyMultipleInvoiceDTO agencyMultipleInvoiceDTO = convertAgencyInvoice(agencyInvoice);
			agencyInvoiceDTOs.add(agencyMultipleInvoiceDTO);
		}
		return agencyInvoiceDTOs;
	}

	// to create agency invoice dto from candidate status
	public CandidateInvoiceDTO convertCandidateStatusToAgencyInvoiceDTO(CandidateStatus candidateStaus) throws RecruizException {
		CandidateInvoiceDTO candidateInvoiceDTO = new CandidateInvoiceDTO();
		Position position = candidateStaus.getPosition();
		Candidate candidate = candidateStaus.getCandidate();
		Client client = candidateStaus.getClient();
		candidateInvoiceDTO.setCandidateEmail(candidate.getEmail());
		candidateInvoiceDTO.setCandidateName(candidate.getFullName());
		candidateInvoiceDTO.setClientName(client.getClientName());
		candidateInvoiceDTO.setPostionName(position.getTitle());
		candidateInvoiceDTO.setPositionCode(position.getPositionCode());
		// candidateInvoiceDTO.setOfferedDate(candidateStatusService.getOfferedDate(client.getId(),
		// position.getPositionCode(), candidate.getCid(),
		// BoardStatus.Offered.getDisplayName()));
		candidateInvoiceDTO.setJoiningDate(candidateStaus.getStatusChangedDate());
		return candidateInvoiceDTO;

	}

	public AgencyInvoicePaymentHistoryDTO converAgencyInvoicePaymentHistory(AgencyInvoicePaymentHistory agencyInvoicePaymentHistory)
			throws RecruizException {
		List<CandidateInvoiceDTO> candidateInvoiceDTOs = null;
		Set<AgencyInvoicePaymentHistory> agencyInvoicePaymentHistories = new HashSet<AgencyInvoicePaymentHistory>();
		agencyInvoicePaymentHistories.add(agencyInvoicePaymentHistory);
		AgencyInvoice agencyInvoice = agencyInvoiceService.getInvoiceByPayemntHistory(agencyInvoicePaymentHistories);
		if (agencyInvoice.getCandidateInvoices() != null) {
			Set<CandidateInvoice> candidateInvoices = agencyInvoice.getCandidateInvoices();
			candidateInvoiceDTOs = convertCandidateInvoice(candidateInvoices);
		}

		AgencyInvoicePaymentHistoryDTO agencyInvoicePaymentHistoryDTO = new AgencyInvoicePaymentHistoryDTO();
		agencyInvoicePaymentHistoryDTO.setHistoryId(agencyInvoicePaymentHistory.getId());
		agencyInvoicePaymentHistoryDTO.setPaymentDate(agencyInvoicePaymentHistory.getPaymentDate());
		agencyInvoicePaymentHistoryDTO.setTotalAmount(agencyInvoicePaymentHistory.getTotalAmount());
		agencyInvoicePaymentHistoryDTO.setRecivedAmount(agencyInvoicePaymentHistory.getRecivedAmount());
		agencyInvoicePaymentHistoryDTO.setCurrency(agencyInvoicePaymentHistory.getCurrency());
		agencyInvoicePaymentHistoryDTO.setCreationDate(agencyInvoicePaymentHistory.getCreationDate());
		agencyInvoicePaymentHistoryDTO.setModificationDate(agencyInvoicePaymentHistory.getModificationDate());
		agencyInvoicePaymentHistoryDTO.setClientName(agencyInvoice.getClientName());
		if (candidateInvoiceDTOs != null) {
			agencyInvoicePaymentHistoryDTO.setCandidateInvoices(candidateInvoiceDTOs);
		}
		return agencyInvoicePaymentHistoryDTO;
	}

	public List<AgencyInvoicePaymentHistoryDTO> converAgencyInvoicePaymentHistory(
			List<AgencyInvoicePaymentHistory> agencyInvoicePaymentHistorys) throws RecruizException {
		double receivedAmount = 0;
		List<AgencyInvoicePaymentHistoryDTO> agencyInvoicePaymentHistoryDTOs = new LinkedList<AgencyInvoicePaymentHistoryDTO>();
		for (AgencyInvoicePaymentHistory agencyInvoicePaymentHistory : agencyInvoicePaymentHistorys) {
			AgencyInvoicePaymentHistoryDTO agencyInvoicePaymentHistoryDTO = converAgencyInvoicePaymentHistory(agencyInvoicePaymentHistory);
			receivedAmount = receivedAmount + agencyInvoicePaymentHistoryDTO.getRecivedAmount();

			Float pendingAmountInFloat = (float) (agencyInvoicePaymentHistoryDTO.getTotalAmount() - receivedAmount);
			agencyInvoicePaymentHistoryDTO.setPendingAmount(pendingAmountInFloat);
			agencyInvoicePaymentHistoryDTOs.add(agencyInvoicePaymentHistoryDTO);
		}
		return agencyInvoicePaymentHistoryDTOs;
	}

	public OrganizationBankInfoDTO convertOrganizationBankDetails(OrganizationBankInfo organizationBankInfo) throws RecruizException {

		OrganizationBankInfoDTO organizationBankInfoDTO = new OrganizationBankInfoDTO();
		organizationBankInfoDTO.setId(organizationBankInfo.getId());
		organizationBankInfoDTO.setAccountNumber(organizationBankInfo.getAccountNumber());
		organizationBankInfoDTO.setBankName(organizationBankInfo.getBankName());
		organizationBankInfoDTO.setAccountName(organizationBankInfo.getAccountName());
		organizationBankInfoDTO.setBranch(organizationBankInfo.getBranch());
		organizationBankInfoDTO.setIfscCode(organizationBankInfo.getIfscCode());
		organizationBankInfoDTO.setCreationDate(organizationBankInfo.getCreationDate());
		organizationBankInfoDTO.setModificationDate(organizationBankInfo.getModificationDate());
		if (organizationBankInfo.getDefaultBankDetails() == null) {
			organizationBankInfoDTO.setDefaultBankDetails(false);
		} else {
			organizationBankInfoDTO.setDefaultBankDetails(organizationBankInfo.getDefaultBankDetails());
		}

		return organizationBankInfoDTO;
	}

	public List<OrganizationBankInfoDTO> convertOrganizationBankDetails(List<OrganizationBankInfo> organizationBankInfos)
			throws RecruizException {
		List<OrganizationBankInfoDTO> organizationBankInfoDTOs = new ArrayList<OrganizationBankInfoDTO>();
		for (OrganizationBankInfo organizationBankInfo : organizationBankInfos) {
			organizationBankInfoDTOs.add(convertOrganizationBankDetails(organizationBankInfo));
		}
		return organizationBankInfoDTOs;
	}

	public CandidateRatingDTO convertCandidateRating(CandidateRating candidateRating) {
		CandidateRatingDTO candidateRatingDTO = new CandidateRatingDTO();
		candidateRatingDTO.setCandidateEmail(candidateRating.getCandidate().getEmail());
		candidateRatingDTO.setCandidateId(candidateRating.getCandidate().getCid());
		candidateRatingDTO.setCandidateName(candidateRating.getCandidate().getFullName());
		candidateRatingDTO.setCandidateRatingQuestionId(candidateRating.getCandidateRatingQuestion().getId());
		candidateRatingDTO.setRatingQuestion(candidateRating.getCandidateRatingQuestion().getRatingQuestion());
		candidateRatingDTO.setRatingScore(candidateRating.getRatingScore());
		return candidateRatingDTO;

	}

	public List<CandidateRatingDTO> convertCandidateRating(List<CandidateRating> candidateRatings) {
		List<CandidateRatingDTO> candidateRatingDTOs = new ArrayList<>();
		if (candidateRatings == null)
			return candidateRatingDTOs;

		for (CandidateRating candidateRating : candidateRatings) {
			candidateRatingDTOs.add(convertCandidateRating(candidateRating));
		}

		return candidateRatingDTOs;
	}

	public SixthSenseAdvanceSearchRequest convertAdvancedSearchQueryEntity(AdvancedSearchQueryEntity advancedSearchQueryEntity) {

		SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest = new SixthSenseAdvanceSearchRequest();
		sixthSenseAdvanceSearchRequest.setSearchType(advancedSearchQueryEntity.getSearchType());
		sixthSenseAdvanceSearchRequest.setBasic(advancedSearchQueryEntity.getBasicSearch());
		sixthSenseAdvanceSearchRequest.setBooleanSearchKeyword(advancedSearchQueryEntity.getBooleanQuery());
		sixthSenseAdvanceSearchRequest.setSources(StringUtils.commaSeparate(advancedSearchQueryEntity.getPortalSourceList()));
		sixthSenseAdvanceSearchRequest.setAllKeywordSearch(advancedSearchQueryEntity.getAllKeyword());
		sixthSenseAdvanceSearchRequest.setAnyKeywordSearch(advancedSearchQueryEntity.getAnyKeyword());
		sixthSenseAdvanceSearchRequest.setExcludeKeywordSearch(advancedSearchQueryEntity.getExcludeKeyword());
		sixthSenseAdvanceSearchRequest.setSearchIn(advancedSearchQueryEntity.getSearchIn());
		sixthSenseAdvanceSearchRequest.setShow(advancedSearchQueryEntity.getShow());
		sixthSenseAdvanceSearchRequest.setJobType(advancedSearchQueryEntity.getJobType());
		sixthSenseAdvanceSearchRequest.setJobStatus(advancedSearchQueryEntity.getJobStatus());
		sixthSenseAdvanceSearchRequest.setResumeFreshness(advancedSearchQueryEntity.getResumeFreshness());
		sixthSenseAdvanceSearchRequest.setSortBy(advancedSearchQueryEntity.getSortBy());
		sixthSenseAdvanceSearchRequest.setSimilarResumeUrl(advancedSearchQueryEntity.getSimilarResumeUrl());

		if ((null != advancedSearchQueryEntity.getMinExp() && advancedSearchQueryEntity.getMinExp() > 0)
				|| (null != advancedSearchQueryEntity.getMaxExp() && advancedSearchQueryEntity.getMaxExp() > 0)) {

			if (advancedSearchQueryEntity.getMaxExp() == 0)
				advancedSearchQueryEntity.setMaxExp(99D);

			sixthSenseAdvanceSearchRequest
					.setTotalExperince(advancedSearchQueryEntity.getMinExp() + "-" + advancedSearchQueryEntity.getMaxExp());
		}

		if (advancedSearchQueryEntity.getMinSalary() > 0 || advancedSearchQueryEntity.getMaxSalary() > 0) {
			if (advancedSearchQueryEntity.getMaxSalary() == 0)
				advancedSearchQueryEntity.setMaxSalary(99D);
			
			String minSalary = advancedSearchQueryEntity.getMinSalary().toString();
			String maxSalary = advancedSearchQueryEntity.getMaxSalary().toString();
			
			if(advancedSearchQueryEntity.getMinSalary()>0.0){
				
				String[] splitter = advancedSearchQueryEntity.getMinSalary().toString().split("\\.");
				if(splitter[1].length()==1){
					minSalary = advancedSearchQueryEntity.getMinSalary().toString()+"0";
					
				}
				
			}
			if(advancedSearchQueryEntity.getMaxSalary()>0.0){
				
				String[] splitter = advancedSearchQueryEntity.getMaxSalary().toString().split("\\.");
				if(splitter[1].length()==1){
					maxSalary = advancedSearchQueryEntity.getMaxSalary().toString()+"0";
					
				}
				
			}
		
			
			sixthSenseAdvanceSearchRequest
					.setSalaryRange(minSalary + "-" + maxSalary);
		}

		if (advancedSearchQueryEntity.getIncludeZeroSalary())
			sixthSenseAdvanceSearchRequest.setIncludeZeroSalary(SIXTH_SENSE_CHECK_VALUE);

		sixthSenseAdvanceSearchRequest.setCurrentLocation(StringUtils.commaSeparate(advancedSearchQueryEntity.getCurrLocationList()));
		sixthSenseAdvanceSearchRequest.setPrefferedLocation(StringUtils.commaSeparate(advancedSearchQueryEntity.getPrefLocationList()));
		sixthSenseAdvanceSearchRequest.setCurrentPrefLocJoinType(advancedSearchQueryEntity.getCurrentPrefLocJoinType());

		if (advancedSearchQueryEntity.getExactPrefLocation())
			sixthSenseAdvanceSearchRequest.setExactPrefLocation(SIXTH_SENSE_CHECK_VALUE);

		sixthSenseAdvanceSearchRequest.setPostPGDegree(StringUtils.commaSeparate(advancedSearchQueryEntity.getPostPGDegreeList()));
		sixthSenseAdvanceSearchRequest
				.setPostPGDegreeSpecialization(StringUtils.commaSeparate(advancedSearchQueryEntity.getPostPGDegreeSpecList()));
		sixthSenseAdvanceSearchRequest.setPostPGDegreeType(StringUtils.commaSeparate(advancedSearchQueryEntity.getPostPGDegreeTypeList()));
		sixthSenseAdvanceSearchRequest.setPgPostPGJoinType(advancedSearchQueryEntity.getPgPostPGJoinType());
		sixthSenseAdvanceSearchRequest.setPgDegree(StringUtils.commaSeparate(advancedSearchQueryEntity.getPgDegreeList()));
		sixthSenseAdvanceSearchRequest
				.setPgDegreeSpecialization(StringUtils.commaSeparate(advancedSearchQueryEntity.getPgDegreeSpecList()));
		sixthSenseAdvanceSearchRequest.setPgDegreeType(StringUtils.commaSeparate(advancedSearchQueryEntity.getPgDegreeTypeList()));
		sixthSenseAdvanceSearchRequest.setUgPGJoinType(advancedSearchQueryEntity.getUgPGJoinType());
		sixthSenseAdvanceSearchRequest.setUgDegree(StringUtils.commaSeparate(advancedSearchQueryEntity.getUgDegreeList()));
		sixthSenseAdvanceSearchRequest
				.setUgDegreeSpecialization(StringUtils.commaSeparate(advancedSearchQueryEntity.getUgDegreeSpecList()));
		sixthSenseAdvanceSearchRequest.setUgDegreeType(StringUtils.commaSeparate(advancedSearchQueryEntity.getUgDegreeTypeList()));

		if (advancedSearchQueryEntity.getHighestDegree())
			sixthSenseAdvanceSearchRequest.setHighestDegree(SIXTH_SENSE_CHECK_VALUE);

		sixthSenseAdvanceSearchRequest.setUniversity(advancedSearchQueryEntity.getUniversity());
		sixthSenseAdvanceSearchRequest.setUniversityDegree(StringUtils.commaSeparate(advancedSearchQueryEntity.getUniversityDegreeList()));

		if (advancedSearchQueryEntity.getPassYearFrom() > 0 || advancedSearchQueryEntity.getPassYearTo() > 0) {
			if (advancedSearchQueryEntity.getPassYearTo() == 0)
				advancedSearchQueryEntity.setPassYearTo(2100);
			sixthSenseAdvanceSearchRequest
					.setPassYear(advancedSearchQueryEntity.getPassYearFrom() + "-" + advancedSearchQueryEntity.getPassYearTo());
		}

		sixthSenseAdvanceSearchRequest.setYearPassDegree(StringUtils.commaSeparate(advancedSearchQueryEntity.getPassYearDegreeList()));
		sixthSenseAdvanceSearchRequest.setIndustry(StringUtils.commaSeparate(advancedSearchQueryEntity.getIndustryList()));
		sixthSenseAdvanceSearchRequest.setFunctionalArea(StringUtils.commaSeparate(advancedSearchQueryEntity.getFunctionalAreaList()));
		sixthSenseAdvanceSearchRequest.setFuncRole(StringUtils.commaSeparate(advancedSearchQueryEntity.getFuncRoleList()));

		if (advancedSearchQueryEntity.getDesignation() != null && !advancedSearchQueryEntity.getDesignationType().isEmpty()) {
			sixthSenseAdvanceSearchRequest.setDesignation(advancedSearchQueryEntity.getDesignation());
			sixthSenseAdvanceSearchRequest.setDesignationType(advancedSearchQueryEntity.getDesignationType());
		}

		if (advancedSearchQueryEntity.getIncludeCompany() != null && !advancedSearchQueryEntity.getIncludeCompany().isEmpty()) {
			sixthSenseAdvanceSearchRequest.setIncludeCompany(advancedSearchQueryEntity.getIncludeCompany());
			sixthSenseAdvanceSearchRequest.setIncludeCompType(advancedSearchQueryEntity.getIncludeCompanyType());
		}

		if (advancedSearchQueryEntity.getExcludeCompany() != null && !advancedSearchQueryEntity.getExcludeCompany().isEmpty()) {
			sixthSenseAdvanceSearchRequest.setExcludeCompany(advancedSearchQueryEntity.getExcludeCompany());
			sixthSenseAdvanceSearchRequest.setExcludeCompType(advancedSearchQueryEntity.getExcludeCompanyType());
		}

		sixthSenseAdvanceSearchRequest.setNoticePeriod(StringUtils.commaSeparate(advancedSearchQueryEntity.getNoticePeriodList()));

		if (advancedSearchQueryEntity.getMinAge() > 0 || advancedSearchQueryEntity.getMaxAge() > 0) {
			if (advancedSearchQueryEntity.getMaxAge() == 0)
				advancedSearchQueryEntity.setMaxAge(99);
			sixthSenseAdvanceSearchRequest.setAge(advancedSearchQueryEntity.getMinAge() + "-" + advancedSearchQueryEntity.getMaxAge());
		}

		if (advancedSearchQueryEntity.getFemaleCandidate())
			sixthSenseAdvanceSearchRequest.setFemaleCandiate(SIXTH_SENSE_CHECK_VALUE);
	
			sixthSenseAdvanceSearchRequest.setFemaleCandiate(advancedSearchQueryEntity.getSex());
		if (advancedSearchQueryEntity.getSpecialAbility())
			sixthSenseAdvanceSearchRequest.setSpecialAbility(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getPreminumResume())
			sixthSenseAdvanceSearchRequest.setPreminumResume(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getSmsEnable())
			sixthSenseAdvanceSearchRequest.setSmsEnable(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getVerifiedMobile())
			sixthSenseAdvanceSearchRequest.setVerifiedMobile(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getVerifiedEmail())
			sixthSenseAdvanceSearchRequest.setVerifiedEmail(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getAttachResume())
			sixthSenseAdvanceSearchRequest.setAttachResume(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getResumeNotViewed())
			sixthSenseAdvanceSearchRequest.setResumeNotViewed(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getProfileWithPhoto())
			sixthSenseAdvanceSearchRequest.setProfileWithPhoto(SIXTH_SENSE_CHECK_VALUE);
		if (advancedSearchQueryEntity.getExcludeConfidentialResume())
			sixthSenseAdvanceSearchRequest.setExcludeConfidentialResume(SIXTH_SENSE_CHECK_VALUE);
		
		
		if(advancedSearchQueryEntity.getSource()!=null && !advancedSearchQueryEntity.getSource().isEmpty())
			sixthSenseAdvanceSearchRequest.setSources(advancedSearchQueryEntity.getSource());

		return sixthSenseAdvanceSearchRequest;
	}

	public TaxDTO convertTax(Tax tax) {
		TaxDTO taxDTO = new TaxDTO();
		taxDTO.setTaxId(tax.getId());
		taxDTO.setTaxName(tax.getTaxName());
		taxDTO.setTaxNumber(tax.getTaxNumber());
		taxDTO.setCreationDate(tax.getCreationDate());
		taxDTO.setModificationDate(tax.getModificationDate());
		return taxDTO;
	}

	public List<TaxDTO> convertTax(List<Tax> taxs) {
		List<TaxDTO> taxDTOs = new ArrayList<TaxDTO>();
		for (Tax tax : taxs) {
			taxDTOs.add(convertTax(tax));
		}
		return taxDTOs;
	}

	public InvoiceSettingsDTO convertInvoiceSettings(InvoiceSettings invoiceSettings) {

		if (invoiceSettings != null) {
			InvoiceSettingsDTO invoiceSettingsDTO = new InvoiceSettingsDTO();
			invoiceSettingsDTO.setId(invoiceSettings.getId());
			invoiceSettingsDTO.setOrganizationName(invoiceSettings.getOrganizationName());
			invoiceSettingsDTO.setOrganization_address_1(invoiceSettings.getOrganization_address_1());
			invoiceSettingsDTO.setOrganization_address_2(invoiceSettings.getOrganization_address_2());
			invoiceSettingsDTO.setOrganizationCity(invoiceSettings.getOrganizationCity());
			invoiceSettingsDTO.setOrganizationState(invoiceSettings.getOrganizationState());
			invoiceSettingsDTO.setOrganizationCountry(invoiceSettings.getOrganizationCountry());
			invoiceSettingsDTO.setOrganizationPin(invoiceSettings.getOrganizationPin());
			invoiceSettingsDTO.setOrganizationPhone(invoiceSettings.getOrganizationPhone());
			invoiceSettingsDTO.setChequePayable(invoiceSettings.getChequePayable());
			invoiceSettingsDTO.setOrganizationAccountName(invoiceSettings.getOrganizationAccountName());
			invoiceSettingsDTO.setOrganizationAccountNumber(invoiceSettings.getOrganizationAccountNumber());
			invoiceSettingsDTO.setOrganizationBankName(invoiceSettings.getOrganizationBankName());
			invoiceSettingsDTO.setOrganizationBankBranchName(invoiceSettings.getOrganizationBankBranchName());
			invoiceSettingsDTO.setOrganizationBankIfsc(invoiceSettings.getOrganizationBankIfsc());
			invoiceSettingsDTO.setNote(invoiceSettings.getNote());
			invoiceSettingsDTO.setBillClientName(invoiceSettings.getBillClientName());
			invoiceSettingsDTO.setBillContactName(invoiceSettings.getBillContactName());
			invoiceSettingsDTO.setBill_address_1(invoiceSettings.getBill_address_1());
			invoiceSettingsDTO.setBill_address_2(invoiceSettings.getBill_address_2());
			invoiceSettingsDTO.setBillCity(invoiceSettings.getBillCity());
			invoiceSettingsDTO.setBillState(invoiceSettings.getBillState());
			invoiceSettingsDTO.setBillCountry(invoiceSettings.getBillCountry());
			invoiceSettingsDTO.setBillPin(invoiceSettings.getBillPin());
			invoiceSettingsDTO.setBillPhone(invoiceSettings.getBillPhone());
			invoiceSettingsDTO.setTaxDetails(invoiceSettings.getTaxDetails());
			invoiceSettingsDTO.setOrgLogoUrl(agencyInvoiceService.getOrgLogoUrl());
			invoiceSettingsDTO.setTaxRelatedDetailsDTOs(convertTaxRelatedDetails(invoiceSettings.getTaxRelatedDetails()));
			return invoiceSettingsDTO;
		}

		else
			return null;
	}

	public List<TaxCalculateDTO> convertTaxCalculate(Map<String, Double> taxes, double amount) {
		List<TaxCalculateDTO> taxCalculateDTOs = new ArrayList<TaxCalculateDTO>();

		if (taxes == null || taxes.isEmpty()) {
			return null;
		}
		for (Map.Entry<String, Double> entry : taxes.entrySet()) {
			TaxCalculateDTO taxCalculateDTO = new TaxCalculateDTO();
			taxCalculateDTO.setValue(Double.parseDouble(new DecimalFormat("##.##").format(amount * entry.getValue() / 100)));
			taxCalculateDTO.setTaxName(entry.getKey());
			taxCalculateDTO.setTaxPercent(entry.getValue());
			taxCalculateDTOs.add(taxCalculateDTO);
		}
		return taxCalculateDTOs;
	}

	List<TaxRelatedDetailsDTO> convertTaxRelatedDetails(Map<String, String> taxRelatedDetails) {
		List<TaxRelatedDetailsDTO> taxRelatedDetailsDTOs = new ArrayList<TaxRelatedDetailsDTO>();

		if (taxRelatedDetails == null || taxRelatedDetails.isEmpty()) {
			return null;
		}
		for (Map.Entry<String, String> entry : taxRelatedDetails.entrySet()) {
			TaxRelatedDetailsDTO taxRelatedDetailsDTO = new TaxRelatedDetailsDTO();
			taxRelatedDetailsDTO.setName(entry.getKey());
			taxRelatedDetailsDTO.setValue(entry.getValue());
			taxRelatedDetailsDTOs.add(taxRelatedDetailsDTO);
		}
		return taxRelatedDetailsDTOs;

	}

	public Map<String, Double> convertTaxDetails(List<TaxCalculateDTO> taxCalculateDTOs) {
		Map<String, Double> taxDetails = new HashMap<String, Double>();
		for (TaxCalculateDTO taxCalculateDTO : taxCalculateDTOs) {
			taxDetails.put(taxCalculateDTO.getTaxName(), taxCalculateDTO.getPercent());
		}
		return taxDetails;
	}

	public Map<String, String> convertTaxRelatedDetails(List<TaxRelatedDetailsDTO> taxRelatedDetailsDTOs) {
		if (taxRelatedDetailsDTOs == null)
			return null;
		Map<String, String> taxRelatedDetails = new HashMap<String, String>();
		for (TaxRelatedDetailsDTO taxRelatedDetailsDTO : taxRelatedDetailsDTOs) {
			if ((taxRelatedDetailsDTO.getName() != null && !taxRelatedDetailsDTO.getName().isEmpty())
					&& (taxRelatedDetailsDTO.getValue() != null && !taxRelatedDetailsDTO.getValue().isEmpty()))
				taxRelatedDetails.put(taxRelatedDetailsDTO.getName(), taxRelatedDetailsDTO.getValue());
		}
		return taxRelatedDetails;
	}

	public OrganizationTaxDetailsDTO convertOrganizationTaxDetails(OrganizationTaxDetails organizationTaxDetails) {
		OrganizationTaxDetailsDTO organizationTaxDetailsDTO = new OrganizationTaxDetailsDTO(organizationTaxDetails.getTaxName(),
				organizationTaxDetails.getTaxValue());
		organizationTaxDetailsDTO.setId(organizationTaxDetails.getId());
		return organizationTaxDetailsDTO;
	}

	public List<OrganizationTaxDetailsDTO> convertOrganizationTaxDetails(List<OrganizationTaxDetails> organizationTaxDetailes) {
		List<OrganizationTaxDetailsDTO> organizationTaxDetailsDTOs = new ArrayList<OrganizationTaxDetailsDTO>();
		for (OrganizationTaxDetails organizationTaxDetails : organizationTaxDetailes) {
			organizationTaxDetailsDTOs.add(convertOrganizationTaxDetails(organizationTaxDetails));
		}
		return organizationTaxDetailsDTOs;
	}

	public SixthSenseUserDTO converSixthSenseUser(List<SixthSenseUser> sixthSenseUsers) {

		SixthSenseUserDTO sixthSenseUserDTO = new SixthSenseUserDTO();
		List<SixthSenseUserCredential> ssUserCredentialList = new ArrayList<SixthSenseUserCredential>();
		for (SixthSenseUser user : sixthSenseUsers) {
			SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
			sixthSenseUserCredential.setUserName(user.getUserName());
			sixthSenseUserCredential.setPassword(user.getPassword());

			ssUserCredentialList.add(sixthSenseUserCredential);
		}
		sixthSenseUserDTO.setUsers(ssUserCredentialList);
		return sixthSenseUserDTO;
	}

	public SixthSenseDeleteUserDTO converSixthSenseDeleteUser(List<SixthSenseUser> sixthSenseUsers) {

		SixthSenseDeleteUserDTO sixthSenseDeleteUserDTO = new SixthSenseDeleteUserDTO();
		List<String> userNameList = new ArrayList<String>();
		for (SixthSenseUser user : sixthSenseUsers) {
			userNameList.add(user.getUserName());
		}
		sixthSenseDeleteUserDTO.getUsers().addAll(userNameList);
		return sixthSenseDeleteUserDTO;
	}

	public SixthSenseOTPProcess getSixthSenseOTPProcess(String source, String sourceUserId) {

		SixthSenseOTPProcess sixthSenseOTPProcess = new SixthSenseOTPProcess();
		sixthSenseOTPProcess.setSource(source);
		sixthSenseOTPProcess.setSourceUserId(sourceUserId);
		return sixthSenseOTPProcess;
	}

	public List<SixthSensePortalSourceDTO> convertSixthSensePortalManageResponse(SixthSensePortalManageResponse portalManageResponse) {

		List<SixthSensePortalSourceDTO> list = new ArrayList<SixthSensePortalSourceDTO>();

		if (portalManageResponse.getNaukri() != null) {
			for (SixthSensePortalCredentialDTO portalCred : portalManageResponse.getNaukri()) {
				SixthSensePortalSourceDTO sixthSensePortalSourceDTO = new SixthSensePortalSourceDTO();
				sixthSensePortalSourceDTO.setSource(SixthSenseSource.naukri.toString());
				sixthSensePortalSourceDTO.setSourceUserId(portalCred.getSourceUserId());
				sixthSensePortalSourceDTO.setPassword(portalCred.getPassword());
				sixthSensePortalSourceDTO.setOtpEmailID(portalCred.getOtpEmailID());
				sixthSensePortalSourceDTO.setOtpEmailPwd(portalCred.getOtpEmailPwd());

				list.add(sixthSensePortalSourceDTO);
			}
		}

		if (portalManageResponse.getMonster() != null) {
			for (SixthSensePortalCredentialDTO portalCred : portalManageResponse.getMonster()) {
				SixthSensePortalSourceDTO sixthSensePortalSourceDTO = new SixthSensePortalSourceDTO();
				sixthSensePortalSourceDTO.setSource(SixthSenseSource.monster.toString());
				sixthSensePortalSourceDTO.setSourceUserId(portalCred.getSourceUserId());
				sixthSensePortalSourceDTO.setPassword(portalCred.getPassword());
				sixthSensePortalSourceDTO.setOtpEmailID(portalCred.getOtpEmailID());
				sixthSensePortalSourceDTO.setOtpEmailPwd(portalCred.getOtpEmailPwd());

				list.add(sixthSensePortalSourceDTO);
			}
		}

		if (portalManageResponse.getTimes() != null) {
			for (SixthSensePortalCredentialDTO portalCred : portalManageResponse.getTimes()) {
				SixthSensePortalSourceDTO sixthSensePortalSourceDTO = new SixthSensePortalSourceDTO();
				sixthSensePortalSourceDTO.setSource(SixthSenseSource.times.toString());
				sixthSensePortalSourceDTO.setSourceUserId(portalCred.getSourceUserId());
				sixthSensePortalSourceDTO.setPassword(portalCred.getPassword());
				sixthSensePortalSourceDTO.setOtpEmailID(portalCred.getOtpEmailID());
				sixthSensePortalSourceDTO.setOtpEmailPwd(portalCred.getOtpEmailPwd());

				list.add(sixthSensePortalSourceDTO);
			}
		}

		if (portalManageResponse.getShine() != null) {
			for (SixthSensePortalCredentialDTO portalCred : portalManageResponse.getShine()) {
				SixthSensePortalSourceDTO sixthSensePortalSourceDTO = new SixthSensePortalSourceDTO();
				sixthSensePortalSourceDTO.setSource(SixthSenseSource.shine.toString());
				sixthSensePortalSourceDTO.setSourceUserId(portalCred.getSourceUserId());
				sixthSensePortalSourceDTO.setPassword(portalCred.getPassword());
				sixthSensePortalSourceDTO.setOtpEmailID(portalCred.getOtpEmailID());
				sixthSensePortalSourceDTO.setOtpEmailPwd(portalCred.getOtpEmailPwd());

				list.add(sixthSensePortalSourceDTO);
			}
		}
		return list;
	}

	public SixthSensePortalManageResponse convertSixthSensePortalManageResponse(
			List<SixthSensePortalSourceDTO> sixthSensePortalSourceDTOs) {

		Set<String> sources = new HashSet<String>();
		SixthSensePortalManageResponse sixthSensePortalManageResponse = new SixthSensePortalManageResponse();

		for (SixthSensePortalSourceDTO sixthSensePortalSourceDTO : sixthSensePortalSourceDTOs) {

			if (SixthSenseSource.naukri.toString().equals(sixthSensePortalSourceDTO.getSource())) {
				SixthSensePortalCredentialDTO portalCred = new SixthSensePortalCredentialDTO();
				portalCred.setSourceUserId(sixthSensePortalSourceDTO.getSourceUserId());
				portalCred.setPassword(sixthSensePortalSourceDTO.getPassword());
				portalCred.setOtpEmailID(sixthSensePortalSourceDTO.getOtpEmailID());
				portalCred.setOtpEmailPwd(sixthSensePortalSourceDTO.getOtpEmailPwd());
				sixthSensePortalManageResponse.getNaukri().add(portalCred);

				sources.add(SixthSenseSource.naukri.toString());
			}

			if (SixthSenseSource.monster.toString().equals(sixthSensePortalSourceDTO.getSource())) {
				SixthSensePortalCredentialDTO portalCred = new SixthSensePortalCredentialDTO();
				portalCred.setSourceUserId(sixthSensePortalSourceDTO.getSourceUserId());
				portalCred.setPassword(sixthSensePortalSourceDTO.getPassword());
				portalCred.setOtpEmailID(sixthSensePortalSourceDTO.getOtpEmailID());
				portalCred.setOtpEmailPwd(sixthSensePortalSourceDTO.getOtpEmailPwd());
				sixthSensePortalManageResponse.getMonster().add(portalCred);

				sources.add(SixthSenseSource.monster.toString());
			}

			if (SixthSenseSource.times.toString().equals(sixthSensePortalSourceDTO.getSource())) {
				SixthSensePortalCredentialDTO portalCred = new SixthSensePortalCredentialDTO();
				portalCred.setSourceUserId(sixthSensePortalSourceDTO.getSourceUserId());
				portalCred.setPassword(sixthSensePortalSourceDTO.getPassword());
				portalCred.setOtpEmailID(sixthSensePortalSourceDTO.getOtpEmailID());
				portalCred.setOtpEmailPwd(sixthSensePortalSourceDTO.getOtpEmailPwd());
				sixthSensePortalManageResponse.getTimes().add(portalCred);

				sources.add(SixthSenseSource.times.toString());
			}

			if (SixthSenseSource.shine.toString().equals(sixthSensePortalSourceDTO.getSource())) {
				SixthSensePortalCredentialDTO portalCred = new SixthSensePortalCredentialDTO();
				portalCred.setSourceUserId(sixthSensePortalSourceDTO.getSourceUserId());
				portalCred.setPassword(sixthSensePortalSourceDTO.getPassword());
				portalCred.setOtpEmailID(sixthSensePortalSourceDTO.getOtpEmailID());
				portalCred.setOtpEmailPwd(sixthSensePortalSourceDTO.getOtpEmailPwd());
				sixthSensePortalManageResponse.getShine().add(portalCred);

				sources.add(SixthSenseSource.shine.toString());
			}
		}
		sixthSensePortalManageResponse.setSources(StringUtils.commaSeparate(sources));
		return sixthSensePortalManageResponse;

	}

	public Collection<FolderDTO> convertFolders(Collection<Folder> folders) {
		List<FolderDTO> folderDtos = new ArrayList<FolderDTO>();
		for (Folder folder : folders) {
			folderDtos.add(convertFolder(folder));
		}

		return folderDtos;

	}

	public FolderDTO convertFolder(Folder folder) {
		FolderDTO folderDTO = new FolderDTO();
		folderDTO.setCandidateCount(folder.getCandidateCount());
		folderDTO.setFolderDesc(folder.getDesc());
		folderDTO.setFolderName(folder.getDisplayName());
		folderDTO.setFolderPublic(folder.isFolderPublic());
		folderDTO.setFolderType(folder.getFolderType().toString());
		folderDTO.setId(folder.getId());
		folderDTO.setFolderOwner(folder.getOwner());
		folderDTO.setPositionCount(folder.getPositionCount());
		folderDTO.setSharedUserCount(folder.getSharedUserCount());
		return folderDTO;
	}

	public List<TeamResponseDTO> convertTeams(Collection<Team> teams) {
		List<TeamResponseDTO> teamResponseDTOs = new ArrayList<>();
		for (Team team : teams) {
			teamResponseDTOs.add(convertTeam(team));
		}

		return teamResponseDTOs;
	}

	public List<TeamResponseDTO> convertTeamFull(Collection<Team> teams) {
		List<TeamResponseDTO> teamResponseDTOs = new ArrayList<>();
		for (Team team : teams) {
			teamResponseDTOs.add(convertTeam(team));
		}

		return teamResponseDTOs;
	}

	public TeamResponseDTO convertTeam(Team team) {
		Set<Long> teamIdReached = new HashSet<>();
		return convertTeam(team, teamIdReached);
	}

	public TeamResponseDTO convertTeam(Team team, Set<Long> teamIdReached) {
		if (team == null)
			return null;

		TeamResponseDTO teamResponseDTO = new TeamResponseDTO();
		
		// to avoid recursive call stackoverflow we check if we had reached this
		// id before in looping
		if (teamIdReached.contains(team.getId()))
			return null;

		teamIdReached.add(team.getId());

		List<UserDTO> userDtos = new ArrayList<>();
		for (TeamMember teamMember : team.getMembers()) {
			UserDTO userDto = convertTeamUser(teamMember.getUser(), teamMember.getTargetAmount(),
					teamMember.getTargetPositionOpeningClosure(), teamMember.getRole().toString());
			userDtos.add(userDto);
		}

		List<TeamResponseDTO> teamResponseDTOs = new ArrayList<>();
		for (Team teamChild : team.getChildren()) {
			teamResponseDTOs.add(convertTeam(teamChild,teamIdReached));
		}
		
		teamResponseDTO.getChildrenTeams().addAll(teamResponseDTOs);
		teamResponseDTO.setParentTeam(convertTeamToSimpleDTO(team.getParent()));
		teamResponseDTO.setRootTeam(team.getRootTeam());
		teamResponseDTO.setMembers(userDtos);
		teamResponseDTO.setTeamDesc(team.getTeamDesc());
		teamResponseDTO.setTeamId(team.getId());
		teamResponseDTO.setTeamName(team.getTeamName());
		teamResponseDTO.setTeamMemberCount(team.getMemberCount());
		teamResponseDTO.setCreationDate(team.getCreationDate());
		teamResponseDTO.setModificationDate(team.getModificationDate());
		teamResponseDTO.setTeamTargetAmount(team.getTeamTargetAmount());
		teamResponseDTO.setTeamTargetPositionOpeningClosure(team.getTeamTargetPositionOpeningClosure());
		if (team.getTeamTargetAmountCurrency() != null)
			teamResponseDTO.setTeamTargetAmountCurrency(team.getTeamTargetAmountCurrency().toString());

		return teamResponseDTO;
	}

	public List<TeamResponseDTO> convertTeamsToSimpleDTO(Collection<Team> teams) {
		List<TeamResponseDTO> teamResponseDTO = new ArrayList<>();
		for (Team team : teams) {
			teamResponseDTO.add(convertTeamToSimpleDTO(team));
		}

		return teamResponseDTO;
	}

	public TeamResponseDTO convertTeamToSimpleDTO(Team team) {
		if (team == null)
			return null;

		TeamResponseDTO teamSimpleResponseDTO = new TeamResponseDTO();
		teamSimpleResponseDTO.setTeamDesc(team.getTeamDesc());
		teamSimpleResponseDTO.setTeamId(team.getId());
		teamSimpleResponseDTO.setTeamName(team.getTeamName());
		teamSimpleResponseDTO.setTeamMemberCount(team.getMemberCount());
		teamSimpleResponseDTO.setTeamTargetAmount(team.getTeamTargetAmount());
		teamSimpleResponseDTO.setTeamTargetPositionOpeningClosure(team.getTeamTargetPositionOpeningClosure());
		teamSimpleResponseDTO.setCreationDate(team.getCreationDate());
		teamSimpleResponseDTO.setModificationDate(team.getModificationDate());
		if (team.getTeamTargetAmountCurrency() != null)
			teamSimpleResponseDTO.setTeamTargetAmountCurrency(team.getTeamTargetAmountCurrency().toString());

		return teamSimpleResponseDTO;
	}

}
