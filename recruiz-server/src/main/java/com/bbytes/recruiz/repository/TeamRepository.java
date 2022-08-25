package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;

public interface TeamRepository extends JpaRepository<Team, Long> {

	Team findOneByTeamName(String teamName);

	List<Team> findDistinctByPositionsIn(Position position);

	@Query("SELECT DISTINCT t FROM team t JOIN t.members mem WHERE mem.user IN :userList")
	List<Team> getDistinctByMembersIn(@Param("userList") Set<User> users);

	@Query("SELECT DISTINCT t FROM team t LEFT JOIN FETCH t.children ct JOIN t.members mem  WHERE mem.user = :user")
	List<Team> findByMembers(@Param("user") User user);

	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM team t WHERE t.teamName = :teamName")
	boolean existsByTeamName(@Param("teamName") String teamName);

	Team findFirstByRootTeamTrue();

}
