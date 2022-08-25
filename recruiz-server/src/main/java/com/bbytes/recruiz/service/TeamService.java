package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.TeamRole;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TeamMemberRepository;
import com.bbytes.recruiz.repository.TeamRepository;
import com.bbytes.recruiz.rest.dto.models.TeamMemberDTO;
import com.bbytes.recruiz.rest.dto.models.TeamResponseDTO;

@Service
public class TeamService extends AbstractService<Team, Long> {

	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private DTOToDomainConverstionService dtoToDomainConverstionService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	public TeamService(TeamRepository teamRepository) {
		super(teamRepository);
		this.teamRepository = teamRepository;
	}

	/**
	 * This method will return the UserRole object for the roleName provided as
	 * argument
	 *
	 * @throws RecruizException
	 *
	 */

	@Transactional(readOnly = true)
	public Team getTeamByName(String teamName) throws RecruizException {
		return teamRepository.findOneByTeamName(teamName);
	}

	@Transactional(readOnly = true)
	public Boolean existsByTeamName(String teamName) throws RecruizException {
		return teamRepository.existsByTeamName(teamName);
	}

	@Transactional(readOnly = true)
	public Team getTeamById(long id) throws RecruizException {
		return findOne(id);
	}
	
	@Transactional(readOnly = true)
	public Team findOne(long id) throws RecruizException {
		if(id==-1)
			return teamRepository.findFirstByRootTeamTrue();
		
		return teamRepository.findOne(id);
	}

	@Transactional(readOnly = true)
	public List<Team> getAllTeamsForUsers(Set<User> users) throws RecruizException {
		return teamRepository.getDistinctByMembersIn(users);
	}

	@Transactional(readOnly = true)
	public List<Team> getAllTeamsForUser(User user) throws RecruizException {
		Set<Team> finalResult = new HashSet<>();
		Set<Long> teamIdReached = new HashSet<>();
		List<Team> topLevels = teamRepository.findByMembers(user);
		if(topLevels==null)
			return new ArrayList<>();
			
		for (Team team : topLevels) {
			List<Team> teamRecruisveList = new ArrayList<>();
			getRecursiveTeamList(team, teamRecruisveList, teamIdReached);
			finalResult.add(team);
			finalResult.addAll(teamRecruisveList);
		}
		return new ArrayList<>(finalResult);
	}
	
	//Getting the list of teams that the user is part of (non recursive till the last team)
	//Added by Sajin 
	@Transactional(readOnly = true)
	public List<Team> getOwnTeamOfUser(User user) throws RecruizException {
		List<Team> finalResult = teamRepository.findByMembers(user);
		return new ArrayList<>(finalResult);
	}
	
	
	@Transactional(readOnly = true)
	public List<Team> getAllDirectTeamsForUser(User user) throws RecruizException {
		return teamRepository.findByMembers(user);
	}
	
	@Transactional(readOnly = true)
	public Set<Long> getAllTeamsIdForUser(User user) throws RecruizException {
		Set<Long> teamIdsResult = new HashSet<>();
		List<Team> finalResult = getAllTeamsForUser(user);
		for (Team team : finalResult) {
			teamIdsResult.add(team.getId());
		}
		return teamIdsResult;
	}
	
	@Transactional(readOnly = true)
	public List<Long> getAllTeamsIdsForCurrentUser() throws RecruizException {
		List<Long> teamIdsResult = new ArrayList<>();
		List<Team> finalResult = getAllTeamsForUser(userService.getLoggedInUserObject());
		for (Team team : finalResult) {
			teamIdsResult.add(team.getId());
		}
		return teamIdsResult;
	}

	private void getRecursiveTeamList(Team team, List<Team> teamRecruisveList, Set<Long> teamIdReached) {
		// to avoid recursive call stackoverflow we check if we had reached this
		// id before in looping
		if (teamIdReached.contains(team.getId()))
			return;

		teamIdReached.add(team.getId());
		for (Team child : team.getChildren()) {
			teamRecruisveList.add(child);
			getRecursiveTeamList(child, teamRecruisveList, teamIdReached);
		}
	}

