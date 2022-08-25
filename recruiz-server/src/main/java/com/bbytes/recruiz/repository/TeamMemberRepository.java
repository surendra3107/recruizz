package com.bbytes.recruiz.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

	List<TeamMember> findByTeam(Team team);

	TeamMember findOneById(long id);

	TeamMember findByTeamAndUser(Team team, User user);

	List<TeamMember> findByTeamAndUserIn(Team team, Collection<User> user);

	@Transactional
	@Modifying
	void deleteByTeamAndUser(Team team, User user);
	
	@Transactional
	@Modifying
	void deleteByUser(User user);

	@Transactional
	@Modifying
	void deleteByTeamAndUserIn(Team team, Collection<User> users);

	@Query("SELECT tm FROM team_member tm WHERE tm.team.id = :teamId  AND tm.user.email = :userEmail")
	TeamMember getByTeamIdAndUserEmail(@Param("teamId") Long teamId, @Param("userEmail") String userEmail);

	@Query("SELECT tm FROM team_member tm WHERE tm.team.id = :teamId  AND tm.user.email IN :userEmails")
	List<TeamMember> getByTeamIdAndUserEmails(@Param("teamId") Long teamId,
			@Param("userEmails") Collection<String> userEmails);
	
	@Query("SELECT tm.user FROM team_member tm WHERE tm.team IN :teams")
	Set<User> getTeamMemeberUserByTeams(@Param("teams") List<Team> teams);

}
