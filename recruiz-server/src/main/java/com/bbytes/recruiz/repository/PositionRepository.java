package com.bbytes.recruiz.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;

//@JaversSpringDataAuditable
public interface PositionRepository extends JpaRepository<Position, Long>, PositionRepositoryCustom {

	Position findOneByPositionCode(String positionCode);
	
	//Added by Sajin
	Position findOneById(Long pid);
	
	Position findOneById(BigInteger pid);

	@Query(value = "SELECT sum(p.totalPosition) from position p where p.status='Active'", nativeQuery = true)
	Integer getTotalOpenedPosition();

	Position findOneByImportIdentifier(String importIdentifier);

	Page<Position> findAll(Pageable pageable);

	Page<Position> findByIdIn(List<Long> ids, Pageable pageable);

	Page<Position> findByClientAndIdIn(Client client, List<Long> ids, Pageable pageable);

	Page<Position> findByClient(Client client, Pageable pageable);

	List<Position> findByClient(Client client);

	List<Position> findByTitleLike(String title);

	List<Position> findByStatus(String status);

	@Query(value = "select distinct CAST(p.location AS CHAR) as location from position p where p.status=?1 and p.publish_career_site = true", nativeQuery = true)
	List<String> getDistinctLocationForCareerSite(String status);

	@Query(value = "select distinct p.industry from position p where p.status=?1 and p.publish_career_site = true", nativeQuery = true)
	List<String> getDistinctIndustryForCareerSite(String status);

	List<Position> findByStatusAndPublishCareerSiteIsTrueOrderByCreationDateDesc(String status);

	List<Position> findByStatusAndPublishCareerSiteIsTrueAndIdInOrderByCreationDateDesc(String status, Set<Long> ids);

	Page<Position> findByLocationOrIndustryAndStatusAndPublishCareerSiteIsTrueOrderByCreationDateDesc(byte[] location,
			String industry, String status, Pageable pageable);

	@Query("select count(p.positionCode) from position p where p.status = 'Active'")
	public Integer getActivePositionCount();

	public List<Position> findByInterviewersAndClientOrderByCreationDateDesc(ClientInterviewerPanel interviewer,
			Client client);

	List<Position> findByHrExecutivesInOrTeamIn(Set<User> users, List<Team> teams);

	List<Position> findDistinctByOwnerOrHrExecutivesInOrTeamIn(String ownerEmail, Set<User> users, List<Team> teams,
			Sort sort);

	List<Position> findDistinctByOwnerOrHrExecutivesInOrTeamIn(String ownerEmail, Set<User> users, List<Team> teams);

	List<Position> findDistinctByOwnerOrHrExecutivesIn(String ownerEmail, Set<User> users);

	List<Position> findDistinctByLocationOrHrExecutivesIn(String ownerEmail, Set<User> users);

	// Returns the list of positions where either the logged in user is the owner or
	// is part of the HR execs for that position.
	List<Position> findDistinctByOwnerAndIdInOrHrExecutivesInAndIdIn(String ownerEmail, List<Long> positionIds1,
			Set<User> users, List<Long> positionIds2, Sort sort);

	// Returns the list of positions where either the logged in user is the owner or
	// is part of the HR execs for that position or team is part of the position
	List<Position> findDistinctByOwnerOrHrExecutivesOrTeamInAndIdIn(String ownerEmail, Set<User> users,
			List<Team> teams, List<Long> positionIds2, Sort sort);

	List<Position> findByIdIn(List<Long> ids);

	List<Position> findDistinctByOwnerAndIdInOrVendorsInAndIdIn(String ownerEmail, List<Long> positionIds1,
			Set<Vendor> vendors, List<Long> positionIds2, Sort sort);

	List<Position> findDistinctByClientAndOwnerAndIdInOrClientAndHrExecutivesInAndIdIn(Client client, String ownerEmail,
			List<Long> ids, Client client1, Set<User> users, List<Long> ids1, Sort sort);

	List<Position> findDistinctByClientAndIdIn(Client client, List<Long> ids, Sort sort);

	List<Position> findDistinctByOwnerOrHrExecutivesInAndIdIn(String ownerEmail, Set<User> users, List<Long> ids);

	Page<Position> findDistinctByOwnerOrHrExecutivesInOrTeamIn(String ownerEmail, Set<User> users, List<Team> teams,
			Pageable pageable);