	@Transactional(readOnly = true)
	public TeamResponseDTO getTeamStructureFull(Long teamId) throws RecruizException {
		TeamResponseDTO teamResponseDTO = new TeamResponseDTO();
		Set<Long> teamIdReached = new HashSet<>();
		Team team = teamRepository.findOne(teamId);
		if (team != null) {
			teamResponseDTO.setParentTeam(dataModelToDTOConversionService.convertTeamToSimpleDTO(team.getParent()));
			getRecursiveTeam(team, teamResponseDTO, teamIdReached);
		}
		return teamResponseDTO;
	}

	private void getRecursiveTeam(Team team, TeamResponseDTO teamResponseDTO, Set<Long> teamIdReached) {
		// to avoid recursive call stackoverflow we check if we had reached this
		// id before in looping
		if (teamIdReached.contains(team.getId()))
			return;

		teamIdReached.add(team.getId());

		for (Team child : team.getChildren()) {
			TeamResponseDTO teamChildResponseDTO = dataModelToDTOConversionService.convertTeamToSimpleDTO(child);
			teamResponseDTO.getChildrenTeams().add(teamChildResponseDTO);
			getRecursiveTeam(child, teamChildResponseDTO, teamIdReached);
		}
	}

	@Transactional(readOnly = true)
	public List<Team> getAllTeamsForCurrentUser() throws RecruizException {
		List<Team> teamList = getAllTeamsForUser(userService.getLoggedInUserObject());
		return teamList;
	}

	@Transactional
	public TeamMember getTeamMember(Long teamId, String userEmail) throws RecruizException {
		TeamMember teamMemberfromDB = teamMemberRepository.getByTeamIdAndUserEmail(teamId, userEmail);
		return teamMemberfromDB;
	}

	@Transactional(readOnly = true)
	public List<Team> getAllTeams() throws RecruizException {
		List<Team> teamList = teamRepository.findAll();
		for (Team team : teamList) {
			if (team.getMembers() != null)
				team.getMembers().size();
		}
		return teamList;
	}

	@Transactional(readOnly = true)
	public List<Team> getAllTeamsForPosition(Position position) throws RecruizException {
		return teamRepository.findDistinctByPositionsIn(position);
	}

	@Transactional
	public Team addUsersToTeam(String teamName, Collection<User> users) throws RecruizException {
		Team team = getTeamByName(teamName);
		return addUserListToTeam(users, team);
	}

	@Transactional
	public Team addUsersToTeam(Long teamId, Collection<User> users) throws RecruizException {
		Team team = getTeamById(teamId);
		return addUserListToTeam(users, team);
	}

	private Team addUserListToTeam(Collection<User> users, Team team) {
		for (User user : users) {
			TeamMember teamMember = new TeamMember();
			teamMember.setRole(TeamRole.member);
			teamMember.setTargetAmount(0L);
			teamMember.setTeam(team);
			teamMember.setUser(user);
			team.getMembers().add(teamMember);
		}
		return save(team);
	}

	@Transactional
	public Team addMembersToTeam(Long teamId, Collection<TeamMember> teamMembers) throws RecruizException {
		Team team = getTeamById(teamId);
		team.getMembers().addAll(teamMembers);
		return save(team);
	}

	@Transactional
	public Team addMemberDtosToTeam(Long teamId, Collection<TeamMemberDTO> teamMemberDTOs) throws RecruizException {
		Team team = getTeamById(teamId);
		List<TeamMember> teamMembers = dtoToDomainConverstionService.convertTeamMemberDTOs(teamMemberDTOs, team);
		teamMembers = teamMemberRepository.save(teamMembers);
		team.getMembers().addAll(teamMembers);
		return save(team);
	}

