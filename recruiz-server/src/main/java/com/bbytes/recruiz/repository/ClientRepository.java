package com.bbytes.recruiz.repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;

//@JaversSpringDataAuditable
public interface ClientRepository extends JpaRepository<Client, Long> {

	Client findOneByClientName(String name);

	Client findByClientName(String clientName);
	
	List<Client> findByClientNameIn(List<String> clientNames);

	@Query("select c.clientName from client c where c.status = 'Active'")
	public List<String> getClientNames();

	@Query("select c.id from client c")
	public List<Long> getClientIds();

	@Query("select c.id from client c")
	public Page<Long> getClientIds(Pageable pageable);

	// @Query(value = "SELECT c.id from client c where c.positions IN ?1",
	// nativeQuery = true)
	// public List<Long> getClientIdsByHrExecutive(List<Position> positions);
	//
	public List<Client> findByPositionsIn(List<Position> positions);

	@Query("select count(c.clientName) from client c where c.status = 'Active'")
	public Integer getActiveClientCount();

	List<ClientOpeningCountDTO> clientListWithTotalOpening(@Param("ids") List<Long> ids);

	Page<ClientOpeningCountDTO> clientListWithTotalOpening(@Param("ids") List<Long> ids, Pageable pageable);

	ClientOpeningCountDTO clientWithTotalOpening(Long id);

	@Query(value = "select c.id from client c where c.owner = ?1", nativeQuery = true)
	public List<BigInteger> getClientIdsByOwner(String ownerEmail);

	public List<Client> findByOwner(String email);

	@Query(value = "select c.id from client c where c.id IN (select distinct pos.client_id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2) or pos.team_id in (?4)) OR c.owner =?3", nativeQuery = true)
	public List<BigInteger> findByClientAndPositions(String loggedInUserEmail, Set<User> loggedInUserSet, String loggedInUserEmail1,
			List<Long> teamIds);
	
	@Query(value = "select c.id from client c where c.id IN (select distinct pos.client_id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2)) OR c.owner =?3", nativeQuery = true)
	public List<BigInteger> findByClientAndPositions(String loggedInUserEmail, Set<User> loggedInUserSet, String loggedInUserEmail1);


	@Query(value = "select c.id from client c INNER JOIN (select cl.id from client cl where cl.id IN (select distinct pos.client_id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2))OR cl.owner = ?3) as table1 ON c.id=table1.id where c.id in (?4)", nativeQuery = true)
	public List<BigInteger> findByClientAndPositionsAndIdIn(String loggedInUserEmail, Set<User> loggedInUserSet, String loggedInUserEmail1,
			List<Long> ids);

	public List<Client> findByPositionsInOrOwner(List<Position> positions, String owner);

	@Query(value = "select c.clientName from client c where c.id IN (select distinct pos.client_id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2))OR c.owner = ?3", nativeQuery = true)
	public List<String> findClientNameByClientAndPositions(String hrManagerEmail, Set<User> loggedInUserSet, String clientOwnerEmail);

	@Query(value = "select c.id from client c where c.id IN (select distinct pos.client_id from position pos left outer join position_hr hrexecutiv on pos.id=hrexecutiv.Position_ID left outer join user u on hrexecutiv.HR_ID=u.user_id where pos.owner=?1 or u.user_id in (?2)) OR c.owner =?3 AND c.id IN (?4)", nativeQuery = true)
	public List<BigInteger> findByClientAndPositionsByManagerClients(String loggedInUserEmail, Set<User> loggedInUserSet,
			String loggedInUserEmail1, List<Long> managerIds);

	@Query(value = "select c.clientName from client c where c.id IN (?1)", nativeQuery = true)
	public List<String> findClientNameByClientIds(List<Long> ids);

	@Query(value = "select distinct id from client where id in (select client_id from position where id in (select Position_ID from position_hr where HR_ID = ?1))", nativeQuery = true)
	public List<BigInteger> getClientIdForHrExecutive(String userId);

	List<Client> findByDummy(boolean dummyState);

	public List<Client> findByClientInterviewerPanelIn(Set<ClientInterviewerPanel> interviewPanelList);

	public List<Client> findByClientDecisionMakerIn(Set<ClientDecisionMaker> dms);

	@Query(value = "select distinct(id),clientName from client where id in (select client_id from position where id in (select Position_ID from position_hr where HR_ID = ?1))", nativeQuery = true)
	public List<Object> getClientNameIdForHrExecutive(String userId);

	@Transactional
	@Modifying
	@Query(value = "delete from custom_field_client where name = :name" , nativeQuery=true)
	void deleteCustomFieldWithName(@Param("name") String name);

	@Query(value="select id from client where clientName = ?1",nativeQuery=true)
	Long getIdByClient(String name);

	@Query(value="select distinct clientLocation from client ")
	Set<String> getClientLocations();
	
	@Query(value = "select distinct(c.id) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where p.team_id in (?2) or p.owner= ?3 or p.id in (select Position_ID from position_hr where HR_ID = ?4))", nativeQuery = true)
	List<BigInteger> getClientIdsForUserWithTeam(String owner, Collection<Long> teamIds, String hrEmail, Long userId);
	    
	@Query(value = "select distinct(c.id) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where "
	    		+ " p.owner= ?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3))", nativeQuery = true)
	List<BigInteger> getClientIdsForUserWithOutTeam(String owner, String hrEmail, Long userId);
	
	@Query(value = "select distinct(c.clientName) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where p.owner= ?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3))", nativeQuery = true)
	List<String> getClientNamesForUserWithOutTeam(String loggedInUserEmail, String loggedInUserEmail2, Long userId);

	@Query(value = "select  distinct(c.clientName) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where p.team_id in (?2) or p.owner= ?3 or p.id in (select Position_ID from position_hr where HR_ID = ?4))", nativeQuery = true)
	List<String> getClientNamesForUserWithTeam(String loggedInUserEmail, Set<Long> teamIds, String loggedInUserEmail2, Long userId);
	
	@Query(value = "select * from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where p.team_id in (?2) or p.owner= ?3 or p.id in (select Position_ID from position_hr where HR_ID = ?4))", nativeQuery = true)
	List<Client> getClientForUserWithTeam(String loggedInUserEmail, Set<Long> teamIds, String loggedInUserEmail2, Long userId);
	
	@Query(value = "select * from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where  p.owner= ?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3))", nativeQuery = true)
	List<Client> getClientForUserWithOutTeam(String loggedInUserEmail, String loggedInUserEmail2, Long userId);
	
	
	@Query(value = "select count(distinct(c.id)) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where p.team_id in (?2) or p.owner= ?3 or p.id in (select Position_ID from position_hr where HR_ID = ?4))", nativeQuery = true)
	Long getClientCountForUserWithTeam(String owner, Collection<Long> teamIds, String hrEmail, Long userId);
	    
	@Query(value = "select count(distinct(c.id)) from client c where c.owner = ?1 or c.id in (select distinct(p.client_id) from position p where "
	    		+ " p.owner= ?2 or p.id in (select Position_ID from position_hr where HR_ID = ?3))", nativeQuery = true)
	Long getClientCountForUserWithOutTeam(String owner, String hrEmail, Long userId);

	//Find clients by creation date 
	public List<Client> findByCreationDateBetween(Date startDate, Date endDate);


}