	Page<Position> findDistinctByOwnerOrHrExecutivesIn(String ownerEmail, Set<User> users, Pageable pageable);

	@Query(value = "select  DISTINCT p.positionCode from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users or p.team IN :teams and p.modificationDate between :start and :end ")
	List<String> getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(@Param("ownerEmail") String ownerEmail,
			@Param("users") Set<User> users, @Param("teams") List<Team> teams, @Param("start") Date start,
			@Param("end") Date end);

	@Query(value = "select  DISTINCT p.positionCode from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users")
	List<String> getPositionCodesForOwnerOrHrExecutivesIn(@Param("ownerEmail") String ownerEmail,
			@Param("users") Set<User> users);

	
	
	@Query(value = "select DISTINCT p.positionCode from position p JOIN p.hrExecutives hrexecs where hrexecs IN :users")
	List<String> getPositionHrExecutivesIn(@Param("users") Set<User> users);
	
	
	// Original
	// @Query(value = "select DISTINCT p.positionCode from position p JOIN
	// p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users and
	// p.modificationDate between :start and :end ")
	// List<String> getPositionCodesForOwnerOrHrExecutivesIn(@Param("ownerEmail")
	// String ownerEmail, @Param("users") Set<User> users, @Param("start") Date
	// start, @Param("end") Date end);

	// Modified @Sajin
	@Query(value = "select  DISTINCT p.positionCode from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users and (p.modificationDate between :start and :end or p.creationDate between :start and :end) ")
	List<String> getPositionCodesForOwnerOrHrExecutivesIn(@Param("ownerEmail") String ownerEmail,
			@Param("users") Set<User> users, @Param("start") Date start, @Param("end") Date end);
	
	// Modified @Sajin
	@Query(value = "select DISTINCT p.id from position p JOIN p.hrExecutives hrexecs where (p.owner = :ownerEmail or hrexecs IN :users) and (p.status='Active') ")
	List<Long> getActivePositionIDsForOwnerOrHrExecutivesIn(@Param("ownerEmail") String ownerEmail,
				@Param("users") Set<User> users);
		
	
	@Query(value = "select count(DISTINCT  p) from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users or p.team IN :teams ")
	Long countOwnerOrHrExecutivesInOrTeamIn(@Param("ownerEmail") String ownerEmail, @Param("users") Set<User> users,
			@Param("teams") List<Team> teams);

	@Query(value = "select count(DISTINCT  p) from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users ")
	Long countOwnerOrHrExecutivesIn(@Param("ownerEmail") String ownerEmail, @Param("users") Set<User> users);

	@Query(value = "select sum(pos.totalPosition) from position pos where pos.id in (select distinct p.id from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users or p.team IN :teams AND p.status = :status)")
	Long countOwnerOrHrExecutivesInOrTeamInAndStatus(@Param("ownerEmail") String ownerEmail,
			@Param("users") Set<User> users, @Param("teams") List<Team> teams, @Param("status") String status);

	@Query(value = "select sum(pos.totalPosition) from position pos where pos.id in (select distinct p.id from position p JOIN p.hrExecutives hrexecs where p.owner = :ownerEmail or hrexecs IN :users  AND p.status = :status) ")
	Long countOwnerOrHrExecutivesInAndStatus(@Param("ownerEmail") String ownerEmail, @Param("users") Set<User> users,
			@Param("status") String status);

	// Page object gives wrong result when used with OR || AND operator
	// Page<Position>
	// findDistinctByClientAndOwnerOrClientAndHrExecutivesIn(Client client,
	// String ownerEmail,
	// Client client1, Set<User> users, Pageable pageable);

