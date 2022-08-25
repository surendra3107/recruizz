package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.TeamDTO;
import com.bbytes.recruiz.rest.dto.models.TeamMemberDTO;
import com.bbytes.recruiz.rest.dto.models.TeamResponseDTO;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.DTOToDomainConverstionService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.TeamService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class TeamController {

	private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private DTOToDomainConverstionService dtoToDomainConverstionService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@RequestMapping(value = "/api/v1/team", method = RequestMethod.POST)
	public RestResponse createTeam(@RequestBody TeamDTO teamDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.CreateTeam.name());

		try {
			if (teamService.existsByTeamName(teamDTO.getTeamName())) {
				return new RestResponse(RestResponse.FAILED, "Team with name '" + teamDTO.getTeamName() + "' exist",
						"team_name_already_exists");
			} else {
				TeamResponseDTO teamResponseDTO = teamCreationOrUpdation(teamDTO);
				return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_add_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/{teamId}", method = RequestMethod.PUT)
	public RestResponse updateTeam(@PathVariable("teamId") Long teamId, @RequestBody TeamDTO teamDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateTeam.name());
		try {
			if (teamService.exists(teamId)) {
				teamDTO.setTeamId(teamId);
				TeamResponseDTO teamResponseDTO = teamCreationOrUpdation(teamDTO);
				return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
			} else {
				return new RestResponse(RestResponse.FAILED, "Team with given id does not exist", "team_update_failed");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_update_failed");
		}
	}

	private TeamResponseDTO teamCreationOrUpdation(TeamDTO teamDTO) throws RecruizException {
		Team team = null;
		if (teamDTO.getTeamId() == null || teamDTO.getTeamId() == 0L) {
			team = dtoToDomainConverstionService.convertTeam(teamDTO);
		} else {
			team = teamService.findOne(teamDTO.getTeamId());
			if (teamDTO.getTeamDesc() != null)
				team.setTeamDesc(teamDTO.getTeamDesc());

			if (teamDTO.getTeamName() != null)
				team.setTeamName(teamDTO.getTeamName());
			
			if (teamDTO.getRootTeam() != null)
				team.setRootTeam(teamDTO.getRootTeam());

			if (teamDTO.getTeamTargetAmount() != null)
				team.setTeamTargetAmount(teamDTO.getTeamTargetAmount());

			if (teamDTO.getTeamTargetPositionOpeningClosure() != null)
				team.setTeamTargetPositionOpeningClosure(teamDTO.getTeamTargetPositionOpeningClosure());

			if (teamDTO.getTeamTargetAmountCurrency() != null)
				team.setTeamTargetAmountCurrency(Currency.valueOf(teamDTO.getTeamTargetAmountCurrency()));

		}

		// clear old teams
		for (Team teamChild : team.getChildren()) {
			teamChild.setParent(null);
		}
		team.getChildren().clear();

		if (teamDTO.getChildrenTeamIds() != null && !teamDTO.getChildrenTeamIds().isEmpty()) {
			Iterable<Team> childrenTeams = teamService.findAll(teamDTO.getChildrenTeamIds());
			List<Team> childrenTeamsToBeAdded = new ArrayList<>();
			for (Team teamChild : childrenTeams) {
				if (team.getId() != teamChild.getId()) {
					teamChild.setParent(team);
					childrenTeamsToBeAdded.add(teamChild);
				}
			}
			team.getChildren().addAll(childrenTeamsToBeAdded);
		}

		if (teamDTO.getParentTeamId() != null) {
			Team parent = teamService.findOne(teamDTO.getParentTeamId());
			if (parent != null && parent.getId() != team.getId())
				team.setParent(parent);
		} else {
			team.setParent(null);
		}

		team = teamService.save(team);
		team = teamService.addTeamMembers(teamDTO.getMembers(), team);

		Team updatedTeamFromDB = teamService.getFullTeamById(team.getId());
		TeamResponseDTO teamResponseDTO = dataModelToDTOConversionService.convertTeam(updatedTeamFromDB);
		return teamResponseDTO;
	}

	@RequestMapping(value = "/api/v1/team/member", method = RequestMethod.PUT)
	public RestResponse updateTeam(@RequestBody TeamMemberDTO teamMemberDTO) {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateTeamMember.name());

		try {
			if (teamService.exists(teamMemberDTO.getTeamId())) {
				TeamMember teamMemberfromDB = teamService.updateTeamMember(teamMemberDTO);
				Team updatedTeamFromDB = teamService.getFullTeamById(teamMemberfromDB.getTeam().getId());
				TeamResponseDTO teamResponseDTO = dataModelToDTOConversionService.convertTeam(updatedTeamFromDB);
				return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
			} else {
				return new RestResponse(RestResponse.FAILED, "Team with given id does not exist", "team_update_failed");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_update_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/add/members/{teamId}", method = RequestMethod.POST)
	public RestResponse addTeamMemebers(@PathVariable("teamId") Long teamId,
			@RequestBody(required = true) List<TeamMemberDTO> teamMemberDTOs) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddTeamMember.name());

		try {
			Team updatedTeamFromDB = teamService.addMemberDtosToTeam(teamId, teamMemberDTOs);
			TeamResponseDTO teamResponseDTO = dataModelToDTOConversionService.convertTeam(updatedTeamFromDB);
			return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_add_users_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/remove/members/{teamId}", method = RequestMethod.POST)
	public RestResponse removeTeamMembers(@PathVariable("teamId") Long teamId,
			@RequestBody(required = true) List<TeamMemberDTO> teamMemberDTOs) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.RemoveTeamMember.name());

		try {
			Collection<String> userEmails = new ArrayList<>();
			for (TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
				userEmails.add(teamMemberDTO.getEmail());
			}
			teamService.removeUserEmailFromTeam(teamId, userEmails);
			Team updatedTeamFromDB = teamService.getFullTeamById(teamId);
			TeamResponseDTO teamResponseDTO = dataModelToDTOConversionService.convertTeam(updatedTeamFromDB);
			return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_remove_users_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/{teamId}", method = RequestMethod.DELETE)
	public RestResponse deleteTeam(@PathVariable("teamId") Long teamId) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteTeam.name());

		try {
			Team teamToDelete = teamService.getTeamById(teamId);
			for (Team childTeam : teamToDelete.getChildren()) {
				childTeam.setParent(null);
				childTeam = teamService.save(childTeam);
			}
			teamToDelete.getChildren().clear();

			if (teamToDelete != null) {
				Set<Position> teamPositions = teamToDelete.getPositions();
				if (null != teamPositions && !teamPositions.isEmpty()) {
					for (Position position : teamPositions) {
						position.setTeam(null);
					}
					positionService.save(teamPositions);
				}
				teamService.delete(teamId);
			}
			return new RestResponse(RestResponse.SUCCESS, "Team deleted successfully");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_delete_failed");
		}
	}

	/**
	 * Check if team name exist
	 * 
	 * @param teamName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/team/{teamName}/exist", method = RequestMethod.GET)
	public RestResponse teamNameExist(@PathVariable("teamName") String teamName) throws RecruizException {
		Boolean exist = teamService.existsByTeamName(teamName);
		return new RestResponse(RestResponse.SUCCESS, exist);
	}

	/**
	 * Get team with team id
	 * 
	 * @param teamName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/team/{teamId}", method = RequestMethod.GET)
	public RestResponse fetchFullTeam(@PathVariable("teamId") Long teamId) throws RecruizException {
/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.FetchFullTeam.name());*/

		Team team = teamService.getFullTeamById(teamId);
		if (team == null)
			return new RestResponse(RestResponse.FAILED, "Team for given id missing");

		TeamResponseDTO teamResponseDTO = dataModelToDTOConversionService.convertTeam(team);
		return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
	}

	@RequestMapping(value = "/api/v1/team/user/memebers", method = RequestMethod.GET)
	public RestResponse getAllMemebersOfteamsForGivenUser() throws RecruizException {

		User user = userService.getLoggedInUserObject();
		List<Team> teams = teamService.getAllTeamsForUser(user);
		// if not part of any team then send empty result 
		if(teams==null || teams.isEmpty())
			return new RestResponse(RestResponse.SUCCESS, new ArrayList<UserDTO>());
			
		Set<User> users = teamService.getTeamMemberUsers(teams);
		Set<UserDTO> userDTOs = dataModelToDTOConversionService.convertUsers(users);
		return new RestResponse(RestResponse.SUCCESS, userDTOs);
	}

	@RequestMapping(value = "/api/v1/team/structure/{teamId}", method = RequestMethod.GET)
	public RestResponse fetchFullTeamStructure(@PathVariable("teamId") Long teamId) throws RecruizException {
/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.FetchFullTeam.name());*/

		TeamResponseDTO teamResponseDTO = teamService.getTeamStructureFull(teamId);
		return new RestResponse(RestResponse.SUCCESS, teamResponseDTO);
	}

	@RequestMapping(value = "/api/v1/team/list", method = RequestMethod.GET)
	public RestResponse getTeamForCurrentUser() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetTeamForCurrentUser.name());*/
		
		try {
			List<Team> teamList = teamService.getAllTeamsForCurrentUser();
			List<TeamResponseDTO> teamResponseDTOs = dataModelToDTOConversionService.convertTeamsToSimpleDTO(teamList);
			return new RestResponse(RestResponse.SUCCESS, teamResponseDTOs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_list_failed");
		}
	}
	
	@RequestMapping(value = "/api/v1/team/own/list", method = RequestMethod.GET)
	public RestResponse getDirectTeamForCurrentUser() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetTeamForCurrentUser.name());*/
		try {
			List<Team> teamList = teamService.getAllDirectTeamsForUser(userService.getLoggedInUserObject());
			List<TeamResponseDTO> teamResponseDTOs = dataModelToDTOConversionService.convertTeamsToSimpleDTO(teamList);
			return new RestResponse(RestResponse.SUCCESS, teamResponseDTOs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_list_failed");
		}
	}
	
	

	@RequestMapping(value = "/api/v1/team/list/all", method = RequestMethod.GET)
	public RestResponse getAllTeam() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllTeam.name());*/
		
		
		try {
			List<Team> teamList = teamService.getAllTeams();
			List<TeamResponseDTO> teamSimpleResponseDTOs = dataModelToDTOConversionService.convertTeamsToSimpleDTO(teamList);
			return new RestResponse(RestResponse.SUCCESS, teamSimpleResponseDTOs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_list_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/list/details/all", method = RequestMethod.GET)
	public RestResponse getAllTeamWithMemberDetails() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllTeam.name());*/

		try {
			List<Team> teamList = teamService.getAllTeams();
			List<TeamResponseDTO> teamResponseDTOs = dataModelToDTOConversionService.convertTeams(teamList);
			return new RestResponse(RestResponse.SUCCESS, teamResponseDTOs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_list_failed");
		}
	}

	@RequestMapping(value = "/api/v1/team/list/user", method = RequestMethod.GET)
	public RestResponse getAllUsers(@RequestParam(value = "teamId", required = false) Long teamId,
			@RequestParam(value = "searchText", required = false) String searchText) throws Exception {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllUserForTeam.name());*/

		try {
			Collection<User> usersFromDB = new ArrayList<>();
			if (searchText != null && !searchText.isEmpty()) {
				Collection<User> usersList = userService.searchTextInNameOrEmailOrMobile(searchText);
				for (User user : usersList) {
					// only active users
					if (user.getAccountStatus())
						usersFromDB.add(user);
				}
			} else {
				usersFromDB = userService.getAllActiveAppAndNonPendingUsers();
			}

			if (teamId != null && teamId != 0) {
				Team team = teamService.findOne(teamId);
				if (team != null) {
					Collection<TeamMember> teamMembers = team.getMembers();
					Collection<User> existingUsers = new ArrayList<>();
					if (teamMembers != null) {
						for (TeamMember teamMember : teamMembers) {
							existingUsers.add(teamMember.getUser());
						}
					}
					usersFromDB.removeAll(existingUsers);
				}
			}

			List<UserDTO> userDTOs = dataModelToDTOConversionService.convertUsers(usersFromDB);
			return new RestResponse(RestResponse.SUCCESS, userDTOs);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new RestResponse(RestResponse.FAILED, "Error while fetching user from db for share", ErrorHandler.USER_NOT_FOUND);
		}

	}
	
	//Added by Sajin
	//This will return the list of teams that the logged in user is part of. Ideally only 1.
	//This will then be used to generate the reports (Teamware Custom)
	
	@RequestMapping(value = "/api/v1/team/ownteam", method = RequestMethod.GET)
	public RestResponse getOwnTeam() {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllTeam.name());*/

		try {
			List<Team> teamList = teamService.getOwnTeamOfUser(userService.getLoggedInUserObject());
			if (teamList == null || teamList.isEmpty()) {
				return new RestResponse(RestResponse.FAILED, "Logged in user is not part of any team");
				
			} else {
				
				List<TeamResponseDTO> teamResponseDTOs = dataModelToDTOConversionService.convertTeams(teamList);
				return new RestResponse(RestResponse.SUCCESS, teamResponseDTOs);
				
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "team_list_failed");
		}
	}

	

}
