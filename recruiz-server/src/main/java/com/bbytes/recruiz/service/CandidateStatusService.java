package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateStatusRepository;

@Service
public class CandidateStatusService extends AbstractService<CandidateStatus, Long> {

	private static Logger logger = LoggerFactory.getLogger(CandidateStatusService.class);

	@Autowired
	private CandidateStatusRepository candidateStatusRepository;

	@Autowired
	private ClientService clientService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateService candidateService;
	

	@Autowired
	public CandidateStatusService(CandidateStatusRepository candidateStatusRepository) {
		super(candidateStatusRepository);
		this.candidateStatusRepository = candidateStatusRepository;
	}

	@Transactional
	public void deleteByCandidateId(Long cid) throws RecruizException {
		List<CandidateStatus> candidateStatus = candidateStatusRepository.findByCandidateCid(cid);
		delete(candidateStatus);
	}
	
	@Transactional
	public void deleteByPositionId(long positionId) throws RecruizException{
		List<CandidateStatus> candidateStatus =  candidateStatusRepository.findByPositionId(positionId);
		delete(candidateStatus);
	}
	
	@Transactional
	public void deleteByClient(){
		
	}
	
	@Transactional
	public void deleteByCandidateIdAndPositionCode(Long cid,String postionCode) throws RecruizException {
		List<CandidateStatus> candidateStatus = candidateStatusRepository.findByCandidateCidAndPositionPositionCode(cid, postionCode);
		delete(candidateStatus);
	}
	
	public Date getOfferedDate(long clientId, String positionCode, long candidateId, String status)
			throws RecruizException {
		Client client = clientService.findOne(clientId);
		Position position = positionService.getPositionByCode(positionCode);
		Candidate candidate = candidateService.getCandidateById(candidateId);

		return candidateStatusRepository.getOfferedDate(client, position, candidate, status);
	}

	public List<CandidateStatus> getByCandidateIdAndClientIdAndPositionCode(long clientId, long candidateId,
			String positionCode) {
		return candidateStatusRepository.findByCandidateCidAndClientIdAndPositionPositionCode(candidateId, clientId,
				positionCode);
	}

	public CandidateStatus getJoinedCandidateStatusForPosition(long clientId, long candidateId, String positionCode,
			String status) {
		List<CandidateStatus> candidateStausList = candidateStatusRepository
				.findByCandidateCidAndClientIdAndPositionPositionCodeAndStatusAndCurrentIsTrue(candidateId, clientId,
						positionCode, status);
		if (candidateStausList != null && !candidateStausList.isEmpty()) {
			return candidateStausList.get(0);
		}
		return null;

	}

	@Transactional
	public void addCandidateStatus(long clientId, long candidateId, String positionCode, String status)
			throws RecruizException {
		List<CandidateStatus> candidateStatus = getByCandidateIdAndClientIdAndPositionCode(clientId, candidateId,
				positionCode);
		if (candidateStatus != null && !candidateStatus.isEmpty()) {
			for (CandidateStatus candidateStatusFromDb : candidateStatus) {
				candidateStatusFromDb.setCurrent(false);
			}
			save(candidateStatus);
		}

		try {
			CandidateStatus nwCandidateStatus = new CandidateStatus();
			nwCandidateStatus.setClient(clientService.findOne(clientId));
			nwCandidateStatus.setPosition(positionService.getPositionByCode(positionCode));
			nwCandidateStatus.setCandidate(candidateService.getCandidateById(candidateId));
			nwCandidateStatus.setStatus(status);
			nwCandidateStatus.setStatusChangedDate(new Date());
			nwCandidateStatus.setCurrent(true);
			save(nwCandidateStatus);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public List<CandidateStatus> getJoinedCandidateStatus() {
	    List<String> status = new ArrayList<>();
	    status.add(BoardStatus.Joined.getDisplayName());
	    status.add(BoardStatus.Employee.getDisplayName());
		return candidateStatusRepository
				.findByStatusInAndCurrentIsTrueOrderByCreationDateDesc(status);
	}
	
	public List<CandidateStatus> getJoinedCandidateStatusByClientNameAndPositionCodeAndStatus(String clientName,String positionCode,String status){
		return candidateStatusRepository.findByClientClientNameAndPositionPositionCodeAndStatusAndCurrentIsTrueOrderByCreationDateDesc(clientName, positionCode, status);
	}

	@Transactional(readOnly = true)
	public List<CandidateStatus> getByCandidate(Candidate candidate) {
		return candidateStatusRepository.findByCandidate(candidate);
	}
	
	@Transactional(readOnly = true)
	public Candidate getByEmail(String candidateEmail) throws RecruizException{
		return candidateService.getCandidateByEmail(candidateEmail);
	}
	
	@Transactional(readOnly = true)
	public long getCandidateIdByEmail(String candidateEmail) throws RecruizException{
		return candidateService.getCandidateByEmail(candidateEmail).getCid();
	}

}