	List<Position> findByClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail, Client client1,
			Set<User> users);

	List<Position> findDistinctByClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail,
			Client client1, Set<User> users);

	List<Position> findByClientAndHrExecutivesIn(Client client, Set<User> users);

	List<Position> findByClientAndStatusAndHrExecutivesIn(Client client, String status, Set<User> users);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = ?2", nativeQuery = true)
	String getStatusCountByClient(Long clientId, String status);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status in (?2)", nativeQuery = true)
	String getStatusCountByClient(Long clientId, List<String> statuses);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status <> ?2 AND closeByDate > ?3 AND closeByDate < ?4", nativeQuery = true)
	String getnearClosureCountByClient(Long clientId, String status, Date today, Date lastDay);

	@Query(value = "select distinct hr_id from position_hr", nativeQuery = true)
	List<BigInteger> getPositionHRIds();

	@Query(value = "select sum(totalPosition) from position where client_id = ?1", nativeQuery = true)
	Long findTotalPositionByClient(long clientId);

	Position findByBoard(Board board);

	@Query(value = "select count(*) from position where id in ( select distinct pos.id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2))", nativeQuery = true)
	String getTotalSize(String loggedInUserEmail, Set<User> loggedInUserSet);

	Position findByPositionCodeAndVendorsIsIn(String positionCode, Set<Vendor> vendors);

	Page<Position> findByVendorsIsIn(Set<Vendor> vendors, Pageable pageable);

	List<Position> findByVendorsIsIn(Set<Vendor> vendors);

	List<Position> findDistinctByStatusAndOwnerOrHrExecutivesInOrTeamIn(String string, String loggedInUserEmail,
			Set<User> loggedInUserSet, List<Team> teams);

	List<Position> findDistinctByStatusAndOwnerOrHrExecutivesIn(String string, String loggedInUserEmail,
			Set<User> loggedInUserSet);

	List<Position> findDistinctByStatusAndHrExecutivesIn(String string, Set<User> loggedInUserSet);

	@Query(value = "select * from position where positionCode in ( select positionCode from round_candidate where candidate_cid = ?1) ", nativeQuery = true)
	List<Position> getPositionsByCandidate(String candidateId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = ?2 and id in (select distinct pos.id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?3 or u.user_id in (?4))", nativeQuery = true)
	String getStatusCountByClientPositionHrAndOwner(Long clientId, String status, String owner, Set<User> hrSet);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status <> ?2 AND closeByDate > ?3 AND closeByDate < ?4 and id in (select distinct pos.id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?5 or u.user_id in (?6))", nativeQuery = true)
	String getnearClosureCountByClientAndHRPosition(Long clientId, String status, Date today, Date lastDay,
			String owner, Set<User> hrset);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status <> ?2 AND closeByDate < ?3 and id in (select distinct pos.id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?4 or u.user_id in (?5))", nativeQuery = true)
	String getPostClosedCountByClient(Long clientId, String status, Date today, String owner, Set<User> hrSet);

	@Query(value = "select distinct HR_ID from position_hr where position_ID IN ( select id from position where owner = ?1) OR HR_ID = ?2", nativeQuery = true)
	List<BigInteger> getPositionHRIdsForHrManager(String hrManagerEmail, String hrManagerUserId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status <> ?2 AND closeByDate < ?3 ", nativeQuery = true)
	String getPositionPostClosedCount(Long clientId, String status, Date today);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status <> ?2 AND closeByDate > ?3 AND closeByDate < ?4 ", nativeQuery = true)
	String getnearClosureCountByClientAndHRPosition(Long clientId, String status, Date today, Date lastDay);

	@Query(value = "SELECT sum(p.totalPosition) from position p where p.status='Active' AND p.id in ?1", nativeQuery = true)
	Integer getTotalOpenedPositionHr(List<Long> ids);

	List<Position> findByDummy(boolean dummyState);

	List<Position> findByCloseByDateBetween(Date startDate, Date lastDate);

	List<Position> findByModificationDateBetween(Date startDate, Date lastDate);

	List<Position> findByStatusNotAndCloseByDateBetween(String statusNotEqual, Date startDate, Date lastDate);

	Page<Position> findByStatusAndHrExecutivesNotIn(String status, Set<User> existingHrs, Pageable pageable);

	@Query(value = "select count(distinct(id)) from position p where p.owner=?1 or p.team_id in (?2) or p.id in (select Position_ID from position_hr where HR_ID = ?3)", nativeQuery = true)
	Integer getCountOfPositionForHr(String loggedInUserEmail, List<Long> teamId, Long userId);

	@Query(value = "select count(distinct(id)) from position p where p.owner=?1  or p.id in (select Position_ID from position_hr where HR_ID = ?2)", nativeQuery = true)
	Integer getCountOfPositionForHr(String email, Long userId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = ?2 AND modification_date > (DATE_SUB(CURDATE(), INTERVAL ?3 MONTH))", nativeQuery = true)
	String getStatusCountByClientForDateRange(Long clientId, String status, int intervalDurationInMonth);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status in (?2) AND modification_date > (DATE_SUB(CURDATE(), INTERVAL ?3 MONTH))", nativeQuery = true)
	String getStatusCountByClientForDateRange(Long clientId, List<String> statuses, int intervalDurationInMonth);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND modification_date > (DATE_SUB(CURDATE(), INTERVAL ?2 MONTH))", nativeQuery = true)
	String getPositionCountByClientForDateRange(Long clientId, int intervalDurationInMonth);

	@Query(value = "SELECT sum(totalPosition) FROM position where client_id = ?1 AND modification_date > (DATE_SUB(CURDATE(), INTERVAL ?2 MONTH))", nativeQuery = true)
	String getTotalOpeningByClientForDateRange(Long clientId, int intervalDurationInMonth);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = ?2 AND modification_date > (DATE_SUB(CURDATE(), INTERVAL ?3 MONTH))", nativeQuery = true)
	String getStatusCountByClientForDateRange(Long clientId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = 'Active' AND closeByDate between CURDATE() AND (DATE_ADD(CURDATE(), INTERVAL 1 MONTH))", nativeQuery = true)
	String getCountForLessThan1MonthClosureDate(Long clientId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = 'Active' AND closeByDate between (DATE_ADD(CURDATE(), INTERVAL 1 MONTH)) AND (DATE_ADD(CURDATE(), INTERVAL 2 MONTH))", nativeQuery = true)
	String getCountFor1To2MonthClosureDate(Long clientId);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND status = 'Active' AND closeByDate > (DATE_ADD(CURDATE(), INTERVAL 2 MONTH))", nativeQuery = true)
	String getCountForMoreThan2MonthClosureDate(Long clientId);

	List<Position> findByClientAndCreationDateBetween(Client client, Date startDate, Date endDate);

	List<Position> findByClientAndTitleInAndCreationDateBetween(Client client, List<String> posNames, Date startDate,
			Date endDate);

	List<Position> findByClientAndPositionCodeInAndCreationDateBetween(Client client, List<String> posCodes,
			Date startDate, Date endDate);

	@Modifying
	@Transactional
	@Query(value = "delete from custom_field_position where name = :name", nativeQuery = true)
	void deleteCustomFieldWithName(@Param("name") String name);

	@Query(value = "select * from position where team_id in(?1) and (DATE(creation_date) between ?2 AND ?3) order by client_id ASC", nativeQuery = true)
	List<Position> getPositionByTeams(List<Long> teamIds, Date startDate, Date endDate);

	@Query(value = "select MONTHNAME(creation_date) AS 'Month',Year(creation_date) AS 'Year', count(*) As 'Total' from position WHERE (team_id IN (?1) OR id in (select Position_ID from position_hr where HR_ID = ?2)) AND (DATE(creation_date) between ?3 AND ?4) group by MONTH order by creation_date ASC", nativeQuery = true)
	List<Object> getDashboardPositionGraphData(List<Long> teamIds, Long hrId, Date startDate, Date endDate);

	@Query(value = "select MONTHNAME(creation_date) AS 'Month',Year(creation_date) AS 'Year', count(*) As 'Total' from client where (owner=?1 OR id in(select distinct(client_id) from position where team_id IN (?2) OR id in (select Position_ID from position_hr where HR_ID = ?3))) AND (DATE(creation_date) between ?4 AND ?5) group by Month order by creation_date ASC", nativeQuery = true)
	List<Object> getDashboardClientGraphData(String hrEmail, List<Long> teamIds, Long hrId, Date startDate,
			Date endDate);

	List<Position> findByTeamAndStatus(Team team, String status);

	List<Position> findByTeamAndStatusIsNotIn(Team team, Set<String> status);

	List<Position> findByTeamAndStatusIsNotInAndModificationDateBetween(Team team, Set<String> status, Date startDate,
			Date endDate);

	List<Position> findByTeamAndStatusAndClosedDateBetween(Team team, Set<String> status, Date startDate, Date endDate);

	@Query(value = "select positionCode from position where team_id in(?1) and status=(?2) and closedDate between ?3 AND ?4", nativeQuery = true)
	List<String> findByTeamAndStatusAndClosedDateBetweenNative(Team team, Set<String> status, Date startDate,
			Date endDate);
	
	//Added by Sajin
	@Query(value = "select id from position where team_id in(?1) and status=(?2)", nativeQuery = true)
	List<Long> findPositionIdByTeamAndStatusAndClosedDateBetweenNative(Team team, Set<String> status);

	List<Position> findByTeamAndClient(Team team, Client client);

	@Query(value = "select * from position where location like ?1 AND verticalCluster is ?2 or verticalCluster like ?3 and team_id = ?4 and client_id in (?5)", nativeQuery = true)
	List<Position> getPositionsByClientsAndLocationAndTeamAndVerticals(String location, String verticalOrginalText,
			String vertical, Long teamId, List<Long> clientIds);

	@Query(value = "select positionCode from position where location like ?1 AND verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and client_id in (?5)", nativeQuery = true)
	List<String> getPositionsCodeByClientsAndLocationAndTeamAndVerticals(String location, String verticalOrginalText,
			String vertical, Long teamId, List<Long> clientIds);

	// Original @Sajin
	// @Query(value = "select positionCode from position where location like ?1 AND
	// verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and client_id
	// in (?5) and modification_date between ?6 AND ?7", nativeQuery = true)
	// List<String>
	// getPositionsCodeByClientsAndLocationAndTeamAndVerticalsAndDateRange(String
	// location, String verticalOrginalText, String vertical,
	// Long teamId, List<Long> clientIds,Date startDate,Date endDate);

	// Modified @Sajin - Added creation date into the query
	@Query(value = "select positionCode from position where location like ?1 AND verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and client_id in (?5) and (modification_date between ?6 AND ?7 or creation_date between ?6 AND ?7)", nativeQuery = true)
	List<String> getPositionsCodeByClientsAndLocationAndTeamAndVerticalsAndDateRange(String location,
			String verticalOrginalText, String vertical, Long teamId, List<Long> clientIds, Date startDate,
			Date endDate);

	// Modified @Sajin - Added creation date into the query and returning ids
	@Query(value = "select id from position where location like ?1 AND verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and client_id in (?5) and (modification_date between ?6 AND ?7 or creation_date between ?6 AND ?7)", nativeQuery = true)
	List<BigInteger> getPositionIdsByClientsAndLocationAndTeamAndVerticalsAndDateRange(String location,
			String verticalOrginalText, String vertical, Long teamId, List<Long> clientIds, Date startDate, Date endDate);

	@Query(value = "select * from position where location like ?1 AND verticalCluster is ?2 or verticalCluster like ?3 and team_id = ?4", nativeQuery = true)
	List<Position> getPositionsByLocationAndTeamAndVerticals(String location, String verticalOrginalText,
			String vertical, Long teamId);

	@Query(value = "select positionCode from position where location like ?1 AND verticalCluster is ?2 or verticalCluster like ?3 and team_id = ?4", nativeQuery = true)
	List<String> getPositionsCodeByLocationAndTeamAndVerticals(String location, String verticalOrginalText,
			String vertical, Long teamId);

	// Original @Sajin
	// @Query(value = "select positionCode from position where location like ?1 AND
	// verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and
	// modification_date between ?5 AND ?6", nativeQuery = true)
	// List<String> getPositionsCodeByLocationAndTeamAndVerticalsAndDateRange(String
	// location, String verticalOrginalText, String vertical, Long teamId,Date
	// startDate,Date endDate);

	// Modified @Sajin - Added creation date into the query
	@Query(value = "select positionCode from position where location like ?1 AND verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and (modification_date between ?5 AND ?6 or creation_date between ?5 AND ?6)", nativeQuery = true)
	List<String> getPositionsCodeByLocationAndTeamAndVerticalsAndDateRange(String location, String verticalOrginalText,
			String vertical, Long teamId, Date startDate, Date endDate);
	
	// Modified @Sajin - Added creation date into the query and returning ids
	@Query(value = "select id from position where location like ?1 AND verticalCluster=?2 or verticalCluster like ?3 and team_id = ?4 and (modification_date between ?5 AND ?6 or creation_date between ?5 AND ?6)", nativeQuery = true)
	List<BigInteger> getPositionIdsByLocationAndTeamAndVerticalsAndDateRange(String location, String verticalOrginalText,
				String vertical, Long teamId, Date startDate, Date endDate);
		

	@Query(value = "select * from position where team_id in(?1)", nativeQuery = true)
	List<Position> getPositionByTeams(String ids);

	@Query(value = "select positionCode from position where team_id in (?1)", nativeQuery = true)
	List<String> getPositionCodeByTeams(List<Long> teamIds);

	@Query(value = "select positionCode from position where team_id in (?1) and status=?2", nativeQuery = true)
	List<String> getPositionCodeByTeamsAndStatus(List<Long> teamIds, String status);

	// Original @Sajin
	// @Query(value = "select positionCode from position where team_id in (?1) and
	// status=?2 and modification_date between ?3 AND ?4 ", nativeQuery = true)
	// List<String> getPositionCodeByTeamsAndStatusAndDateRange(List<Long> teamIds,
	// String status,Date startDate,Date endDate);

	// Modified @Sajin
	@Query(value = "select positionCode from position where team_id in (?1) and status=?2 and (modification_date between ?3 AND ?4 or creation_date between ?3 AND ?4)", nativeQuery = true)
	List<String> getPositionCodeByTeamsAndStatusAndDateRange(List<Long> teamIds, String status, Date startDate,
			Date endDate);
	
	// Modified @Sajin - Return ids
	@Query(value = "select id from position where team_id in (?1) and status=?2", nativeQuery = true)
	List<Long> getIdsByTeamsAndStatusAndDateRange(List<Long> teamIds, String status);

	@Query(value = "select positionCode from position where team_id in (?1) and status=?2 and closedDate between ?3 AND ?4  ", nativeQuery = true)
	List<String> getPositionCodeByTeamsAndClosedStatusAndDateRange(List<Long> teamIds, String status, Date startDate,
			Date endDate);

	@Query(value = "select * from position where team_id in (?1) AND verticalCluster = ?2", nativeQuery = true)
	List<Position> getPositionByTeamsAndVertical(String ids, String verticalCluster);

	@Query(value = "select positionCode from position where team_id in (?1) AND verticalCluster = ?2", nativeQuery = true)
	List<String> getPositionCodeByTeamsAndVertical(String ids, String verticalCluster);

	@Query(value = "SELECT YEAR(t.modification_date), MONTHNAME(t.modification_date), WEEK(t.modification_date) AS week, t.requisitionId, t.title, COUNT(rc.candidate_cid) AS sourcedCount, rc.positionCode FROM position t, round_candidate rc WHERE t.positionCode = rc.positionCode AND t.modification_date between ?1 AND ?2 GROUP BY rc.positionCode ORDER BY t.modification_date;", nativeQuery = true)
	List<Object> getPositionForteamwarePrefTrends(Date startDate, Date endDate);

	@Query(value = "select distinct(p.id) from position p where p.team_id in (?1) or p.owner=?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3); ", nativeQuery = true)
	List<Long> getPositionIdsForUserWithTeam(List<Long> teamIds, String owner, Long userId);

	@Query(value = "SELECT * FROM interview_schedule WHERE interviewSchedulerEmail = ?1 AND positionCode IN (SELECT p.positionCode FROM position p WHERE p.team_id IN (?2) OR p.owner = ?3 OR p.id IN (SELECT Position_ID FROM position_hr WHERE HR_ID = 4)) AND startsAt BETWEEN ?5 AND ?6", nativeQuery = true)
	List<InterviewSchedule> getAllSchedulesForUser(String hrEmail, List<Long> teamIds, String owner, Long hrId,
			Date start, Date end);

	@Query(value = "select sum(distinct(p.totalPosition)) from position p where p.team_id in (?1) or p.owner=?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3); ", nativeQuery = true)
	Long getTotalOpenedPositionForUser(List<Long> teamIds, String owner, Long userId);

	@Query(value = "select clientName from client where id = (select distinct(client_id) from position where positionCode = ?1)", nativeQuery = true)
	String getClientNameForPosition(String pcode);
	
	//Added by Sajin
	@Query(value = "select clientName from client where id = (select distinct(client_id) from position where id = ?1)", nativeQuery = true)
	String getClientNameForPositionID(Long pid);
	
	//Added by Sajin
	@Query(value = "select clientName from client where id = (select distinct(client_id) from position where id = ?1)", nativeQuery = true)
	String getClientNameForPositionID(BigInteger pid);

	@Query(value = "select sum(totalPosition) from position where positionCode in (?1)", nativeQuery = true)
	Long getTotalPositionsByPositionCodes(List<String> positioCodes);
	
	//Added by Sajin
	@Query(value = "select sum(totalPosition) from position where id in (?1)", nativeQuery = true)
	Long getTotalPositionsByPositionIDs(List<Long> positionIds);
	
	//Added by Sajin
	@Query(value = "select sum(totalPosition) from position where id in (?1)", nativeQuery = true)
	Long getTotalPositionsByPositionIDsBigInteger(List<BigInteger> positionIds);

	@Query(value = "SELECT  distinct WEEK(t.creation_date) AS week FROM position t WHERE t.creation_date between ?1 AND ?2 ORDER BY week asc;", nativeQuery = true)
	List<Integer> getWeeksForGivenDateRangeBasedOnCreationDate(Date startDate, Date endDate);

	@Query(value = "select positionCode from position where week(creation_date) = ?1", nativeQuery = true)
	List<String> getPositionCodeFromWeeks(Integer weekNo);

	@Query(value = "select distinct(client_id) from position", nativeQuery = true)
	List<Long> getDistinctClientIds();

	@Query(value = "select distinct(verticalCluster) from position", nativeQuery = true)
	List<String> getDistinctVertical();

	@Query(value = "select positionCode from position where week(modification_date) = ?1 AND year(modification_date) = ?2 AND client_id IN (?3) AND verticalCluster in (?4)  AND location = ?5 AND status IN ?6 AND (owner=?7 or  team_id in (?8) or id in (select Position_ID from position_hr where HR_ID = ?9))", nativeQuery = true)
	List<String> getPositionCodesByClientVerticalAndLocation(Integer weekNo, Integer year, List<Long> clientIds,
			List<String> vertical, String location, List<String> statusIn, String loggedInUserEmail, List<Long> teamIds,
			Long userId);

	@Query(value = "select positionCode from position where week(modification_date) = ?1 AND year(modification_date) = ?2 AND client_id IN (?3) AND verticalCluster in (?4)  AND status IN ?5  AND (owner=?6 or  team_id in (?7) or id in (select Position_ID from position_hr where HR_ID = ?8))", nativeQuery = true)
	List<String> getPositionCodesByClientVertical(Integer weekNo, Integer year, List<Long> clientIds,
			List<String> vertical, List<String> status, String loggedInUserEmail, List<Long> teamIds, Long userId);

	@Query(value = "SELECT  distinct week(t.modification_date) AS year FROM position t WHERE MonthNAME(t.modification_date) = ?1 AND Year(t.modification_date) = ?2 AND  t.modification_date between ?3 AND ?4 ORDER BY year asc", nativeQuery = true)
	List<Integer> getWeekForGivenYearAndMonth(String month, Integer year, java.sql.Date startDate,
			java.sql.Date endDate);

	@Query(value = "SELECT  distinct MonthNAME(t.modification_date) AS month FROM position t WHERE Year(t.modification_date) = ?1 AND  t.modification_date between ?2 AND ?3 ORDER BY month asc", nativeQuery = true)
	List<String> getMonthForGivenYear(Integer year, java.sql.Date startDate, java.sql.Date endDate);

	@Query(value = "SELECT  distinct Year(t.modification_date) AS year FROM position t WHERE t.modification_date between ?1 AND ?2 ORDER BY year asc;", nativeQuery = true)
	List<Integer> getYearForGivenDate(java.sql.Date startDate, java.sql.Date endDate);

	@Query(value = "SELECT title from position where positionCode IN (?1)", nativeQuery = true)
	List<String> getPositionNamesfromPositionCodes(List<String> positionCodes);
	
	@Query(value = "SELECT title from position where id IN (?1)", nativeQuery = true)
	List<String> getPositionNamesfromPositionIDs(List<Long> positionIDs);
	

	@Query(value = "select * from position where (DATE(creation_date) between ?1 AND ?2) order by client_id ASC", nativeQuery = true)
	List<Position> getPositionsBetweenModificationDates(Date startDate, Date endDate);
	
	@Query(value = "select positionCode from position where id IN (?1)", nativeQuery = true)
	List<String> getPositionCodesforPostionIds(List<Long> positionIds);

	@Query(value = "SELECT count(*) FROM position where client_id = ?1 AND closeByDate < ?2 AND status = 'Active'", nativeQuery = true)
	String getCountForPastCloseByDate(Long id, Date date);

	@Query(value = "select * from position p where p.title =?2 AND p.client_id = ?1", nativeQuery = true)
	Position getPositionByNameAndClientName(long clientName, String positionName);
}
