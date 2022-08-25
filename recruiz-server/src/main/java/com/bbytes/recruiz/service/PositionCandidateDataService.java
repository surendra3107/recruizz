package com.bbytes.recruiz.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.PositionCandidateData;
import com.bbytes.recruiz.repository.PositionCandidateDataRepository;
import com.bbytes.recruiz.utils.TeamwareConstants;

/**
 * @author Sajin
 *
 */
@Service
public class PositionCandidateDataService extends AbstractService<PositionCandidateData, Long> {
	private static Logger logger = LoggerFactory.getLogger(PositionService.class);
	
	@Autowired
	private UserService userService;

	private PositionCandidateDataRepository positionCandidateDataRepository;

	@Autowired
	public PositionCandidateDataService(PositionCandidateDataRepository positionCandidateDataRepository) {
		super(positionCandidateDataRepository);
		this.positionCandidateDataRepository = positionCandidateDataRepository;
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionIdsAndStatusAndOwnerAndDateRange(List<Long> positionIds, String status, String owner,
			Date startDate, Date endDate) {
		
		Long userID = userService.getUserByEmail(owner).getUserId();
		
		logger.error("Position Ids: " +positionIds + "Status: " +status +" user ID: " +userID +"Startdate: " +startDate +"EndDate: " +endDate);
		logger.error("Query Count: " +positionCandidateDataRepository.getCountByPositionIDsAndStatusAndOwnerAndDateRange(positionIds,
				status, userID, startDate, endDate));		
		
		return positionCandidateDataRepository.getCountByPositionIDsAndStatusAndOwnerAndDateRange(positionIds,
				status, userID, startDate, endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionIDsAndStatusesAndOwnerAndDateRange(List<Long> positionIDs, List<String> statuses, String owner,
			Date startDate, Date endDate) {
		
		Long userID = userService.getUserByEmail(owner).getUserId();
		
		logger.error("Position Ids: " +positionIDs + "Status: " +statuses +" user ID: " +userID +"Startdate: " +startDate +"EndDate: " +endDate);
		logger.error("Query Count: " +positionCandidateDataRepository.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIDs,
				statuses, userID, startDate, endDate));
		
		return positionCandidateDataRepository.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIDs,
				statuses, userID, startDate, endDate);
	}
	
	
	@Transactional(readOnly = true)
	public Long getcountByPositionIdAndStatusesInAndDateRange(BigInteger positionId, List<String> statuses,Date startDate,Date endDate) {
		return positionCandidateDataRepository.countByPositionIdAndStatusesInAndModificationDateBetween(positionId, statuses, startDate, endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionIdAndStatusAndDateRange(BigInteger positionId, String status,Date startDate,Date endDate) {
		return positionCandidateDataRepository.countByPositionIdAndStatusAndModificationDateBetween(positionId, status,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionIdAndDateRange(BigInteger positionId,Date startDate,Date endDate) {
		return positionCandidateDataRepository.countByPositionIdAndModificationDateBetween(positionId,startDate,endDate);
	}

	public Set<Long> findCandidateIdsBySourcebyAndStatusBetweenDate(String clientSubmission, Long userId, List<String> statusList, Date startDate,
			Date endDate) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = positionCandidateDataRepository.findCandidateIdsBySourcebyAndStatusBetweenDate(userId, statusList,
				startDate, endDate,clientSubmission);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
		
	}

	public Set<Long> findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(String clientSubmission, Long userId, String selected, Date startDate,
			Date endDate) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = positionCandidateDataRepository.findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(userId, selected,
				startDate, endDate,clientSubmission);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
		
	}

	public Set<Long> findCandidateIdsByClientsbyAndStatusBetweenDate(List<Long> positionNameList, List<String> statusList,
			Date startDate, Date endDate, String clientSubmission) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = positionCandidateDataRepository.findCandidateIdsByClientbyAndStatusBetweenDate(positionNameList, statusList,
				startDate, endDate,clientSubmission);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}

	public Set<Long> findCandidateIdsByClientbyAndSelectedStatusBetweenDate(List<Long> positionNameList,
			String selected, Date startDate, Date endDate, String clientSubmission) {
	
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = positionCandidateDataRepository.findCandidateIdsByClientbyAndSelectedStatusBetweenDate(positionNameList, selected,
				startDate, endDate,clientSubmission);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}

}