	@Transactional
	public void removeUserEmailFromTeam(Long teamId, Collection<String> userEmails) throws RecruizException {
		List<TeamMember> tms = teamMemberRepository.getByTeamIdAndUserEmails(teamId, userEmails);
		teamMemberRepository.delete(tms);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void removeUser(User user) throws RecruizException {
		teamMemberRepository.deleteByUser(user);
	}

	@Transactional
	public void addNewTeam(String teamName, String teamDesc, Collection<TeamMember> teamMembers) throws RecruizException {
		Team team = new Team();
		team.getMembers().addAll(teamMembers);
		team.setTeamName(teamName);
		team.setTeamDesc(teamDesc);
		saveAndFlush(team);
	}

	@Transactional
	public void deleteTeamByTeamName(String teamName) throws RecruizException {
		Team team = getTeamByName(teamName);
		teamRepository.delete(team);
	}

	@Transactional
	public void deleteTeamByTeamName(long teamId) throws RecruizException {
		teamRepository.delete(teamId);
	}

	@Transactional
	public TeamMember updateTeamMember(TeamMemberDTO teamMemberDTO) throws RecruizException {
		TeamMember teamMemberfromDB = getTeamMember(teamMemberDTO.getTeamId(), teamMemberDTO.getEmail());
		teamMemberfromDB.setRole(TeamRole.valueOf(teamMemberDTO.getRole()));
		teamMemberfromDB.setTargetAmount(teamMemberDTO.getTargetAmount());
		teamMemberfromDB.setTargetPositionOpeningClosure(teamMemberDTO.getTargetPositionOpeningClosure());
		return teamMemberRepository.save(teamMemberfromDB);

	}

	@Transactional
	public Collection<TeamMember> updateTeamMembers(Collection<TeamMemberDTO> teamMemberDTOs) throws RecruizException {
		Collection<TeamMember> updatedList = new ArrayList<>();
		for (TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
			TeamMember teamMemberfromDB = updateTeamMember(teamMemberDTO);
			updatedList.add(teamMemberRepository.save(teamMemberfromDB));
		}

		return updatedList;

	}

	@Transactional
	public Team addTeamMembers(Collection<TeamMemberDTO> teamMemberDTOs, Team team) throws RecruizException {
		if (team != null && team.getId() != null && team.getId() != 0 && teamMemberDTOs != null) {
			for (TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
				TeamMember teamMember = dtoToDomainConverstionService.convertTeamMemberDTO(teamMemberDTO, team);
				TeamMember teamMemberFromDB = teamMemberRepository.findByTeamAndUser(teamMember.getTeam(), teamMember.getUser());

				if (teamMemberFromDB == null)
					team.getMembers().add(teamMember);
				else {
					team.getMembers().remove(teamMemberFromDB);
					BeanUtils.copyProperties(teamMember, teamMemberFromDB, "id", "team", "user", "creationDate", "modificationDate");
					team.getMembers().add(teamMemberFromDB);

				}
			}
		}
		team = save(team);
		return team;

	}

	@Transactional(readOnly = true)
	public boolean isTeamExists(String teamName) {
		return teamRepository.findOneByTeamName(teamName) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public Team getFullTeamById(Long teamId) {
		Team team = findOne(teamId);
		if (team != null) {
			team.getMembers().size();
			team.getChildren().size();
			if (team.getParent() != null)
				team.getParent().getId();
		}
		return team;
	}

	public List<Long> getAllTeamIds() {
		List<Team> teams = teamRepository.findAll();
		List<Long> teamIds = new ArrayList<>();
		if (null != teams && !teams.isEmpty()) {
			for (Team team : teams) {
				teamIds.add(team.getId());
			}
		}
		return teamIds;
	}

	public Set<User> getTeamMemberUsers(List<Team> teams) {
		return teamMemberRepository.getTeamMemeberUserByTeams(teams);
	}

}
