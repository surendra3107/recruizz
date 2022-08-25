package com.bbytes.recruiz.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateActivity;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.InactiveSinceRange;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.RoundCandidateRepository;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.InactiveCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class RoundCandidateService extends AbstractService<RoundCandidate, Long> {

	private final Logger logger = LoggerFactory.getLogger(RoundCandidateService.class);

	private RoundCandidateRepository roundCandidateRepository;
	
	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CandidateStatusService candidateStatusService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Value("${base.url}")
	private String baseUrl;

	private List<String> CandidateList;
	
	@Autowired
	public RoundCandidateService(RoundCandidateRepository roundCandidateRepository) {
		super(roundCandidateRepository);
		this.roundCandidateRepository = roundCandidateRepository;
	}

	@Transactional(readOnly = true)
	public boolean isRoundCandidateExist(Candidate candidate, String roundId) {
		boolean state = roundCandidateRepository.findOneByCandidateAndRoundId(candidate, roundId) == null ? false : true;
		return state;
	}

	@Transactional(readOnly = true)
	public RoundCandidate getCandidateByIdAndRoundId(Candidate candidate, String roundId) {
		return roundCandidateRepository.findOneByCandidateAndRoundId(candidate, roundId);
	}

	@Transactional(readOnly = true)
	public List<RoundCandidate> getCandidateByRoundId(String roundId) {
		return roundCandidateRepository.findByRoundId(roundId);
	}

	@Transactional(readOnly = true)
	public RoundCandidate getCandidate(Candidate candidate) {
		return roundCandidateRepository.findOneByCandidate(candidate);
	}

	@Transactional(readOnly = true)
	public List<RoundCandidate> getAllRoundCandidates(Candidate candidate) {
		return roundCandidateRepository.findByCandidate(candidate);
	}

	@Transactional(readOnly = true)
	public RoundCandidate getRoundcandidateByPosition(Candidate candidate, String positionCode) {
		return roundCandidateRepository.findOneByCandidateAndPositionCode(candidate, positionCode);
	}

	@Transactional(readOnly = true)
	public double getMinCardIndex(String roundId) {
		RoundCandidate roundCandidate = roundCandidateRepository.findTop1ByRoundIdOrderByCardIndexAsc(roundId);
		// since no candidate in round then starting min index from 100000
		if (roundCandidate != null && roundCandidate.getCardIndex() != null) {
			return roundCandidate.getCardIndex();
		} else {
			return 100000;
		}
			
	}

	@Transactional(readOnly = true)
	public List<RoundCandidate> getByPostionCodeAndStatus(String postionCode, String status) throws RecruizException {
		return roundCandidateRepository.findByPositionCodeAndStatus(postionCode, status);
	}

	@Transactional(readOnly = true)
	public Map<String, String> getBoardCandidateCount(String positionCode) throws RecruizException {
		List<Object> boardRoundIds = roundCandidateRepository.findBoardRound(positionService.getPositionBoard(positionCode).getId() + "");

		Map<String, String> roundName_CountMap = new LinkedHashMap<String, String>();
		for (Object roundId : boardRoundIds) {

			String roundName = roundService.getRoundName(roundId + "");
			// checking user has view all candidate permission
			if (checkPermissionService.isSuperAdmin() || checkPermissionService.hasViewAllCandidatesPermission()) {
				String count = roundCandidateRepository.findCandidateCountForRound(roundId + "");
				roundName_CountMap.put(roundName, count);
			} else {
				String count = roundCandidateRepository.findCandidateCountForRoundAndSourcedBy(roundId + "",
						userService.getLoggedInUserEmail());
				roundName_CountMap.put(roundName, count);
			}

		}
		return roundName_CountMap;
	}

	@Transactional(readOnly = true)
	public RoundCandidate getExistingBoardCandidate(Candidate candidate, String positionCode) {
		return roundCandidateRepository.findByPositionCodeAndCandidate(positionCode, candidate);
	}

	@Transactional(readOnly = true)
	public List<Long> getCandidateIds(Collection<String> positionCodes, String sourcedBy, Collection<String> statusList, Date startDate,
			Date endDate) {

		List<Long> candidateIds = new ArrayList<Long>();
		List<Object> candidateIdsFromDB = null;

		if (statusList != null && !statusList.isEmpty()) {
			if (positionCodes != null && !positionCodes.isEmpty()) {
				candidateIdsFromDB = roundCandidateRepository.findCandidateIdsByPositionCodeAndSourcebyAndStatusBetweenDate(positionCodes,
						sourcedBy, statusList, startDate, endDate);
			} else {
				candidateIdsFromDB = roundCandidateRepository.findCandidateIdsBySourcebyAndStatusBetweenDate(sourcedBy, statusList,
						startDate, endDate);
			}
		} else {

			// if status is empty then it like candidate sourced to pipeline and
			// looking for creation date
			if (positionCodes != null && !positionCodes.isEmpty()) {
				candidateIdsFromDB = roundCandidateRepository.findCandidateIdsByPositionCodeAndSourcebyBetweenDate(positionCodes, sourcedBy,
						startDate, endDate);
			} else {
				candidateIdsFromDB = roundCandidateRepository.findCandidateIdsBySourcebyBetweenDate(sourcedBy, startDate, endDate);
			}
		}

		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}

	/**
	 * @param roundCandidateDTO
	 * @param sourceMode
	 * @throws Exception
	 */
	//@Transactional
	public List<String> addCandidateToPosition(CandidateToRoundDTO roundCandidateDTO, String sourceMode) throws Exception {
		Position position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
		if (position == null)
			return CandidateList;

		Set<Candidate> candidateSet = new HashSet<Candidate>();
		// TODO : We need to pull all candidates in one query
		for (String email : roundCandidateDTO.getCandidateEmailList()) {
			Candidate candidate = candidateService.getCandidateByEmail(email);
			if (candidate != null && Status.Active.toString().equalsIgnoreCase(candidate.getStatus()))
				candidateSet.add(candidate);
		}
		Candidate[] candidateArray = candidateSet.toArray(new Candidate[candidateSet.size()]);

		Round round = null;
		if (roundCandidateDTO.getRoundId() == null || roundCandidateDTO.getRoundId().trim().isEmpty()) {
			round = roundService.getRoundByBoardAndType(position.getBoard(), RoundType.Source.getDisplayName());
			CandidateList = sourceCandidateToBoard(position, round, candidateArray);
		} else {
			if (sourceMode != null && !sourceMode.isEmpty()) {
				if (!sourceMode.equalsIgnoreCase(GlobalConstants.SOURCE_MODE_OUTSIDE)) {
					throw new RecruizWarnException(ErrorHandler.INVALID_SOURCE_MODE, ErrorHandler.INVALID_MODE);
				}

				round = roundService.getRoundByBoardAndType(position.getBoard(), RoundType.Source.getDisplayName());

				CandidateList = sourceCandidateToBoard(position, round, candidateArray);
			} else {
				String roundId = roundCandidateDTO.getRoundId();
				round = roundService.findOne(Long.parseLong(roundId));
				CandidateList = sourceCandidateToBoard(position, round, candidateArray);
			}
		}
		return CandidateList;
	}

	/**
	 * @param roundCandidateDTO
	 * @param roundId
	 * @param round
	 * @throws Exception
	 */

	public List<String> sourceCandidateToBoard(Position position, Round round, Candidate... candidates) throws Exception {
		
		List<String> existingCandidateList = new ArrayList<String>();

		if (round.getBoard().getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| round.getBoard().getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (round.getBoard().getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| round.getBoard().getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		// if org is agency and position is published to connect, sync
		// candidates with corporate
		Organization org = userService.getLoggedInUserObject().getOrganization();
		if (GlobalConstants.PUBLISH_MODE_CONNECT.equals(position.getPublishMode())
				&& GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(org.getOrgType())) {
			ResponseEntity<RestResponse> restResponse = recruizConnectService.sourceCandidateToPosition(position.getConnectCorporateId(),
					position.getConnectInstanceId(), position.getPositionCode(), round, candidates);
			if (!restResponse.getBody().isSuccess())
				throw new RecruizWarnException((String) restResponse.getBody().getData(), (String) restResponse.getBody().getReason());
		}

		// defining list to add to candidate activity
		List<CandidateActivity> allCandidateActivity = new ArrayList<CandidateActivity>();
		

		CandidateActivity candidateActivity = null;

		// getting min index of candidate card in a round
		double minCardIndex = getMinCardIndex(round.getId() + "");

		Set<RoundCandidate> candidateList = new HashSet<RoundCandidate>();
		for (Candidate candidate : candidates) {
			RoundCandidate existingCandidate = getExistingBoardCandidate(candidate,
					positionService.getPositionByBoard(round.getBoard()).getPositionCode());

			if (existingCandidate == null) {
				RoundCandidate roundCandidate = new RoundCandidate();
				roundCandidate.setCandidate(candidate);
				// due to dummy invoice feature all board candidate satus is
				// joined
				if (position.getPositionCode().equals("pos_1") && position.getClient().getClientName().equals("Dummy_Auk Labs Inc.")) {
					roundCandidate.setStatus(BoardStatus.Joined.toString());
				} else {
					roundCandidate.setStatus(BoardStatus.YetToProcess.toString());
				}

				// calculating the mid index for candidate card
				double calculatedIndex = getMidOfIndex(0, minCardIndex);
				minCardIndex = calculatedIndex;

				roundCandidate.setRoundId(round.getId() + "");
				roundCandidate.setRound(round);
				roundCandidate.setCardIndex(calculatedIndex);
				roundCandidate.setPositionCode(position.getPositionCode());
				roundCandidate.setSourcedBy(userService.getLoggedInUserEmail());
				save(roundCandidate);

				candidateStatusService.addCandidateStatus(position.getClient().getId(), candidate.getCid(), position.getPositionCode(),
						roundCandidate.getStatus());

				candidateList.add(roundCandidate);
				// adding to candidate activity list
				candidateActivity = candidateActivityService.addedToBoardEvent(position, candidate);
				if (candidateActivity != null) {
					allCandidateActivity.add(candidateActivity);
				}
			} else {
				
				existingCandidateList.add(existingCandidate.getCandidate().getFullName());
				System.out.println("Candidate " +existingCandidate.getCandidate().getFullName() +" has already been tagged to this position");

			}
		}

		if (allCandidateActivity != null && !allCandidateActivity.isEmpty()) {
			
			candidateActivityService.save(allCandidateActivity);
		}
		return existingCandidateList;
	}

	public double getMidOfIndex(double startIndex, double endIndex) {

		return (startIndex + endIndex) / 2;

	}

	@Transactional(readOnly = true)
	public List<RoundCandidate> getCandidatebyStatusAndCandidate(String status, Candidate candidate) {
		return roundCandidateRepository.findByStatusAndCandidate(status, candidate);
	}

	@Transactional(readOnly = true)
	public Set<RoundCandidate> getCandidatebySourcedBy(String roundId, String sourcedBy) {
		return roundCandidateRepository.findByRoundIdAndSourcedBy(roundId, sourcedBy);
	}

	@Transactional(readOnly = true)
	public Set<RoundCandidate> findByModificationDateBetweenAndRoundIdAndSourcedByAndStatus(Date startDate, Date endDate, String roundId,
			String sourcedBy, String status) {
		return roundCandidateRepository.findByModificationDateBetweenAndRoundIdAndSourcedByAndStatus(startDate, endDate, roundId, sourcedBy,
				status);
	}

	@Transactional(readOnly = true)
	public Set<RoundCandidate> findByModificationDateBetweenAndRoundIdAndSourcedBy(Date startDate, Date endDate, String roundId,
			String sourcedBy) {
		return roundCandidateRepository.findByModificationDateBetweenAndRoundIdAndSourcedBy(startDate, endDate, roundId, sourcedBy);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidateByPositionCode(String positionCode) {
		List<RoundCandidate> roundCandidates = roundCandidateRepository.findByPositionCode(positionCode);
		List<Candidate> candidates = new ArrayList<>();

		if (roundCandidates != null && !roundCandidates.isEmpty()) {
			for (RoundCandidate roundCandidate : roundCandidates) {
				Candidate candidate = roundCandidate.getCandidate();
				candidate.getKeySkills().size();
				candidate.getEducationDetails().size();
				candidates.add(candidate);
			}
		}
		return candidates;
	}

	@Transactional
	public void moveCandidate(List<String> candidateEmailList, String connectSrcRoundId, String connectDestRoundId, double cardIndex)
			throws RecruizException {

		Round connectSrcRound = roundService.findByConnectId(connectSrcRoundId);
		Round connectDestRound = roundService.findByConnectId(connectDestRoundId);

		// getting all round candidates by round id and sorting by card index
		List<RoundCandidate> destRoundCandidateList = getCandidateByRoundId(connectDestRound.getId() + "");
		Collections.sort(destRoundCandidateList);

		for (String email : candidateEmailList) {

			Candidate candidate = candidateService.getCandidateByEmail(email);
			if (candidate != null) {
				RoundCandidate existingCandidate = getCandidateByIdAndRoundId(candidate, connectSrcRound.getId() + "");
				if (existingCandidate != null) {

					existingCandidate.setRoundId(String.valueOf(connectDestRound.getId()));
					existingCandidate.setRound(connectDestRound);
					existingCandidate.setStatus(BoardStatus.InProgress.toString());
					existingCandidate.setCardIndex(cardIndex);

					save(existingCandidate);
				}
			}
		}
	}

	// to send reminder to position hr for inactive candidate since last 10 days
	@Transactional(readOnly = true)
	public void sendInactiveCandidateReminder() {
		try {
			String template = "email-template-inactive-candidate-reminder.html";

			DateTime modificationDate = new DateTime();
			Date lastDate = new Date(modificationDate.minusDays(10).toDate().getTime());

			List<User> activeHrs = userService.getHrList();
			if (null != activeHrs && !activeHrs.isEmpty()) {
				Set<String> candidateStatus = new HashSet<>();
				candidateStatus.add(BoardStatus.InProgress.name());
				candidateStatus.add(BoardStatus.YetToProcess.name());

				for (User hr : activeHrs) {
					List<Position> hrPositions = positionService.getPositionListByUser(hr);
					if (hrPositions != null && !hrPositions.isEmpty()) {
						LinkedList<InactiveCandidateDTO> inactiveCandidateList = new LinkedList<>();
						for (Position position : hrPositions) {
							if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.name())) {
								continue;
							}

							String clientName = position.getClient().getClientName();
							String positionTitle = position.getTitle();
							Long inactiveCandidateCount = roundCandidateRepository.countByPositionCodeAndModificationDateBeforeAndStatusIn(
									position.getPositionCode(), lastDate, candidateStatus);
							String pipelineLink = baseUrl + GlobalConstants.INACTIVE_CANDIDATE_BOARD_URL + position.getPositionCode()
									+ GlobalConstants.INACTIVE_FILTER_PARAM + InactiveSinceRange.Zero_To_TenDays.getDisplayName();

							if (null != inactiveCandidateCount && inactiveCandidateCount > 0) {
								inactiveCandidateList
										.add(new InactiveCandidateDTO(clientName, positionTitle, inactiveCandidateCount, pipelineLink));
							}
						}

						if (null != inactiveCandidateList && !inactiveCandidateList.isEmpty()) {
							Map<String, Object> inactiveCandidateReminder = new HashMap<>();
							inactiveCandidateReminder.put("inactiveCandidateList", inactiveCandidateList);
							emailTemplateDataService.initEmailBodyDefaultVariables(inactiveCandidateReminder);

							String templateString = emailTemplateDataService.getHtmlContentFromFile(inactiveCandidateReminder, template);

							String renderedTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(templateString);
							String subject = "Reminder: Inactive pipeline candidate as on "
									+ DateTimeUtils.formatDateToWithTimeZone(modificationDate.toDate(), hr.getTimezone());

							List<String> emailList = new ArrayList<>();
							emailList.add(hr.getEmail());

							emailService.sendEmail(emailList, renderedTemplate, subject);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndStatusIn(String positionCode, List<String> statusIn) {
		return roundCandidateRepository.countByPositionCodeAndStatusIn(positionCode, statusIn);
	}

	@Transactional(readOnly = true)
	public Long countByPositionCodeAndStatusInAndSourcedBy(String positionCode, List<String> status, String ownerEmail) {
		return roundCandidateRepository.countByPositionCodeAndStatusInAndSourcedBy(positionCode, status, ownerEmail);
	}
	
	//@author - Sajin
	@Transactional(readOnly = true)
	public Long countByPositionCodeInAndStatusInAndSourcedBy(List<String> positionCodes, List<String> status, String ownerEmail) {
		return roundCandidateRepository.countByPositionCodeInAndStatusInAndSourcedBy(positionCodes, status, ownerEmail);
	}
	
	@Transactional(readOnly = true)
	public Long countByPositionCodeAndStatusInAndSourcedByAndDateRange(String positionCode, List<String> status, String ownerEmail,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusInAndSourcedByAndModificationDateBetween(positionCode, status, ownerEmail,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long countByPositionCodeAndStatusInAndDateRange(String positionCode, List<String> status,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusInAndModificationDateBetween(positionCode, status,startDate,endDate);
	}
	

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndStatus(String positionCode, String status) {
		return roundCandidateRepository.countByPositionCodeAndStatus(positionCode, status);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndStatusAndOwner(String positionCode, String status, String owner) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndSourcedBy(positionCode, status, owner);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndStatusAndOwnerAndDateRange(String positionCode, String status, String owner,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndSourcedByAndModificationDateBetween(positionCode, status, owner,startDate,endDate);
	}
	
	
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusAndOwnerAndDateRange(String positionCode, String status, String owner,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndSourcedByAndModificationDateBetween(positionCode, status, owner, startDate, endDate);
	}
	
	//@author - Sajin
	@Transactional(readOnly = true)
	public Long getCountByPositionCodeListAndStatusAndOwnerAndDateRange(List<String> positionCodes, String status, String owner,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndSourcedByAndModificationDateBetween(positionCodes, status, owner,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndStatusAndDateRange(String positionCode, String status,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndModificationDateBetween(positionCode, status,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndDateRange(String positionCode,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndModificationDateBetween(positionCode,startDate,endDate);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatus(List<String> positionCodes, String status) {
		return roundCandidateRepository.countByPositionCodeInAndStatus(positionCodes, status);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusAndDateRange(List<String> positionCodes, String status,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusAndModificationDateBetween(positionCodes, status,startDate,endDate);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusIn(List<String> positionCodes, List<String> statuses) {
		return roundCandidateRepository.countByPositionCodeInAndStatusIn(positionCodes, statuses);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusInAndDateRange(List<String> positionCodes, List<String> statuses,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusInAndModificationDateBetween(positionCodes, statuses,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusInAndOnwerAndDateRange(List<String> positionCodes, List<String> statuses,String userEmail,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusInAndSourcedByAndModificationDateBetween(positionCodes, statuses,userEmail,startDate,endDate);
	}
	
	

	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusAndOwner(List<String> positionCodes, String status, String owner) {
		return roundCandidateRepository.countByPositionCodeInAndStatusAndSourcedBy(positionCodes, status, owner);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusAndOwnerAndDateRange(List<String> positionCodes, String status, String owner,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusAndSourcedByAndModificationDateBetween(positionCodes, status, owner,startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusesAndOwnerAndDateRange(List<String> positionCodes, List<String> statuses, String owner,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusInAndSourcedByAndModificationDateBetween(positionCodes, statuses, owner,startDate,endDate);
	}
	
	
	//New Method to add JoinedByHr also
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndStatusOrJoinedByHrAndOwnerAndDateRange(List<String> positionCodes, String status, String owner, String joinedByHr, Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndStatusAndSourcedByOrJoinedByHrAndModificationDateBetween(positionCodes, status, owner, joinedByHr, startDate,endDate);
	}
	

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndRound(String positionCode, Round round) {
		return roundCandidateRepository.countByPositionCodeAndRound(positionCode, round);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndRoundAndStatus(String positionCode, Long round, String status) {
		return roundCandidateRepository.countByPositionCodeAndRoundAndStatus(positionCode, round, status);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCode(String positionCode) {
		return roundCandidateRepository.countByPositionCode(positionCode);
	}

	public Page<RoundCandidate> getYetToBoardCandidate(Pageable pageable) {
		List<String> status = new ArrayList<>();
		status.add(BoardStatus.Joined.name());
		status.add(BoardStatus.OfferAccepted.name());
		return roundCandidateRepository.findDistinctByStatusIn(status, pageable);
	}

	public List<RoundCandidate> getCandidateByStatusAndCandidate(Candidate candidate) {
		List<String> status = new ArrayList<>();
		status.add(BoardStatus.Joined.name());
		status.add(BoardStatus.OfferAccepted.name());
		return roundCandidateRepository.findByStatusInAndCandidate(status, candidate);
	}

	public List<RoundCandidate> getAllRoundCandidateByCandidate(Candidate candidate) {
		return roundCandidateRepository.findByCandidate(candidate);
	}

	// to get all round candidate by ids
	public List<RoundCandidate> getCandidatedByIdsIin(Set<Long> roundCandidateIds) {
		return roundCandidateRepository.findByIdIn(roundCandidateIds);
	}

	public Long getCountByPositionAndStatusAndDateRange(String positionCode, String status, Date startDate, Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndStatusAndModificationDateBetween(positionCode, status, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndSourcedBy(String positionCode, String sourcedBy) {
		return roundCandidateRepository.countByPositionCodeAndSourcedBy(positionCode, sourcedBy);
	}
	
	//@author - Sajin
	@Transactional(readOnly = true)
	public Long getCountByPositionCodeInAndSourcedBy(List<String> positionCodes, String sourcedBy) {
		return roundCandidateRepository.countByPositionCodeInAndSourcedBy(positionCodes, sourcedBy);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodeAndSourcedByAndModificationDateBetween(String positionCode, String sourcedBy, Date startDate,
			Date endDate) {
		return roundCandidateRepository.countByPositionCodeAndSourcedByAndModificationDateBetween(positionCode, sourcedBy, startDate,
				endDate);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndSourcedBy(List<String> positionCode, String sourcedBy) {
		return roundCandidateRepository.countByPositionCodeInAndSourcedBy(positionCode, sourcedBy);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndSourcedByAndDateRange(List<String> positionCode, String sourcedBy, Date startDate, Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndSourcedByAndModificationDateBetween(positionCode, sourcedBy, startDate, endDate);
	}
	
	//Added by Sajin
	@Transactional(readOnly = true)
	public Long getCountByPositionIDsAndSourcedByAndDateRange(List<Long> positionIds, String sourcedBy, Date startDate, Date endDate) {
		return roundCandidateRepository.countByPositionIDsInAndSourcedByAndModificationDateBetween(positionIds, sourcedBy, startDate, endDate);
	}

	public List<RoundCandidate> getCandidateByPositionAndDateBetween(String positionCode, Date startDate, Date endDate) {
		return roundCandidateRepository.getCandidateByPositionCodeAndDateBetween(positionCode, startDate, endDate);
	}

	public Long getCandidateCountByPositionAndDateBetween(String positionCode, Date startDate, Date endDate) {
		return roundCandidateRepository.getCandidateCountByPositionCodeAndDateBetween(positionCode, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public Long getCountByPositionCodes(List<String> positionCodes) {
		return roundCandidateRepository.countByPositionCodeIn(positionCodes);
	}
	
	@Transactional(readOnly = true)
	public Long getCountByPositionCodesAndDateRange(List<String> positionCodes,Date startDate,Date endDate) {
		return roundCandidateRepository.countByPositionCodeInAndModificationDateBetween(positionCodes,startDate,endDate);
	}


	@Transactional(readOnly = true)
	public List<RoundCandidate> getRoundCandidateByPositionCodeAndSourcedBy(String positionCode,String email) {
		
		return roundCandidateRepository.getRoundCandidateByPositionCodeAndSourcedBy(positionCode, email);
	}

	@Transactional(readOnly = true)
	public Set<String> getRecruitersEmailByPositionCode(String positionCode) {
		
		return roundCandidateRepository.getRecruitersEmailByPositionCode(positionCode);
	}


	public List<RoundCandidate> getRoundCandidateByPositionCodeAndSourcedBy(String positionCode, String hrEmail,
			Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		return roundCandidateRepository.getRoundCandidateByPositionCodeAndSourcedByForCustomStatus(positionCode, hrEmail, startDate, endDate);
	}


	public Set<Long> findCandidateIdsBySourcebyAndStatusBetweenDate(String email, List<String> statusList,
			Date startDate, Date endDate) {
	
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = roundCandidateRepository.findCandidateIdsBySourcebyAndStatusBetweenDate(email, statusList,
				startDate, endDate);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}


	public Set<Long> findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(String email, String status,
			Date startDate, Date endDate) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = roundCandidateRepository.findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(email, status,
				startDate, endDate);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}


	public Set<Long> findCandidateIdsByClientsbyAndStatusBetweenDate(List<String> positionNameList,
			List<String> statusList, Date startDate, Date endDate) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = roundCandidateRepository.findCandidateIdsByClientbyAndStatusBetweenDate(positionNameList, statusList,
				startDate, endDate);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}


	public Set<Long> findCandidateIdsByClientbyAndSelectedStatusBetweenDate(List<String> positionNameList,
			String selected, Date startDate, Date endDate) {
		
		Set<Long> candidateIds = new LinkedHashSet<>();
		List<Object> candidateIdsFromDB = roundCandidateRepository.findCandidateIdsByClientbyAndSelectedStatusBetweenDate(positionNameList, selected,
				startDate, endDate);
		
		
		if (candidateIdsFromDB != null && !candidateIdsFromDB.isEmpty()) {
			for (Object obj : candidateIdsFromDB) {
				candidateIds.add(((BigInteger) obj).longValue());
			}
		}
		return candidateIds;
	}

	public List<RoundCandidate> getCandidateSourcedBy(String email) {
		return roundCandidateRepository.getCandidateSourcedBy(email);
		
	}

}
